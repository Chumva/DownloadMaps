package com.work.golinko.downloadmaps;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.work.golinko.downloadmaps.model.container.RegionsItemContainer;
import com.work.golinko.downloadmaps.model.entity.RegionItem;
import com.work.golinko.downloadmaps.model.load.DownloadService;
import com.work.golinko.downloadmaps.model.load.Downloader;
import com.work.golinko.downloadmaps.parser.SitesXmlPullParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.work.golinko.downloadmaps.model.load.DownloadService.backgroundThread;
import static com.work.golinko.downloadmaps.model.load.DownloadService.currentDownloadId;

public class MainActivity extends AppCompatActivity {

    ArrayList<RegionItem> regionItems;
    private RegionsAdapter mAdapter;
    private ListView sitesList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final View downloadProgressLayout = findViewById(R.id.downloadProgressLayout);
        
        updateDescriptionTextWithSize();

        downloadProgressLayout.setVisibility(View.VISIBLE);
        sitesList = findViewById(R.id.sitesList);

        //Set the click listener to launch the new Activity or download request when a row is clicked.
        sitesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                RegionItem curRegion = regionItems.get(pos);
                if (curRegion.hasSubRegions()) {
                    Intent intent = new Intent(getApplicationContext(), RegionsActivity.class);
                    intent.putExtra("REGION", curRegion);
                    startActivityForResult(intent, 0);

                } else if (!curRegion.isDownloadable()) {
                    Toast.makeText(MainActivity.this, " " + id + " - Sorry, no map for this region", Toast.LENGTH_SHORT).show();
                } else {
                    curRegion.setDeepestRegion(true);

                    Toast.makeText(MainActivity.this, " " + id + " - ", Toast.LENGTH_SHORT).show();
                    String name = curRegion.getFullName();
                    Log.i("StackSites", name);

                    downloadMap(name);
                    curRegion.setDeepestRegion(false);
                }

            }

        });

        /*
         * Download regions.xml
         * If network is available download the xml from the Internet.
		 * If not then try to use the local file from last time.
		 */
        downloadXML();
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

    private void downloadXML() {
        if (isNetworkAvailable()) {
            Log.i("OsmAnd", "starting download Task");
            SitesDownloadTask download = new SitesDownloadTask();
            download.execute();
        } else {
            try {
                mAdapter = new RegionsAdapter(getApplicationContext(), -1, SitesXmlPullParser.getStackSitesFromFile(MainActivity.this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            sitesList.setAdapter(mAdapter);
            Log.i("OsmAnd", "else mAdapter");
        }
    }

    public void updateDescriptionTextWithSize() {
        TextView descriptionText = findViewById(R.id.rightTextView);
//        TextView messageTextView = (TextView) findViewById(R.id.leftTextView);
        ProgressBar sizeProgress = findViewById(R.id.progressBar);

        long freeBytesInternal = new File(this.getFilesDir().getAbsoluteFile().toString()).getUsableSpace();
        long totalBytesInternal = new File(this.getFilesDir().getAbsoluteFile().toString()).getTotalSpace();

        double gb = (float) freeBytesInternal / 1024f / 1024f / 1024f;
        double gb2 = (float) totalBytesInternal / 1024f / 1024f / 1024f;

        if (null != descriptionText) {
            descriptionText.setText(String.valueOf(new DecimalFormat("0.00").format(gb)).concat(" GB"));
        }

//        if (null != messageTextView) {
//            messageTextView.setText(String.valueOf(new DecimalFormat("0.00").format(gb2)).concat(" GB"));
//        }

        if (null != sizeProgress) {
            sizeProgress.setMax((int) gb2);
            sizeProgress.setProgress((int) (gb2 - gb));
        }


    }

    //Helper method to determine if Internet connection is available.
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.i("OsmAnd", "isNetworkAvailable");

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    private void downloadMap(String name) {
        if (hasWriteAccess()) {
            Intent intent = new Intent(getApplicationContext(), DownloadService.class);
            intent.putExtra(DownloadService.FILENAME, name);
            startService(intent);
        }
    }

    /*
     * AsyncTask that will download the xml file for us and store it locally.
     * After the download is done we'll parse the local file.
     */
    public class SitesDownloadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.i("OsmAnd", "doInBackground");

            //Download the file
            try {
                Downloader.DownloadFromUrl("https://raw.githubusercontent.com/osmandapp/OsmAnd-resources/master/countries-info/regions.xml", openFileOutput("regions.xml", Context.MODE_PRIVATE));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //setup our Adapter and set it to the ListView.
            try {
                regionItems = SitesXmlPullParser.getStackSitesFromFile(MainActivity.this);
                mAdapter = new RegionsAdapter(MainActivity.this, -1, regionItems);
                sitesList.setAdapter(mAdapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}