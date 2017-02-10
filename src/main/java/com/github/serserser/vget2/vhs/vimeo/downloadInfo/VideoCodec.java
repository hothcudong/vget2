package com.github.serserser.vget2.vhs.vimeo.downloadInfo;

public class VideoCodec {
    private VideoDownloadLink hd;
    private VideoDownloadLink sd;
    private VideoDownloadLink mobile;

    public VideoDownloadLink getHd() {
        return hd;
    }

    public void setHd(VideoDownloadLink hd) {
        this.hd = hd;
    }

    public VideoDownloadLink getSd() {
        return sd;
    }

    public void setSd(VideoDownloadLink sd) {
        this.sd = sd;
    }

    public VideoDownloadLink getMobile() {
        return mobile;
    }

    public void setMobile(VideoDownloadLink mobile) {
        this.mobile = mobile;
    }
}
