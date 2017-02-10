package com.github.serserser.vget2.vhs.youtube;

import com.github.serserser.vget2.vhs.youtubeVideoParams.*;

import java.util.HashMap;
import java.util.Map;

public class YoutubeITags {

    private final Map<Integer, StreamInfo> itagMap = new HashMap<Integer, StreamInfo>();

    private static YoutubeITags instance;

    public static YoutubeITags getInstance() {
        if ( instance == null ) {
            instance = new YoutubeITags();
        }
        return instance;
    }

    public StreamInfo getStream(int tag) {
        return itagMap.get(tag);
    }

    private YoutubeITags() {
        itagMap.put(120, new StreamCombined(Container.FLV, Encoding.H264, YoutubeQuality.p720, Encoding.AAC,
                AudioQuality.k128));
        itagMap.put(102, new StreamCombined(Container.WEBM, Encoding.VP8, YoutubeQuality.p720, Encoding.VORBIS,
                AudioQuality.k192));
        itagMap.put(101, new StreamCombined(Container.WEBM, Encoding.VP8, YoutubeQuality.p360, Encoding.VORBIS,
                AudioQuality.k192)); // webm
        itagMap.put(100, new StreamCombined(Container.WEBM, Encoding.VP8, YoutubeQuality.p360, Encoding.VORBIS,
                AudioQuality.k128)); // webm
        itagMap.put(85, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p1080, Encoding.AAC,
                AudioQuality.k192)); // mp4
        itagMap.put(84, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p720, Encoding.AAC,
                AudioQuality.k192)); // mp4
        itagMap.put(83, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p240, Encoding.AAC,
                AudioQuality.k96)); // mp4
        itagMap.put(82, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p360, Encoding.AAC,
                AudioQuality.k96)); // mp4
        itagMap.put(46, new StreamCombined(Container.WEBM, Encoding.VP8, YoutubeQuality.p1080, Encoding.VORBIS,
                AudioQuality.k192)); // webm
        itagMap.put(45, new StreamCombined(Container.WEBM, Encoding.VP8, YoutubeQuality.p720, Encoding.VORBIS,
                AudioQuality.k192)); // webm
        itagMap.put(44, new StreamCombined(Container.WEBM, Encoding.VP8, YoutubeQuality.p480, Encoding.VORBIS,
                AudioQuality.k128)); // webm
        itagMap.put(43, new StreamCombined(Container.WEBM, Encoding.VP8, YoutubeQuality.p360, Encoding.VORBIS,
                AudioQuality.k128)); // webm
        itagMap.put(38, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p3072, Encoding.AAC,
                AudioQuality.k192)); // mp4
        itagMap.put(37, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p1080, Encoding.AAC,
                AudioQuality.k192)); // mp4
        itagMap.put(36, new StreamCombined(Container.GP3, Encoding.MP4, YoutubeQuality.p240, Encoding.AAC,
                AudioQuality.k36)); // 3gp
        itagMap.put(35, new StreamCombined(Container.FLV, Encoding.H264, YoutubeQuality.p480, Encoding.AAC,
                AudioQuality.k128)); // flv
        itagMap.put(34, new StreamCombined(Container.FLV, Encoding.H264, YoutubeQuality.p360, Encoding.AAC,
                AudioQuality.k128)); // flv
        itagMap.put(22, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p720, Encoding.AAC,
                AudioQuality.k192)); // mp4
        itagMap.put(18, new StreamCombined(Container.MP4, Encoding.H264, YoutubeQuality.p360, Encoding.AAC,
                AudioQuality.k96)); // mp4
        itagMap.put(17, new StreamCombined(Container.GP3, Encoding.MP4, YoutubeQuality.p144, Encoding.AAC,
                AudioQuality.k24)); // 3gp
        itagMap.put(6, new StreamCombined(Container.FLV, Encoding.H263, YoutubeQuality.p270, Encoding.MP3,
                AudioQuality.k64)); // flv
        itagMap.put(5, new StreamCombined(Container.FLV, Encoding.H263, YoutubeQuality.p240, Encoding.MP3,
                AudioQuality.k64)); // flv

        itagMap.put(133, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p240));
        itagMap.put(134, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p360));
        itagMap.put(135, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p480));
        itagMap.put(136, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p720));
        itagMap.put(137, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p1080));
        itagMap.put(138, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p2160));
        itagMap.put(160, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p144));
        itagMap.put(242, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p240));
        itagMap.put(243, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p360));
        itagMap.put(244, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p480));
        itagMap.put(247, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p720));
        itagMap.put(248, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p1080));
        itagMap.put(264, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p1440));
        itagMap.put(271, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p1440));
        itagMap.put(272, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p2160));
        itagMap.put(278, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p144));
        itagMap.put(298, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p720));
        itagMap.put(299, new StreamVideo(Container.MP4, Encoding.H264, YoutubeQuality.p1080));
        itagMap.put(302, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p720));
        itagMap.put(303, new StreamVideo(Container.WEBM, Encoding.VP9, YoutubeQuality.p1080));

        itagMap.put(139, new StreamAudio(Container.MP4, Encoding.AAC, AudioQuality.k48));
        itagMap.put(140, new StreamAudio(Container.MP4, Encoding.AAC, AudioQuality.k128));
        itagMap.put(141, new StreamAudio(Container.MP4, Encoding.AAC, AudioQuality.k256));
        itagMap.put(171, new StreamAudio(Container.WEBM, Encoding.VORBIS, AudioQuality.k128));
        itagMap.put(172, new StreamAudio(Container.WEBM, Encoding.VORBIS, AudioQuality.k192));

        itagMap.put(249, new StreamAudio(Container.WEBM, Encoding.OPUS, AudioQuality.k50));
        itagMap.put(250, new StreamAudio(Container.WEBM, Encoding.OPUS, AudioQuality.k70));
        itagMap.put(251, new StreamAudio(Container.WEBM, Encoding.OPUS, AudioQuality.k160));
    }

}
