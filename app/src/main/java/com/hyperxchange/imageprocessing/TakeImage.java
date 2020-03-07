package com.hyperxchange.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.hyperxchange.imageprocessing.Helper.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TakeImage extends AppCompatActivity implements SurfaceHolder.Callback{

    Camera camera;
    AmazonS3 s3Client;
    TransferUtility transferUtility;
    SurfaceView mPreview;
    boolean isSafeToTakePicture = false;
    private String TAG_CLASS = TakeImage.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_image);

        /**
         * function to
         * Setting the AWS Configuration
         * Uploading the Image
         */

//        s3credentialsProvider();
//        setTransferUtility(); 



        Bundle extras = getIntent().getExtras();
        final String message = extras.getString("message");
        final Bundle bundle = new Bundle();
        bundle.putString("message", message);

        Log.d(TAG_CLASS,"message from TakeImage " + message);

        mPreview = findViewById(R.id.cameraSurface);
        mPreview.getHolder().addCallback(TakeImage.this);
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

                File hyperXchangeDir = new File(Environment.getExternalStorageDirectory()
                        + File.separator + Constants.device_folder_name );
                if (!hyperXchangeDir.exists()) {
                    hyperXchangeDir.mkdirs();
                    Log.d(TAG_CLASS,"MKDIR ");
                }
                try {
                    if (bytes != null) {
                        Log.d(TAG_CLASS,"bytes " + bytes);
                        File[]  files = hyperXchangeDir.listFiles();

                        Integer count = 0;
                        for (int i = 0; i < files.length; i++) {
                            count++;
                        }
                        Log.d(TAG_CLASS,"No of files " + count);
                        File rearImage = new File(hyperXchangeDir, message + ".jpg");
                        FileOutputStream fileOutputStream = new FileOutputStream(rearImage);
                        fileOutputStream.write(bytes);
                        Log.d(TAG_CLASS,"Write bytes done");
                        fileOutputStream.close();
                        Log.d(TAG_CLASS, "rearImage " + String.valueOf(rearImage));
                        // add pathname
                        //uploadFileToS3(rearImage, message);
                        hyperXchangeDir = null;
                        rearImage = null;
                        completeActivity(true);

                    } else {
                        isSafeToTakePicture = true;
                    }
                } catch (Exception e) {
                    Log.d(TAG_CLASS, e.toString());
                }
                isSafeToTakePicture = true;
                Toast.makeText(getApplicationContext(), "Done",Toast.LENGTH_LONG).show();
                Intent intent = null;
                intent = new Intent(TakeImage.this, MainActivity.class);
                if (intent != null)
                {
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                startActivity(intent);
                finish();
            }
        };
        Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, final Camera camera) {
                try {
                    if (isSafeToTakePicture) {
                        try {
                            camera.takePicture(null, null, pictureCallback);
                        } catch (RuntimeException e) {
                            Log.d(TAG_CLASS, e.toString());
                        }
                        isSafeToTakePicture = false;
                        //completeActivity(true);
                    } else {
                        Log.d(TAG_CLASS, "Not able to take picture.");
                    }

                } catch (RuntimeException e) {
                    Log.d(TAG_CLASS, e.toString());
                }
            }
        };
        try {
            camera = Camera.open(0);    // For back_Camera
            camera.setDisplayOrientation(90);
            camera.setPreviewCallback(previewCallback);
            isSafeToTakePicture = true;
            Log.d(TAG_CLASS, "Preview started");
            //completeActivity(true);
        } catch (Exception e) {
            Log.d(TAG_CLASS, e.toString());
        }

    }

    /**
     * AWS S3 Configuration
     **/
    private void setTransferUtility() {
        transferUtility = new TransferUtility(s3Client, getApplicationContext());
        Log.d(TAG_CLASS,"transferUtility");
    }

    private void s3credentialsProvider() {
        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider =
                new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        Constants.identity_pool_id, // Identity Pool ID
                        Constants.region // Region
                );
        Log.d(TAG_CLASS, "s3credentialsProvider");
        createAmazonS3Client(cognitoCachingCredentialsProvider);
    }

    private void createAmazonS3Client(CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider) {

        s3Client = new AmazonS3Client(cognitoCachingCredentialsProvider);

        s3Client.setRegion(Region.getRegion(Constants.region));
        Log.d(TAG_CLASS, "createAmazonS3Client");
    }

    /**
     * This method is used to upload the file to S3 by using TransferUtility class
     * @param uploadToS3
     * @param message
     */
    public void uploadFileToS3(File uploadToS3, String message){

        TransferObserver transferObserver = transferUtility.upload(
                Constants.bucket,     /* The bucket to upload to */
                message + ".jpg",    /* The key for the uploaded object */
                uploadToS3       /* The file where the data to upload exists */
        );

        Log.d(TAG_CLASS, "uploadFileToS3");
        transferObserverListener(transferObserver);
    }

    private void transferObserverListener(TransferObserver transferObserver) {
        transferObserver.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                Toast.makeText(getApplicationContext(), "State Change"
                        + state, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                Toast.makeText(getApplicationContext(), "Progress in %"
                        + percentage, Toast.LENGTH_SHORT).show();
                Log.d(TAG_CLASS, "Calling onProgess");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error");
            }

        });
        Log.d(TAG_CLASS, "transferObserverListener");
    }


    /**
     * Camera view and take Image
     * @param holder
     */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(mPreview.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        params.setPreviewSize(selected.width, selected.height);
        camera.setParameters(params);
        camera.startPreview();
        isSafeToTakePicture = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            Log.d(TAG_CLASS, "Surface Destroyed.");
        } catch (RuntimeException e) {
            Log.d(TAG_CLASS, e.toString());
        }
    }

    private void completeActivity(final boolean status) {
        if (status) {
            setResult(RESULT_OK);
        } else
            setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        try {
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
            if (mPreview != null)
                mPreview = null;
        } catch (RuntimeException e) {
            Log.d(TAG_CLASS, e.toString());
        }
        Runtime.getRuntime().gc();
    }


}
