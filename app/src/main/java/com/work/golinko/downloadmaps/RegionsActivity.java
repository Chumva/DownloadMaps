package com.work.golinko.downloadmaps;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.work.golinko.downloadmaps.model.entity.RegionItem;
import com.work.golinko.downloadmaps.model.load.DownloadService;

import java.util.ArrayList;

import static com.work.golinko.downloadmaps.model.load.DownloadService.backgroundThread;
import static com.work.golinko.downloadmaps.model.load.DownloadService.currentDownloadId;


public class RegionsActivity extends AppCompatActivity {
    private ListView sitesList;
    private RegionsAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subregions);

        sitesList = findViewById(R.id.sitesList);
        RegionItem regionItem = (RegionItem) getIntent().getSerializableExtra("REGION");
        final ArrayList<RegionItem> regionItems = regionItem.getSubRegions();

        //Set the click listener to launch the browser when a row is clicked.
        mAdapter = new RegionsAdapter(RegionsActivity.this, -1, regionItems);
        sitesList.setAdapter(mAdapter);
        sitesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                RegionItem curRegion = regionItems.get(pos);
                if (curRegion.hasSubRegions()) {
//                    downloadMap(name, url);

                    Intent intent = new Intent(getApplicationContext(), RegionsActivity.class);
                    intent.putExtra("REGION", curRegion);
                    startActivityForResult(intent, 0);

                } else if (!curRegion.isDownloadable()) {
                    Toast.makeText(RegionsActivity.this, " " + id + " - Sorry, no map for this region", Toast.LENGTH_SHORT).show();
                } else {

                    curRegion.setDeepestRegion(true);

                    Toast.makeText(RegionsActivity.this, "Start downloading "+curRegion.getName(), Toast.LENGTH_SHORT).show();
                    String name = curRegion.getFullName();
                    Log.i("OsmAnd", name);

                    downloadMap(name);
                    curRegion.setDeepestRegion(false);
                }
                curRegion.setDeepestRegion(false);
            }

        });
    }

    public void cancelDownload(View viev) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.remove(currentDownloadId);
        synchronized (backgroundThread) {
            backgroundThread.notify();
        }
    }

    public boolean hasWriteAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission");
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }
        return false;
    }

    private void downloadMap(String name) {
        if (hasWriteAccess()) {
            Intent intent = new Intent(getApplicationContext(), DownloadService.class);
            intent.putExtra(DownloadService.FILENAME, name);
            startService(intent);
        }
    }


}