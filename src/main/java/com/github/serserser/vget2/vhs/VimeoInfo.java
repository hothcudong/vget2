package com.github.serserser.vget2.vhs;

import java.net.URL;

import com.github.serserser.vget2.info.VideoInfo;
import com.github.serserser.vget2.vhs.vimeo.VimeoQuality;

public class VimeoInfo extends VideoInfo {

    private VimeoQuality vq;

    public VimeoInfo(URL web) {
        super(web);
    }

    public VimeoQuality getVideoQuality() {
        return vq;
    }

    public void setVideoQuality(VimeoQuality vq) {
        this.vq = vq;
    }
}