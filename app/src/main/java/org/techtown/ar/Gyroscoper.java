package org.techtown.ar;

//sensor part
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.ContactsContract;
import android.widget.TextView;
import android.hardware.GeomagneticField;


public class Gyroscoper {
    // 각속도계 센서로부터 값을 받아와 갱신하는 클래스

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;

    //Using the Gyroscope
    private SensorEventListener mGyroLis;
    private Sensor mGgyroSensor = null;

    //Roll and Pitch
    private double pitch;
    private double roll;
    private double yaw;

    //timestamp and dt
    private double timestamp;
    private double dt;

    // for radian -> dgree
    private double RAD2DGR = 180 / Math.PI;
    private static final float NS2S = 1.0f/1000000000.0f;

    public TextView textView;
    DataManager dataManager;
    VisualPointer visualPointer;


    Gyroscoper(Context context) {
        textView= ((Activity) context).findViewById(R.id.GyroInfo);
        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mGgyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGyroLis = new GyroscopeListener();
        mSensorManager.registerListener(mGyroLis, mGgyroSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager=dataManager;
    }

    public void setVisualPointer(VisualPointer visualPointer) {
        this.visualPointer = visualPointer;
    }

    private class GyroscopeListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            /* 각 축의 각속도 성분을 받는다. */
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

            /* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(dt)을 구한다.
             * dt : 센서가 현재 상태를 감지하는 시간 간격
             * NS2S : nano second -> second */
            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            /* 맨 센서 인식을 활성화 하여 처음 timestamp가 0일때는 dt값이 올바르지 않으므로 넘어간다. */
            if (dt - timestamp*NS2S != 0) {

                /* 각속도 성분을 적분 -> 회전각(pitch, roll)으로 변환.
                 * 여기까지의 pitch, roll의 단위는 '라디안'이다.
                 * SO 아래 로그 출력부분에서 멤버변수 'RAD2DGR'를 곱해주어 degree로 변환해줌.  */
                pitch = pitch + gyroY*dt;
                roll = roll + gyroX*dt;
                yaw = yaw + gyroZ*dt;


                dataManager.setGyroPLR(pitch*RAD2DGR,yaw*RAD2DGR,roll*RAD2DGR);
                visualPointer.pointToSite();

                //디버그 콘솔에 세부 센서값 출력
                textView.setText("LOG GYROSCOPE\n[X]:" + String.format("%.4f", event.values[0])
                        + "\n[Y]:" + String.format("%.4f", event.values[1])
                        + "\n[Z]:" + String.format("%.4f", event.values[2])
                        + "\n[Pitch]: " + String.format("%.1f", pitch)
                        + "\n[Roll]: " + String.format("%.1f", roll)
                        + "\n[Yaw]: " + String.format("%.1f", yaw)
                        + "\n[dt]: " + String.format("%.4f", dt)
                        + "\n[constG]: " + String.format("%.2f", dataManager.getGyroConst())
                );
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


}
