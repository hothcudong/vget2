package com.github.serserser.vget2.vhs.vimeo.downloadInfo;

public class VimeoRequest {
    private String signature;
    private String session;
    private long timestamp;
    private long expires;
    private VimeoFiles files;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public VimeoFiles getFiles() {
        return files;
    }

    public void setFiles(VimeoFiles files) {
        this.files = files;
    }
}
