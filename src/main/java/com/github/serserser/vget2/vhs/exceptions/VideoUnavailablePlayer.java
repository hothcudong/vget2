package com.github.serserser.vget2.vhs.exceptions;

import com.github.axet.wget.info.ex.DownloadError;

public class VideoUnavailablePlayer extends DownloadError {
    private static final long serialVersionUID = 10905065542230199L;

    public VideoUnavailablePlayer() {
        super("unavailable-player");
    }
}