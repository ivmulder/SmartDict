package com.ione.smartdict;

public class DictionaryFactory {

    public static Dsl4jDictionary getDictionary(String dictionaryType, String filePath) {
        if (dictionaryType == null) {
            return null;
        }
        if (dictionaryType.equalsIgnoreCase("DSL")) {
            return new Dsl4jDictionary(filePath);
        }
        // Add other dictionary types here
        return null;
    }


    private static String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf(".");
        return lastIndex != -1 ? filePath.substring(lastIndex + 1) : "";
    }
}

