package com.github.serserser.vget2.vhs.youtube;

import com.github.serserser.vget2.vhs.youtubeVideoParams.StreamInfo;

import java.net.URL;

public class YoutubeVideoDownload {
    public StreamInfo stream;
    public URL url;

    public YoutubeVideoDownload(StreamInfo s, URL u) {
        this.stream = s;
        this.url = u;
    }
}
