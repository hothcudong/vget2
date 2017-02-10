package com.github.serserser.vget2.vhs;

import java.util.Comparator;

public class VideoContentFirstComparator implements Comparator<VideoDownload> {
    int ordinal(VideoDownload o1) {
        if (o1.stream instanceof YouTubeInfo.StreamCombined ) {
            YouTubeInfo.StreamCombined c1 = (YouTubeInfo.StreamCombined) o1.stream;
            return c1.vq.ordinal();
        }
        if (o1.stream instanceof YouTubeInfo.StreamVideo ) {
            YouTubeInfo.StreamVideo c1 = (YouTubeInfo.StreamVideo) o1.stream;
            return c1.vq.ordinal();
        }
        if (o1.stream instanceof YouTubeInfo.StreamAudio ) {
            YouTubeInfo.StreamAudio c1 = (YouTubeInfo.StreamAudio) o1.stream;
            return c1.aq.ordinal();
        }
        throw new RuntimeException("bad video array type");
    }

    @Override
    public int compare(VideoDownload o1, VideoDownload o2) {
        Integer i1 = ordinal(o1);
        Integer i2 = ordinal(o2);
        Integer ic = i1.compareTo(i2);

        return ic;
    }

}