package com.ieb.smalltest.sprite;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.ieb.smalltest.Main;

import java.io.IOException;
import java.io.InputStream;

/** A helper for loading images, and doing basic manipulation */
public class SpriteSheet {
    public final Bitmap toad, dude, stuff;

    // TODO: Auto-chop the tiles (maybe with a #F0F pixel)
    // TODO: Pre-calculate flipped versions of each tile

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
    }
}
