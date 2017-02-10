package com.github.serserser.vget2.vhs.youtubeVideoParams;

public class StreamVideo extends StreamInfo {
    public Encoding video;
    public YoutubeQuality vq;

    public StreamVideo() {
    }

    public StreamVideo(Container c, Encoding v, YoutubeQuality vq) {
        super(c);

        this.vq = vq;
        this.video = v;
    }

    public String toString() {
        return c.toString() + " " + video.toString() + "(" + vq.toString() + ")";
    }
}
