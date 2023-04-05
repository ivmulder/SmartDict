package com.ione.smartdict;

import io.github.eb4j.dsl.DslDictionary;
import io.github.eb4j.dsl.DslResult;
import io.github.eb4j.dsl.visitor.HtmlDslVisitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dsl4jDictionary implements Dictionary {
    private DslDictionary dslDictionary;
    private HtmlDslVisitor htmlDslVisitor;
    private String dictionaryName;

    public Dsl4jDictionary(InputStream dictionaryInputStream, String dictionaryName) throws IOException {
        File tempDictionaryFile = File.createTempFile("tempDictionary", ".dsl");
        tempDictionaryFile.deleteOnExit();
        Files.copy(dictionaryInputStream, tempDictionaryFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        Path path = tempDictionaryFile.toPath();
        Path indexPath = Paths.get(tempDictionaryFile.getAbsolutePath() + ".idx");
        dslDictionary = DslDictionary.loadDictionary(path, indexPath);
        htmlDslVisitor = new HtmlDslVisitor(path.getParent().toString());
        this.dictionaryName = dictionaryName;
    }

    @Override
    public String lookup(String word) throws IOException {
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

    @Override
    public List<String> search(String query) throws IOException {
        List<String> results = new ArrayList<>();
        DslResult dslResult = dslDictionary.lookup(query);
        Map<String, String> entries = (Map<String, String>) dslResult.getEntries(htmlDslVisitor);

        for (String key : entries.keySet()) {
            if (key.toLowerCase().contains(query.toLowerCase())) {
                results.add(key);
            }
        }

        return results;
    }




}
