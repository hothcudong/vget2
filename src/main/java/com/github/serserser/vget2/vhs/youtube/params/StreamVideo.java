package com.github.serserser.vget2.vhs.youtube.params;

public class StreamVideo extends GenericStreamInfo {
    public Encoding video;
    public YoutubeQuality vq;

    public StreamVideo() {
    }

    public StreamVideo(Container c, Encoding v, YoutubeQuality vq) {
        super(c);

        this.vq = vq;
        this.video = v;
    }
}
