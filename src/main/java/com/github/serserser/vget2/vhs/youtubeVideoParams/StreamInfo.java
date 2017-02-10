package com.github.serserser.vget2.vhs.youtubeVideoParams;

public class StreamInfo {
    public Container c;

    public StreamInfo() {
    }

    public StreamInfo(Container c) {
        this.c = c;
    }

    public String toString() {
        return c.toString();
    }
}
