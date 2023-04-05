package com.ione.smartdict;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
    private DrawerLayout drawerLayout;
    private TextInputEditText searchField;
    private DictionaryAdapter adapter;

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
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.exists()) {
            DocumentFile[] dictionaryFiles = folder.listFiles();
            for (DocumentFile file : dictionaryFiles) {
                if (file.isFile() && file.getName() != null && (file.getName().toLowerCase().endsWith(".dsl") ||file.getName().toLowerCase().endsWith(".dsl.dz"))) {
                    Log.d("MainActivity", "Processing file: " + file.getName());
                    try {
                        String fileName = getFileName(this, file.getUri());
                        InputStream inputStream = getContentResolver().openInputStream(file.getUri());
                        if (inputStream != null) {
                            Dictionary dictionary = new Dsl4jDictionary(inputStream, fileName);
                            if (dictionary != null) {
                                adapter.addDictionary(dictionary);
                            }
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, R.string.error_no_dictionary_files, Toast.LENGTH_SHORT).show();
        }
    }


    private String getFileName(@NonNull Context context, @NonNull Uri uri) {
        String fileName = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columnIndex >= 0) {
                    fileName = cursor.getString(columnIndex);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error getting file name", e);
        }
        return fileName;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_READ_STORAGE && resultCode == RESULT_OK && data != null) {
            Uri folderUri = data.getData();
            getContentResolver().takePersistableUriPermission(folderUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);

            loadDictionaries(folderUri);
        } else if (requestCode == REQUEST_FOLDER_ACCESS && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, R.string.error_folder_access_required, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
