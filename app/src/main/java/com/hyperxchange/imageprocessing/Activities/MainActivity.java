package com.hyperxchange.imageprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hyperxchange.imageprocessing.Helper.Constants;
import com.hyperxchange.imageprocessing.Services.UDP;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    TextView message, title;
    private static boolean RUN_ONCE = true;
    public  static final int RequestPermissionCode  = 1 ;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BroadcastReceiver mHadler;
 //       Bundle extras = getIntent().getExtras();
//        String misg = extras.getString("message");
        setContentView(R.layout.activity_main);

        message = (TextView)this.findViewById(R.id.message);
        title = (TextView)this.findViewById(R.id.title);


        // Crash -- //

        if (!RUN_ONCE)
        {
            Bundle extras = getIntent().getExtras();
            String msg = extras.getString("message");

            message.setText(msg);

//            message.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    message.setVisibility(View.INVISIBLE);
//                }
//            }, 5000);
        }

        runOnce();

        boolean  INCheck = isNetworkAvailable();

        if (!INCheck)
        {
            Intent intent = new Intent(MainActivity.this, UDP.class);
            startActivity(intent);
            finish();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void runOnce() {
        if (RUN_ONCE) {
            RUN_ONCE = false;
            EnableRuntimePermission();
            requestPermission();
            subscribeFCMTopic();
        }
    }

    private void subscribeFCMTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Failed";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void EnableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA))
        {

            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
}
