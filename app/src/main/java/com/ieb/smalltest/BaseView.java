package com.ieb.smalltest;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseView extends View {
    private Timer mTimer;
    private TimerTask mTimerTask;

    public BaseView(Context context) {
        super(context);
    }

    /** handle a generic motion event */
    public abstract boolean motionEvent(MotionEvent ev);

    /** handle a generic key event */
    public abstract boolean keyEvent(KeyEvent event);

    /** Stop the activity timer for this view. Should be called when the view is not visible */
    public void StopTimer() {
        if (mTimer != null) {
            Log.i("BaseView", "Stop timer");
            mTimerTask.cancel();
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
            mTimerTask = null;
        }
    }

    /** Start the activity timer for this view. Should be called when the view becomes visible */
    public void StartTimer() {
        StopTimer();
        if (mTimer == null) {
            Log.i("BaseView", "Start timer");
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                public void run() {
                    OnTimerTick();
                }
            };
            mTimer.schedule(mTimerTask, 1, TimerTickRate());
        }
    }

    /** Override to set the tick rate of this view. This is only queried when the view becomes visible */
    protected int TimerTickRate(){ return 33; }

    /** Override to perform per-tick actions */
    protected void OnTimerTick(){}

    @Override
    protected void onVisibilityChanged(@NotNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE){
            StartTimer();
        } else {
            StopTimer();
        }
    }


}
