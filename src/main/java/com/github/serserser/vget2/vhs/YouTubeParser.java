package com.github.serserser.vget2.vhs;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.serserser.vget2.exceptions.DownloadEmptyTitle;
import com.github.serserser.vget2.info.Parser;
import com.github.serserser.vget2.info.VideoFileInfo;
import com.github.serserser.vget2.info.VideoInfo;
import com.github.serserser.vget2.vhs.decryption.Html5SignatureDecryptor;
import com.github.serserser.vget2.vhs.decryption.SignatureDecryptor;
import com.github.serserser.vget2.vhs.exceptions.AgeException;
import com.github.serserser.vget2.vhs.exceptions.EmbeddingDisabled;
import com.github.serserser.vget2.vhs.exceptions.VideoDeleted;
import com.github.serserser.vget2.vhs.exceptions.VideoUnavailablePlayer;
import com.github.serserser.vget2.vhs.youtube.YoutubeITags;
import com.github.serserser.vget2.vhs.youtube.YoutubeVideoDownload;
import com.github.serserser.vget2.vhs.youtube.params.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.axet.wget.WGet;
import com.github.axet.wget.info.ex.DownloadError;
import com.github.axet.wget.info.ex.DownloadRetry;

public class YouTubeParser extends Parser {

    private YoutubeITags itags;

    public YouTubeParser() {
        itags = YoutubeITags.getInstance();
    }

    public List<YoutubeVideoDownload> extractLinks(final YouTubeInfo info) {
        return extractLinks(info, new AtomicBoolean());
    }

    protected List<YoutubeVideoDownload> extractLinks(final YouTubeInfo info, final AtomicBoolean stop) {
        try {
            List<YoutubeVideoDownload> sNextVideoURL = new ArrayList<>();

            try {
                sNextVideoURL.addAll(streamCapture(info, stop));
            } catch ( DownloadError e ) {
                try {
                    sNextVideoURL.addAll(extractEmbedded(info, stop));
                } catch ( EmbeddingDisabled ignore ) {
                    throw e;
                }
            }
            return sNextVideoURL;
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    private List<YoutubeVideoDownload> streamCapture(final YouTubeInfo info, final AtomicBoolean stop) throws Exception {
        List<YoutubeVideoDownload> sNextVideoURL = new ArrayList<>();

        String html;
        html = WGet.getHtml(info.getWeb(), new WGet.HtmlLoader() {
            @Override
            public void notifyRetry(int retry, int delay, Throwable e) {
                info.setRetrying(retry, delay, e);
            }

            @Override
            public void notifyDownloading() {
                info.setState(VideoInfo.States.DOWNLOADING);
            }

            @Override
            public void notifyMoved() {
                info.setState(VideoInfo.States.RETRYING);
            }
        }, stop);
        sNextVideoURL.addAll(extractHtmlInfo(info, html, stop));
        extractIcon(info, html);

        return sNextVideoURL;
    }

    /**
     * Add resolution video for specific youtube link.
     *
     * @param url download source url
     */
    public YoutubeVideoDownload filter(String itag, URL url) {
        Integer i = Integer.decode(itag);
        GenericStreamInfo vd = itags.getStream(i);

        return new YoutubeVideoDownload(vd, url);
    }

    private static String extractId(URL url) {
        {
            Pattern u = Pattern.compile("youtube.com/watch?.*v=([^&]*)");
            Matcher um = u.matcher(url.toString());
            if ( um.find() )
                return um.group(1);
        }

        {
            Pattern u = Pattern.compile("youtube.com/v/([^&]*)");
            Matcher um = u.matcher(url.toString());
            if ( um.find() )
                return um.group(1);
        }

        return null;
    }

    /**
     * allows to download age restricted videos
     */
    private List<YoutubeVideoDownload> extractEmbedded(final YouTubeInfo info, final AtomicBoolean stop) throws Exception {

        List<YoutubeVideoDownload> sNextVideoURL = new ArrayList<>();

        String id = extractId(info.getWeb());
        if ( id == null ) {
            throw new RuntimeException("unknown url");
        }

        info.setTitle(String.format("https://www.youtube.com/watch?v=%s", id));

        String get = String.format("https://www.youtube.com/get_video_info?authuser=0&video_id=%s&el=embedded", id);

        URL url = new URL(get);

        String qs = WGet.getHtml(url, new WGet.HtmlLoader() {
            @Override
            public void notifyRetry(int retry, int delay, Throwable e) {
                info.setRetrying(retry, delay, e);
            }

            @Override
            public void notifyDownloading() {
                info.setState(VideoInfo.States.DOWNLOADING);
            }

            @Override
            public void notifyMoved() {
                info.setState(VideoInfo.States.RETRYING);
            }
        }, stop);

        Map<String, String> map = getQueryMap(qs);

        if ( map.get("status").equals("fail") ) {
            String r = URLDecoder.decode(map.get("reason"), WGet.UTF8);
            if ( map.get("errorcode").equals("150") )
                throw new EmbeddingDisabled("error code 150");
            if ( map.get("errorcode").equals("100") )
                throw new VideoDeleted("error code 100");

            throw new DownloadError(r);
            // throw new PrivateVideoException(r);
        }

        info.setTitle(URLDecoder.decode(map.get("title"), WGet.UTF8));

        // String fmt_list = URLDecoder.decode(map.get("fmt_list"), UTF8);
        // String[] fmts = fmt_list.split(",");

        String url_encoded_fmt_stream_map = URLDecoder.decode(map.get("url_encoded_fmt_stream_map"), WGet.UTF8);

        sNextVideoURL.addAll(extractUrlEncodedVideos(url_encoded_fmt_stream_map, info, stop));

        // 'iurlmaxresæ or 'iurlsd' or 'thumbnail_url'
        String icon = map.get("thumbnail_url");
        icon = URLDecoder.decode(icon, WGet.UTF8);
        info.setIcon(new URL(icon));

        return sNextVideoURL;
    }

    private void extractIcon(VideoInfo info, String html) {
        try {
            Pattern title = Pattern.compile("itemprop=\"thumbnailUrl\" href=\"(.*)\"");
            Matcher titleMatch = title.matcher(html);
            if ( titleMatch.find() ) {
                String sline = titleMatch.group(1);
                sline = StringEscapeUtils.unescapeHtml4(sline);
                info.setIcon(new URL(sline));
            }
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> getQueryMap(String qs) {
        try {
            HashMap<String, String> map = new HashMap<>();
            qs = qs.trim();
            String[] pairs = qs.split("&");
            for ( String pair : pairs ) {
                int idx = pair.indexOf("=");
                map.put(URLDecoder.decode(pair.substring(0, idx), WGet.UTF8),
                        URLDecoder.decode(pair.substring(idx + 1), WGet.UTF8));
            }
            return map;
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException(qs, e);
        }
    }

    private List<YoutubeVideoDownload> extractHtmlInfo(YouTubeInfo info, String html, AtomicBoolean stop) throws Exception {

        List<YoutubeVideoDownload> sNextVideoURL = new ArrayList<>();

        {
            Pattern age = Pattern.compile("(verify_age)");
            Matcher ageMatch = age.matcher(html);
            if ( ageMatch.find() )
                throw new AgeException();
        }

        {
            Pattern age = Pattern.compile("(unavailable-player)");
            Matcher ageMatch = age.matcher(html);
            if ( ageMatch.find() )
                throw new VideoUnavailablePlayer();
        }

        // grab html5 player url
        {
            Pattern playerURL = Pattern.compile("(//.*?/player-[\\w\\d\\-]+\\/.*\\.js)");
            Matcher playerVersionMatch = playerURL.matcher(html);
            if ( playerVersionMatch.find() ) {
                info.setPlayerURI(new URI("https:" + playerVersionMatch.group(1)));
            }
        }

        // combined streams
        {
            Pattern urlencod = Pattern.compile("\"url_encoded_fmt_stream_map\":\"([^\"]*)\"");
            Matcher urlencodMatch = urlencod.matcher(html);
            if ( urlencodMatch.find() ) {
                String url_encoded_fmt_stream_map;
                url_encoded_fmt_stream_map = urlencodMatch.group(1);

                // normal embedded video, unable to grab age restricted videos
                Pattern encod = Pattern.compile("url=(.*)");
                Matcher encodMatch = encod.matcher(url_encoded_fmt_stream_map);
                if ( encodMatch.find() ) {
                    String sline = encodMatch.group(1);

                    sNextVideoURL.addAll(extractUrlEncodedVideos(sline, info, stop));
                }

                // stream video
                Pattern encodStream = Pattern.compile("stream=(.*)");
                Matcher encodStreamMatch = encodStream.matcher(url_encoded_fmt_stream_map);
                if ( encodStreamMatch.find() ) {
                    String sline = encodStreamMatch.group(1);

                    String[] urlStrings = sline.split("stream=");

                    for ( String urlString : urlStrings ) {
                        urlString = StringEscapeUtils.unescapeJava(urlString);

                        Pattern link = Pattern.compile("(sparams.*)&itag=(\\d+)&.*&conn=rtmpe(.*),");
                        Matcher linkMatch = link.matcher(urlString);
                        if ( linkMatch.find() ) {

                            String sparams = linkMatch.group(1);
                            String itag = linkMatch.group(2);
                            String url = linkMatch.group(3);

                            url = "https" + url + "?" + sparams;

                            url = URLDecoder.decode(url, WGet.UTF8);

                            sNextVideoURL.add(filter(itag, new URL(url)));
                        }
                    }
                }
            }
        }

        // separate streams
        {
            Pattern urlencod = Pattern.compile("\"adaptive_fmts\":\\s*\"([^\"]*)\"");
            Matcher urlencodMatch = urlencod.matcher(html);
            if ( urlencodMatch.find() ) {
                String url_encoded_fmt_stream_map;
                url_encoded_fmt_stream_map = urlencodMatch.group(1);

                // normal embedded video, unable to grab age restricted videos
                Pattern encod = Pattern.compile("url=(.*)");
                Matcher encodMatch = encod.matcher(url_encoded_fmt_stream_map);
                if ( encodMatch.find() ) {
                    String sline = encodMatch.group(1);

                    sNextVideoURL.addAll(extractUrlEncodedVideos(sline, info, stop));
                }

                // stream video
                Pattern encodStream = Pattern.compile("stream=(.*)");
                Matcher encodStreamMatch = encodStream.matcher(url_encoded_fmt_stream_map);
                if ( encodStreamMatch.find() ) {
                    String sline = encodStreamMatch.group(1);

                    String[] urlStrings = sline.split("stream=");

                    for ( String urlString : urlStrings ) {
                        urlString = StringEscapeUtils.unescapeJava(urlString);

                        Pattern link = Pattern.compile("(sparams.*)&itag=(\\d+)&.*&conn=rtmpe(.*),");
                        Matcher linkMatch = link.matcher(urlString);
                        if ( linkMatch.find() ) {

                            String sparams = linkMatch.group(1);
                            String itag = linkMatch.group(2);
                            String url = linkMatch.group(3);

                            url = "https" + url + "?" + sparams;

                            url = URLDecoder.decode(url, WGet.UTF8);

                            sNextVideoURL.add(filter(itag, new URL(url)));
                        }
                    }
                }
            }
        }

        {
            Pattern title = Pattern.compile("<meta name=\"title\" content=(.*)");
            Matcher titleMatch = title.matcher(html);
            if ( titleMatch.find() ) {
                String sline = titleMatch.group(1);
                String name = sline.replaceFirst("<meta name=\"title\" content=", "").trim();
                name = StringUtils.strip(name, "\">");
                name = StringEscapeUtils.unescapeHtml4(name);
                info.setTitle(name);
            }
        }
        if ( info.getTitle() == null ) {
            throw new DownloadEmptyTitle("Empty title"); // some times youtube return strange html, cause this error
        }

        return sNextVideoURL;
    }

    private List<YoutubeVideoDownload> extractUrlEncodedVideos(String sline, YouTubeInfo info,
                                                               AtomicBoolean stop) throws Exception {
        String[] urlStrings = sline.split("url=");

        List<YoutubeVideoDownload> sNextVideoURL = new ArrayList<>();

        for ( String urlString : urlStrings ) {
            urlString = StringEscapeUtils.unescapeJava(urlString);

            String urlFull = URLDecoder.decode(urlString, WGet.UTF8);

            // universal request
            {
                String url = null;
                {
                    Pattern link = Pattern.compile("([^&,]*)[&,]");
                    Matcher linkMatch = link.matcher(urlString);
                    if ( linkMatch.find() ) {
                        url = linkMatch.group(1);
                        url = URLDecoder.decode(url, WGet.UTF8);
                    }
                }

                String itag = null;
                {
                    Pattern link = Pattern.compile("itag=(\\d+)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if ( linkMatch.find() ) {
                        itag = linkMatch.group(1);
                    }
                }

                String sig = null;

                if ( sig == null ) {
                    Pattern link = Pattern.compile("&signature=([^&,]*)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if ( linkMatch.find() ) {
                        sig = linkMatch.group(1);
                    }
                }

                if ( sig == null ) {
                    Pattern link = Pattern.compile("sig=([^&,]*)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if ( linkMatch.find() ) {
                        sig = linkMatch.group(1);
                    }
                }

                if ( sig == null ) {
                    Pattern link = Pattern.compile("[&,]s=([^&,]*)");
                    Matcher linkMatch = link.matcher(urlFull);
                    if ( linkMatch.find() ) {
                        sig = linkMatch.group(1);
                        if ( info.getPlayerURI() == null ) {
                            SignatureDecryptor ss = new SignatureDecryptor(sig);
                            sig = ss.decrypt();
                        } else {
                            Html5SignatureDecryptor ss = new Html5SignatureDecryptor(sig, info.getPlayerURI());
                            sig = ss.decrypt(stop);
                        }
                    }
                }

                if ( url != null && itag != null && sig != null ) {
                    try {
                        url += "&signature=" + sig;
                        sNextVideoURL.add(filter(itag, new URL(url)));
                    } catch ( MalformedURLException e ) {
                        // ignore bad urls
                    }
                }
            }
        }

        return sNextVideoURL;
    }

    @Override
    public List<VideoFileInfo> extract(VideoInfo vinfo, AtomicBoolean stop) {
        List<YoutubeVideoDownload> videos = extractLinks((YouTubeInfo) vinfo, stop);

        if ( videos.size() == 0 ) {
            // rare error:
            //
            // The live recording you're trying to play is still being processed
            // and will be available soon. Sorry, please try again later.
            //
            // retry. since youtube may already rendrered propertly quality.
            throw new DownloadRetry("empty video download list," + " wait until youtube will process the video");
        }

        List<YoutubeVideoDownload> audios = new ArrayList<>();

        for ( int i = videos.size() - 1; i >= 0; i-- ) {
            if ( videos.get(i).getStream() == null ) {
                videos.remove(i);
            } else if ( (videos.get(i).getStream() instanceof AudioStream) ) {
                audios.add(videos.remove(i));
            }
        }

        videos.sort(new YoutubeVideoContentFirstComparator());
        audios.sort(new YoutubeVideoContentFirstComparator());

        for ( int i = 0; i < videos.size(); ) {
            YoutubeVideoDownload v = videos.get(i);

            YouTubeInfo yinfo = (YouTubeInfo) vinfo;
            yinfo.setStreamInfo(v.getStream());

            VideoFileInfo info = new VideoFileInfo(v.getUrl());

            if ( v.getStream() instanceof StreamCombined ) {
                vinfo.setInfo(Collections.singletonList(info));
            }

            if ( v.getStream() instanceof StreamVideo ) {
                if ( audios.size() > 0 ) {
                    VideoFileInfo info2 = new VideoFileInfo(audios.get(0).getUrl()); // take first (highest quality)
                    vinfo.setInfo(Arrays.asList(info, info2));
                } else {
                    // no audio stream?
                    vinfo.setInfo(Collections.singletonList(info));
                }
            }

            vinfo.setSource(v.getUrl());
            return vinfo.getInfo();
        }

        for ( int i = 0; i < audios.size(); ) { // only audio mode?
            VideoFileInfo info = new VideoFileInfo(audios.get(i).getUrl());
            vinfo.setInfo(Collections.singletonList(info));

            vinfo.setSource(info.getSource());
            return vinfo.getInfo();
        }

        // throw download stop if user choice not maximum quality and we have no
        // video rendered by youtube

        throw new DownloadError("no video with required quality found,"
                + " increace VideoInfo.setVq to the maximum and retry download");
    }

}
