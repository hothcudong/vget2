package com.github.serserser.vget2.vhs.vimeo.downloadInfo;

import java.util.Map;

public class VimeoVideo {
    private Map<String, String> thumbs;
    private String title;

    public Map<String, String> getThumbs() {
        return thumbs;
    }

    public void setThumbs(Map<String, String> thumbs) {
        this.thumbs = thumbs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
