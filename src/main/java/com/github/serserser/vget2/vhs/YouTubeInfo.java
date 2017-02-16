package com.github.serserser.vget2.vhs;

import java.net.URI;
import java.net.URL;

import com.github.serserser.vget2.info.VideoInfo;
import com.github.serserser.vget2.vhs.youtube.params.*;

public class YouTubeInfo extends VideoInfo {

    private GenericStreamInfo vq;
    private URI playerURI;

    public YouTubeInfo(URL web) {
        super(web);
    }

    public GenericStreamInfo getVideoQuality() {
        return vq;
    }

    public void setStreamInfo(GenericStreamInfo vq) {
        this.vq = vq;
    }

    public URI getPlayerURI() {
        return playerURI;
    }

    public void setPlayerURI(URI playerURI) {
        this.playerURI = playerURI;
    }
}