package com.ione.smartdict;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DictionaryFactory {

    public static Dictionary createDictionary(Context context, String filePath) {
        try {
            Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(filePath));
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                String fileName = new File(filePath).getName();
                return new Dsl4jDictionary(inputStream, fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    private static String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf(".");
        return lastIndex != -1 ? filePath.substring(lastIndex + 1) : "";
    }
}

