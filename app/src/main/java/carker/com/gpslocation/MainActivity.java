package carker.com.gpslocation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * 定位优先使用GPS定位，若三秒未返回结果，则使用GPRS进行定位。
 * 后续改进：
 * 若gps有值，则根据GPS与GPRS值进行比较，比较方法来自:
 */
public class MainActivity extends Activity {
    private TextView positionTextView;
    private LocationManager locationManager;
    private String provider;
    ProgressDialog pd;
    private boolean first = true;

    private final int FAILE_MESSAGE = 1;
    private final int COUNT = 2;
    private int count=0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COUNT:
                    Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(COUNT);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        positionTextView = (TextView) findViewById(R.id.textView);
        locationManager = (LocationManager) getSystemService(Context.
                LOCATION_SERVICE);
        showProgressDialog();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(first) {
            init();
            first = false;
        }
    }

    private void init() {
        // 获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
// 当没有可用的位置提供器时,弹出Toast提示用户
            Toast.makeText(this, R.string.open_location_service,
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);

            } catch (ActivityNotFoundException ex) {
                intent.setAction(Settings.ACTION_SETTINGS);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
// 显示当前设备的位置信息
            showLocation(location);
        }
        locationManager.requestLocationUpdates(provider, 2000, 1,
                locationListener);
        //利用
        handler.postDelayed(runnable, 1000);

    }

    protected void onDestroy() {
        Log.i("tag", "onDestroy()");
        super.onDestroy();
        if (locationManager != null) {
// 关闭程序时将监听器移除
            locationManager.removeUpdates(locationListener);
        }
        if (pd != null) {
            pd.dismiss();
        }
        if(handler!=null) {
            handler.removeCallbacks(runnable);
        }
    }

    LocationListener locationListener = new LocationListener() {

        @Override

        public void onStatusChanged(String provider, int status, Bundle
                extras) {
            Log.i("tag", "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {

            Log.i("tag", "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("tag", "onProviderDisabled");
        }

        @Override
        public void onLocationChanged(Location location) {
// 更新当前设备的位置信息
            showLocation(location);
        }
    };

    private void showLocation(Location location) {
        String currentPosition = "latitude is " + location.getLatitude() + "\n"
                + "longitude is " + location.getLongitude();
        positionTextView.setText(currentPosition);
        pd.dismiss();
    }

    private void showProgressDialog() {
        if (pd == null)
            pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("正在查询，请稍后...");
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.show();
        handler.removeCallbacks(runnable);
    }

}