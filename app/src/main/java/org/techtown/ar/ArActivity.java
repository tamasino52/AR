package org.techtown.ar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

public class ArActivity extends Activity {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        //위치 값
        longitude = 126.9487;
        latitude = 37.4795;

        arSceneView = findViewById(R.id.ar_scene_view);
        //뷰, 이미지 로딩
        CompletableFuture<ViewRenderable> informLayout = ViewRenderable.builder()
                .setView(this, R.layout.layout_inform).build();
        CompletableFuture<ViewRenderable> bfView = ViewRenderable.builder()
                .setView(this, R.layout.layout_butterfly).build();
        CompletableFuture.allOf(informLayout, bfView)
                .handle((notUsed, throwable) -> {
                    if(throwable != null) {
                        DemoUtils.displayError(this, "Unable", throwable);
                        return null;
                    }
                    try {
                        informLayoutRenderable = informLayout.get();
                        butterflyRenderable = bfView.get();
                        hasFinishedLoading = true;
                    } catch (InterruptedException | ExecutionException ex) {
                        DemoUtils.displayError(this, "Unable", ex);
                    }
                    return null;
                });
        //Scene 초기화
        arSceneView.getScene().addOnUpdateListener(frameTime -> {
            if(!hasFinishedLoading) return;
            //터치하지 않고도 생성
            if(locationScene == null) {
                locationScene = new LocationScene(this, arSceneView);
                locationScene.mLocationMarkers
                        .add(new LocationMarker(longitude+0.0001, latitude, getInformView()));
                locationScene.mLocationMarkers
                        .add(new LocationMarker(longitude, latitude, getButterfly()));
            }

            Frame frame = arSceneView.getArFrame();
            if(frame == null) return;
            if(frame.getCamera().getTrackingState() != TrackingState.TRACKING) return;
            if(locationScene != null) locationScene.processFrame(frame);

            //화면 인식 후
            if(loadingMessage != null) {
                for(Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                    if(plane.getTrackingState() == TrackingState.TRACKING) {
                        hideLoadingMessage();
                    }
                }
            }
        });
        ARLocationPermissionHelper.requestPermission(this);
    }

    private void hideLoadingMessage() {
        if (loadingMessage == null) {
            return;
        }
        loadingMessage.dismiss();
        loadingMessage = null;
    }

    private Node getButterfly() {
        Node base = new Node();
        base.setRenderable(butterflyRenderable);
        Context c = this;

        View bView = butterflyRenderable.getView();
        bView.setOnTouchListener((v, event) -> {
            Toast.makeText(c, "노랑나비 : 정신적 고통으로부터의 해방, 환생", Toast.LENGTH_LONG).show();
            return false;
        });
        return base;
    }

    private Node getInformView() {
        Node base = new Node();
        base.setRenderable(informLayoutRenderable);
        Context c = this;

        View eView = informLayoutRenderable.getView();
        eView.setOnTouchListener((v, event) -> {
            Toast.makeText(c, "Example Touched", Toast.LENGTH_LONG).show();
            return false;
        });
        return base;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(locationScene != null) {
            locationScene.resume();
        }

        if(arSceneView.getSession() == null) {
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if(session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to camera", ex);
            finish();
            return;
        }

        if(arSceneView.getSession() != null) {
            showLoadingMessage();
        }
    }

    private void showLoadingMessage() {
        if (loadingMessage != null && loadingMessage.isShownOrQueued()) {
            return;
        }

        loadingMessage =
                Snackbar.make(
                        ArActivity.this.findViewById(android.R.id.content),
                        R.string.plane_finding,
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessage.getView().setBackgroundColor(0xbf323232);
        loadingMessage.show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        arSceneView.destroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }
}
