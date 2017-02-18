package com.github.serserser.dummyApps.applicationManagedDownload;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.serserser.vget2.VGet;
import com.github.serserser.vget2.info.Parser;
import com.github.serserser.vget2.info.ServiceProvider;
import com.github.serserser.vget2.info.VideoFileInfo;
import com.github.serserser.vget2.info.VideoInfo;
import com.github.axet.wget.info.ex.DownloadInterruptedError;

// TODO: [jgolda] The case of notifications about state should be solved in a different way
// TODO: [jgolda] (maybe by registering a listener in downloader object)

public class App {

    public static void main(String[] args) {
        // ex: http://www.youtube.com/watch?v=Nj6PFaDmp6c
        String url = args[0];
        // ex: /Users/axet/Downloads/
        File path = new File(args[1]);

        try {
            final AtomicBoolean stop = new AtomicBoolean(false);

            URL web = new URL(url);

            // [OPTIONAL] limit maximum quality, or do not call this function if
            // you wish maximum quality available.
            //
            // if youtube does not have video with requested quality, program
            // will raise en exception.

            // create proper html parser depends on url
            ServiceProvider provider = new ServiceProvider();

            Parser parser = provider.getParserForUrl(web);

            // download limited video quality from youtube
            // parser = new YouTubeQParser(YoutubeQuality.p480);

            // download mp4 format only, fail if non exist
            // parser = new YouTubeMPGParser();

            // create proper videoinfo to keep specific video information
            VideoInfo videoinfo = provider.getInfoForUrl(web);

            VGet v = new VGet(web, path);

            // [OPTIONAL] call v.extract() only if you d like to get video title
            // or download url link before start download. or just skip it.
            v.extract(parser, stop);

            System.out.println("Title: " + videoinfo.getTitle());
            List<VideoFileInfo> list = videoinfo.getInfo();
            if (list != null) {
                for (VideoFileInfo d : list) {
                    // [OPTIONAL] setTarget file for each download source video/audio
                    // use d.getContentType() to determine which or use
                    // v.targetFile(dinfo, ext, conflict) to set name dynamically or
                    // d.targetFile = new File("/Downloads/CustomName.mp3");
                    // to set file name manually.
                    System.out.println("Download URL: " + d.getSource());
                }
            }

            v.download(parser);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}