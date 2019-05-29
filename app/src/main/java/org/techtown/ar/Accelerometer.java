package org.techtown.ar;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Accelerometer {
    // 가속도계 센서의 값을 읽어오고 그것을 DataManager 클래스에 저장하는 클래스

    //Using Accelerometer & Gyroscoper
    public SensorManager mSensorManager = null;

    //Using Accelerometer
    public SensorEventListener mAccLis;
    public Sensor mAccelorometerSensor = null;


    TextView outputConsole;
    DataManager dataManager;

    double accX;
    double accY;
    double accZ;

    double angleXZ;
    double angleYZ;
    VisualPointer visualPointer;
    Context context;

    Accelerometer(Context context) {
        this.context=context;
        outputConsole=((Activity) context).findViewById(R.id.accInfo);
        //Using Accelerometer & Gyroscoper
        mSensorManager = (SensorManager) ((Activity) context).getSystemService(Context.SENSOR_SERVICE);
        //Using Accelerometer
        mAccelorometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccLis = new AccelerometerListener();

        //Listener for Accelerometer
        mSensorManager.registerListener(mAccLis,mAccelorometerSensor,SensorManager.SENSOR_DELAY_UI);
    }


    public class AccelerometerListener implements SensorEventListener {
        //센서값이 변할 때 마다 DataManger의 인자를 갱신
        @Override
        public void onSensorChanged(SensorEvent event) {
            accX = event.values[0];
            accY = event.values[1];
            accZ = event.values[2];

            angleXZ = Math.atan2(accX,accZ) * 180/Math.PI;
            angleYZ = Math.atan2(accY,accZ) * 180/Math.PI;
            //디버그 콘솔에서 센서 값을 알 수 있도록 설정
            dataManager.setAccXYZ(accX,accY,accZ);
            dataManager.setAngle(angleXZ,angleYZ);

            //디버그 콘솔에 세부 센서값 출력
            outputConsole.setText("ACCELEROMETER\n[X]:" + String.format("%.4f", dataManager.getAccX())
                    + "\n[Y]:" + String.format("%.4f", dataManager.getAccY())
                    + "\n[Z]:" + String.format("%.4f", dataManager.getAccZ())
                    + "\n[angleXZ]: " + String.format("%.1f", dataManager.getAngleXZ())
                    + "\n[angleYZ]: " + String.format("%.1f", dataManager.getAngleYZ())
                    + "\n[constD]: " + String.format("%.1f", dataManager.getAccConst())
            );
            visualPointer.pointToSite();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager=dataManager;
    }
    public void setVisualPointer(VisualPointer visualPointer) {
        this.visualPointer=visualPointer;
    }



}
