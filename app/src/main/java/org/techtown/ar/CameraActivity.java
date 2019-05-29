package org.techtown.ar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.os.Handler;
import android.widget.ImageView;

public class CameraActivity extends Activity {
    //AR기능을 담당하는 액티비티s
    public CameraPreview cameraPreview;
    public DataManager dataManager;
    public VisualPointer visualPointer;
    public GPSLocation gpsLocation;
    public Accelerometer accelerometer;
    public Gyroscoper gyroscoper;
    public Magnetic magnetic;

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
        dataManager.setSitePR(-1.2,0.1);
        dataManager.setAccConst((double) 2000);
        dataManager.setGyroConst((double) 1900);


        gpsLocation= new GPSLocation(this);
        accelerometer = new Accelerometer(this);
        gyroscoper = new Gyroscoper(this);
        magnetic = new Magnetic(this);


        gpsLocation.setDataManager(dataManager);
        accelerometer.setDataManager(dataManager);
        gyroscoper.setDataManager(dataManager);
        magnetic.setDataManager(dataManager);

        gpsLocation.setVisualPointer(visualPointer);
        accelerometer.setVisualPointer(visualPointer);
        gyroscoper.setVisualPointer(visualPointer);


    }
}
