/*
 * This is the source code of VortexGram for Android.
 *
 * Writes a crash to a file the user can find and send, because a fork cannot
 * read logcat off someone's phone and guessing at a crash wastes builds.
 */

package com.radolyn.ayugram.utils;

import android.os.Build;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AyuCrashLogger {

    private static final String DIR_NAME = "VortexGram crash logs";
    private static volatile boolean installed;

    /**
     * Records uncaught exceptions to a file, then hands the throwable to
     * whatever handler was already in place, so the app still dies the way
     * Android expects and nothing about its behaviour changes.
     */
    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        try {
            final Thread.UncaughtExceptionHandler previous =
                    Thread.getDefaultUncaughtExceptionHandler();

            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                try {
                    write(thread, throwable);
                } catch (Throwable ignored) {
                    // Never let the logger interfere with the crash itself.
                }
                if (previous != null) {
                    previous.uncaughtException(thread, throwable);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    /** @return the folder the logs are written to, for showing in settings */
    public static File getLogDir() {
        File base = ApplicationLoader.applicationContext.getExternalFilesDir(null);
        if (base == null) {
            base = ApplicationLoader.applicationContext.getFilesDir();
        }
        return new File(base, DIR_NAME);
    }

    private static void write(Thread thread, Throwable throwable) throws Exception {
        File dir = getLogDir();
        if (!dir.exists() && !dir.mkdirs()) {
            return;
        }

        String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
        File out = new File(dir, "crash-" + stamp + ".txt");

        StringWriter trace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(trace));

        StringBuilder sb = new StringBuilder();
        sb.append("VortexGram crash\n");
        sb.append("when:    ").append(new Date()).append('\n');
        sb.append("version: ").append(BuildVars.BUILD_VERSION_STRING).append('\n');
        sb.append("android: ").append(Build.VERSION.RELEASE)
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("device:  ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n');
        sb.append("thread:  ").append(thread == null ? "?" : thread.getName()).append("\n\n");
        sb.append(trace);

        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(sb.toString().getBytes("UTF-8"));
        }
    }
}
