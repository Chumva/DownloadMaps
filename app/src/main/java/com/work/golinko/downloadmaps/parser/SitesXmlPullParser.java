package com.work.golinko.downloadmaps.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import com.work.golinko.downloadmaps.model.container.RegionsItemContainer;
import com.work.golinko.downloadmaps.model.entity.RegionItem;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SitesXmlPullParser {
    public static final String WORLD_BASEMAP = "World_basemap";
    public static final String WORLD_SEAMARKS = "World_seamarks";
    static ArrayList<RegionItem> RegionItems = new ArrayList<RegionItem>();
    static final String KEY_REGION = "region";
    static final String KEY_NAME = "name";
    static final String KEY_HAS_MAP = "map";


    public static ArrayList<RegionItem> getStackSitesFromFile(Context ctx) {

        FileInputStream fis = null;
        try {
            fis = ctx.openFileInput("regions.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = dBuilder.parse(fis);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Node root = doc.getDocumentElement();
        root.normalize();

        if (root.getNodeName().equals("regions_list")) {
            Log.i("OsmAnd", "Inside regions list");

            if (root.hasChildNodes()) {
                RegionItems = subRegionsParser(root, null);
            }
        }

        Log.i("OsmAnd", " return RegionItems" + RegionItems.size());
        sortRegionsByName(RegionItems);
        RegionsItemContainer.getInstance().setRegionItems(RegionItems);

        return RegionItems;
    }

    public static ArrayList<RegionItem> subRegionsParser(Node node, RegionItem parent) {
        RegionItem curRegionItem = null;
        ArrayList<RegionItem> RItems = new ArrayList<RegionItem>();

        NodeList nList = node.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node nd = nList.item(i);

            if (nd != null && (nd.getNodeType() == Node.ELEMENT_NODE && nd.getNodeName().equals(KEY_REGION))) {
                Element element2 = (Element) nd;
                String name = element2.getAttribute(KEY_NAME);
                String hasMap = element2.getAttribute(KEY_HAS_MAP);
                Log.i("StackSites", hasMap);
                if (name.equals(WORLD_SEAMARKS) || name.equals(WORLD_BASEMAP)) {
                    continue;
                }

                curRegionItem = new RegionItem();
                curRegionItem.setName(name);
                curRegionItem.setParent(parent);

                if ((hasMap.equals("") || hasMap.equals("yes")) && parent != null) {
                    curRegionItem.setDownloadable(true);
                }


                if (nd.hasChildNodes()) {
                    curRegionItem.setSubRegions(subRegionsParser(nd, curRegionItem));
                }
                RItems.add(curRegionItem);
            }
        }//end of for loop
        sortRegionsByName(RItems);
        return RItems;
    }

    public static void sortRegionsByName(ArrayList<RegionItem> arrayList) {
        Collections.sort(arrayList, new Comparator<RegionItem>() {
            public int compare(RegionItem v1, RegionItem v2) {
                return v1.getName().compareTo(v2.getName());
            }
        });
    }

}

