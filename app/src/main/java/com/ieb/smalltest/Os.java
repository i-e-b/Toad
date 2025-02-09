package com.ieb.smalltest;


import static android.content.res.Configuration.UI_MODE_NIGHT_YES;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Single points for Android system calls.
 */
public class Os {
    public static boolean isDarkMode(View v){
        int uiMode = v.getResources().getConfiguration().uiMode;
        return ((uiMode & UI_MODE_NIGHT_YES) > 0);
    }

    public static void setSize(Paint mPaint, int s){
        mPaint.setTextSize(s);
    }
    public static void setGrey(Paint mPaint, int c2) {
        mPaint.setARGB(255, c2, c2, c2);
    }
    public static void measureText(Paint mPaint, String txt, Rect rect){
        mPaint.getTextBounds(txt, 0,txt.length(), rect);
    }

    public static void measureChr(Paint mPaint, String txt, Rect rect) {
        mPaint.getTextBounds(txt, 0,2, rect);
    }

    public static int measureChrSize(Paint mPaint, String txt) {
        Rect rect = new Rect();
        mPaint.getTextBounds(txt, 0,2, rect);
        return (Math.abs(rect.top) + Math.abs(rect.right)) / 2;
    }

    private static SharedPreferences getPref(Context c){
        return c.getSharedPreferences("scores", Context.MODE_PRIVATE);
    }

    public static int getLastLevel(Context c){
        return getPref(c).getInt("lastLvl", 0);
    }

    public static void setLastLevel(Context c, int level){
        SharedPreferences.Editor e = getPref(c).edit();
        e.putInt("lastLvl", level);
        e.apply();
    }

    public static int getScore(Context c, int level){
        String levelName = ""+level;
        return getPref(c).getInt(levelName, 0);
    }

    public static void setScore(Context c, int level, int score){
        String levelKey = ""+level;
        SharedPreferences pref = getPref(c);
        int best = pref.getInt(levelKey, 0);

        if (best < 1 || score < best) { // yay! Best score.
            // save the preference
            SharedPreferences.Editor e = pref.edit();
            e.putInt(levelKey, score);
            e.apply();
        }
    }

    private static final Rect bTr = new Rect();
    public static void boxText(Canvas canvas, String text, float x, float y, Paint mPaint){
        int start = 0, end = 0, length = text.length();
        float maxWidth = canvas.getWidth() - x;

        // TemporaryBuffer.obtain(
        while (start < length) {
            end += mPaint.breakText(text, start, length, true, maxWidth, null);
            measureText(mPaint, text, bTr);

            if (start >= end) break; // `breakText` couldn't manage.
            canvas.drawText(text, start, end, x, y, mPaint);
            y += bTr.bottom - bTr.top;

            if (start == end) break;
            start = end;
        }
    }

    public static void drawText(Canvas canvas, String text, float x, float y, Paint mPaint) {
        canvas.drawText(text, x, y, mPaint);
    }

    public static void drawRect(Paint p, Canvas canvas, float l, float t, float r, float b) {
        canvas.drawRect(l,t,r,b, p);
    }
}
