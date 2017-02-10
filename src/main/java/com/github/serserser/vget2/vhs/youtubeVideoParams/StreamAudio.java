package com.github.serserser.vget2.vhs.youtubeVideoParams;

public class StreamAudio extends StreamInfo {
    public Encoding audio;
    public AudioQuality aq;

    public StreamAudio() {
    }

    public StreamAudio(Container c, Encoding a, AudioQuality q) {
        super(c);
        this.audio = a;
        this.aq = q;
    }

    public String toString() {
        return c.toString() + " " + audio.toString() + " " + aq.toString();
    }
}
