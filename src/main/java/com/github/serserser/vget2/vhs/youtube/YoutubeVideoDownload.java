package com.github.serserser.vget2.vhs.youtube;

import com.github.serserser.vget2.vhs.youtubeVideoParams.StreamInfo;

import java.net.URL;

public class YoutubeVideoDownload {
    private StreamInfo stream;
    private URL url;

    public YoutubeVideoDownload(StreamInfo s, URL u) {
        this.stream = s;
        this.url = u;
    }

    public StreamInfo getStream() {
        return stream;
    }

    public URL getUrl() {
        return url;
    }
}
