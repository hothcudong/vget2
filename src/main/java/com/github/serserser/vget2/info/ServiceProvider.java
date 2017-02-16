package com.github.serserser.vget2.info;

import com.github.serserser.vget2.vhs.VimeoInfo;
import com.github.serserser.vget2.vhs.VimeoParser;
import com.github.serserser.vget2.vhs.YouTubeInfo;
import com.github.serserser.vget2.vhs.YouTubeParser;

import java.net.URL;

public class ServiceProvider {

    public static final String UNSUPPORTED_WEB_SITE_MESSAGE = "Unsupported web site";

    public Parser getParserForUrl(URL url) {
        if ( isYoutube(url) ) {
            return new YouTubeParser();
        } else if ( isVimeo(url) ) {
            return new VimeoParser();
        } else {
            throw new RuntimeException(UNSUPPORTED_WEB_SITE_MESSAGE);
        }
    }

    public VideoInfo getInfoForUrl(URL url) {
        if ( isYoutube(url) ) {
            return new YouTubeInfo(url);
        } else if ( isVimeo(url)) {
            return new VimeoInfo(url);
        } else {
            throw new RuntimeException(UNSUPPORTED_WEB_SITE_MESSAGE);
        }
    }

    private boolean isVimeo(URL url) {
        return url.toString().contains("vimeo.com");
    }

    private boolean isYoutube(URL url) {
        return url.toString().contains("youtube.com");
    }
}
