package com.github.serserser.vget2.vhs.parsers.youtube.extract;

import java.util.ArrayList;
import java.util.List;

public abstract class PatternBasedExtractor {

    public abstract String extract(String url);

    protected abstract void setNextHandler(PatternBasedExtractor nextHandler);

    protected PatternBasedExtractor() {
    }

    public static PatternBasedExtractorBuilder builder() {
        return new PatternBasedExtractorBuilder();
    }


    public static class PatternBasedExtractorBuilder {

        List<PatternBasedExtractor> extractors;

        private PatternBasedExtractorBuilder() {
            extractors = new ArrayList<>();
        }

        public PatternBasedExtractorBuilder addExtractor(PatternBasedExtractor extractor) {
            extractors.add(extractor);
            return this;
        }

        public PatternBasedExtractor build() {
            if ( extractors.isEmpty() ) {
                return new NullExtractor();
            }

            PatternBasedExtractor root = extractors.get(0);

            PatternBasedExtractor current = root;
            for ( int i = 1; i < extractors.size(); i++ ) {
                PatternBasedExtractor next = extractors.get(i);

                current.setNextHandler(next);

                current = next;
            }

            if ( ! (current instanceof NullExtractor ) ) {
                current.setNextHandler(new NullExtractor());
            }

            return root;
        }
    }

}
