package com.ieb.toad.world.loader;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.util.Log;

import com.ieb.toad.Main;
import com.ieb.toad.sprite.Shy;
import com.ieb.toad.sprite.Toad;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.world.platforms.OneWayPlatform;
import com.ieb.toad.world.platforms.Platform;

import org.jetbrains.annotations.NotNullByDefault;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/** Load TMX files from <a href="https://www.mapeditor.org/">Tiled map editor</a>.
 * Designed for version 1.11.2
 * <pre>
 * This expects files with CSV encoded tile layers:
 * - foreground: displays over all else including sprites.
 * - items: special layer that interprets tiles as spawns for some items.
 * - main: main level tiles. Displays under sprites.
 * - background: displays under all else.
 *
 * And object layers:
 * - walls rectangle objects, for defining platforms and walls. Type should be in type/class.
 * - spawns: point objects for spawning creeps, players, items.
 * - camera_zones: rectangle objects, for camera locking. Camera tries to stay inside rect, run over top&right if it doesn't fit.
 * </pre>
 * Note, tile layers are visual only. Physics objects come from wall definitions.
 * */
public class TiledLoader {
    private static final String TAG = "TiledLoader";

    public static final int SCALE = 4; // multiply tile sizes by this

    private final AssetManager assets;
    private final SpriteSheetManager spriteMgr;

    public int backgroundColor; // argb32 background color for the level
    public int chunkWidth; // size of level chunks, in tile count
    public int chunkHeight; // size of level chunks, in tile count

    public final List<Thing> things; // TODO: better structure for larger levels
    public Toad toad;

    private final Dictionary<String, LayerChunk> backgroundChunks, mainChunks, foregroundChunks;

    public TiledLoader(final Main context) throws IOException {
        assets = context.getAssets();
        spriteMgr = new SpriteSheetManager(context);
        things = new ArrayList<>(128);

        backgroundChunks = new Hashtable<>(64);
        mainChunks = new Hashtable<>(64);
        foregroundChunks = new Hashtable<>(64);
    }

    /** Try to load a TMX level by level index */
    public boolean loadLevel(int index){
        try {
            InputStream levelFile = assets.open("level"+index+".tmx");
            Document doc = parseXML(levelFile);
            levelFile.close();
            if (doc == null) return false;

            getGlobalSettings(doc);

            processObjectGroups(doc);

            processTileLayers(doc);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "loadLevel: ", e);
            return false;
        }
    }

    /** return chunks from the background layer that are at least partially visible on camera */
    public Enumeration<LayerChunk> getBackgroundChunks(Rect coverage) {
        // TODO: filter based on location
        return backgroundChunks.elements();
    }

    /** return chunks from the main layer that are at least partially visible on camera */
    public Enumeration<LayerChunk> getMainChunks(Rect coverage) {
        // TODO: filter based on location
        return mainChunks.elements();
    }

    /** return chunks from the foreground layer that are at least partially visible on camera */
    public Enumeration<LayerChunk> getForegroundChunks(Rect coverage) {
        // TODO: filter based on location
        return foregroundChunks.elements();
    }

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

    private void processTileLayers(Document doc) {
        NodeList layers = doc.getElementsByTagName("layer");
        int layerCount = layers.getLength();
        for (int lyr = 0; lyr < layerCount; lyr++) {
            Node group = layers.item(lyr);
            NamedNodeMap layerAttrs = group.getAttributes();
            String name = getStrAttr(layerAttrs, "name");

            switch (name){
                case "background":
                    processTileLayer(group, backgroundChunks);
                    break;

                case "main":
                    processTileLayer(group, mainChunks);
                    break;

                case "items":
                    // TODO: add extra spawns
                    break;

                case "foreground":
                    processTileLayer(group, foregroundChunks);
                    break;

                default:
                    Log.w(TAG, "loadLevel: unexpected tile layer: "+name);
                    break;
            }
        }
    }

    private void processTileLayer(Node group, Dictionary<String, LayerChunk> chunks) {
        Node dataNode = getFirstChild(group, "data");

        if (dataNode == null) return;
        NamedNodeMap dataAttrs = dataNode.getAttributes();
        String encoding = getStrAttr(dataAttrs, "encoding");
        if (!encoding.equals("csv")) throw new RuntimeException("Invalid level format. Expected 'csv', got '"+encoding+"'");

        NodeList nodes = dataNode.getChildNodes();
        int nodeCount = nodes.getLength();
        for (int i = 0; i < nodeCount; i++) {
            Node obj = nodes.item(i);
            String name = obj.getNodeName();
            NamedNodeMap attrs = obj.getAttributes();

            if (!name.equals("chunk")) continue;

            /*
        <chunk x="-16" y="0" width="16" height="16">
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
         */
            // location in tile space
            int ix = (int) getDoubleAttr(attrs, "x");
            int iy = (int) getDoubleAttr(attrs, "y");
            int iw = (int) getDoubleAttr(attrs, "width");
            int ih = (int) getDoubleAttr(attrs, "height");

            // convert from tile index to world space
            int x = chunkWidth * SCALE * ix;
            int y = chunkHeight * SCALE * iy;

            LayerChunk chunk = new LayerChunk(spriteMgr, iw, ih, x, y);
            chunks.put(chunk.key, chunk);

            String[] csv = obj.getTextContent().split("[,\r\n]");
            int idx = 0;
            for (String t : csv) {
                if (t.isBlank()) continue;
                int tile = Integer.parseInt(t) - 1; // 0=empty -> -1=empty
                chunk.set(idx, tile);
                idx++;
            }
        }


    }

    private Node getFirstChild(Node group, String elementName) {
        if (group == null) return null;
        NodeList children = group.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++) {
            Node n = children.item(i);
            if (n.getNodeName().equals(elementName)) return n;
        }
        return null;
    }

    private void processObjectGroups(Document doc) {
        NodeList objectGroups = doc.getElementsByTagName("objectgroup");
        int groupCount = objectGroups.getLength();
        for (int grp = 0; grp < groupCount; grp++) {
            Node group = objectGroups.item(grp);
            NamedNodeMap groupAttrs = group.getAttributes();
            String name = getStrAttr(groupAttrs, "name");

            switch (name){
                case "walls":
                    processWalls(group.getChildNodes());
                    break;

                case "spawns":
                    processSpawns(group.getChildNodes());
                    break;

                case "camera_zones":
                    break;

                default:
                    Log.w(TAG, "loadLevel: unexpected object layer: "+name);
                    break;
            }
        }
    }

    private void processSpawns(NodeList group) {
        int count = group.getLength();
        for (int i = 0; i < count; i++) {
            Node obj = group.item(i);
            String name = obj.getNodeName();
            if (name == null || !name.equals("object")) continue;
            NamedNodeMap attrs = obj.getAttributes();

            // <object id="9" name="player" x="-184.5" y="223">
            int objId = getIntAttr(attrs, "id");
            String type = getStrAttr(attrs, "name");
            int x = SCALE * (int)getDoubleAttr(attrs, "x");
            int y = SCALE * (int)getDoubleAttr(attrs, "y");

            switch (type){
                case "player":
                    toad = new Toad(spriteMgr);
                    toad.px = x;
                    toad.py = y - toad.radius;
                    things.add(toad);
                    break;

                case "shyguy":
                    Thing shy = new Shy(spriteMgr);
                    shy.px = 500;
                    shy.py = 750;
                    things.add(shy);
                    break;

                default:
                    Log.w(TAG, "loadLevel: unknown spawn type: "+type);
                    break;
            }
        }
    }

    private void processWalls(NodeList group) {
        int count = group.getLength();
        for (int i = 0; i < count; i++) {
            Node obj = group.item(i);
            String name = obj.getNodeName();
            if (name == null || !name.equals("object")) continue;
            NamedNodeMap attrs = obj.getAttributes();

            //<object id="3" type="solid" x="-256" y="32" width="16" height="223"/>
            int objId = getIntAttr(attrs, "id");
            String type = getStrAttr(attrs, "type");
            int x = SCALE * (int)getDoubleAttr(attrs, "x");
            int y = SCALE * (int)getDoubleAttr(attrs, "y");
            int w = SCALE * (int)getDoubleAttr(attrs, "width");
            int h = SCALE * (int)getDoubleAttr(attrs, "height");

            if (w < 1 || h < 1){
                Log.w(TAG, "processWalls: wall has invalid size: id="+objId);
                continue;
            }

            switch (type){
                case "solid":
                    things.add(new Platform(x, y, w, h));
                    break;

                case "oneway":
                    things.add(new OneWayPlatform(x, y, w, h));
                    break;

                case "spike":
                    Log.w(TAG, "processWalls: spike platforms not implemented yet");
                    things.add(new Platform(x, y, w, h));
                    break;
                case "death":
                    Log.w(TAG, "processWalls: death platforms not implemented yet");
                    things.add(new Platform(x, y, w, h));
                    break;

                default:
                    Log.w(TAG, "loadLevel: unknown platform type: '"+type+"' at id="+objId);
                    break;
            }
        }
    }

    private void getGlobalSettings(Document doc) {
        Node mapNode = doc.getElementsByTagName("map").item(0);
        NamedNodeMap mapAttrs = mapNode.getAttributes();

        boolean isInfinite = getStrAttr(mapAttrs, "infinite").equals("1");
        if (!isInfinite) throw new RuntimeException("levels should be 'infinite' type");

        chunkWidth = getIntAttr(mapAttrs, "tilewidth");
        chunkHeight = getIntAttr(mapAttrs, "tileheight");
        backgroundColor = getHexAttr(mapAttrs, "backgroundcolor") + 0xFF000000;
    }

    private String getStrAttr(NamedNodeMap attrs,String name){
        if (attrs == null) return "";
        Node attr = attrs.getNamedItem(name);
        if (attr == null) return "";
        String val = attr.getNodeValue();
        if (val == null) return "";
        return val;
    }

    private int getHexAttr(NamedNodeMap attrs,String name){
        String s = getStrAttr(attrs, name);
        if (s.isBlank()) return 0;
        if (s.startsWith("#")) s = s.substring(1);

        return Integer.parseInt(s, 16);
    }

    private int getIntAttr(NamedNodeMap attrs,String name){
        String s = getStrAttr(attrs, name);
        if (s.isBlank()) return 0;

        return Integer.parseInt(s);
    }

    private double getDoubleAttr(NamedNodeMap attrs,String name){
        String s = getStrAttr(attrs, name);
        if (s.isBlank()) return 0;

        return Double.parseDouble(s);
    }
}
