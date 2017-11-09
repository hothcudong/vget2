package com.github.serserser.vget2.vhs.parsers.youtube.extract;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleExtractor extends PatternBasedExtractor {

    public static PatternBasedExtractor ofPattern(String pattern) {
        SimpleExtractor extractor = new SimpleExtractor(pattern);
        extractor.setNextHandler(new NullExtractor());
        return extractor;
    }

    public SimpleExtractor(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    private PatternBasedExtractor nextHandler;

    private final Pattern pattern;

    @Override
    public String extract(String url) {
        Matcher matcher = pattern.matcher(url);

        if ( matcher.find() ) {
            return matcher.group(1);
        } else {
            return nextHandler.extract(url);
        }
    }

    @Override
    protected void setNextHandler(PatternBasedExtractor nextHandler) {
        this.nextHandler = nextHandler;
    }
}
