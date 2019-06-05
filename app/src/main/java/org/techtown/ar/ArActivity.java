package org.techtown.ar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;

public class ArActivity extends AppCompatActivity {
    private ArSceneView arSceneView;
    private LocationScene locationScene;
    private ViewRenderable informLayoutRenderable;
    private ViewRenderable butterflyRenderable;
    private boolean hasFinishedLoading = false;
    private boolean installRequested;
    private Snackbar loadingMessage = null;

    public double longitude;
    public double latitude;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        //위치 값
        longitude = 126.9487;
        latitude = 37.4795;

        arSceneView = findViewById(R.id.ar_scene_view);
        //뷰, 이미지 설정
        CompletableFuture<ViewRenderable> informLayout = ViewRenderable.builder()
                .setView(this, R.layout.layout_inform).build();
        CompletableFuture<ViewRenderable> bfView = ViewRenderable.builder()
                .setView(this, R.layout.layout_butterfly).build();
        
    }
}
