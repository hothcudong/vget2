package com.github.serserser.vget2.vhs.decryption;

import com.github.axet.wget.WGet;
import com.github.axet.wget.info.ex.DownloadError;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Html5SignatureDecryptor {
    public String sig;
    public URI playerURI;
    public static ConcurrentMap<String, String> playerCache = new ConcurrentHashMap<String, String>();

    public Html5SignatureDecryptor(String signatur, URI playerURI) {
        this.sig = signatur;
        this.playerURI = playerURI;
    }

    /**
     * Gets the corresponding html5player.js in order to decode youtube video signature
     *
     * @return player.js file
     */
    public String getHtml5PlayerScript(final AtomicBoolean stop, final Runnable notify) {
        String url = playerCache.get(playerURI.toString());

        if (url == null) {
            try {
                String result = WGet.getHtml(playerURI.toURL(), new WGet.HtmlLoader() {
                    @Override
                    public void notifyRetry(int retry, int delay, Throwable e) {
                        notify.run();
                    }

                    @Override
                    public void notifyMoved() {
                        notify.run();
                    }

                    @Override
                    public void notifyDownloading() {
                        notify.run();
                    }
                }, stop);
                playerCache.put(playerURI.toString(), result);
                return result;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return url;
    }

    /**
     * Determines the main decode function name. Unfortunately the name of the decode-funtion might change from
     * version to version, but the part of the code that makes use of this function usually doesn't change. So let's
     * give it a try.
     *
     * @param playerJS
     *            corresponding javascript html5 player file
     * @return name of decode-function or null
     */
    public String getMainDecodeFunctionName(String playerJS) {
        Pattern decodeFunctionName = Pattern.compile("\\.sig\\|\\|([a-zA-Z0-9$]+)\\(");
        Matcher decodeFunctionNameMatch = decodeFunctionName.matcher(playerJS);
        if (decodeFunctionNameMatch.find()) {
            return decodeFunctionNameMatch.group(1);
        }
        return null;
    }

    /**
     * Extracts the relevant decode functions of the html5player script. Besides the main decode function we need to
     * extract some utility functions the decode-function is using.
     *
     * @param playerJS
     *            the html5player script
     * @param functionName
     *            the main decode function name
     * @return returns only those functions which are relevant for the signature decoding
     */
    public String extractDecodeFunctions(String playerJS, String functionName) {
        StringBuilder decodeScript = new StringBuilder();
        Pattern decodeFunction = Pattern
                // this will probably change from version to version so
                // changes have to be done here
                .compile(String.format("(%s=function\\([a-zA-Z0-9$]+\\)\\{.*?\\})[,;]", functionName),
                        Pattern.DOTALL);
        Matcher decodeFunctionMatch = decodeFunction.matcher(playerJS);
        if (decodeFunctionMatch.find()) {
            decodeScript.append(decodeFunctionMatch.group(1)).append(';');
        } else {
            throw new DownloadError("Unable to extract the main decode function!");
        }

        // determine the name of the helper function which is used by the
        // main decode function
        Pattern decodeFunctionHelperName = Pattern.compile("\\);([a-zA-Z0-9]+)\\.");
        Matcher decodeFunctionHelperNameMatch = decodeFunctionHelperName.matcher(decodeScript.toString());
        if (decodeFunctionHelperNameMatch.find()) {
            final String decodeFuncHelperName = decodeFunctionHelperNameMatch.group(1);

            Pattern decodeFunctionHelper = Pattern.compile(
                    String.format("(var %s=\\{[a-zA-Z0-9]*:function\\(.*?\\};)", decodeFuncHelperName),
                    Pattern.DOTALL);
            Matcher decodeFunctionHelperMatch = decodeFunctionHelper.matcher(playerJS);
            if (decodeFunctionHelperMatch.find()) {
                decodeScript.append(decodeFunctionHelperMatch.group(1));
            } else {
                throw new DownloadError("Unable to extract the helper decode functions!");
            }

        } else {
            throw new DownloadError("Unable to determine the name of the helper decode function!");
        }
        return decodeScript.toString();
    }

    /**
     * Decodes the youtube video signature using the decode functions provided in the html5player script.
     */
    public String decrypt(AtomicBoolean stop, Runnable notify) {
        ScriptEngineManager manager = new ScriptEngineManager();
        // use a js script engine
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        final String playerScript = getHtml5PlayerScript(stop, notify);
        final String decodeFuncName = getMainDecodeFunctionName(playerScript);
        final String decodeScript = extractDecodeFunctions(playerScript, decodeFuncName);

        String decodedSignature = null;
        try {
            // evaluate script
            engine.eval(decodeScript);
            Invocable inv = (Invocable) engine;
            // execute the javascript code directly
            decodedSignature = (String) inv.invokeFunction(decodeFuncName, sig);
        } catch (Exception e) {
            throw new DownloadError("Unable to decrypt signature!");
        }

        return decodedSignature;
    }
}