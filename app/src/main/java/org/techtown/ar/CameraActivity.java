package org.techtown.ar;

import android.app.Activity;
import android.content.Context;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;

public class CameraActivity extends Activity {
    //AR기능을 담당하는 액티비티s
    public CameraPreview cameraPreview;
    public DataManager dataManager;
    public VisualPointer visualPointer;
    public GPSLocation gpsLocation;
    public Accelerometer accelerometer;
    public Gyroscoper gyroscoper;
    TextView headingInfo;
    //일정시간마다 방위각을 기준으로 각속도 오프셋 수정
    Timer mLongPressTimer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);

        cameraPreview = new CameraPreview(this);
        ImageView imageView = (ImageView) findViewById(R.id.visualPointer);
        imageView.bringToFront();
        setAllSensor();
        doFullScreen();
        compassCreate();
        dataManager.setDestinationGPS(37.4947909, 126.9594342);

        mLongPressTimer = new Timer();
        TimerTask t = new TimerTask()
        {
            public void run()
            {
                // 메소드 호출 또는 동작 정의
                double currentPitch = dataManager.getPitch();
                double locationBearing = dataManager.getBearing();
                double machineBearing = dataManager.heading;
                if(Math.abs(machineBearing - locationBearing)<10 && Math.abs(currentPitch - dataManager.getSitePitch())>10) {
                    dataManager.setSitePitch(currentPitch);
                }


            }
        };
        mLongPressTimer.schedule(t, 0, 5000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        doFullScreen();
    }


    public void onCameraScreenTouched(View v) {
        // 2초간 멈추게 하고싶다면
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                doFullScreen();
            }
        }, 2000);
    }

    // 현재 화면을 전체화면으로 전환해주는 함수
    private void doFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void setAllSensor() {
        dataManager = new DataManager(this);
        visualPointer = new VisualPointer(this);

        visualPointer.setPointerView(R.id.visualPointer);
        visualPointer.setDataManager(dataManager);

        dataManager.setSiteXZYZ(0,50);
        dataManager.setSitePitch(-1.2);
        dataManager.setSiteRoll(0.1);
        dataManager.setAccConst((double) 2000);
        dataManager.setGyroConst((double) 1900);

        gpsLocation= new GPSLocation(this);
        accelerometer = new Accelerometer(this);
        gyroscoper = new Gyroscoper(this);

        gpsLocation.setDataManager(dataManager);
        accelerometer.setDataManager(dataManager);
        gyroscoper.setDataManager(dataManager);

        gpsLocation.setVisualPointer(visualPointer);
        accelerometer.setVisualPointer(visualPointer);
        gyroscoper.setVisualPointer(visualPointer);

    }

    // 나침반기능 코드
    private static final String TAG = "CompassActivity";

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"

    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;

    public void compassCreate() {
        sotwFormatter = new SOTWFormatter(this);
        arrowView = (ImageView) findViewById(R.id.compassHand);
        setupCompass();
        compass.start();
        sotwLabel = (TextView) findViewById(R.id.compassInfo);
    }
    private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }

    private void adjustArrow(float azimuth) {
        //Log.d(TAG, "will set rotation from " + currentAzimuth + " to "+ azimuth);
        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        currentAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }

    private void adjustSotwLabel(float azimuth) {
        sotwLabel.setText(sotwFormatter.format(azimuth));
    }

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);
                        adjustSotwLabel(azimuth);
                        dataManager.setHeading(azimuth);
                    }
                });
            }
        };
    }







}
