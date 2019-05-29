package org.techtown.ar;
import android.app.Activity;
import android.content.Context;
import android.view.Surface;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class VisualPointer {
    // DataManger 함수로부터 센서데이터를 받아서 화면상에 AR 이미지를 띄우주는 클래스

    DataManager dataManager;
    Context context;
    ImageView imageView;
    int centerX,centerY;

    VisualPointer(Context context) {
        this.context = context;
        setPointerView(R.id.visualPointer);
        centerX=700;
        centerY=1200;
    }

    // 초기에 이 함수를 통해서 데이터매니저 클래스와 연결시켜주어야함
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    // AR로 띄워질 이미지의 ID를 입력받고 인자로 등록하는 함수
    public void setPointerView(int viewId) {
        imageView = (ImageView) ((Activity) context).findViewById(viewId);
    }
    // 데이터매니저로부터 값을 받아 시야각을 계산하는 함수
    public void pointToSite() {
        double dXZ = dataManager.getGyroConst()*Math.sin(Math.PI*(dataManager.getPitch()-dataManager.getSitePitch())/180);
        double dYZ = dataManager.getAccConst()*Math.sin(Math.PI*(dataManager.getAngleYZ()-dataManager.getSiteYZ())/180);
        movePointOffset(dXZ,dYZ);
    }
    // 계산된 시야각에 따라서 실제 이미지의 위치를 변경하는 함수
    public void movePointOffset(double X, double Y) {
        if (X>0) {
            imageView.setX((float) X+centerX);
        } else {
            imageView.setX((float) X+centerX);
        }

        if (Y>0) {
            imageView.setY((float) Y+centerY);
        } else {
            imageView.setY((float) Y+centerY);
        }
    }


}
