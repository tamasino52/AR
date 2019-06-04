package org.techtown.ar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import org.techtown.ar.*;
import org.w3c.dom.NodeList;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;


import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.util.HttpConnect;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class TmapActivity extends Activity implements TMapGpsManager.onLocationChangedCallback {
    private Context mContext = null;
    private boolean m_bTrackingMode = true;

    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    private static String mApikey = "5048eadb-2001-47be-8a5e-dde06eb463cf";
    private static int mMarkerID;

    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();
    private ArrayList<String> mArrayMakerID = new ArrayList<String>();
    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();

    public DataManager dataManager;
    public SeekBar seekBarAround;
    public Integer turnType[] = {};

    public GPSLocation gpsLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tmap);
        doFullScreen();

        //seekBar 세팅
        seekBarAround = (SeekBar) findViewById(R.id.seekBarAround);
        seekBarAround.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        changeCircleSize(tmapview,0);
                        break;
                    case 1:
                        changeCircleSize(tmapview,200);
                        break;
                    case 2:
                        changeCircleSize(tmapview,500);
                        break;
                    case 3:
                        changeCircleSize(tmapview,1000);
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        mContext = this;
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mapView);
        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey(mApikey);

        tmapview.setCompassMode(false);
        tmapview.setIconVisibility(true);

        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapgps = new TMapGpsManager(TmapActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);

        tmapgps.OpenGps();
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        gpsLocation = new GPSLocation(mContext);

        addPoint();
        showMarkerPoint();


        tmapview.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                // 마커를 클릭시 도착경로로 지정 tmapgps 통해서 내 좌표 받아오기
                TMapPoint myPoint = new TMapPoint(tmapgps.getLocation().getLatitude(), tmapgps.getLocation().getLongitude());
                for (TMapMarkerItem item : arrayList) {
                    try {
                        TMapPoint searchPoint = item.getTMapPoint();
                        searchRoute(myPoint, searchPoint);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),"ERROR URL", Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });


        //TMapPoint myPoint = new TMapPoint(tmapview.getLatitude(), tmapview.getLongitude());
        TMapPoint myPoint = new TMapPoint(gpsLocation.location.getLatitude(), gpsLocation.location.getLongitude());
        TMapPoint searchPoint = new TMapPoint(37.512159, 126.925482);//경로 탐색 실행
        showTurnType(myPoint, searchPoint);
      //  searchRoute(myPoint,searchPoint);

    }


    //Tmap API를 주로 다루게 될 API
    @Override
    public void onLocationChange(Location location) //위치가 변했을 때
    {
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
            drawCirclePoint(tmapview,new TMapPoint(location.getLatitude(),location.getLongitude()),300);
        }
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    //화면을 터치하면 소프트키가 2초간 등장했다가 다시 사라짐
    public void onMapScreenTouched(View v) {
        doFullScreen();
    }

    //내 위치 버튼 눌렀을 때 실행
    public void onMyLocationButtonClicked(View v) {
        tmapview.setCenterPoint(tmapgps.getLocation().getLongitude(),tmapgps.getLocation().getLatitude());
        tmapview.invalidate();
    }

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





    // 뷰, 위치, 원의 크기를 입력하면 해당 위치에 원을 그려줌
    public void drawCirclePoint(TMapView tMapView, TMapPoint myPoint, int CircleSize) {
        if(tMapView.getCircleFromID("myCircle")==null) {
            TMapCircle tMapCircle = new TMapCircle();
            tMapCircle.setCenterPoint( myPoint );
            tMapCircle.setRadius(CircleSize);
            tMapCircle.setCircleWidth(2);
            tMapCircle.setLineColor(Color.BLUE);
            tMapCircle.setAreaColor(Color.GRAY);
            tMapCircle.setAreaAlpha(100);
            tMapView.addTMapCircle("myCircle", tMapCircle);
        }
        else {
            TMapCircle tMapCircle = tMapView.getCircleFromID("myCircle");
            tMapCircle.setCenterPoint(myPoint);
        }
        tmapview.invalidate();
    }

    // 뷰, 원의 크기 입력시 원 사이즈 변경
    public void changeCircleSize(TMapView tMapView, int CircleSize) {
        if(tMapView.getCircleFromID("myCircle")==null) {}
        else {
            TMapCircle tMapCircle = tMapView.getCircleFromID("myCircle");
            tMapCircle.setRadius(CircleSize);
        }
        tmapview.invalidate();
    }


    public void addPoint() //m_mapPoint List에 출력하고자 하는 위치들 추가
    {
        m_mapPoint.add(new MapPoint("대방", 37.512159, 126.925482));
        m_mapPoint.add(new MapPoint("서초구 소녀상", 37.489611957223715, 127.0053777974778));
        m_mapPoint.add(new MapPoint("강남", 37.510350, 127.066847));
        m_mapPoint.add(new MapPoint("부천 안중근 공원", 37.504412, 126.759060));
        m_mapPoint.add(new MapPoint("창의관", 37.4947909, 126.9594342));
    }

    public void showTurnType(final TMapPoint startPoint, final TMapPoint endPoint) //URL을 통해 받아온 xml 문서를 파싱하여
    // 경로 안내 문구 + turn type 상수을 log로 출력
    {
        (new Thread(){
            public void run(){
                try {
                    Document e = null;
                    StringBuilder uri = new StringBuilder();  //URL 앞부분을 저장한 String builder 선언
                    uri.append("https://api2.sktelecom.com/tmap/");
                    uri.append("routes/pedestrian?version=1");
                    StringBuilder content = new StringBuilder(); //URL content에 parameter로 받아온 값들 입력
                    content.append("reqCoordType=WGS84GEO&resCoordType=WGS84GEO&format=xml");
                    content.append("&startY=").append(startPoint.getLatitude());
                    content.append("&startX=").append(startPoint.getLongitude());
                    content.append("&endY=").append(endPoint.getLatitude());
                    content.append("&endX=").append(endPoint.getLongitude());
                    content.append("&startName=").append(URLEncoder.encode("출발지", "UTF-8"));
                    content.append("&endName=").append(URLEncoder.encode("도착지", "UTF-8"));

                    StringBuilder StringEx1; //url header 부분과 api key를 저장할 string builder 선언
                    StringEx1 = new StringBuilder();
                    StringEx1.append(uri.toString());
                    StringEx1.append("&appKey=").append("5048eadb-2001-47be-8a5e-dde06eb463cf");
                    URLConnection con = HttpConnect.postHttps(StringEx1.toString(), content.toString(), false); //url header와 content를 결합

                    try {
                        //HttpURLConnection ez = (HttpURLConnection) con;
                        e = HttpConnect.getDocument(con);
                    } catch (Exception ezx) {
                        Log.i("error", "에러 발생");
                    }

                    if (e != null) {
                        NodeList list = e.getElementsByTagName("Placemark");

                        for(int i = 0 ; i<list.getLength(); i++)
                        {
                            NodeList placemarkList = list.item(i).getChildNodes();
                            Integer turn;
                            for(int j = 0 ; j < placemarkList.getLength() ; j ++)
                            {
                                if (placemarkList.item(j).getNodeName().equals("description")) {
                                    Log.i("debug", placemarkList.item(j).getTextContent().trim());
                                }
                                if(placemarkList.item(j).getNodeName().equals("tmap:turnType")) {
                                    turn = Integer.parseInt(placemarkList.item(j).getTextContent());
                                    Log.i("turn type","turn type : "+turn);
                                    //turnType[j] = turn;
                                }
                            }
                        }
                    }
                  /*  for(int i =0 ; i< turnType.length ; i++)
                    {
                        Log.i("String turn type", "turn type : "+ turnType[i]);
                    } */
                }catch(Exception e ) {
                    Log.i("error발생", "error");
                }
            }
        }).start();
    }


    public void searchRoute(TMapPoint start, TMapPoint end) { //TMap 위에 경로를 탐색한 경로 Poly line 그리는 함수
        try {
            TMapData tMapData = new TMapData();
            tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start, end, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                    tmapview.addTMapPath(tMapPolyLine);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMarkerPoint() //배열에 저장된 Point들의 위치를 지도 위에 찍어주는 함수
    {
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.statue_place); //Marker 위에 표현할 bitmap 이미지 지정
        bitmap = resizeBitmap(bitmap,250);
        for (int i = 0; i < m_mapPoint.size(); i++) {
            TMapPoint point = new TMapPoint(m_mapPoint.get(i).getLatitude(), m_mapPoint.get(i).getLongitude());
            TMapMarkerItem markerItem1 = new TMapMarkerItem();
            markerItem1.setIcon(bitmap);
            markerItem1.setTMapPoint(point);
            tmapview.addMarkerItem("markerItem" + i, markerItem1);
            markerItem1.setVisible(TMapMarkerItem.VISIBLE);
            markerItem1.setCanShowCallout(true);
            markerItem1.setCalloutTitle(m_mapPoint.get(i).getName());
        }
    }

    public void deleteMarkerPoint() //지도 위에 표시된 모든 marker를 삭제하는 함수
    {
        tmapview.removeAllMarkerItem();
    }

    //이미지 리사이즈, resizeBitmap(원본비트맵, 변경할 사이즈의 Width)
    static public Bitmap resizeBitmap(Bitmap original, int resizeWidth) {

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        int targetHeight = (int) (resizeWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, targetHeight, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }

}



