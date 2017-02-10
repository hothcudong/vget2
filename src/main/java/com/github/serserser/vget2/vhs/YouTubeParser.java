package com.github.serserser.vget2.vhs;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.serserser.vget2.exceptions.DownloadEmptyTitle;
import com.github.serserser.vget2.info.VGetParser;
import com.github.serserser.vget2.info.VideoFileInfo;
import com.github.serserser.vget2.info.VideoInfo;
import com.github.serserser.vget2.vhs.exceptions.AgeException;
import com.github.serserser.vget2.vhs.exceptions.EmbeddingDisabled;
import com.github.serserser.vget2.vhs.exceptions.VideoDeleted;
import com.github.serserser.vget2.vhs.exceptions.VideoUnavailablePlayer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.axet.wget.WGet;
import com.github.axet.wget.info.ex.DownloadError;
import com.github.axet.wget.info.ex.DownloadRetry;

public class YouTubeParser extends VGetParser {

    public YouTubeParser() {
    }

    public static boolean probe(URL url) {
        return url.toString().contains("youtube.com");
    }

    public List<VideoDownload> extractLinks(final YouTubeInfo info) {
        return extractLinks(info, new AtomicBoolean(), new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public List<VideoDownload> extractLinks(final YouTubeInfo info, final AtomicBoolean stop, final Runnable notify) {
        try {
            List<VideoDownload> sNextVideoURL = new ArrayList<VideoDownload>();

            try {
                streamCapture(sNextVideoURL, info, stop, notify);
            } catch (DownloadError e) {
                try {
                    extractEmbedded(sNextVideoURL, info, stop, notify);
                } catch (EmbeddingDisabled ignore) {
                    throw e;
                }
            }
            return sNextVideoURL;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * do not allow to download age restricted videos
     * 
     * @param sNextVideoURL
     *            url list to download
     * @param info
     *            download info
     * @param stop
     *            stop flag
     * @param notify
     *            notify object
     * @throws Exception
     *             download error
     */
    public void streamCapture(List<VideoDownload> sNextVideoURL, final YouTubeInfo info, final AtomicBoolean stop,
            final Runnable notify) throws Exception {
        String html;
        html = WGet.getHtml(info.getWeb(), new WGet.HtmlLoader() {
            @Override
            public void notifyRetry(int retry, int delay, Throwable e) {
                info.setRetrying(retry, delay, e);
                notify.run();
            }

            @Override
            public void notifyDownloading() {
                info.setState(VideoInfo.States.DOWNLOADING);
                notify.run();
            }

            @Override
            public void notifyMoved() {
                info.setState(VideoInfo.States.RETRYING);
                notify.run();
            }
        }, stop);
        extractHtmlInfo(sNextVideoURL, info, html, stop, notify);
        extractIcon(info, html);
    }

    /**
     * Add resolution video for specific youtube link.
     * 
     * @param sNextVideoURL
     *            download urls
     * @param url
     *            download source url
     */
    public void filter(List<VideoDownload> sNextVideoURL, String itag, URL url) {
        Integer i = Integer.decode(itag);
        YouTubeInfo.StreamInfo vd = itagMap.get(i);

        sNextVideoURL.add(new VideoDownload(vd, url));
    }

    // http://en.wikipedia.org/wiki/YouTube#Quality_and_codecs

    public static final Map<Integer, YouTubeInfo.StreamInfo> itagMap = new HashMap<Integer, YouTubeInfo.StreamInfo>() {
        private static final long serialVersionUID = -6925194111122038477L;

        {
            put(120, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.FLV, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p720, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k128));
            put(102, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP8, YouTubeInfo.YoutubeQuality.p720, YouTubeInfo.Encoding.VORBIS,
                    YouTubeInfo.AudioQuality.k192));
            put(101, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP8, YouTubeInfo.YoutubeQuality.p360, YouTubeInfo.Encoding.VORBIS,
                    YouTubeInfo.AudioQuality.k192)); // webm
            put(100, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP8, YouTubeInfo.YoutubeQuality.p360, YouTubeInfo.Encoding.VORBIS,
                    YouTubeInfo.AudioQuality.k128)); // webm
            put(85, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p1080, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k192)); // mp4
            put(84, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p720, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k192)); // mp4
            put(83, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p240, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k96)); // mp4
            put(82, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p360, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k96)); // mp4
            put(46, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP8, YouTubeInfo.YoutubeQuality.p1080, YouTubeInfo.Encoding.VORBIS,
                    YouTubeInfo.AudioQuality.k192)); // webm
            put(45, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP8, YouTubeInfo.YoutubeQuality.p720, YouTubeInfo.Encoding.VORBIS,
                    YouTubeInfo.AudioQuality.k192)); // webm
            put(44, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP8, YouTubeInfo.YoutubeQuality.p480, YouTubeInfo.Encoding.VORBIS,
                    YouTubeInfo.AudioQuality.k128)); // webm
            put(43, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP8, YouTubeInfo.YoutubeQuality.p360, YouTubeInfo.Encoding.VORBIS,
                    YouTubeInfo.AudioQuality.k128)); // webm
            put(38, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p3072, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k192)); // mp4
            put(37, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p1080, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k192)); // mp4
            put(36, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.GP3, YouTubeInfo.Encoding.MP4, YouTubeInfo.YoutubeQuality.p240, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k36)); // 3gp
            put(35, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.FLV, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p480, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k128)); // flv
            put(34, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.FLV, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p360, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k128)); // flv
            put(22, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p720, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k192)); // mp4
            put(18, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p360, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k96)); // mp4
            put(17, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.GP3, YouTubeInfo.Encoding.MP4, YouTubeInfo.YoutubeQuality.p144, YouTubeInfo.Encoding.AAC,
                    YouTubeInfo.AudioQuality.k24)); // 3gp
            put(6, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.FLV, YouTubeInfo.Encoding.H263, YouTubeInfo.YoutubeQuality.p270, YouTubeInfo.Encoding.MP3,
                    YouTubeInfo.AudioQuality.k64)); // flv
            put(5, new YouTubeInfo.StreamCombined(YouTubeInfo.Container.FLV, YouTubeInfo.Encoding.H263, YouTubeInfo.YoutubeQuality.p240, YouTubeInfo.Encoding.MP3,
                    YouTubeInfo.AudioQuality.k64)); // flv

            put(133, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p240));
            put(134, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p360));
            put(135, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p480));
            put(136, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p720));
            put(137, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p1080));
            put(138, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p2160));
            put(160, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p144));
            put(242, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p240));
            put(243, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p360));
            put(244, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p480));
            put(247, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p720));
            put(248, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p1080));
            put(264, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p1440));
            put(271, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p1440));
            put(272, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p2160));
            put(278, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p144));
            put(298, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p720));
            put(299, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.H264, YouTubeInfo.YoutubeQuality.p1080));
            put(302, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p720));
            put(303, new YouTubeInfo.StreamVideo(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VP9, YouTubeInfo.YoutubeQuality.p1080));

            put(139, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.AAC, YouTubeInfo.AudioQuality.k48));
            put(140, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.AAC, YouTubeInfo.AudioQuality.k128));
            put(141, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.MP4, YouTubeInfo.Encoding.AAC, YouTubeInfo.AudioQuality.k256));
            put(171, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VORBIS, YouTubeInfo.AudioQuality.k128));
            put(172, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.VORBIS, YouTubeInfo.AudioQuality.k192));

            put(249, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.OPUS, YouTubeInfo.AudioQuality.k50));
            put(250, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.OPUS, YouTubeInfo.AudioQuality.k70));
            put(251, new YouTubeInfo.StreamAudio(YouTubeInfo.Container.WEBM, YouTubeInfo.Encoding.OPUS, YouTubeInfo.AudioQuality.k160));
        }
    };

    public static String extractId(URL url) {
        {
            Pattern u = Pattern.compile("youtube.com/watch?.*v=([^&]*)");
            Matcher um = u.matcher(url.toString());
            if (um.find())
                return um.group(1);
        }

        {
            Pattern u = Pattern.compile("youtube.com/v/([^&]*)");
            Matcher um = u.matcher(url.toString());
            if (um.find())
                return um.group(1);
        }

        return null;
    }

    /**
     * allows to download age restricted videos
     * 
     * @param info
     * @param stop
     * @param notify
     * @throws Exception
     */
    public void extractEmbedded(List<VideoDownload> sNextVideoURL, final YouTubeInfo info, final AtomicBoolean stop,
            final Runnable notify) throws Exception {
        String id = extractId(info.getWeb());
        if (id == null) {
            throw new RuntimeException("unknown url");
        }

        info.setTitle(String.format("https://www.youtube.com/watch?v=%s", id));

        String get = String.format("https://www.youtube.com/get_video_info?authuser=0&video_id=%s&el=embedded", id);

        URL url = new URL(get);

        String qs = WGet.getHtml(url, new WGet.HtmlLoader() {
            @Override
            public void notifyRetry(int retry, int delay, Throwable e) {
                info.setRetrying(retry, delay, e);
                notify.run();
            }

            @Override
            public void notifyDownloading() {
                info.setState(VideoInfo.States.DOWNLOADING);
                notify.run();
            }

            @Override
            public void notifyMoved() {
                info.setState(VideoInfo.States.RETRYING);
                notify.run();
            }
        }, stop);

        Map<String, String> map = getQueryMap(qs);

        if (map.get("status").equals("fail")) {
            String r = URLDecoder.decode(map.get("reason"), WGet.UTF8);
            if (map.get("errorcode").equals("150"))
                throw new EmbeddingDisabled("error code 150");
            if (map.get("errorcode").equals("100"))
                throw new VideoDeleted("error code 100");

            throw new DownloadError(r);
            // throw new PrivateVideoException(r);
        }

        info.setTitle(URLDecoder.decode(map.get("title"), WGet.UTF8));

        // String fmt_list = URLDecoder.decode(map.get("fmt_list"), UTF8);
        // String[] fmts = fmt_list.split(",");

        String url_encoded_fmt_stream_map = URLDecoder.decode(map.get("url_encoded_fmt_stream_map"), WGet.UTF8);

        extractUrlEncodedVideos(sNextVideoURL, url_encoded_fmt_stream_map, info, stop, notify);

        // 'iurlmaxresæ or 'iurlsd' or 'thumbnail_url'
        String icon = map.get("thumbnail_url");
        icon = URLDecoder.decode(icon, WGet.UTF8);
        info.setIcon(new URL(icon));
    }

    public void extractIcon(VideoInfo info, String html) {
        try {
            Pattern title = Pattern.compile("itemprop=\"thumbnailUrl\" href=\"(.*)\"");
            Matcher titleMatch = title.matcher(html);
            if (titleMatch.find()) {
                String sline = titleMatch.group(1);
                sline = StringEscapeUtils.unescapeHtml4(sline);
                info.setIcon(new URL(sline));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getQueryMap(String qs) {
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            qs = qs.trim();
            String[] pairs = qs.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                map.put(URLDecoder.decode(pair.substring(0, idx), WGet.UTF8),
                        URLDecoder.decode(pair.substring(idx + 1), WGet.UTF8));
            }
            return map;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(qs, e);
        }
    }

    public void extractHtmlInfo(List<VideoDownload> sNextVideoURL, YouTubeInfo info, String html, AtomicBoolean stop,
            Runnable notify) throws Exception {
        {
            Pattern age = Pattern.compile("(verify_age)");
            Matcher ageMatch = age.matcher(html);
            if (ageMatch.find())
                throw new AgeException();
        }

        {
            Pattern age = Pattern.compile("(unavailable-player)");
            Matcher ageMatch = age.matcher(html);
            if (ageMatch.find())
                throw new VideoUnavailablePlayer();
        }

        // grab html5 player url
        {
            Pattern playerURL = Pattern.compile("(//.*?/player-[\\w\\d\\-]+\\/.*\\.js)");
            Matcher playerVersionMatch = playerURL.matcher(html);
            if (playerVersionMatch.find()) {
                info.setPlayerURI(new URI("https:" + playerVersionMatch.group(1)));
            }
        }

        // combined streams
        {
            Pattern urlencod = Pattern.compile("\"url_encoded_fmt_stream_map\":\"([^\"]*)\"");
            Matcher urlencodMatch = urlencod.matcher(html);
            if (urlencodMatch.find()) {
                String url_encoded_fmt_stream_map;
                url_encoded_fmt_stream_map = urlencodMatch.group(1);

                // normal embedded video, unable to grab age restricted videos
                Pattern encod = Pattern.compile("url=(.*)");
                Matcher encodMatch = encod.matcher(url_encoded_fmt_stream_map);
                if (encodMatch.find()) {
                    String sline = encodMatch.group(1);

                    extractUrlEncodedVideos(sNextVideoURL, sline, info, stop, notify);
                }

                // stream video
                Pattern encodStream = Pattern.compile("stream=(.*)");
                Matcher encodStreamMatch = encodStream.matcher(url_encoded_fmt_stream_map);
                if (encodStreamMatch.find()) {
                    String sline = encodStreamMatch.group(1);

                    String[] urlStrings = sline.split("stream=");

                    for (String urlString : urlStrings) {
                        urlString = StringEscapeUtils.unescapeJava(urlString);

                        Pattern link = Pattern.compile("(sparams.*)&itag=(\\d+)&.*&conn=rtmpe(.*),");
                        Matcher linkMatch = link.matcher(urlString);
                        if (linkMatch.find()) {

                            String sparams = linkMatch.group(1);
                            String itag = linkMatch.group(2);
                            String url = linkMatch.group(3);

                            url = "https" + url + "?" + sparams;

                            url = URLDecoder.decode(url, WGet.UTF8);

                            filter(sNextVideoURL, itag, new URL(url));
                        }
                    }
                }
            }
        }

        // separate streams
        {
            Pattern urlencod = Pattern.compile("\"adaptive_fmts\":\\s*\"([^\"]*)\"");
            Matcher urlencodMatch = urlencod.matcher(html);
            if (urlencodMatch.find()) {
                String url_encoded_fmt_stream_map;
                url_encoded_fmt_stream_map = urlencodMatch.group(1);

                // normal embedded video, unable to grab age restricted videos
                Pattern encod = Pattern.compile("url=(.*)");
                Matcher encodMatch = encod.matcher(url_encoded_fmt_stream_map);
                if (encodMatch.find()) {
                    String sline = encodMatch.group(1);

                    extractUrlEncodedVideos(sNextVideoURL, sline, info, stop, notify);
                }

                // stream video
                Pattern encodStream = Pattern.compile("stream=(.*)");
                Matcher encodStreamMatch = encodStream.matcher(url_encoded_fmt_stream_map);
                if (encodStreamMatch.find()) {
                    String sline = encodStreamMatch.group(1);

                    String[] urlStrings = sline.split("stream=");

                    for (String urlString : urlStrings) {
                        urlString = StringEscapeUtils.unescapeJava(urlString);

                        Pattern link = Pattern.compile("(sparams.*)&itag=(\\d+)&.*&conn=rtmpe(.*),");
                        Matcher linkMatch = link.matcher(urlString);
                        if (linkMatch.find()) {

                            String sparams = linkMatch.group(1);
                            String itag = linkMatch.group(2);
                            String url = linkMatch.group(3);

                            url = "https" + url + "?" + sparams;

                            url = URLDecoder.decode(url, WGet.UTF8);

                            filter(sNextVideoURL, itag, new URL(url));
                        }
                    }
                }
            }
        }

        {
            Pattern title = Pattern.compile("<meta name=\"title\" content=(.*)");
            Matcher titleMatch = title.matcher(html);
            if (titleMatch.find()) {
                String sline = titleMatch.group(1);
                String name = sline.replaceFirst("<meta name=\"title\" content=", "").trim();
                name = StringUtils.strip(name, "\">");
                name = StringEscapeUtils.unescapeHtml4(name);
                info.setTitle(name);
            }
        }
        if (info.getTitle() == null)
            throw new DownloadEmptyTitle("Empty title"); // some times youtube return strange html, cause this error
    }

    public void extractUrlEncodedVideos(List<VideoDownload> sNextVideoURL, String sline, YouTubeInfo info,
            AtomicBoolean stop, Runnable notify) throws Exception {
        String[] urlStrings = sline.split("url=");

        for (String urlString : urlStrings) {
            urlString = StringEscapeUtils.unescapeJava(urlString);

            String urlFull = URLDecoder.decode(urlString, WGet.UTF8);

            // universal request
            {
                String url = null;
                {
                    Pattern link = Pattern.compile("([^&,]*)[&,]");
                    Matcher linkMatch = link.matcher(urlString);
                    if (linkMatch.find()) {
                        url = linkMatch.group(1);
                        url = URLDecoder.decode(url, WGet.UTF8);
                    }
                }

                String itag = null;
                {
                    Pattern link = Pattern.compile("itag=(\\d+)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if (linkMatch.find()) {
                        itag = linkMatch.group(1);
                    }
                }

                String sig = null;

                if (sig == null) {
                    Pattern link = Pattern.compile("&signature=([^&,]*)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if (linkMatch.find()) {
                        sig = linkMatch.group(1);
                    }
                }

                if (sig == null) {
                    Pattern link = Pattern.compile("sig=([^&,]*)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if (linkMatch.find()) {
                        sig = linkMatch.group(1);
                    }
                }

                if (sig == null) {
                    Pattern link = Pattern.compile("[&,]s=([^&,]*)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if (linkMatch.find()) {
                        sig = linkMatch.group(1);
                        if (info.getPlayerURI() == null) {
                            SignatureDecryptor ss = new SignatureDecryptor(sig);
                            sig = ss.decrypt();
                        } else {
                            Html5SignatureDecryptor ss = new Html5SignatureDecryptor(sig, info.getPlayerURI());
                            sig = ss.decrypt(stop, notify);
                        }
                    }
                }

                if (url != null && itag != null && sig != null) {
                    try {
                        url += "&signature=" + sig;
                        filter(sNextVideoURL, itag, new URL(url));
                        continue;
                    } catch (MalformedURLException e) {
                        // ignore bad urls
                    }
                }
            }
        }
    }

    @Override
    public List<VideoFileInfo> extract(VideoInfo vinfo, AtomicBoolean stop, Runnable notify) {
        List<VideoDownload> videos = extractLinks((YouTubeInfo) vinfo, stop, notify);

        if (videos.size() == 0) {
            // rare error:
            //
            // The live recording you're trying to play is still being processed
            // and will be available soon. Sorry, please try again later.
            //
            // retry. since youtube may already rendrered propertly quality.
            throw new DownloadRetry("empty video download list," + " wait until youtube will process the video");
        }

        List<VideoDownload> audios = new ArrayList<VideoDownload>();

        for (int i = videos.size() - 1; i >= 0; i--) {
            if (videos.get(i).stream == null) {
                videos.remove(i);
            } else if ((videos.get(i).stream instanceof YouTubeInfo.StreamAudio)) {
                audios.add(videos.remove(i));
            }
        }

        videos.sort(new VideoContentFirstComparator());
        audios.sort(new VideoContentFirstComparator());

        for (int i = 0; i < videos.size();) {
            VideoDownload v = videos.get(i);

            YouTubeInfo yinfo = (YouTubeInfo) vinfo;
            yinfo.setStreamInfo(v.stream);

            VideoFileInfo info = new VideoFileInfo(v.url);

            if (v.stream instanceof YouTubeInfo.StreamCombined ) {
                vinfo.setInfo(Arrays.asList(info));
            }

            if (v.stream instanceof YouTubeInfo.StreamVideo ) {
                if (audios.size() > 0) {
                    VideoFileInfo info2 = new VideoFileInfo(audios.get(0).url); // take first (highest quality)
                    vinfo.setInfo(Arrays.asList(info, info2));
                } else {
                    // no audio stream?
                    vinfo.setInfo(Arrays.asList(info));
                }
            }

            vinfo.setSource(v.url);
            return vinfo.getInfo();
        }

        for (int i = 0; i < audios.size();) { // only audio mode?
            VideoFileInfo info = new VideoFileInfo(audios.get(i).url);
            vinfo.setInfo(Arrays.asList(info));

            vinfo.setSource(info.getSource());
            return vinfo.getInfo();
        }

        // throw download stop if user choice not maximum quality and we have no
        // video rendered by youtube

        throw new DownloadError("no video with required quality found,"
                + " increace VideoInfo.setVq to the maximum and retry download");
    }

    @Override
    public VideoInfo info(URL web) {
        return new YouTubeInfo(web);
    }
}
