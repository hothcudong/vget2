package com.github.serserser.vget2.vhs.exceptions;

import com.github.axet.wget.info.ex.DownloadError;

public class PrivateVideoException extends DownloadError {
    private static final long serialVersionUID = 1L;

    public PrivateVideoException() {
        super("Private video");
    }

    public PrivateVideoException(String s) {
        super(s);
    }
}