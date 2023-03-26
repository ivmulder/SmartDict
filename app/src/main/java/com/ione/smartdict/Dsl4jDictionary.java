package com.ione.smartdict;

import io.github.eb4j.dsl.DslDictionary;
import io.github.eb4j.dsl.DslResult;
import io.github.eb4j.dsl.visitor.HtmlDslVisitor;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Dsl4jDictionary {
    private DslDictionary dslDictionary;
    private HtmlDslVisitor htmlDslVisitor;
    private String name;
    private String description;

    public Dsl4jDictionary(String filePath) {
        try {
            Path dictionaryPath = Paths.get(filePath);
            Path indexPath = Paths.get(dictionaryPath + ".idx");
            dslDictionary = DslDictionary.loadDictionary(dictionaryPath, indexPath);
            htmlDslVisitor = new HtmlDslVisitor(dictionaryPath.getParent().toString());
            name = dslDictionary.getDictionaryName();
//            description = dslDictionary.getDictionaryComment();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String lookup(String word) {
        try {
            DslResult dslResult = dslDictionary.lookup(word);
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, String> entry : dslResult.getEntries(htmlDslVisitor)) {
                String key = entry.getKey();
                String article = entry.getValue();
                result.append("<p><strong>").append(key).append("</strong>").append(article).append("</p>");
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
