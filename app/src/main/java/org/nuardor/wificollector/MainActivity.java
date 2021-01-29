package org.nuardor.wificollector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.daonn.ex_wifiscan.WifiAdapter;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {

    IntentFilter intentFilter = new IntentFilter();
    WifiManager wifiManager;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<WifiData> wifiList;

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {   // wifiManager.startScan(); 시  발동되는 메소드

            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false); // 스캔 성공 여부 값 반환
            if (success) {
                final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(300);
                startLocationService();
                scanSuccess();
                try {
                    Toast.makeText(getApplicationContext(), "GPS 측정 중 입니다.", 10000).show();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                scanFailure();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }// onReceive()..
    };

    private void scanSuccess() {    // Wifi검색 성공

        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult w = results.get(i);

            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(currentTime);

            System.out.println("BSSID:" + w.BSSID);
            writeLog(date_text + ", " + w.BSSID + ", " + w.level);
            if (w.level >= -40) {
                System.out.println("Good");
            } else if (w.level < -40 && w.level >= -85) {
                System.out.println("Middle");
            } else {
                System.out.println("Bad");
            }
            System.out.println("LEVEL:" + w.level);

        }
        mAdapter = new WifiAdapter(results);
        recyclerView.setAdapter(mAdapter);
    }

    private void scanFailure() {    // Wifi검색 실패
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv_recyclerview);

        //권한에 대한 자동 허가 요청 및 설명
        AutoPermissions.Companion.loadAllPermissions(this, 101);

        //Wifi Scan 관련
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);


    }// onCreate()..

    //버튼을 눌렀을 때
    public void clickWifiScan(View view) {
            boolean success = wifiManager.startScan();
            if (!success)
                Toast.makeText(MainActivity.this, "Wifi Scan에 실패하였습니다.", Toast.LENGTH_SHORT).show();
    }// clickWifiScan()..


    //Permission에 관한 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int i, String[] strings) {

    }

    @Override
    public void onGranted(int i, String[] strings) {

    }

    public static void writeLog(String str) {
        Date currentTime = Calendar.getInstance().getTime();
        String date_hour = new SimpleDateFormat("yyyyMMdd HH", Locale.getDefault()).format(currentTime);

        String str_Path_Full = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/log/wifi/" + date_hour + " wifi.csv";
        System.out.println("str_Path_Full:" + str_Path_Full);
        File file = new File(str_Path_Full);
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("File Create Fail : " + e);

            }
        } else {
            try {
                BufferedWriter bfw = new BufferedWriter(new FileWriter(str_Path_Full, true));
                bfw.write(str);
                int strLenth = str.length();
                if (strLenth > 0) {
                    bfw.write("\n");
                }
                bfw.flush();
                bfw.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    }

    public static void writeGps(String str) {
        Date currentTime = Calendar.getInstance().getTime();
        String date_hour = new SimpleDateFormat("yyyyMMdd HH", Locale.getDefault()).format(currentTime);

        String str_Path_Full = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/log/gps/" + date_hour + " gps.csv";
        System.out.println("str_Path_Full:" + str_Path_Full);
        File file = new File(str_Path_Full);
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("File Create Fail : " + e);

            }
        } else {
            try {
                BufferedWriter bfw = new BufferedWriter(new FileWriter(str_Path_Full, true));
                bfw.write(str);
                int strLenth = str.length();
                if (strLenth > 0) {
                    bfw.write("\n");
                }
                bfw.flush();
                bfw.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    }

    private void startLocationService() {
        // get manager instance
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // set listener
        GPSListener gpsListener = new GPSListener();
        long minTime = 30000;
        float minDistance = 10;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        //wifiManager.startScan();

        //Toast.makeText(getApplicationContext(), "Location Service started.\nyou can test using DDMS.", 1000).show();
    }

    private class GPSListener implements LocationListener {

        public void onLocationChanged(Location location) {
            //capture location data sent by current provider
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(currentTime);

            writeGps(date_text + ", " + latitude + ", " + longitude);
            final Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            Toast.makeText(getApplicationContext(), "GPS 측정을 완료하였습니다.", 2000).show();
            vibrator.vibrate(1000);
            wifiManager.startScan();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }





}