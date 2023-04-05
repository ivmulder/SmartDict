package com.ione.smartdict;

import java.io.IOException;
import java.util.List;

public interface Dictionary {
    String lookup(String word) throws IOException;
    String getDictionaryName();
    List<String> search(String query) throws IOException;
}

