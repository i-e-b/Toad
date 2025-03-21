package com.ieb.toad.sprite;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.ieb.toad.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** A helper for loading images, and doing basic manipulation */
public class SpriteSheet {
    public final Bitmap toad, dude, stuff;

    public final List<Rect> toadTiles = new ArrayList<>();

    // We slice up the input image into tiles using a start and stop pixel.
    // The start pixel is (magenta, #FF00FF RGB). The top-left pixel of the tile
    // is the bottom-right neighbor of the start pixel (start pixel is
    // NOT part of the tile).
    // We then scan the tile until we find a stop pixel (cyan, #00FFFF RGB)
    // The bottom-right pixel of the tile is the top-left neighbour of
    // the stop pixel (stop pixel is NOT part of the tile).

    // TODO: Auto-chop the tiles (maybe with a #F0F pixel and #0FF)
    // TODO: Pre-calculate flipped versions of each tile
    //
    // https://stackoverflow.com/questions/4160149/how-to-draw-on-bitmap-in-android
    // https://www.skoumal.com/en/android-how-to-draw-text-on-a-bitmap/

    /** Prepare for loading graphics */
    public SpriteSheet(final Main context) throws IOException {
        AssetManager assets = context.getAssets();
        Rect hitBox = new Rect(0,0,0,0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        InputStream toadFile = assets.open("toad.png");
        toad = BitmapFactory.decodeStream(toadFile, hitBox, options);
        toadFile.close();

        InputStream dudeFile = assets.open("dude.png");
        dude = BitmapFactory.decodeStream(dudeFile, hitBox, options);
        dudeFile.close();

        InputStream stuffFile = assets.open("stuff.png");
        stuff = BitmapFactory.decodeStream(stuffFile, hitBox, options);
        stuffFile.close();

        findTiles(toad, toadTiles);
    }

    private void findTiles(Bitmap source, List<Rect> output) {
        if (source == null) return;

        List<Point> starts = new ArrayList<>(); // in order, these are the tile indexes
        List<Point> stops = new ArrayList<>(); // pick the nearest one that is bottom-right of the start

        // Find all starts and stops
        int h = source.getHeight();
        int w = source.getWidth();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int c = source.getPixel(x, y); // TODO: optimise this after it's fully working
                if ((c&0x00FFFFFF) == 0x00FF00FF) starts.add(new Point(x+1,y+1));
                if ((c&0x00FFFFFF) == 0x0000FFFF) stops.add(new Point(x-1,y-1));
            }
        }

        // Match up starts and stops
        for (Point start : starts) {
            int minX = w;
            int minY = h;
            int minDist = (minX*minX)+(minY*minY);
            for (Point stop : stops) {
                int dx = stop.x - start.x;
                int dy = stop.y - start.y;
                if (dx <= 0 || dy <= 0) continue; // not to the right
                int dist = (dx*dx)+(dy*dy);
                if (dist > minDist) continue; // further than other points
                minDist = dist;
                minX = stop.x;
                minY = stop.y;
            }

            output.add(new Rect(start.x, start.y, minX, minY));
            Log.i("Tile", "("+start.x+", "+start.y+", "+minX+", "+minY+")");
        }
    }
}
