package com.ione.smartdict;

import com.ione.smartdict.Dictionary;
import io.github.eb4j.dsl.DslDictionary;
import io.github.eb4j.dsl.visitor.HtmlDslVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Dsl4jDictionary implements Dictionary {
    private DslDictionary dslDictionary;
    private HtmlDslVisitor htmlDslVisitor;
    private String dictionaryName;

    public Dsl4jDictionary(String dictionaryPath) throws IOException {
        Path path = Paths.get(dictionaryPath);
        Path indexPath = Paths.get(dictionaryPath + ".idx");
        dslDictionary = DslDictionary.loadDictionary(path, indexPath);
        htmlDslVisitor = new HtmlDslVisitor(path.getParent().toString());
        dictionaryName = new File(dictionaryPath).getName();
    }

    @Override
    public String search(String word) throws IOException {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : dslDictionary.lookup(word).getEntries(htmlDslVisitor)) {
            String key = entry.getKey();
            String article = entry.getValue();
            result.append("<p><strong>").append(key).append("</strong>").append(article).append("</p>");
        }
        return result.toString();
    }

    @Override
    public String getDictionaryName() {
        return dictionaryName;
    }
}
