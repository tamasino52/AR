package org.techtown.ar;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;


public class GPSLocation {
    //GPS좌표를 받아와 갱신하는 클래스
    public LocationManager locationManager;
    public Location location;
    public TextView textView, distanceInfo;
    public DataManager dataManager;
    public VisualPointer visualPointer;
    final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                double longitude = location.getLongitude();    //경도
                double latitude = location.getLatitude();         //위도
                float accuracy = location.getAccuracy();        //신뢰도
                textView.setText(
                        "longitude:"+ longitude
                                +"\nlatitude:"+latitude
                                +"\naccuracy:"+accuracy
                                +"\nState:GPS"
                                + "\nMDirection: " + String.format("%.4f",
                                location.getBearing()));
                dataManager.setGPS(longitude,latitude,accuracy);
            }
            else {
                //Network 위치제공자에 의한 위치변화
                //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
                double longitude = location.getLongitude();    //경도
                double latitude = location.getLatitude();         //위도
                float accuracy = location.getAccuracy();        //신뢰도
                if(textView!=null) {
                    textView.setText("longitude:"+ longitude
                            +"\nlatitude:"+latitude
                            +"\naccuracy:"+accuracy
                            +"\nState:Network"
                            +"\nMDirection: " + String.format("%.4f",
                            location.getBearing()));
                    dataManager.setGPS(longitude,latitude,accuracy);
                }
                //남은 거리 표시
                if(distanceInfo != null) {
                    if (dataManager.isDestinationExist()) {
                        distanceInfo.setText((int)dataManager.howFarFromDest()+"m");
                    } else {
                        distanceInfo.setText("남은거리");
                    }
                }
            }
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };



    public GPSLocation(Context context) {
        textView = ((Activity) context).findViewById(R.id.GPSInfo);
        distanceInfo = ((Activity) context).findViewById(R.id.distanceInfo);
        //Permission Check part
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없을 경우 최초 권한 요청 또는 사용자에 의한 재요청 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // 권한 재요청
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            }
        }
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                500,1, mLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                500, 1, mLocationListener);



    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void setVisualPointer(VisualPointer visualPointer) {
        this.visualPointer = visualPointer;
    }
}
