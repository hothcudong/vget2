package com.github.serserser.vget2.info;

import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.serserser.vget2.info.VideoInfo.States;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.ex.DownloadInterruptedError;

public abstract class Parser {

    protected abstract List<VideoFileInfo> extract(final VideoInfo vinfo, final AtomicBoolean stop);

    public void info(VideoInfo info, AtomicBoolean stop) {
        try {
            List<VideoFileInfo> dinfo = extract(info, stop);

            info.setInfo(dinfo);

            for (DownloadInfo i : dinfo) {
                i.setReferer(info.getWeb());
                i.extract(stop, () -> {});
            }
        } catch (DownloadInterruptedError e) {
            info.setState(States.STOP, e);
            throw e;
        } catch (RuntimeException e) {
            info.setState(States.ERROR, e);
            throw e;
        }
    }

}
