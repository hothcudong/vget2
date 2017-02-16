package com.github.serserser.vget2;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.serserser.vget2.exceptions.DownloadEmptyTitle;
import com.github.serserser.vget2.exceptions.DownloadFatal;
import com.github.serserser.vget2.info.Parser;
import com.github.serserser.vget2.info.ServiceProvider;
import com.github.serserser.vget2.info.VideoFileInfo;
import com.github.serserser.vget2.info.VideoInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.axet.threads.LimitThreadPool;
import com.github.axet.wget.Direct;
import com.github.axet.wget.DirectMultipart;
import com.github.axet.wget.DirectRange;
import com.github.axet.wget.DirectSingle;
import com.github.axet.wget.RetryWrap;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.DownloadInfo.Part;
import com.github.axet.wget.info.ex.DownloadError;
import com.github.axet.wget.info.ex.DownloadIOCodeError;
import com.github.axet.wget.info.ex.DownloadIOError;
import com.github.axet.wget.info.ex.DownloadInterruptedError;
import com.github.axet.wget.info.ex.DownloadMultipartError;
import com.github.axet.wget.info.ex.DownloadRetry;

public class VGet {

    ServiceProvider parserProvider;

    /*---------------------------------------------------------------------------------------------*/

    private static final int THREAD_COUNT = 4;

    protected VideoInfo info;
    // target directory, where we have to download. automatically name files
    // based on video title and conflict files.
    private File targetDir;

    // instead adding (1), (2) ... to filename suffix for conflict files
    // if target file exists, override it. ignores video titles and ignores
    // (exists files)
    private File targetForce = null;

    /**
     * extract video information constructor
     *
     * @param source url source to get video from
     */
    public VGet(URL source) {
        this(source, null);
    }

    public VGet(URL source, File targetDir) {
        parserProvider = new ServiceProvider();
        this.info = parserProvider.getInfoForUrl(source);
        this.targetDir = targetDir;
    }

    /**
     * Set targetFile manually after you call .extract()
     *
     * @param info download info.
     */
    public VGet(VideoInfo info) {
        this.info = info;
    }

    /**
     * set target file for single download source. file willbe overdien if exists.
     * <p>
     * will fail if vget extract several sources (for video/audio urls). use VideoFileInfo.targetFile for multiply
     * sources.
     *
     * @param file target file
     */
    public void setTarget(File file) {
        this.targetForce = file;
    }

    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }

    public VideoInfo getVideo() {
        return info;
    }

    public void download() {
        download(null, new AtomicBoolean(false));
    }

    public void download(Parser user) {
        download(user, new AtomicBoolean(false));
    }

    /**
     * Drop all forbidden characters from filename
     *
     * @param f input file name
     * @return normalized file name
     */
    private static String replaceBadChars(String f) {
        String replace = " ";
        f = f.replaceAll("/", replace);
        f = f.replaceAll("\\\\", replace);
        f = f.replaceAll(":", replace);
        f = f.replaceAll("\\?", replace);
        f = f.replaceAll("\\\"", replace);
        f = f.replaceAll("\\*", replace);
        f = f.replaceAll("<", replace);
        f = f.replaceAll(">", replace);
        f = f.replaceAll("\\|", replace);
        f = f.trim();
        f = StringUtils.removeEnd(f, ".");
        f = f.trim();

        String ff;
        while ( !(ff = f.replaceAll("  ", " ")).equals(f) ) {
            f = ff;
        }

        return f;
    }

    private static String maxFileNameLength(String str) {
        int max = 255;
        if ( str.length() > max )
            str = str.substring(0, max);
        return str;
    }

    private boolean done(AtomicBoolean stop) {
        if ( stop.get() )
            throw new DownloadInterruptedError("stop");
        if ( Thread.currentThread().isInterrupted() )
            throw new DownloadInterruptedError("interrupted");

        return false;
    }

    private VideoFileInfo getNewInfo(List<VideoFileInfo> infoList, VideoFileInfo infoOld) {
        if ( infoOld == null )
            return null;

        for ( VideoFileInfo d : infoList ) {
            if ( infoOld.resume(d) )
                return d;
        }

        return null;
    }

    private void retry(Parser user, AtomicBoolean stop, Throwable e) {
        boolean retracted = false;
        int retry = 0;

        while ( !retracted ) {
            retry++; // first retry
            if ( !RetryWrap.retry(retry) ) {
                throw new DownloadError(e);
            }
            for ( int i = RetryWrap.RETRY_DELAY; i >= 0; i-- ) {
                if ( stop.get() )
                    throw new DownloadInterruptedError("stop");
                if ( Thread.currentThread().isInterrupted() )
                    throw new DownloadInterruptedError("interrupted");

                info.setRetrying(retry, i, e);

                try {
                    Thread.sleep(1000);
                } catch ( InterruptedException ee ) {
                    throw new DownloadInterruptedError(ee);
                }
            }

            try {
                // if we continue to download from old source, and this
                // proxy server is down we have to try to extract new info
                // and try to resume download

                List<VideoFileInfo> infoOldList = info.getInfo();

                Parser parser;
                if ( user == null ) {
                    parser = parserProvider.getParserForUrl(info.getWeb());
                } else {
                    parser = user;
                }
                parser.info(info, stop);

                if ( infoOldList != null ) {
                    List<VideoFileInfo> infoNewList = info.getInfo();

                    for ( VideoFileInfo infoOld : infoOldList ) {
                        DownloadInfo infoNew = getNewInfo(infoNewList, infoOld);

                        if ( infoOld != null && infoNew != null && infoOld.resume(infoNew) ) {
                            infoNew.copy(infoOld);
                        } else {
                            if ( infoOld.targetFile != null ) {
                                FileUtils.deleteQuietly(infoOld.targetFile);
                                infoOld.targetFile = null;
                            }
                        }
                    }
                }
                retracted = true;
            } catch ( DownloadIOCodeError ee ) {
                if ( retry(ee) ) {
                    info.setState(VideoInfo.States.RETRYING, ee);
                } else {
                    throw ee;
                }
            } catch ( DownloadRetry ee ) {
                info.setState(VideoInfo.States.RETRYING, ee);
            }
        }
    }

    // return ".ext" ex: ".mp3" ".webm"
    private String getExt(DownloadInfo dinfo) {
        String ct = dinfo.getContentType();
        if ( ct == null )
            throw new DownloadRetry("null content type");

        // for single file download keep only extension
        ct = ct.replaceFirst("video/", "");
        ct = ct.replaceFirst("audio/", "");

        return "." + ct.replaceAll("x-", "").toLowerCase();
    }

    // return ".content.ext" ex: ".audio.mp3"
    private String getContentExt(DownloadInfo dinfo) {
        String ct = dinfo.getContentType();
        if ( ct == null )
            throw new DownloadRetry("null content type");

        // for multi file download keep content type and extension. some video can have same extensions for booth
        // audio/video streams
        ct = ct.replaceFirst("/", ".");

        return "." + ct.replaceAll("x-", "").toLowerCase();
    }

    private boolean exists(File f, AtomicBoolean conflict) {
        if ( f.exists() )
            return true;
        for ( VideoFileInfo dinfo : info.getInfo() ) {
            if ( dinfo.targetFile != null && dinfo.targetFile.equals(f) ) {
                if ( conflict != null )
                    conflict.set(true);
                return true;
            }
        }
        return false;
    }

    private void targetFileForce(VideoFileInfo dinfo) {
        if ( targetForce != null ) {
            if ( exists(targetForce, null) ) {
                // VGet v = new VGet(videoinfo, path);
                // v.extract(user, stop, notify);
                // List<VideoFileInfo> list = videoinfo.getInfo();
                // list.get(0).targetFile = new File("111.mp3");
                // list.get(1).targetFile = new File("222.mp3");
                // // or
                // v.targetFile(list.get(0), v.getExt(list.get(0)), new AtomicBoolean());
                // v.targetFile(list.get(1), v.getExt(list.get(1)), new AtomicBoolean());
                throw new DownloadError("Do not use setTarget file on multiply source download");
            }

            dinfo.targetFile = targetForce;

            if ( dinfo.multipart() ) {
                if ( !DirectMultipart.canResume(dinfo, dinfo.targetFile) ) {
                    FileUtils.deleteQuietly(dinfo.targetFile);
                    dinfo.reset();
                }
            } else if ( dinfo.getRange() ) {
                if ( !DirectRange.canResume(dinfo, dinfo.targetFile) ) {
                    FileUtils.deleteQuietly(dinfo.targetFile);
                    dinfo.reset();
                }
            } else {
                if ( !DirectSingle.canResume(dinfo, dinfo.targetFile) ) {
                    FileUtils.deleteQuietly(dinfo.targetFile);
                    dinfo.reset();
                }
            }
        }
    }

    // return true, video download have the same ".ext" for multiple videos
    private void targetFileExt(VideoFileInfo dinfo, String ext, AtomicBoolean conflict) {
        if ( dinfo.targetFile == null ) {
            if ( targetDir == null ) {
                throw new RuntimeException("Set download file or directory first");
            }

            File f;

            Integer idupcount = 0;

            String sfilename = replaceBadChars(info.getTitle());

            sfilename = maxFileNameLength(sfilename);

            do {
                // add = " (1)"
                String add = idupcount > 0 ? " (".concat(idupcount.toString()).concat(")") : "";
                f = new File(targetDir, sfilename + add + ext);
                idupcount += 1;
            } while ( exists(f, conflict) );

            dinfo.targetFile = f;

            // if we don't have resume file (targetForce==null) then we shall
            // start over.
            dinfo.reset();
        }
    }

    /**
     * set targetFile for specified VideoFileInfo.
     *
     * @param dinfo    VideoFileInfo to set targetFile for
     * @param ext      File extension. call getExt() or getContentExt()
     * @param conflict True if we have same file extension for multiply files. File name will be renamed to " (1).ext"
     */
    private void targetFile(VideoFileInfo dinfo, String ext, AtomicBoolean conflict) {
        targetFileForce(dinfo);
        targetFileExt(dinfo, ext, conflict);
    }

    private boolean retry(Throwable e) {
        if ( e == null )
            return true;

        if ( e instanceof DownloadIOCodeError ) {
            DownloadIOCodeError c = (DownloadIOCodeError) e;
            switch ( c.getCode() ) {
                case HttpURLConnection.HTTP_FORBIDDEN:
                case 416: // 416 Requested Range Not Satisfiable
                    return true;
                default:
                    return false;
            }
        }

        return false;
    }

    /**
     * @return return status of download information. subclassing for VideoInfo.empty();
     */
    private boolean empty() {
        return getVideo().empty();
    }

    public void extract() {
        extract(new AtomicBoolean(false));
    }

    private void extract(AtomicBoolean stop) {
        extract(null, stop);
    }

    /**
     * extract video information, retry until success
     *
     * @param inputParser user info object
     * @param stop        stop signal boolean
     */
    public void extract(Parser inputParser, AtomicBoolean stop) {
        Parser parser;

        try {
            while ( !done(stop) ) {
                try {
                    if ( info.empty() ) {
                        info.setState(VideoInfo.States.EXTRACTING);
                        if ( inputParser == null ) {
                            parser = parserProvider.getParserForUrl(info.getWeb());
                        } else {
                            parser = inputParser;
                        }
                        parser.info(info, stop);
                        info.setState(VideoInfo.States.EXTRACTING_DONE);
                    }
                    return;
                } catch ( DownloadRetry | DownloadEmptyTitle | DownloadIOError e ) {
                    retry(inputParser, stop, e);
                } catch ( DownloadMultipartError e ) {
                    checkFileNotFound(e);
                    checkRetry(e);
                    retry(inputParser, stop, e);
                } catch ( DownloadIOCodeError e ) {
                    if ( retry(e) )
                        retry(inputParser, stop, e);
                    else
                        throw e;
                }
            }
        } catch ( DownloadInterruptedError e ) {
            info.setState(VideoInfo.States.STOP);
            throw e;
        }
    }

    private void checkRetry(DownloadMultipartError e) {
        for ( Part ee : e.getInfo().getParts() ) {
            if ( !retry(ee.getException()) ) {
                throw e;
            }
        }
    }

    /**
     * check if all parts has the same filenotfound exception. if so throw DownloadError.FilenotFoundexcepiton
     *
     * @param e error occured
     */
    private void checkFileNotFound(DownloadMultipartError e) {
        FileNotFoundException f = null;
        for ( Part ee : e.getInfo().getParts() ) {
            // no error for this part? skip it
            if ( ee.getException() == null )
                continue;
            // this exception has no cause? then it is not FileNotFound
            // excpetion. then do noting. this is checking function. do not
            // rethrow
            if ( ee.getException().getCause() == null )
                return;
            if ( ee.getException().getCause() instanceof FileNotFoundException ) {
                // our first filenotfoundexception?
                if ( f == null ) {
                    // save it for later checks
                    f = (FileNotFoundException) ee.getException().getCause();
                } else {
                    // check filenotfound error message is it the same?
                    FileNotFoundException ff = (FileNotFoundException) ee.getException().getCause();
                    if ( !ff.getMessage().equals(f.getMessage()) ) {
                        // if the filenotfound exception message is not the
                        // same. then we cannot retrhow filenotfound exception.
                        // return and continue checks
                        return;
                    }
                }
            } else {
                break;
            }
        }
        if ( f != null )
            throw new DownloadError(f);
    }

    public void download(final AtomicBoolean stop) {
        download(null, stop);
    }

    public void download(Parser user, final AtomicBoolean stop) {
        try {
            if ( empty() ) {
                extract(user, stop);
            }

            // retry exception loop
            while ( !done(stop) ) {
                try {
                    final List<VideoFileInfo> dinfoList = info.getInfo();

                    final LimitThreadPool l = new LimitThreadPool(THREAD_COUNT);

                    final Thread main = Thread.currentThread();

                    // new targetFile() call
                    {
                        // update targetFile only if not been set on previous while(!done()) loops.
                        List<VideoFileInfo> targetNull = new ArrayList<>();

                        // safety checks. should it be 'vhs' dependent? does other services return other than
                        // "video/audio"?
                        for ( final VideoFileInfo dinfo : dinfoList ) {
                            if ( dinfo.targetFile == null )
                                targetNull.add(dinfo);
                            {
                                String c = dinfo.getContentType();
                                if ( c == null )
                                    c = "";
                                boolean v = c.contains("video/");
                                boolean a = c.contains("audio/");
                                if ( !v && !a ) {
                                    throw new DownloadRetry(
                                            "unable to download video, bad content " + dinfo.getContentType());
                                }
                            }
                        }

                        // did we meet two similar extensions for audio/video content? conflict == true if so.
                        // we can continue but one result file name would be like "SomeTitle (1).mp3"
                        AtomicBoolean conflict = new AtomicBoolean(false);
                        // 1) ".ext"
                        for ( final VideoFileInfo dinfo : targetNull ) {
                            dinfo.targetFile = null;
                            targetFile(dinfo, getExt(dinfo), conflict);
                        }
                        // conflict means we have " (1).ext" download. try add ".content.ext" as extension
                        // to make file names looks more pretty.
                        if ( conflict.get() ) {
                            conflict = new AtomicBoolean(false);
                            // 2) ".content.ext"
                            for ( final VideoFileInfo dinfo : targetNull ) {
                                dinfo.targetFile = null;
                                targetFile(dinfo, getContentExt(dinfo), conflict);
                            }
                        }
                    }

                    for ( final VideoFileInfo dinfo : dinfoList ) {
                        if ( dinfo.targetFile == null ) {
                            throw new RuntimeException("bad target");
                        }

                        Direct directV;

                        if ( dinfo.multipart() ) {
                            // multi part? overwrite.
                            directV = new DirectMultipart(dinfo, dinfo.targetFile);
                        } else if ( dinfo.getRange() ) {
                            // range download? try to resume download from last position
                            if ( dinfo.targetFile.exists() && dinfo.targetFile.length() != dinfo.getCount() ) {
                                // all files have set targetFile, so targetNull == empty
                                if ( targetDir == null )
                                    targetDir = dinfo.targetFile.getParentFile();
                                dinfo.targetFile = null;
                                AtomicBoolean conflict = new AtomicBoolean(false);
                                targetFile(dinfo, getExt(dinfo), conflict);
                                if ( conflict.get() ) {
                                    dinfo.targetFile = null;
                                    targetFile(dinfo, getContentExt(dinfo), conflict);
                                }
                            }
                            directV = new DirectRange(dinfo, dinfo.targetFile);
                        } else {
                            // single download? overwrite file
                            directV = new DirectSingle(dinfo, dinfo.targetFile);
                        }
                        final Direct direct = directV;

                        final Runnable r = () -> {
                            switch ( dinfo.getState() ) {
                                case DOWNLOADING:
                                    info.setState(VideoInfo.States.DOWNLOADING);
                                    break;
                                case RETRYING:
                                    info.setRetrying(dinfo.getRetry(), dinfo.getDelay(), dinfo.getException());
                                    break;
                                default:
                                    // we can safely skip all statues.
                                    // (extracting - already passed, STOP /
                                    // ERROR / DONE i will catch up here
                            }
                        };

                        try {
                            l.blockExecute(() -> {
                                try {
                                    direct.download(stop, r);
                                } catch ( DownloadInterruptedError e ) {
                                    // we need to handle this task error to l.waitUntilTermination()
                                    main.interrupt();
                                } catch ( RuntimeException e ) {
                                    dinfo.setState(com.github.axet.wget.info.URLInfo.States.ERROR, e);
                                    main.interrupt();
                                }
                            });
                        } catch ( InterruptedException e ) {
                            l.interrupt();
                            // wait for childs to exit
                            boolean clear = true;
                            while ( clear ) {
                                try {
                                    l.join();
                                    clear = false;
                                } catch ( InterruptedException ee ) {
                                    // we got interrupted twice from main.interrupt()
                                }
                            }
                            throw new DownloadInterruptedError(e);
                        }
                    }

                    try {
                        l.waitUntilTermination();
                    } catch ( InterruptedException e ) {
                        l.interrupt();
                        // wait for childs to exit
                        boolean clear = true;
                        while ( clear ) {
                            try {
                                l.join();
                                clear = false;
                            } catch ( InterruptedException ee ) {
                                // we got interrupted twice from main.interrupt()
                            }
                        }
                        // do we have any error?
                        for ( final VideoFileInfo dinfo : dinfoList ) {
                            if ( dinfo.getException() != null ) {
                                // yes some kind of fatal error on one or more files
                                throw new DownloadFatal(e, dinfoList);
                            }
                        }
                        // nope, download was interrupted manually
                        throw new DownloadInterruptedError(e);
                    }

                    info.setState(VideoInfo.States.DONE);
                    // break while()
                    return;
                } catch ( DownloadRetry | DownloadIOError e ) {
                    retry(user, stop, e);
                } catch ( DownloadMultipartError e ) {
                    checkFileNotFound(e);
                    checkRetry(e);
                    retry(user, stop, e);
                } catch ( DownloadIOCodeError e ) {
                    if ( retry(e) )
                        retry(user, stop, e);
                    else
                        throw e;
                }
            }
        } catch ( DownloadInterruptedError e ) {
            info.setState(VideoInfo.States.STOP, e);
            throw e;
        } catch ( RuntimeException e ) {
            info.setState(VideoInfo.States.ERROR, e);
            throw e;
        }
    }
}
