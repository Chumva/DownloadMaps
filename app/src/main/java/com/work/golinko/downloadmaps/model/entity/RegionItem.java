package com.work.golinko.downloadmaps.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;


public class RegionItem implements Serializable {
    private String name;
    private ArrayList<RegionItem> subRegions;

    private RegionItem parent;
    private boolean isDeepestRegion;
    private boolean isDownloadable;

    public boolean isDownloadable() {
        return isDownloadable;
    }

    public void setDownloadable(boolean downloadable) {
        isDownloadable = downloadable;
    }


    public RegionItem getParent() {
        return parent;
    }

    public void setParent(RegionItem parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name = getUpperFirstChar(name);
        this.name = name;
    }

    public ArrayList<RegionItem> getSubRegions() {
        return subRegions;
    }

    public void setSubRegions(ArrayList<RegionItem> subRegions) {
        this.subRegions = subRegions;
    }

    private boolean isDeepestRegion() {
        return isDeepestRegion;
    }

    public void setDeepestRegion(boolean deepestRegion) {
        isDeepestRegion = deepestRegion;
    }

    public boolean hasSubRegions() {
        return subRegions != null;
    }

    @Override
    public String toString() {
        return name;
    }

//    here we get full name for uri request
    public String getFullName() {
        String result = "";
        if (parent != null) {
            if (parent.getParent() != null) {
                result += parent.getFullName() + "_" + name;
            } else {
                result += name;
            }
        }
        if (isDeepestRegion()) {
            result += "_" + getHighestRegion();
        }

        result = getUpperFirstChar(result);
        return result;
    }

    private String getHighestRegion() {
        if (parent == null) {
            return name;
        } else {
            return parent.getHighestRegion();
        }
    }

    private String getUpperFirstChar(String name) {
        name = name.toLowerCase(Locale.ENGLISH);
        String firstLetter = name.substring(0, 1).toUpperCase(Locale.ENGLISH);
        String endOfName = name.substring(1, name.length());
        name = firstLetter.concat(endOfName);
        return name;
    }
}
