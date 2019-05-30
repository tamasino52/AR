package org.techtown.ar;

import android.content.Context;
import android.location.Location;

import java.util.Timer;
import java.util.TimerTask;

public class DataManager {
    //모든 센서값의 데이터를 저장하는 클래스

    int roundingConst=7;
    double accX=0, accY=0, accZ=0;
    double angleXZ=0, angleYZ=0;
    double historicAngleXZ[], historicAngleYZ[];
    double roundAngleXZ=0, roundAngleYZ=0;
    double GPSlongitude=0, GPSlatitude=0;
    float GPSaccuracy=0;
    double accConst=0, GPSConst=0, GyroConst=0;
    double pitch=0, law=0, roll=0;
    double siteXZ=0, siteYZ=0;
    double sitePitch=0, siteRoll=0, siteYaw=0;
    double heading=0;
    double destGPSlongitude=0, destGPSlatitude=0;
    Boolean isDestinationExist =false;
    Location myLocation= new Location("myLocation");
    Location destLocation=new Location("Destination");

    int count=0;

    DataManager(Context context) {
        historicAngleXZ= new double[roundingConst];
        historicAngleYZ= new double[roundingConst];
    }

    public boolean isDestinationExist() {
        return isDestinationExist;
    }
    public void setDestinationGPS(double longitude, double latitude) {
        this.destGPSlatitude = latitude;
        this.destGPSlongitude = longitude;
        isDestinationExist = true;
    }

    public void setSiteRoll(double siteRoll) {
        this.siteRoll = siteRoll;
    }

    public void setSitePitch(double sitePitch) {
        this.sitePitch = sitePitch;
    }

    public void deleteDestinationGPS() {
        isDestinationExist = false;
    }

    //현위치와 목적지 사이에 거리 구하는 함수
    public double howFarFromDest() {
        myLocation.setLatitude(GPSlatitude);
        myLocation.setLongitude(GPSlongitude);
        destLocation.setLatitude(destGPSlatitude);
        destLocation.setLongitude(destGPSlongitude);
        return myLocation.distanceTo(destLocation);
    }

    public double getBearing() {
        return bearing2place(myLocation,destLocation);
    }

    //현위치와 목적지 사이의 방위각 구하는 함수
    public double bearing2place(Location myLocation, Location destLocation) {
        double P1_latitude = myLocation.getLatitude(), P1_longitude = myLocation.getLongitude();
        double P2_latitude = destLocation.getLatitude(), P2_longitude = destLocation.getLongitude();

        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에
        //라디안 각도로 변환한다.
        double Cur_Lat_radian = P1_latitude * (3.141592 / 180);
        double Cur_Lon_radian = P1_longitude * (3.141592 / 180);
        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에
        // 라디안 각도로 변환한다.
        double Dest_Lat_radian = P2_latitude * (3.141592 / 180);
        double Dest_Lon_radian = P2_longitude * (3.141592 / 180);
        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian)
                * Math.sin(Dest_Lat_radian) + Math.cos(Cur_Lat_radian)
                * Math.cos(Dest_Lat_radian)
                * Math.cos(Cur_Lon_radian - Dest_Lon_radian));
        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는
        //방향을 설정해야 한다. 라디안값이다.
        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math
                .sin(Cur_Lat_radian)
                * Math.cos(radian_distance))
                / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));
        // acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.
        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0) {
            true_bearing = radian_bearing * (180 / 3.141592);
            true_bearing = 360 - true_bearing;
        } else {
            true_bearing = radian_bearing * (180 / 3.141592);
        }
        return true_bearing;
    }


    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setSiteXZYZ(double siteXZ, double siteYZ) {
        this.siteXZ = siteXZ;
        this.siteYZ = siteYZ;
    }

    public void setSiteYaw(double Yaw) {
        this.siteYaw = Yaw;
    }


    public double getSitePitch() {
        return sitePitch;
    }
    public double getSiteRoll() {
        return siteRoll;
    }
    public double getSiteXZ() {
        return siteXZ;
    }
    public double getSiteYZ() {
        return siteYZ;
    }
    public double getAccConst() {
        return accConst;
    }
    public double getAccX() {
        return accX;
    }
    public double getAccY() {
        return accY;
    }
    public double getAccZ() {
        return accZ;
    }
    public double getAngleXZ() {
        return angleXZ;
    }
    public double getAngleYZ() {
        return angleYZ;
    }
    public double getGPSConst() {
        return GPSConst;
    }
    public double getGPSlatitude() {
        return GPSlatitude;
    }
    public double getGPSlongitude() {
        return GPSlongitude;
    }
    public double getGyroConst() {
        return GyroConst;
    }



    // 가속도센서값 roundingConst개를 취합해 가중치를 부여하여 선형 보간처리
    public double getHistoricAngleXZ() {
        double temp =0;
        for(int i=count; i<roundingConst+count; i++) {
            temp += historicAngleXZ[i%roundingConst];
        }
        return temp/roundingConst;
    }
    // 가속도센서값 roundingConst개를 취합해 가중치를 부여하여 선형 보간처리
    public double getHistoricAngleYZ() {
        double temp =0;
        for(int i=count; i<roundingConst+count; i++) {
            temp += historicAngleYZ[i%roundingConst];
        }
        return temp/roundingConst;
    }

    // 가속도 센서값의 분산
    public double getStdHistoricAngleXZ() {
        double avg = getHistoricAngleXZ();
        double temp = 0;
        for (int i = 0; i < roundingConst; i++) {
            temp += Math.abs(historicAngleXZ[i] - avg);
        }
        return temp / roundingConst;
    }
    // 가속도 센서값의 분산
    public double getStdHistoricAngleYZ() {
        double avg = getHistoricAngleYZ();
        double temp =0;
        for(int i=0; i<roundingConst; i++) {
            temp += Math.abs(historicAngleYZ[i]-avg);
        }
        return temp / roundingConst;
    }

    public double getLaw() {
        return law;
    }
    public double getPitch() {
        return pitch;
    }
    public double getRoll() {
        return roll;
    }
    public double getRoundAngleXZ() {
        return roundAngleXZ;
    }
    public double getRoundAngleYZ() {
        return roundAngleYZ;
    }
    public float getGPSaccuracy() {
        return GPSaccuracy;
    }

    public void setAccXYZ(double accX, double accY, double accZ) {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
    }

    public void setAngle(double angleXZ, double angleYZ) {
        count = ++count%roundingConst;
        this.historicAngleXZ[count] = angleXZ;
        this.historicAngleYZ[count] = angleYZ;
        this.angleXZ = getHistoricAngleXZ();
        this.angleYZ = getHistoricAngleYZ();
    }

    public void setGPS(double GPSlongitude, double GPSlatitude, float GPSaccuracy) {
        this.GPSlongitude = GPSlongitude;
        this.GPSlatitude = GPSlatitude;
        this.GPSaccuracy=GPSaccuracy;
    }

    public void setGyroPLR(double pitch, double law, double roll) {
        this.pitch=pitch;
        this.law=law;
        this.roll=roll;
    }

    public void setAccConst(double accConst) {
        this.accConst = accConst;
    }
    public void setGPSConst(double GPSConst) {
        this.GPSConst = GPSConst;
    }
    public void setGyroConst(double gyroConst) {
        GyroConst = gyroConst;
    }

}
