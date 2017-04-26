package com.baecon.rockpaperscissorsapp.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baecon.rockpaperscissorsapp.R;
import com.baecon.rockpaperscissorsapp.model.Game;
import com.baecon.rockpaperscissorsapp.rest.ApiClient;
import com.baecon.rockpaperscissorsapp.rest.ApiInterface;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.io.IOException;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Response;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProximityManager proximityManager;
    private static final String APIKEY = "ok";


    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = getSharedPreferences("userstats", MODE_PRIVATE);

        TextView history = (TextView) findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
               startActivity(historyIntent);
           }
       });

        TextView options = (TextView) findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent optionsIntent = new Intent(MainActivity.this, OptionActivity.class);
                startActivity(optionsIntent);
            }
        });

        KontaktSDK.initialize(APIKEY);

        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());


    }

    @Override
    protected void onStart() {
        super.onStart();
        startScanning();
    }

    @Override
    protected void onStop() {
        proximityManager.stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {

                if (isValidBeacon(String.valueOf(ibeacon.getProximityUUID())) ) {
                    //TODO starte spiel
                    // speeichere beaconid fürs backend in shared prefs
                    NotificationCompat.Builder mBuilder =
                            (NotificationCompat.Builder) new NotificationCompat.Builder(MainActivity.this)
                            .setSmallIcon(R.drawable.battleicon)
                            .setContentTitle("Yo")
                            .setContentText("wanna fight?");
                    Intent notificationIntent = new Intent(MainActivity.this, GameActivity.class);
                    PendingIntent notificaitonPendingIntent =
                            PendingIntent.getActivity(
                                    MainActivity.this,
                                    0,
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                    mBuilder.setContentIntent(notificaitonPendingIntent);

                    int mNotificationId = 001;
                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());

                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(R.drawable.battleicon)
                            .setMessage("iBeacon " + ibeacon.getUniqueId() + " wurde gefunden.")
                            .setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                                    startActivity(gameIntent);
                                }
                            })
                            .setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        };
    }

    private boolean isValidBeacon(String beaconID){
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<String> call = apiService.isValidBeacon(beaconID);
//        try {
//            Log.d(TAG,"returning True for UUID: " + beaconID);
//            Response<String> isValid = call.execute();
//            Log.d(TAG,"" + isValid.message());
//            Log.d(TAG,"Api says: " + isValid.body().toString());
            return TRUE;
//            return Boolean.valueOf(call.execute().body());
//        } catch (IOException e) {
//            Log.d(TAG,"returning False for UUID: " + beaconID);
//            return FALSE;
//        }
    }

}
