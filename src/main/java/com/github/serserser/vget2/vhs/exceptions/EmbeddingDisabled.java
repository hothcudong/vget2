package com.github.serserser.vget2.vhs.exceptions;

import com.github.axet.wget.info.ex.DownloadError;

public class EmbeddingDisabled extends DownloadError {
    private static final long serialVersionUID = 1L;

    public EmbeddingDisabled(String msg) {
        super(msg);
    }
}