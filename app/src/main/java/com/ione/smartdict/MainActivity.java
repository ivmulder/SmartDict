package com.ione.smartdict;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            } else {
                loadDictionaries();
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        } else {
            loadDictionaries();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDictionaries();
            } else {
                Toast.makeText(this, "Permission denied. Cannot load dictionaries.", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    private void loadDictionaries() {
        File smartDictFolder = new File(Environment.getExternalStorageDirectory(), "SmartDict");
        if (smartDictFolder.exists() && smartDictFolder.isDirectory()) {
            File[] dictionaryFiles = smartDictFolder.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dsl") || lowerName.endsWith(".dsl.dz");
            });

            if (dictionaryFiles != null) {
                for (File dictionaryFile : dictionaryFiles) {
                    try {
                        Uri fileUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", dictionaryFile);
                        InputStream inputStream = getContentResolver().openInputStream(fileUri);
                        if (inputStream != null) {
                            String fileName = dictionaryFile.getName();
                            Dictionary dictionary = new Dsl4jDictionary(inputStream, fileName);
                            adapter.addDictionary(dictionary);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_READ_STORAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        String fileName = getFileNameFromUri(uri);
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


