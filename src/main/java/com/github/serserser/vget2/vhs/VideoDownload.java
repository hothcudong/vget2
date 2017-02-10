package com.github.serserser.vget2.vhs;

import java.net.URL;

public class VideoDownload {
    public YouTubeInfo.StreamInfo stream;
    public URL url;

    public VideoDownload(YouTubeInfo.StreamInfo s, URL u) {
        this.stream = s;
        this.url = u;
    }
}
