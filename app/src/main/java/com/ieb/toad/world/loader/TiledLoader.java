package com.ieb.toad.world.loader;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ieb.toad.Main;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TiledLoader {
    private static final String TAG = "TiledLoader";
    private final AssetManager assets;

    public TiledLoader(final Main context){
        assets = context.getAssets();
    }

    public void loadLevel(int number){
        try {
            InputStream levelFile = assets.open("level"+number+".tmx");
            Document doc = parseXML(levelFile);
            levelFile.close();

            Node mapNode = doc.getElementsByTagName("map").item(0);
            Log.i(TAG, "loadLevel: "+mapNode.getAttributes().getNamedItem("backgroundcolor").getNodeValue());
        } catch (Exception e) {
            Log.e(TAG, "loadLevel: ", e);
        }
    }
    /*
        https://developer.android.com/reference/javax/xml/parsers/DocumentBuilder.html
        https://developer.android.com/reference/org/w3c/dom/Node
     */

    private Document parseXML(InputStream source) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(source);
        } catch (Exception e) {
            Log.e(TAG, "parseXML: ", e);
            return null;
        }
    }
}
