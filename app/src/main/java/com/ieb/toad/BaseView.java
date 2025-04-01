package com.ieb.toad;

import android.content.Context;
import android.util.Log;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseView extends View {
    private Timer simulationTimer, backgroundTimer;
    private TimerTask simulationTimerTask, backgroundTimerTask;

    public BaseView(Context context) {
        super(context);
    }

    /** Stop the activity timer for this view. Should be called when the view is not visible */
    public void StopTimer() {
        if (simulationTimer != null) {
            Log.i("BaseView", "Stop timer");
            if (simulationTimerTask != null) simulationTimerTask.cancel();
            simulationTimer.cancel();
            simulationTimer.purge();
            simulationTimer = null;
            simulationTimerTask = null;
        }
        if (backgroundTimer != null){
            if (backgroundTimerTask != null) backgroundTimerTask.cancel();
            backgroundTimer.cancel();
            backgroundTimer.purge();
            backgroundTimer = null;
            backgroundTimerTask = null;
        }
    }

    protected long preTime, postTime, idleTime;

    /** Start the activity timer for this view. Should be called when the view becomes visible */
    public void StartTimer() {
        StopTimer();
        if (simulationTimer == null) {
            Log.i("BaseView", "Start timer");
            simulationTimer = new Timer();
            simulationTimerTask = new TimerTask() {
                public void run() {
                    preTime = System.currentTimeMillis();
                    idleTime = preTime - postTime;

                    OnSimulationTimerTick(preTime);

                    postTime = System.currentTimeMillis();
                }
            };
            simulationTimer.schedule(simulationTimerTask, 1, SimulationTimerTickRate());
        }

        if (backgroundTimer == null) {
            backgroundTimer = new Timer();
            backgroundTimerTask = new TimerTask() {
                public void run() {
                    OnBackgroundTimerTick();
                }
            };
            backgroundTimer.schedule(backgroundTimerTask, 100, BackgroundTimerTickRate());
        }
    }

    /** Override to set the main simulation tick rate */
    protected int SimulationTimerTickRate(){ return 16; /* 33 = 30fps; 16 = 60fps; */ }

    /** Override to set the tick rate of background tasks */
    protected int BackgroundTimerTickRate(){ return 200; /* 50 = 20fps; 33 = 30fps; */ }

    /** Override to perform per-simulation-tick actions */
    protected void OnSimulationTimerTick(long time){}

    /** Override to perform background actions */
    protected void OnBackgroundTimerTick(){}

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
