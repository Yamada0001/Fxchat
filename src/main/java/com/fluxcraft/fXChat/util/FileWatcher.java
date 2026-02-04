package com.fluxcraft.fXChat.util;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileWatcher {
    private final ScheduledExecutorService scheduler;
    private final Map<File, Long> watchedFiles = new ConcurrentHashMap<>();
    private final Runnable reloadCallback;

    public FileWatcher(Runnable onReloadCallback) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FXChat-FileWatcher");
            t.setDaemon(true);
            return t;
        });
        this.reloadCallback = onReloadCallback;
    }

    public void watchFile(File file) {
        if (file != null && file.exists()) {
            watchedFiles.put(file, file.lastModified());
        }
    }

    public void start(int intervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            boolean needsReload = false;
            for (Map.Entry<File, Long> entry : watchedFiles.entrySet()) {
                File file = entry.getKey();
                long current = file.lastModified();
                if (current > entry.getValue()) {
                    watchedFiles.put(file, current);
                    needsReload = true;
                }
            }
            if (needsReload) reloadCallback.run();
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
