package com.github.serserser.vget2.vhs.parsers.youtube.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullExtractor extends PatternBasedExtractor {

    private static final Logger logger = LoggerFactory.getLogger(NullExtractor.class);

    @Override
    public String extract(String url) {
        return "";
    }

    @Override
    protected void setNextHandler(PatternBasedExtractor nextHandler) {
        logger.warn("The signature couldn't be extracted");
    }
}
