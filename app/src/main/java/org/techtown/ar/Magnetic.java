package org.techtown.ar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Display;
import android.view.WindowManager;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


public class Magnetic implements SensorEventListener {
    /** Called when the activity is first created. */
    CompassView2 compassView;
    DataManager dataManager;
    Context context;
    float[] m_acc_data = null, m_mag_data = null;
    float[] m_rotation = new float[9];
    float[] m_result_data = new float[3];
    TextView textView;
    Magnetic(Context context) {
        textView = ((Activity) context).findViewById(R.id.headingInfo);
        this.context = context;
        compassView = new CompassView2(context);
        //시스템으로부터 센서 메니저 객체 얻어오기
        SensorManager sensorM = (SensorManager)((Activity) context).getSystemService(Context.SENSOR_SERVICE);
        //센서값이 바뀔때마다 수정되야 하므로 리스너를 등록한다.
        //센서 리스너 객체(센서이벤트리스너 임플리먼츠), 센서타입, 센서 민감도를 매니져에 등록한다.
        sensorM.registerListener(this,//Activity가 직접 리스너를 구현
                sensorM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    //센서의 정확도가 바뀌었을때(호출될일 없음, 향후 업데이트를 위해서)
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //등록한 방향 센서 값이 바뀌었을 때 호출되는 콜백 메소드
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE) {

        }
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            m_result_data = event.values.clone();
        }

        // Radian 값을 Degree 값으로 변환한다.

        // 0 이하의 값인 경우 360을 더한다.
        if(m_result_data[0] < 0) m_result_data[0] += 360;

        // 첫번째 데이터인 방위값으로 문자열을 구성하여 텍스트뷰에 출력한다.
        textView.setText("azimuth(z) : " + (int)m_result_data[0]);

        //헤딩값을 View에 반영한다.
        //compassView.updateUI(heading);
    }

    public class CompassView2 extends View{
        //나침반 이미지
        Bitmap compassImg;
        //화면의 정중앙의 좌표
        int centerX, centerY;
        //나침반의 폭과 높이의 반지름(절반)
        int compassX, compassY;
        public CompassView2(Context context) {
            super(context);
            //화면의 폭과 높이를 얻어오기 위해
            WindowManager managerW = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Display display = managerW.getDefaultDisplay();
            //화면의 폭과 높이 얻어오기
            int screenW = display.getWidth();
            //getHeight하면 위에 상태바를 포함한 화면 전체의 길이를 반환한다.
            int screenH = display.getHeight();
            //중심의 좌표
            centerX = screenW/2;
            centerY = screenH/2;
            //리소스에 등록된 이미지 읽어오기
            compassImg=BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
            //이미지 크기 조정하기
            compassImg = Bitmap.createScaledBitmap(compassImg, screenW, screenW, false);
            //크기를 조정한 이미지의 크기 얻어오기
            compassX = compassImg.getWidth()/2;
            compassY = compassImg.getHeight()/2;

            //핸들러에 메세지 보내서 핸들러가 무한 루프가 돌도록 한다.
            handler.sendEmptyMessage(0);
        }
        //화면 그리는 메소드
        @Override
        protected void onDraw(Canvas canvas) {
            //캔바스 회전시키기(시계방향으로 회전시킬 360분법의 각, 회전축x, 회전축y)
            canvas.rotate( -heading, centerX, centerY);
            canvas.drawBitmap(compassImg, (centerX-compassX), (centerY-compassY), null);
        }

        //화면을 갱신하는 메소드
        int heading;
        public void updateUI(int heading){
            this.heading = heading;
        }
        //화면을 주기적으로 갱신하기 위한 Handler 객체(*자주쓰임)
        Handler handler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                // 화면을 갱신한다
                invalidate();
                // 10/1000초마다 자신의 객체에 다시 메세지를 보내서 무한 루프가 되도록 한다.
                handler.sendEmptyMessageDelayed(0, 10);

            }
        };

    }

}




