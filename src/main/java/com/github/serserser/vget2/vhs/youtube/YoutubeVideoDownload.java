package com.github.serserser.vget2.vhs.youtube;

import com.github.serserser.vget2.vhs.youtube.params.GenericStreamInfo;

import java.net.URL;

public class YoutubeVideoDownload {
    private GenericStreamInfo stream;
    private URL url;

    public YoutubeVideoDownload(GenericStreamInfo s, URL u) {
        this.stream = s;
        this.url = u;
    }

    public GenericStreamInfo getStream() {
        return stream;
    }

    public URL getUrl() {
        return url;
    }
}
