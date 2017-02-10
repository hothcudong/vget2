package com.github.serserser.vget2.vhs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.serserser.vget2.info.VGetParser;
import com.github.serserser.vget2.info.VideoFileInfo;
import com.github.serserser.vget2.vhs.vimeo.VimeoVideoDownload;
import com.github.serserser.vget2.vhs.vimeo.downloadInfo.VimeoData;
import org.apache.commons.lang3.StringEscapeUtils;

import com.github.serserser.vget2.info.VideoInfo;
import com.github.serserser.vget2.info.VideoInfo.States;
import com.github.serserser.vget2.vhs.vimeo.VimeoQuality;
import com.github.axet.wget.WGet;
import com.github.axet.wget.WGet.HtmlLoader;
import com.github.axet.wget.info.ex.DownloadError;
import com.google.gson.Gson;

public class VimeoParser extends VGetParser {

    public VimeoParser() {
    }

    public static boolean probe(URL url) {
        return url.toString().contains("vimeo.com");
    }

    public static String extractId(URL url) {
        // standard web url. format: "https://vimeo.com/49243107" or
        // "http://vimeo.com/channels/staffpicks/49243107"
        {
            Pattern u = Pattern.compile("vimeo.com.*/(\\d+)");
            Matcher um = u.matcher(url.toString());

            if (um.find())
                return um.group(1);
        }
        // rss feed url. format:
        // "http://vimeo.com/moogaloop.swf?clip_id=49243107"
        {
            Pattern u = Pattern.compile("vimeo.com.*=(\\d+)");
            Matcher um = u.matcher(url.toString());

            if (um.find())
                return um.group(1);
        }
        return null;
    }

    public List<VimeoVideoDownload> extractLinks(final VideoInfo info, final AtomicBoolean stop, final Runnable notify) {
        List<VimeoVideoDownload> list = new ArrayList<VimeoVideoDownload>();

        try {
            String id;
            String clip;
            {
                id = extractId(info.getWeb());
                if (id == null) {
                    throw new DownloadError("unknown url");
                }
                clip = "http://vimeo.com/m/" + id;
            }

            URL url = new URL(clip);

            String html = WGet.getHtml(url, new HtmlLoader() {
                @Override
                public void notifyRetry(int retry, int delay, Throwable e) {
                    info.setRetrying(retry, delay, e);
                    notify.run();
                }

                @Override
                public void notifyDownloading() {
                    info.setState(States.EXTRACTING);
                    notify.run();
                }

                @Override
                public void notifyMoved() {
                    info.setState(States.RETRYING);
                    notify.run();
                }
            }, stop);

            String config;
            {
                Pattern u = Pattern.compile("data-config-url=\"([^\"]+)\"");
                Matcher um = u.matcher(html);
                if (!um.find()) {
                    throw new DownloadError("unknown config vimeo respond");
                }
                config = um.group(1);
            }

            config = StringEscapeUtils.unescapeHtml4(config);

            String htmlConfig = WGet.getHtml(new URL(config), new HtmlLoader() {
                @Override
                public void notifyRetry(int retry, int delay, Throwable e) {
                    info.setRetrying(retry, delay, e);
                    notify.run();
                }

                @Override
                public void notifyDownloading() {
                    info.setState(States.EXTRACTING);
                    notify.run();
                }

                @Override
                public void notifyMoved() {
                    info.setState(States.RETRYING);
                    notify.run();
                }
            }, stop);

            VimeoData data = new Gson().fromJson(htmlConfig, VimeoData.class);

            String icon = data.getVideo().getThumbs().values().iterator().next();

            info.setTitle(data.getVideo().getTitle());

            if (data.getRequest().getFiles().getH264().getHd() != null)
                list.add(new VimeoVideoDownload(VimeoQuality.pHi, new URL(data.getRequest().getFiles().getH264().getHd().getUrl())));

            if (data.getRequest().getFiles().getH264().getSd() != null)
                list.add(new VimeoVideoDownload(VimeoQuality.pLow, new URL(data.getRequest().getFiles().getH264().getSd().getUrl())));

            info.setIcon(new URL(icon));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public List<VideoFileInfo> extract(VideoInfo vinfo, AtomicBoolean stop, Runnable notify) {
        List<VimeoVideoDownload> sNextVideoURL = extractLinks(vinfo, stop, notify);

        Collections.sort(sNextVideoURL, new VimeoVideoContentFirstComparator());

        for (int i = 0; i < sNextVideoURL.size();) {
            VimeoVideoDownload v = sNextVideoURL.get(i);

            VimeoInfo yinfo = (VimeoInfo) vinfo;
            yinfo.setVideoQuality(v.getVq());
            VideoFileInfo info = new VideoFileInfo(v.getUrl());
            vinfo.setInfo(Arrays.asList(info));
            vinfo.setSource(v.getUrl());
            return vinfo.getInfo();
        }

        // throw download stop if user choice not maximum quality and we have no
        // video rendered by

        throw new DownloadError("no video with required quality found");
    }

    @Override
    public VideoInfo info(URL web) {
        return new VimeoInfo(web);
    }
}
