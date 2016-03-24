package com.paulb.metronom;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Paul
 *
 * This class is for the binding service
 */

public class MetroService extends Service {

    ExecutorService es; //setup the executor for task
    MetroTask mt; //class implements main task for the service
    CameraManager manager; //needs camera manager to get to the flashligth
    Vibrator vibro; //needs to vibrate
    MediaPlayer sound; //needs to play the sound
    int stop; //for the #selfStop function
    MyBinder binder = new MyBinder(); //Binder for callback to service from activity

    /**
     * @see IBinder
     * returns the instance of service
     */
    class MyBinder extends Binder {
        MetroService getService() {
            return MetroService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize main variables for the hardware
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        vibro = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sound = MediaPlayer.create(this, R.raw.sound);

        es = Executors.newFixedThreadPool(1); //declare 1 work thread for executor
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (null == intent)
            Log.d("STOP", "INIT INTENT");
        else {
            //Handle intent that receive from activity
            boolean action = intent.getBooleanExtra("action", false);
            int frequency = intent.getIntExtra("frequency", 100);
            boolean _vibro = intent.getBooleanExtra("_vibro", true);
            boolean _flash = intent.getBooleanExtra("_flash", false);
            boolean _sound = intent.getBooleanExtra("_sound", false);
            PendingIntent pi = intent.getParcelableExtra(MetroActivity.PARAM_PINTENT);
            stop = startId;
            //create task for executors service
            mt = new MetroTask(frequency, action, manager, vibro, sound, _vibro, _flash, _sound, pi);

                mt.setFrequency(frequency);
                mt.setAction(action);
                mt.setCamManager(manager);
                mt.setVibro(vibro);
                mt.setSound(sound);
                mt.set_vibro(_vibro);
                mt.set_flash(_flash);
                mt.set_sound(_sound);
            es.execute(mt);
        }
        return START_STICKY; //no needs to reastart the service if the system kill it
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        manager = null;
        vibro.cancel();
        vibro = null;
        sound.release();
        sound = null;
        stopSelf(stop);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
