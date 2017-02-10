package com.github.serserser.vget2.vhs.youtubeVideoParams;

public class StreamCombined extends StreamInfo {
    public Encoding video;
    public YoutubeQuality vq;
    public Encoding audio;
    public AudioQuality aq;

    public StreamCombined() {
    }

    public StreamCombined(Container c, Encoding v, YoutubeQuality vq, Encoding a, AudioQuality aq) {
        super(c);

        this.video = v;
        this.vq = vq;
        this.audio = a;
        this.aq = aq;
    }

    public String toString() {
        return c.toString() + " " + video.toString() + "(" + vq.toString() + ") " + audio.toString() + "("
                + aq.toString() + ")";
    }
}
