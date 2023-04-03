package com.ione.smartdict;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_FOLDER_ACCESS = 101;
    private static final int REQUEST_READ_STORAGE = 100;
    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 101;
    private DrawerLayout drawerLayout;
    private TextInputEditText searchField;
    private DictionaryAdapter adapter;
    private Dictionary dictionary;

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);

        searchField = findViewById(R.id.search_field);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    searchField.clearFocus();
                }
            }
        });

        searchField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getX() < searchField.getCompoundPaddingLeft()) {
                        drawerLayout.openDrawer(GravityCompat.START);
                        return true;
                    }
                }
                return false;
            }
        });

        searchField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (((TextInputEditText) v).getText().toString().isEmpty()) {
                    hideKeyboard();
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DictionaryAdapter();
        recyclerView.setAdapter(adapter);

        requestFolderAccess();
    }

    private void requestFolderAccess() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_READ_STORAGE);
    }

    private void loadDictionaries(Uri folderUri) {
        Log.d("MainActivity", "loadDictionaries called with folderUri: " + folderUri.toString());

        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.exists()) {
            DocumentFile[] dictionaryFiles = folder.listFiles();
            Log.d("MainActivity", "Number of files in folder: " + dictionaryFiles.length);
            for (DocumentFile dictionaryFile : dictionaryFiles) {
                String fileName = dictionaryFile.getName();
                if (fileName != null && (fileName.toLowerCase().endsWith(".dsl") || fileName.toLowerCase().endsWith(".dsl.dz"))) {
                    Log.d("MainActivity", "Processing dictionary file: " + fileName);
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(dictionaryFile.getUri());
                        if (inputStream != null) {
                            Dsl4jDictionary dsl4jDictionary = new Dsl4jDictionary(inputStream, fileName);
                            adapter.addDictionary(dsl4jDictionary);
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("MainActivity", "Skipped non-dictionary file: " + fileName);
                }
            }
        } else {
            Log.d("MainActivity", "Folder not found or not accessible");
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                String folderUriString = sharedPreferences.getString("folder_uri", null);
                if (folderUriString != null) {
                    Uri folderUri = Uri.parse(folderUriString);
                    loadDictionaries(folderUri);
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot load dictionaries.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String displayName = "unknown_dictionary.dsl";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        return displayName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_READ_STORAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            Log.d("MainActivity", "Folder selected: " + uri.toString());
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("folder_uri", uri.toString());
                editor.apply();
                loadDictionaries(uri);
            }
        }
    }


}


