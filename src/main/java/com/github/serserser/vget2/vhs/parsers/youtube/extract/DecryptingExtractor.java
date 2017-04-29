package com.github.serserser.vget2.vhs.parsers.youtube.extract;

import com.github.serserser.vget2.vhs.decryption.SignatureDecryptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecryptingExtractor extends PatternBasedExtractor {

    public DecryptingExtractor(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    private PatternBasedExtractor nextHandler;

    private final Pattern pattern;


    @Override
    public String extract(String url) {
        Matcher linkMatch = pattern.matcher(url);
        if ( linkMatch.find() ) {
            String sig = linkMatch.group(1);
            SignatureDecryptor decryptor = new SignatureDecryptor(sig);
            return decryptor.decrypt();
        } else {
            return nextHandler.extract(url);
        }
    }

    @Override
    protected void setNextHandler(PatternBasedExtractor nextHandler) {
        this.nextHandler = nextHandler;
    }
}
