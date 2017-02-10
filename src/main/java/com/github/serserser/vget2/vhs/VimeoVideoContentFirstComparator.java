package com.github.serserser.vget2.vhs;

import com.github.serserser.vget2.vhs.vimeo.VimeoVideoDownload;

import java.util.Comparator;

// FIXME: [jgolda] how about trying to join it
// FIXME: [jgolda] in some intelligent way with the YoutubeVideoContentFirstComparator
// FIXME: [jgolda] or some stuff like this :D
public class VimeoVideoContentFirstComparator implements Comparator<VimeoVideoDownload> {
    @Override
    public int compare(VimeoVideoDownload o1, VimeoVideoDownload o2) {
        Integer i1 = o1.getVq().ordinal();
        Integer i2 = o2.getVq().ordinal();
        Integer ic = i1.compareTo(i2);

        return ic;
    }
}
