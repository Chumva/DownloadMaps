package com.work.golinko.downloadmaps.model.load;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.util.Locale;

public class DownloadService extends IntentService {

    public static final String URL = "http://download.osmand.net/download.php?standard=yes&file=";
    public static final String FILENAME = "name";
    public static final String FILEXTENSION = "_2.obf.zip";
    public static long currentDownloadId;
    public static Thread backgroundThread;
    private static DownloadManager downloadManager;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String name = intent.getStringExtra(FILENAME);
        Log.i("StackSites", name);

        String url = URL + name + FILEXTENSION;
        Log.i("StackSites", url);

        Uri uri = Uri.parse(url);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        request.setTitle("Downloading " + name);
        request.setDescription("Downloading-" + name);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/OsmAndMaps/" + "/" + name + FILEXTENSION);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        currentDownloadId = downloadManager.enqueue(request);
        backgroundThread = Thread.currentThread();

        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


}
