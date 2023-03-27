package com.ione.smartdict;

import java.io.IOException;
import java.util.List;

public interface Dictionary {
    List<String> search(String word) throws IOException;

    String lookup(String word) throws IOException;

    String getDictionaryName();
}
