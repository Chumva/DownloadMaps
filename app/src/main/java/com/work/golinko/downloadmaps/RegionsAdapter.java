package com.work.golinko.downloadmaps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.work.golinko.downloadmaps.model.entity.RegionItem;

import java.util.List;


/*
 * Custom Adapter class that is responsible for holding the list of sites after they
 * get parsed out of XML and building row views to display them on the screen.
 */
public class RegionsAdapter extends ArrayAdapter<RegionItem> {

    RegionsAdapter(Context ctx, int textViewResourceId, List<RegionItem> sites) {
        super(ctx, textViewResourceId, sites);

    }

    /*
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     *
     * This method is responsible for creating row views out of a StackSite object that can be put
     * into our ListView
     */
    @NonNull
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        RelativeLayout row = (RelativeLayout) convertView;
        Log.i("StackSites", "getView pos = " + pos);
        if (null == row) {
            //No recycled View, we have to inflate one.
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = (RelativeLayout) inflater.inflate(R.layout.row_site, null);
        }

        TextView nameTxt = row.findViewById(R.id.nameTxt);
        nameTxt.setText(getItem(pos).getName());
        ImageView icomMapImage = row.findViewById(R.id.map_icon);
        ImageView icomImportImage = row.findViewById(R.id.import_icon);
        if (!getItem(pos).isDownloadable()) {
            icomImportImage.setVisibility(View.INVISIBLE);
        } else {
            icomImportImage.setVisibility(View.VISIBLE);

        }
        if (getItem(pos).getParent() == null) {
            icomMapImage.setImageResource(R.drawable.ic_world_globe_dark);
        }
        return row;

    }
}
