package com.ieb.toad;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.ieb.toad.input.VirtualGamepad;

import java.io.IOException;


public class Main extends Activity {
    private BaseView view;
    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {
            //Catch your exception
            Log.e(TAG, "uncaughtException: " + paramThread.getName(), paramThrowable);
            // Without System.exit() this will not work.
            System.exit(2);
        });

        // hide action bar
        ActionBar bar = this.getActionBar();
        if (bar != null) this.getActionBar().hide();

        try {
            showFirstScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Finish the main activity */
    public void Close(){
        this.finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        VirtualGamepad.motionEvent(ev);
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent (MotionEvent ev){
        VirtualGamepad.motionEvent(ev);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event){
        VirtualGamepad.keyEvent(event);
        if (VirtualGamepad.isSelect()) Close();
        return true;
    }

    public void showFirstScreen() throws IOException {
        if (view != null) view.StopTimer();
        FirstScreen v = new FirstScreen(this);
        setContentView(v);
        view = v;
        view.StartTimer();
    }
}