package com.github.serserser.vget2.vhs.vimeo.downloadInfo;

import java.util.ArrayList;

public class VimeoFiles {
    private ArrayList<String> codecs;
    private VideoCodec h264;

    public ArrayList<String> getCodecs() {
        return codecs;
    }

    public void setCodecs(ArrayList<String> codecs) {
        this.codecs = codecs;
    }

    public VideoCodec getH264() {
        return h264;
    }

    public void setH264(VideoCodec h264) {
        this.h264 = h264;
    }
}
