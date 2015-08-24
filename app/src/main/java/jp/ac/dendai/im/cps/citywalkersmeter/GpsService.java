package jp.ac.dendai.im.cps.citywalkersmeter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class GpsService extends Service implements LocationListener {

    private NotificationManager mNotificationManager;
    private ArrayList<LogData> logs = new ArrayList<>();
    private LocationManager locationManager;
    private SharedPreferences prefs;

    private static String POST_URL = "http://citylog.cps.im.dendai.ac.jp/api/logs/update";
    private static String PARAM_USER_ID = "userId";
    private static String PARAM_LOGS = "logs";


    public GpsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //gps
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //GPSが無効になっている場合は有効にするよう促す
        final boolean gpsEnabled = locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER));
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settingsIntent);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 5, this);

        prefs = getSharedPreferences("city_walker_id", Context.MODE_PRIVATE);

        //通知押下時に、ServiceのOnStartCommandを指出すためのIntent
        Intent notificationIntent = new Intent(getApplicationContext(), jp.ac.dendai.im.cps.citywalkersmeter.MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 77, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //サービス永続化のために、通知を作成
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(pendingIntent);
        builder.setTicker("準備中");
        builder.setContentTitle("CityWalkerMeter");
        builder.setContentText("位置情報計測中...");

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.string.app_name, builder.build());

        //サービス永続化
        startForeground(R.string.app_name, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void exec_post(LogData data) {
        HttpPostHandler postHandler = new HttpPostHandler() {
            @Override
            public void onPostCompleted(String response) {
                String str = "送信成功 : " + response;
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                Log.d("posttest", response);
            }

            @Override
            public void onPostFailed(String response) {
                String str = "送信失敗 : " + response;
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                Log.d("posttest", response);
            }
        };

        HttpPostTask task = new HttpPostTask(POST_URL, postHandler);
        //ここでパラメータ付与
        String sendData = "[" + data.JSONFormat() + "]";
        task.addPostParam(PARAM_USER_ID, String.valueOf(prefs.getInt(PARAM_USER_ID, -1)));
        task.addPostParam(PARAM_LOGS, sendData);

        Log.d("posttest", sendData);
        Log.d("posttest", task.getStatus().toString());

        task.execute();
    }

    //---------GPS location-----------

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "GPS検知", Toast.LENGTH_SHORT).show();

        LogData data = LogData.newInstance(
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                (int)(location.getTime() / 1000),
                location.getSpeed(),
                location.getAltitude(),
                location.getBearing()
        );

        logs.add(data);

        if (logs.size() >= 5) {
            for (LogData item : logs) {
                exec_post(item);
            }

            Toast.makeText(getApplicationContext(), "5個たまったので送信しました", Toast.LENGTH_SHORT).show();
            logs.clear();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        /*
        switch (status) {
            case LocationProvider.AVAILABLE: {
                text += "LocatoinProvider.AVAILABLE\n";
                textView.setText(text);
                break;
            }
            case LocationProvider.OUT_OF_SERVICE: {
                text += "LocationProvider.OUT_OF_SERVICE\n";
                textView.setText(text);
                break;
            }
            case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                text += "LocationProvider.TEMPORARILY_UNAVAILABLE";
                textView.setText(text);
                break;
            }
        }
        */
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
