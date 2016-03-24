package com.paulb.metronom;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.ToggleButton;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class MetroActivity_ extends Activity {

    SeekBar bpmBar;
    TextView bpmTv;
    ImageButton btnStart;
    ImageView indicator;
    ToggleButton btnVibro;
    ToggleButton btnFlash;
    ToggleButton btnSound;

    static myHandler h;

    boolean action = false;
    volatile int frequency;
    volatile boolean _vibro = true;
    volatile boolean _flash = false;
    volatile boolean _sound = false;

    final static int INVISIBLE = 0;
    final static int VISIBLE = 1;

    private static class myHandler extends Handler {
        private WeakReference<MetroActivity_> mTarget;

        myHandler(MetroActivity_ target) {
            mTarget = new WeakReference<MetroActivity_>(target);
        }

        public void setTarget(MetroActivity_ target) {
            mTarget.clear();
            mTarget = new WeakReference<MetroActivity_>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MetroActivity_ activity = mTarget.get();
            switch (msg.what) {
                case(INVISIBLE): activity.indicator.setVisibility(View.INVISIBLE);
                    break;
                case(VISIBLE): activity.indicator.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metro);

        init();

        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] camList;
        String str = null;
        try {
            camList = manager.getCameraIdList();
            str = camList[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        final String camId = str;

        final Vibrator vibro = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final MediaPlayer sound = MediaPlayer.create(this, R.raw.sound);

        btnStart.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (action) {
                    action = false;
                } else {
                    action = true;
                }

                Runnable doIt = new Runnable() {
                    @Override
                    public void run() {
                        boolean b = false;

                        while (action) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(frequency);
                                if (!b) {
                                    if (_flash) {
                                        try {
                                            manager.setTorchMode(camId, false);
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (_vibro)
                                        vibro.cancel();
                                    if (_sound)
                                        if(sound.isPlaying()) {
                                            sound.pause();
                                        }
                                    h.sendEmptyMessage(INVISIBLE);
                                    b = true;
                                } else {
                                    if (_flash) {
                                        try {
                                            manager.setTorchMode(camId, true);
                                        } catch (CameraAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (_vibro)
                                        vibro.vibrate(frequency);
                                    if (_sound) {
                                        if(sound.isPlaying()) {
                                            sound.pause();
                                        } else {
                                            sound.start();
                                        }                                    }
                                    h.sendEmptyMessage(VISIBLE);
                                    b = false;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        h.sendEmptyMessage(VISIBLE);
                    }
                };
                Thread timer = new Thread(doIt);
                if (action) {
                    btnStart.setImageResource(R.drawable.button_stop);
                    timer.start();
                } else {
                    btnStart.setImageResource(R.drawable.button_start);
                    try {
                        manager.setTorchMode(camId, false);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    if (timer != null) {
                        Thread dummy = timer;
                        timer = null;
                        dummy.interrupt();
                    }
                }
            }
        });

        bpmBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 10) {
                    progress = 10;
                    bpmBar.setProgress(10);
                }
                frequency = 60000 / progress;
                bpmTv.setText(String.valueOf(seekBar.getProgress()) + " bpm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnVibro.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    _vibro = false;
                else
                    _vibro = true;
            }
        });

        btnFlash.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    _flash = true;
                } else {
                    _flash = false;
                }
            }
        });

        btnSound.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    _sound = true;
                else
                    _sound = false;
            }
        });
    }

    private boolean isCameraSupported(PackageManager packageManager){

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }

    private boolean isFlashSupported(PackageManager packageManager){
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return true;
        }
        return false;
    }

    private void init() {
        bpmBar = (SeekBar) findViewById(R.id.seekBar);
        bpmTv = (TextView) findViewById(R.id.bpm);
        btnStart = (ImageButton) findViewById(R.id.startBtn);
        indicator = (ImageView) findViewById(R.id.indicator);
        btnVibro = (ToggleButton) findViewById(R.id.btnVibro);
        btnFlash = (ToggleButton) findViewById(R.id.btnFlash);
        btnSound = (ToggleButton) findViewById(R.id.btnSound);

        if(h == null) h = new myHandler(this);
        else h.setTarget(this);
        frequency = 600;
    }
}
