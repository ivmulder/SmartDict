package com.ione.smartdict;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;


import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
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


//        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    searchField.clearFocus();
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
//                    return true;
//                }
//                return false;
//            }
//        });

//        searchField.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                if (v.getText().toString().isEmpty()) {
//                    searchField.clearFocus();
//                    hideKeyboard();
//                    return true;
//                }
//            }
//            return false;
//        });


        searchField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (((TextInputEditText) v).getText().toString().isEmpty()) {
                    hideKeyboard();
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> sampleDictionaryItems = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
        adapter = new DictionaryAdapter(sampleDictionaryItems);
        recyclerView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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

    private void loadDictionaries() {
        File smartDictFolder = new File(Environment.getExternalStorageDirectory(), "SmartDict");
        if (smartDictFolder.exists() && smartDictFolder.isDirectory()) {
            File[] dictionaryFiles = smartDictFolder.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".dsl") || lowerName.endsWith(".dsl.dz");
            });

            if (dictionaryFiles != null) {
                for (File dictionaryFile : dictionaryFiles) {
                    String dictionaryFilePath = dictionaryFile.getAbsolutePath();
                    String indexFilePath = dictionaryFilePath + ".idx";
                    Dictionary dictionary = DictionaryFactory.createDictionary(dictionaryFilePath, indexFilePath);
                    // Add the dictionary to the list of dictionaries, adapter, or any data structure you're using
                }
            }
        }
    }

}


