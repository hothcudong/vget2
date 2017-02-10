package com.github.serserser.vget2;

import java.net.URL;
import java.util.List;

import com.github.serserser.vget2.vhs.youtube.YoutubeVideoDownload;
import com.github.serserser.vget2.vhs.YouTubeInfo;
import com.github.serserser.vget2.vhs.YouTubeParser;

public class ExtractDownloadLinks {

    public static void main(String[] args) {
        try {
            // ex: http://www.youtube.com/watch?v=Nj6PFaDmp6c
            String url = args[0];

            YouTubeInfo info = new YouTubeInfo(new URL(url));

            YouTubeParser parser = new YouTubeParser();

            List<YoutubeVideoDownload> list = parser.extractLinks(info);

            for (YoutubeVideoDownload d : list) {
                System.out.println(d.stream + " " + d.url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
