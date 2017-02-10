package com.github.serserser.vget2.vhs;

import com.github.serserser.vget2.vhs.youtube.YoutubeVideoDownload;
import com.github.serserser.vget2.vhs.youtubeVideoParams.StreamAudio;
import com.github.serserser.vget2.vhs.youtubeVideoParams.StreamCombined;
import com.github.serserser.vget2.vhs.youtubeVideoParams.StreamVideo;

import java.util.Comparator;

public class YoutubeVideoContentFirstComparator implements Comparator<YoutubeVideoDownload> {
    int ordinal(YoutubeVideoDownload o1) {
        if (o1.getStream() instanceof StreamCombined ) {
            StreamCombined c1 = (StreamCombined) o1.getStream();
            return c1.vq.ordinal();
        }
        if (o1.getStream() instanceof StreamVideo ) {
            StreamVideo c1 = (StreamVideo) o1.getStream();
            return c1.vq.ordinal();
        }
        if (o1.getStream() instanceof StreamAudio ) {
            StreamAudio c1 = (StreamAudio) o1.getStream();
            return c1.getAq().ordinal();
        }
        throw new RuntimeException("bad video array type");
    }

    @Override
    public int compare(YoutubeVideoDownload o1, YoutubeVideoDownload o2) {
        Integer i1 = ordinal(o1);
        Integer i2 = ordinal(o2);
        Integer ic = i1.compareTo(i2);

        return ic;
    }

}