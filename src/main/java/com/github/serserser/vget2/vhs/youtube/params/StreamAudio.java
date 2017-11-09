package com.github.serserser.vget2.vhs.youtube.params;

public class StreamAudio extends GenericStreamInfo {
    private Encoding audio;
    private AudioQuality aq;

    public StreamAudio() {
    }

    public StreamAudio(Container c, Encoding a, AudioQuality q) {
        super(c);
        this.audio = a;
        this.aq = q;
    }

    public Encoding getAudio() {
        return audio;
    }

    public void setAudio(Encoding audio) {
        this.audio = audio;
    }

    public AudioQuality getAq() {
        return aq;
    }

    public void setAq(AudioQuality aq) {
        this.aq = aq;
    }
}
