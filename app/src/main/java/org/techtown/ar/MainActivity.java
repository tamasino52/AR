package org.techtown.ar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        doFullScreen();
    }


    // TMap 버튼을 클릭했을 때 실행되는 이벤트 함수
    public void onTmapButtonClicked(View v) {
        // 현재 보여지는 액티비티를 Tmap 액티비티로 교체
        Intent intent = new Intent(getApplicationContext(),TmapActivity.class);
        startActivity(intent);
    }

    // 카메라 버튼을 클릭했을 때 실행되는 이벤트 함수
    public void onCameraButtonClicked(View v) {
        // 현재 보여지는 액티비티를 카메라 액티비티로 교체
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        startActivity(intent);
    }

    // 전체화면 버튼을 클릭했을 때 실행되는 이벤트 함수
    public void onFullscreenButtonClicked(View v) {
        doFullScreen();
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
}
