package com.quintus.labs.datingapp.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;



public class GPS implements LocationListener {
    Context mContext;
    Location mlocation;
    LocationManager mLocationManager;
    String mProvider = LocationManager.GPS_PROVIDER;
    // lấy vị trí của thiết bị
    public GPS(Context mContext) {

        this.mContext = mContext;
        // lấy thông tin vị trí
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
  // cấp quyền để lấy vị trí
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;
        }
    // lấy vị trí cuối cùng từ điểm kết nối or phát sóng wifi
        mlocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        // cập nhật vị trí cho location
        onLocationChanged(mlocation);
    }


    @Override
    public void onLocationChanged(Location location) {
        this.mlocation = mlocation;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getLocation() {
        return mlocation;
    }
// tính khoảng cách giữa 2 điểm vị trí
    public int calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // lat1-lon1: vĩ độ và kinh độ  điểm thứ 1
        //lat2-lon2: vĩ độ và kinh độ điểm thứ 2
        double theta = lon1 - lon2;
        // chuyển vĩ độ kinh độ sang radian
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        //tính khoảng cách từ cosin của khoảng cách
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        //chuyển đổi khoảng cách từ độ sang dặm Mỗi độ tương đương với khoảng 60 hải lý và mỗi hải lý tương đương với khoảng 1.1515 dặm.
        dist = dist * 60 * 1.1515;
     // làm tròn về số nguyên  và trả về
        int dis = (int) Math.floor(dist);
        if (dis < 1) {
            return 1;
        }

        return dis;
    }
   // chuyển đổi góc từ độ sang  radian
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
   // chuyển đổi từ radian sang độ
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    //ví dụ Vĩ độ và kinh độ của Điểm A:
    //
    //Vĩ độ 37.7749° → deg2rad(37.7749) ≈ 0.6593 rad
    //Kinh độ -122.4194° → deg2rad(-122.4194) ≈ -2.1355 rad
    //Vĩ độ và kinh độ của Điểm B:
    //
    //Vĩ độ 34.0522° → deg2rad(34.0522) ≈ 0.5943 rad
    //Kinh độ -118.2437° → deg2rad(-118.2437) ≈ -2.0637 rad
    // double theta = lon1 - lon2;
    //theta = -2.1355 - (-2.0637) ≈ -0.0718 rad
    //dist ≈ Math.sin(0.6593) * Math.sin(0.5943) + Math.cos(0.6593) * Math.cos(0.5943) * Math.cos(-0.0718)
    //
    //dist ≈ 0.3586 * 0.5687 + 0.9336 * 0.8020 * 0.9975
    //
    //dist ≈ 0.2036 + 0.7488
    //
    //dist ≈ 0.9524
    //dist = Math.acos(0.9524) ≈ 0.3047 rad
    //
    //dist = rad2deg(0.3047) ≈ 17.45°
    //
    //dist = 17.45 * 60 * 1.1515 ≈ 1200.42 dặm
    //dis = (int) Math.floor(1200.42) ≈ 1200
}
