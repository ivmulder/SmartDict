package com.ione.smartdict;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.ione.smartdict.Dictionary;
import com.ione.smartdict.DictionaryAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            requestFolderAccess();
//        } else {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
//            } else {
//                loadDictionaries();
//            }
//        }
        requestFolderAccess();
    }

    private void requestFolderAccess() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_READ_STORAGE);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_READ_STORAGE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadDictionaries();
//            } else {
//                Toast.makeText(this, "Permission denied. Cannot load dictionaries.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void loadDictionaries() {
//        try {
//            Uri smartDictUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(Environment.getExternalStorageDirectory(), "SmartDict"));
//            List<Uri> dictionaryUris = new ArrayList<>();
//
//            DocumentFile documentFile = DocumentFile.fromTreeUri(this, smartDictUri);
//            if (documentFile != null) {
//                DocumentFile[] documentFiles = documentFile.listFiles();
//
//                for (DocumentFile file : documentFiles) {
//                    String fileName = file.getName();
//                    if (fileName != null && (fileName.toLowerCase().endsWith(".dsl") || fileName.toLowerCase().endsWith(".dsl.dz"))) {
//                        dictionaryUris.add(file.getUri());
//                    }
//                }
//            }
//
//            for (Uri dictionaryUri : dictionaryUris) {
//                InputStream inputStream = getContentResolver().openInputStream(dictionaryUri);
//                if (inputStream != null) {
//                    String dictionaryName = DocumentFile.fromSingleUri(this, dictionaryUri).getName();
//                    if (dictionaryName != null) {
//                        Dictionary dictionary = new Dsl4jDictionary(inputStream, dictionaryName);
//                        adapter.addDictionary(dictionary);
//                        inputStream.close();
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void loadDictionaries(Uri folderUri) {
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.exists()) {
            DocumentFile[] dictionaryFiles = folder.listFiles();
            for (DocumentFile dictionaryFile : dictionaryFiles) {
                String fileName = dictionaryFile.getName();
                if (fileName != null && (fileName.toLowerCase().endsWith(".dsl") || fileName.toLowerCase().endsWith(".dsl.dz"))) {
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
                }
            }
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

}


