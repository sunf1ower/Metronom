package com.paulb.metronom;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by Paul on 21.03.2016.
 */
public class MetroTask implements Runnable{

    private boolean action = false;
    private int frequency;
    private boolean _vibro = true;
    private boolean _flash = false;
    private boolean _sound = false;

    PendingIntent pi; //callback to activity

    //hardware things
    private CameraManager camManager;
    private String camId;
    private Vibrator vibro;
    private MediaPlayer sound;

    public CameraManager getCamManager() {
        return camManager;
    }

    public void setCamManager(CameraManager manager) {
        this.camManager = manager;
    }

    public Vibrator getVibro() {
        return vibro;
    }

    public void setVibro(Vibrator vibro) {
        this.vibro = vibro;
    }

    public MediaPlayer getSound() {
        return sound;
    }

    public void setSound(MediaPlayer sound) {
        this.sound = sound;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public boolean isAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }

    public boolean is_vibro() {
        return _vibro;
    }

    public void set_vibro(boolean vibro) {
        this._vibro = vibro;
    }

    public boolean is_flash() {
        return _flash;
    }

    public void set_flash(boolean flash) {
        this._flash = flash;
    }

    public boolean is_sound() {
        return _sound;
    }

    public void set_sound(boolean sound) {
        this._sound = sound;
    }

    //setup the task fo thread
    public MetroTask(int frequency, boolean action, CameraManager manager, Vibrator vibro,
                     MediaPlayer sound, boolean _vibro, boolean _flash, boolean _sound, PendingIntent pi) {
        setFrequency(frequency);
        setAction(action);
        setCamManager(manager);
        setVibro(vibro);
        setSound(sound);
        this.camId = getCamId(manager);
        set_vibro(_vibro);
        set_flash(_flash);
        set_sound(_sound);
        this.pi = pi;
    }

    public String getCamId(CameraManager manager) {
        try {
            return manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public void run() {
        //switch for the frequency
        boolean onOffSwitch = false;

        //doing things while action is true
        while (action) {
            try {
                if (!onOffSwitch) { //off mode
                    if (_flash) {
                        try {
                            camManager.setTorchMode(camId, false); //set the flashlight torch mode off
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    if (_vibro)
                        vibro.cancel(); //cancel the vibration
                    if (_sound)
                        if(sound.isPlaying()) { //pause sound playing
                            sound.pause();
                        }
                    try {
                        pi.send(MetroActivity.INVISIBLE); //callback to indicator in activity
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    //h.sendEmptyMessage(INVISIBLE);
                    onOffSwitch = true; //reverse the mode
                } else {
                    if (_flash) {
                        try {
                            camManager.setTorchMode(camId, true); //set the flashlight torch mode on
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    if (_vibro)
                        vibro.vibrate(frequency); //vibrate for the time of frequency
                    if (_sound) {
                        try {
                            if(sound.isPlaying()) { //play sound
                                sound.pause();
                            } else {
                                sound.start();
                            }
                        } catch (Exception e) {
                            Log.d("SOUND", "Trouble");
                        }
                    }
                    try {
                        pi.send(MetroActivity.VISIBLE); //callback to indicator in activity
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    //h.sendEmptyMessage(VISIBLE);
                    onOffSwitch = false; //reverse the mode
                }
                TimeUnit.MILLISECONDS.sleep(frequency); //sleep thread for frequency time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stopDoingThings();
        //h.sendEmptyMessage(VISIBLE);
    }

    //Cancel all things that was being enabled if the user stop the service
    private void stopDoingThings() {
        try {
            pi.send(MetroActivity.VISIBLE);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        try {
            camManager.setTorchMode(camId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        vibro.cancel();
        try {
            if(sound.isPlaying()) {
                sound.pause();
            }
        } catch (Exception e) {
            Log.d("SOUND", "Trouble");
        }
    }
}