package com.paulb.metronom;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Paul
 *
 * This class is for the main activity
 */

public class MetroActivity extends Activity {

    /**
     * Create variable for all view in the activity
     */
    SeekBar bpmBar;
    ImageButton bpmMinus;
    ImageButton bpmPlus;
    TextView bpmTv;
    ImageButton btnStart;
    ImageView indicator;
    ToggleButton btnVibro;
    ToggleButton btnFlash;
    ToggleButton btnSound;

    /**
     * Service binding and create intents for it
     */
    MetroService metroService;
    ServiceConnection sCon;
    boolean bound = false;
    PendingIntent pi;
    Intent serviceIntent;

    boolean action = true; //describes the mode for the application if #true main task is running
    public int frequency; //frequency for the main task

    public final static String PARAM_PINTENT = "pendingIntent"; //callback to the activity from service
    public final static int VISIBLE = 1; //state for #indicator
    public final static int INVISIBLE = 0; //state for #indicator

    @Override
    protected void onPause() {
        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) //support for Lollipop with flashligth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metro);

        init(); //Initialize all views in main activity
        Intent i = new Intent("empty");
        pi = createPendingResult(1, i, 0); //create callback for activity

        //Establish service connection
        sCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // get instance of binding service
                metroService = ((MetroService.MyBinder) service).getService();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };

        bpmBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Setup frequency and update #seekBar
                if (progress < 10) {
                    progress = 10;
                    bpmBar.setProgress(10);
                }
                frequency = 60000 / progress;
                if (metroService != null)
                    metroService.mt.setFrequency(frequency);
                bpmTv.setText(String.valueOf(seekBar.getProgress()) + " bpm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void onClickStart(View v) {
        //Start/stop, bind/unbind the service
        if (action) {
            btnStart.setImageResource(R.drawable.button_stop);
            initServiceIntent();
            bindService(serviceIntent, sCon, 0);
            startService(serviceIntent);
            action = false;
        } else {
            btnStart.setImageResource(R.drawable.button_start);
            if (bound) {
                unbindService(sCon);
                bound = false;
                stopService(serviceIntent);
                metroService.mt.setAction(false);
                action = true;
            }
        }
    }

    /**
     * Create main Intent for start the service
     */
    private void initServiceIntent() {

        serviceIntent = new Intent(getApplicationContext(), MetroService.class).putExtra("action", action)
                .putExtra("frequency", frequency);
        if (btnVibro.isChecked())
            serviceIntent.putExtra("_vibro", true);
        else serviceIntent.putExtra("_vibro", false);
        if (btnFlash.isChecked())
            serviceIntent.putExtra("_flash", true);
        else serviceIntent.putExtra("_flash", false);
        if (btnSound.isChecked())
            serviceIntent.putExtra("_sound", true);
        else serviceIntent.putExtra("_sound", false);
        serviceIntent.putExtra(PARAM_PINTENT, pi);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Callback to service and update the indicator in main activity
        if (resultCode == 0)
            indicator.setVisibility(View.INVISIBLE);
        else
            indicator.setVisibility(View.VISIBLE);
    }

    /**
     * ToggleBar buttons. Switch mode.
     */
    View.OnClickListener tbListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (metroService != null) {
                int fr;
                switch (v.getId()) {
                    case R.id.btnVibro:
                        if (btnVibro.isChecked()) {
                            metroService.mt.set_vibro(true);
                        } else {
                            metroService.mt.set_vibro(false);
                        }
                        break;
                    case R.id.btnFlash:
                        if (btnFlash.isChecked()) {
                            metroService.mt.set_flash(true);
                        } else {
                            metroService.mt.set_flash(false);
                        }
                        break;
                    case R.id.btnSound:
                        if (btnSound.isChecked()) {
                            metroService.mt.set_sound(true);
                        } else {
                            metroService.mt.set_sound(false);
                        }
                        break;
                    case R.id.btnMinus:
                        fr = bpmBar.getProgress();
                        bpmBar.setProgress(fr-10);
                        fr = metroService.mt.getFrequency();
                        metroService.mt.setFrequency(fr-10);
                        break;
                    case R.id.btnPlus:
                        fr = bpmBar.getProgress();
                        bpmBar.setProgress(fr+10);
                        fr = metroService.mt.getFrequency();
                        metroService.mt.setFrequency(fr+10);
                        break;
                }
            }
        }
    };

    private boolean isCameraSupported(PackageManager packageManager) {

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }

    private boolean isFlashSupported(PackageManager packageManager) {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return true;
        }
        return false;
    }

    /**
     * Initializer
     */
    private void init() {
        bpmBar = (SeekBar) findViewById(R.id.seekBar);
        bpmMinus = (ImageButton) findViewById(R.id.btnMinus);
        bpmMinus.setOnClickListener(tbListener);
        bpmPlus = (ImageButton) findViewById(R.id.btnPlus);
        bpmPlus.setOnClickListener(tbListener);
        bpmTv = (TextView) findViewById(R.id.bpm);
        btnStart = (ImageButton) findViewById(R.id.startBtn);
        indicator = (ImageView) findViewById(R.id.indicator);
        btnVibro = (ToggleButton) findViewById(R.id.btnVibro);
        btnVibro.setOnClickListener(tbListener);
        btnFlash = (ToggleButton) findViewById(R.id.btnFlash);
        btnFlash.setOnClickListener(tbListener);
        btnSound = (ToggleButton) findViewById(R.id.btnSound);
        btnSound.setOnClickListener(tbListener);
        frequency = 600;
    }
}
