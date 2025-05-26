package com.ieb.toad.world.loader;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.util.Log;

import com.ieb.toad.Main;
import com.ieb.toad.sprite.Cherry;
import com.ieb.toad.sprite.Coin;
import com.ieb.toad.sprite.Grass;
import com.ieb.toad.sprite.Potion;
import com.ieb.toad.sprite.Shy;
import com.ieb.toad.sprite.Snifit;
import com.ieb.toad.sprite.Toad;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Direction;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.world.platforms.DeathPlane;
import com.ieb.toad.world.platforms.LadderPlatform;
import com.ieb.toad.world.platforms.OneWayPlatform;
import com.ieb.toad.world.platforms.SolidPlatform;
import com.ieb.toad.world.portals.DirectionPortal;
import com.ieb.toad.world.portals.DoorBox;
import com.ieb.toad.world.portals.DoorThing;
import com.ieb.toad.world.portals.PotBox;

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

    // TODO: allow camera boxes to have different background colors
    public int backgroundColor; // argb32 background color for the level
    public int chunkWidth; // size of level chunks, in tile count
    public int chunkHeight; // size of level chunks, in tile count

    public final List<Thing> fgThings; // used for platforms, creeps and player
    public final List<Thing> bgThings; // used for collectables and grass
    public final List<DoorThing> doorThings; // used for doors and portals
    public final List<CameraZone> camZones; // camera pinning and similar effects
    public final List<Rect> checkpoints; // checkpoint zones
    public Toad toad;

    private final Dictionary<String, LayerChunk> backgroundChunks, mainChunks, foregroundChunks;

    public TiledLoader(final Main context) throws IOException {
        assets = context.getAssets();
        spriteMgr = new SpriteSheetManager(context);
        fgThings = new ArrayList<>(128);
        bgThings = new ArrayList<>(128);
        doorThings = new ArrayList<>(32);
        camZones = new ArrayList<>(16);
        checkpoints = new ArrayList<>(32);

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
                    processItemTileLayer(group, mainChunks); // same as spawns, just a bit easier to edit
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

    private void processItemTileLayer(Node group, Dictionary<String, LayerChunk> chunks) {
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

            // location in tile space
            int ix = (int) getDoubleAttr(attrs, "x");
            int iy = (int) getDoubleAttr(attrs, "y");
            int iw = (int) getDoubleAttr(attrs, "width");

            // convert from tile index to world space
            int tilePx = spriteMgr.tiles.pixelSize;
            int hw = SCALE * tilePx / 2;
            int dx = chunkWidth * SCALE * ix;
            int dy = chunkHeight * SCALE * iy;

            String[] csv = obj.getTextContent().split("[,\r\n]");
            int x = 0, y = 0;
            for (String t : csv) {
                if (x >= iw){ x = 0; y++;}
                if (t.isBlank()) continue;
                int tile = Integer.parseInt(t) - 1; // 0=empty -> -1=empty

                if (tile >= 0) spawnFromTile(tile, hw, dx, x, tilePx, dy, y, ix, iy);
                x++;
            }
        }
    }

    private static final int GRASS_START_TILE = 520;
    private static final int GRASS_END_TILE = 527;
    private static final int CHERRY_START_TILE = 598;
    private static final int CHERRY_END_TILE = 605;
    private static final int POW_START_TILE = 572;
    private static final int POW_END_TILE = 579;
    private static final int KEY_TILE = 442;
    private static final int COIN_TILE = 443;
    private static final int POTION_TILE = 468;
    private static final int MUSHROOM_TILE = 469;
    private void spawnFromTile(int tileId, int hw, int dx, int x, int tilePx, int dy, int y, int ix, int iy) {
        int cx = hw + dx + (x * tilePx *SCALE);
        int cy = hw + dy + (y * tilePx *SCALE);
        int by = dy + ((y+1) * tilePx *SCALE);

        if (tileId == COIN_TILE){
            Coin coin = new Coin(spriteMgr);
            coin.px = cx; coin.py = cy;
            bgThings.add(coin);
        } else if (tileId>=CHERRY_START_TILE && tileId <=CHERRY_END_TILE){
            Cherry cherry = new Cherry(spriteMgr);
            cherry.px = cx;cherry.py = cy;
            cherry.advanceAnim(tileId - CHERRY_START_TILE);
            bgThings.add(cherry);
        } else if (tileId>=GRASS_START_TILE && tileId <=GRASS_END_TILE){
            Grass grass = new Grass(spriteMgr);
            grass.px = cx;grass.py = by;
            grass.advanceAnim(tileId - GRASS_START_TILE);
            bgThings.add(grass);
        } else if (tileId==POTION_TILE){
            Thing potion = new Potion(spriteMgr);
            potion.px = cx;potion.py = by;
            bgThings.add(potion);
        } else {
            Log.w(TAG, "processItemTileLayer: unknown tile spawn '"+ tileId +"' in chunk at "+ ix +","+ iy);
        }
    }

    /** @noinspection SameParameterValue*/
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
                    processCamZones(group.getChildNodes());
                    break;

                case "checkpoints":
                    processCheckpoints(group.getChildNodes());
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
                    fgThings.add(toad);
                    break;

                case "shyguy":
                    Thing shy = new Shy(spriteMgr);
                    shy.px = x;
                    shy.py = y - shy.radius;
                    fgThings.add(shy);
                    break;

                case "snifit":
                    Thing snifit = new Snifit(spriteMgr);
                    snifit.px = x;
                    snifit.py = y - snifit.radius;
                    fgThings.add(snifit);
                    break;

                default:
                    Log.w(TAG, "loadLevel: unknown spawn type: '"+type+"' in objectId="+objId);
                    break;
            }
        }
    }

    private void processCheckpoints(NodeList group) {
        int count = group.getLength();
        for (int i = 0; i < count; i++) {
            Node obj = group.item(i);
            String name = obj.getNodeName();
            if (name == null || !name.equals("object")) continue;
            NamedNodeMap attrs = obj.getAttributes();

            /*
  <object id="85" x="176" y="112" width="32" height="48"/>
  */
            int x = SCALE * (int)getDoubleAttr(attrs, "x");
            int y = SCALE * (int)getDoubleAttr(attrs, "y");
            int w = SCALE * (int)getDoubleAttr(attrs, "width");
            int h = SCALE * (int)getDoubleAttr(attrs, "height");

            checkpoints.add(new Rect(x,y,x+w,y+h));
        }
    }

    private void processCamZones(NodeList group) {
        int count = group.getLength();
        for (int i = 0; i < count; i++) {
            Node obj = group.item(i);
            String name = obj.getNodeName();
            if (name == null || !name.equals("object")) continue;
            NamedNodeMap attrs = obj.getAttributes();

            /*
  <object id="23" x="-256" y="-48" width="768" height="304"/>
  <object id="43" x="768" y="-80" width="752" height="400">
   <properties>
    <property name="color" type="color" value="#ff000000"/>
   </properties>
  </object>*/
            int objId = getIntAttr(attrs, "id");
            int x = SCALE * (int)getDoubleAttr(attrs, "x");
            int y = SCALE * (int)getDoubleAttr(attrs, "y");
            int w = SCALE * (int)getDoubleAttr(attrs, "width");
            int h = SCALE * (int)getDoubleAttr(attrs, "height");

            CameraZone cz = new CameraZone(x,y,w,h);
            camZones.add(cz);

            // might have a background color:
            if (obj.hasChildNodes()){
                NodeList props = obj.getChildNodes().item(0).getChildNodes();
                int propCount = props.getLength();
                for (int j = 0; j < propCount; j++) {
                    Node prop = props.item(j);
                    String propName = prop.getNodeName();
                    if (propName == null || !propName.equals("property")) continue;

                    NamedNodeMap propAttrs = prop.getAttributes();
                    String key = getStrAttr(propAttrs, "name");
                    switch (key){
                        case "color":
                            cz.color = getHexAttr(propAttrs, "value");
                            break;

                        default:
                            Log.w(TAG, "processCamZones: unknown property '"+key+"' in objId="+objId);
                            break;
                    }
                }
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

            String target = getStrAttr(attrs, "name");
            switch (type){
                case "solid":
                    fgThings.add(new SolidPlatform(x, y, w, h));
                    break;

                case "oneway":
                    fgThings.add(new OneWayPlatform(x, y, w, h));
                    break;

                case "door":
                    doorThings.add(new DoorBox(x,y,w,h, target, false, objId));
                    break;

                case "locked_door":
                    doorThings.add(new DoorBox(x,y,w,h, target, true, objId));
                    break;

                case "pot":
                    doorThings.add(new PotBox(x,y,w,h, target, objId));
                    break;

                case "portal_up":
                    doorThings.add(new DirectionPortal(x,y,w,h, target, Direction.UP, objId));
                    break;

                case "ladder":
                    fgThings.add(new LadderPlatform(x, y, w, h));
                    break;

                case "spike":
                    Log.w(TAG, "processWalls: spike platforms not implemented yet");
                    fgThings.add(new SolidPlatform(x, y, w, h));
                    break;

                case "death":
                    Log.w(TAG, "processWalls: death platforms not implemented yet");
                    fgThings.add(new DeathPlane(x, y, w, h));
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
