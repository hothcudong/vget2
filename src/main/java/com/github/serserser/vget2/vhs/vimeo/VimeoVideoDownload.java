package com.github.serserser.vget2.vhs.vimeo;

import java.net.URL;

public class VimeoVideoDownload {
    private VimeoQuality vq;
    private URL url;

    public VimeoVideoDownload(VimeoQuality vq, URL u) {
        this.vq = vq;
        this.url = u;
    }

    public VimeoQuality getVq() {
        return vq;
    }

    public URL getUrl() {
        return url;
    }
}
