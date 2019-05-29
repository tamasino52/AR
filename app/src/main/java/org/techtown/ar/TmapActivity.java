package org.techtown.ar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.NodeList;

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

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
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

    //Tmap API를 주로 다루게 될 API
    @Override
    public void onLocationChange(Location location) //위치가 변했을 때
    {
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tmap);

        mContext = this;
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mapView);
        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey(mApikey);

        tmapview.setCompassMode(true);
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

        TextView textView1 = (TextView) findViewById(R.id.textView1);


        addPoint();
        showMarkerPoint();

        TMapPoint myPoint = new TMapPoint(tmapview.getLatitude(), tmapview.getLongitude());
        TMapPoint searchPoint = new TMapPoint(37.512159, 126.925482);//경로 탐색 실행
        searchRoute(searchPoint);
        //getXml(myPoint, searchPoint);
        showTurnType(myPoint, searchPoint);

        deleteMarkerPoint();
    }

    public void addPoint() //m_mapPoint List에 출력하고자 하는 위치들 추가
    {
        m_mapPoint.add(new MapPoint("대방", 37.512159, 126.925482));
        m_mapPoint.add(new MapPoint("서초구 소녀상", 37.489611957223715, 127.0053777974778));
        m_mapPoint.add(new MapPoint("강남", 37.510350, 127.066847));
        m_mapPoint.add(new MapPoint("부천 안중근 공원", 37.504412, 126.759060));
    }


/*    public void getXml(TMapPoint startPoint, TMapPoint endPoint) { //XML 문서를 함수로 직접 받아와 파싱(실패)
        new TMapData().findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, startPoint, endPoint, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document doc) {
                Element root = doc.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
                for (int i = 0; i < nodeListPlacemark.getLength(); i++) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                    for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                        if (nodeListPlacemarkItem.item(j).getNodeName().equals("description")) {
                            Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim());
                        }
                    }
                }
            }
        });
    } */



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
                                }
                            }
                        }
                    }
                }catch(Exception e ) {
                    Log.i("error발생", "error");
                }
            }
        }).start();
    }


    public void searchRoute(TMapPoint end) { //TMap 위에 경로를 탐색한 경로 Poly line 그리는 함수
        TMapPoint start = new TMapPoint(tmapview.getLatitude(), tmapview.getLongitude());
        try {
            new TMapData().findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start, end, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine tMapPolyLine) {
                    tmapview.addTMapPath(tMapPolyLine);
                }
            });
        }
        //tMapPolyLine.setLineColor(Color.BLUE);
        //  tMapPolyLine.setLineWidth(2);
        //   tmapview.addTMapPolyLine("line", tMapPolyLine);
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMarkerPoint() //배열에 저장된 Point들의 위치를 지도 위에 찍어주는 함수
    {
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.arrow); //Marker 위에 표현할 bitmap 이미지 지정

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
}



