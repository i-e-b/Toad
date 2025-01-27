package com.ieb.smalltest;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.ArrayList;


public class Main extends Activity {
    private BaseView view;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // hide action bar
        ActionBar bar = this.getActionBar();
        if (bar != null) this.getActionBar().hide();

        try {
            showFirstScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        if (view != null) return view.motionEvent(ev);
        return false;
    }

    @Override
    public boolean dispatchGenericMotionEvent (MotionEvent ev){
        if (view != null) return view.motionEvent(ev);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event){
        if (view != null) return view.keyEvent(event);
        return false;
    }

    public void showFirstScreen() throws IOException {
        if (view != null) view.StopTimer();
        FirstScreen v = new FirstScreen(this);
        setContentView(v);
        view = v;
        view.StartTimer();
    }
}