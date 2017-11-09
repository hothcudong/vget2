package com.github.serserser.vget2.vhs.parsers.youtube.util;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    private Regex() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Builder() {
        }

        private String inputString = "";
        private String patternString = "";

        public Builder inputString(String input) {
            this.inputString = input;
            return this;
        }

        public Builder pattern(String pattern) {
            this.patternString = pattern;
            return this;
        }

        public boolean found() {
            Matcher matcher = createMatcher();
            return matcher.find();
        }

        public Finder find() {
            return new Finder(createMatcher());
        }

        public Optional<String> group(int groupNumber) {
            Matcher matcher = createMatcher();
            if (hasGroupWithNumber(matcher, groupNumber)) {
                return Optional.ofNullable(matcher.group(groupNumber));
            } else {
                return Optional.empty();
            }
        }

        private boolean hasGroupWithNumber(Matcher matcher, int groupNumber) {
            return matcher.find() && groupNumber <= matcher.groupCount();
        }

        private Matcher createMatcher() {
            Pattern pattern = Pattern.compile(patternString);
            return pattern.matcher(inputString);
        }
    }

    public static class Finder {

        private final Matcher matcher;

        Finder(Matcher matcher) {
            this.matcher = matcher;
        }

        public void thenThrow(Supplier<? extends Exception> exceptionToThrow) throws Exception {
            if (matcher.find()) {
                throw exceptionToThrow.get();
            }
        }

        public void thenExecute(Runnable runnableToExecute) {
            if (matcher.find()) {
                runnableToExecute.run();
            }
        }

        public void thenExecute(Consumer<Matcher> consumer) {
            if (matcher.find()) {
                consumer.accept(matcher);
            }
        }
    }
}
