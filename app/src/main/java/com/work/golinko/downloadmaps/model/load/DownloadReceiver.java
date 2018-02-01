package com.work.golinko.downloadmaps.model.load;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import static android.content.Context.DOWNLOAD_SERVICE;


public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long broadcasterDownloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        if (broadcasterDownloadID == DownloadService.currentDownloadId) {
            if (getDownloadStatus(context) == DownloadManager.STATUS_SUCCESSFUL) {
                Toast.makeText(context, "Download complete.", Toast.LENGTH_LONG).show();
                synchronized (DownloadService.backgroundThread) {
                    DownloadService.backgroundThread.notify();
                }
            } else {
                Toast.makeText(context, "Download not complete.", Toast.LENGTH_LONG).show();
                synchronized (DownloadService.backgroundThread) {
                    DownloadService.backgroundThread.notify();
                }
            }
        }
    }

    private int getDownloadStatus(Context context) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(DownloadService.currentDownloadId);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            return status;
        }
        return DownloadManager.ERROR_UNKNOWN;
    }

}


