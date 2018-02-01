package com.work.golinko.downloadmaps.model.container;

import com.work.golinko.downloadmaps.model.entity.RegionItem;

import java.util.ArrayList;

public class RegionsItemContainer {

    private ArrayList<RegionItem> regionItems;

    public ArrayList<RegionItem> getRegionItems() {
        return regionItems;
    }

    public void setRegionItems(ArrayList<RegionItem> arrayList) {
        regionItems = arrayList;
    }

    private RegionsItemContainer() {
        regionItems = new ArrayList<>();

    }

    private static class InstanceHolder {

        private static final RegionsItemContainer INSTANCE = new RegionsItemContainer();

    }

    public static RegionsItemContainer getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
