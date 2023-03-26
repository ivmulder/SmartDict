package com.ione.smartdict;

import java.io.IOException;

public interface Dictionary {
    String search(String word) throws IOException;
}
