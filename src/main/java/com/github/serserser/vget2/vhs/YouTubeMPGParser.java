package com.github.serserser.vget2.vhs;

import java.net.URL;
import java.util.List;

import com.github.serserser.vget2.vhs.youtube.YoutubeITags;
import com.github.serserser.vget2.vhs.youtubeVideoParams.Container;
import com.github.serserser.vget2.vhs.youtubeVideoParams.StreamInfo;
import com.github.serserser.vget2.vhs.youtube.YoutubeVideoDownload;

public class YouTubeMPGParser extends YouTubeParser {

    private YoutubeITags itags;

    public YouTubeMPGParser() {
        itags = YoutubeITags.getInstance();
    }

    public void filter(List<YoutubeVideoDownload> sNextVideoURL, String itag, URL url) {
        Integer i = Integer.decode(itag);
        StreamInfo vd = itags.getStream(i);

        // get rid of webm
        if (vd.c == Container.WEBM)
            return;

        super.filter(sNextVideoURL, itag, url);
    }
}
