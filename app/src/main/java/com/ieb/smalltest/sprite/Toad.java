package com.ieb.smalltest.sprite;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.ieb.smalltest.Main;
import com.ieb.smalltest.world.Camera;
import com.ieb.smalltest.world.Collision;
import com.ieb.smalltest.world.Thing;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class Toad extends Thing {

    private final Animation run_left = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{1, 18, 35, 18});
    private final Animation run_right = new Animation(100, Animation.FOREVER, 16, 28, 62, new int[]{205,188,171,188});
    private final Animation carry_left = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{52,69,86,69});
    private final Animation carry_right = new Animation(100, Animation.FOREVER, 16, 28, 62, new int[]{154,134,120,134});

    private final Animation enter_left = new Animation(500, Animation.ONCE, 16, 28, 29, new int[]{103});
    private final Animation enter_right = new Animation(500, Animation.ONCE, 16, 28, 62, new int[]{103});

    private final Animation fall_left = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{120});
    private final Animation fall_right = new Animation(100, Animation.FOREVER, 16, 28, 62, new int[]{86});

    private final Animation pull_left = new Animation(50, Animation.ONCE, 16, 28, 29, new int[]{171,188});
    private final Animation pull_right = new Animation(50, Animation.ONCE, 16, 28, 62, new int[]{35,18});

    private final Animation hurt = new Animation(500, Animation.ONCE, 16, 28, 29, new int[]{205});

    private final Animation duck = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{137});
    private final Animation duck_carry = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{154});

    private final Bitmap mToad;

    /** Load Toad graphics */
    public Toad(final Main context) throws IOException {
        AssetManager assets = context.getAssets();
        Rect dummy = new Rect(0, 0, 0,0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        InputStream toadFile = assets.open("toad.png");
        mToad = BitmapFactory.decodeStream(toadFile, dummy, options);
        toadFile.close();
        hitBox = new Rect(0,0,0,0);
        type = Collision.PLAYER;
        radius = 32;
        gravity = 1.0; // fully affected by gravity
    }

    public void draw(Camera camera, Paint paint, int x, int y){

        hitBox.bottom=(int)(y+radius);//(28*4);
        hitBox.top = hitBox.bottom - (28 * 4);
        hitBox.left = x - (int)radius;
        hitBox.right = x + (int)radius;
        camera.drawBitmap(mToad, run_left.rect(), hitBox, paint);

        paint.setARGB(120,0,100,0);
        camera.drawCircle((float)p1x, (float)p1y, (float)radius, paint);
        paint.setARGB(120,100,0,0);
        camera.drawRect(hitBox, paint);
    }

    public void stepMillis(long ms) {
        run_left.advance(ms);
    }

    @Override
    public void draw(@NotNull Camera camera, Paint paint) {
        paint.setARGB(255,0,0,0);
        draw(camera, paint, (int) p1x, (int) p1y);
    }
}
