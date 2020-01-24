package in.cashify.scanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;


import in.cashify.scanner.common.AppConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

import android.hardware.Camera;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private ImageView scannedImageView;


    Semaphore mCameraOpenCloseLock = new Semaphore(1);


    Camera mCamera;
    Boolean isRequestedForPermission;
    SurfaceView mSurfacePreview;
    Camera.CameraInfo mCameraInfo;
    SurfaceHolder holder;
    HandlerThread mBackgroundThread;
    Handler mBackgroundHandler;
    int rotation;
    Bitmap bitmap;
    ImageView button;


    Boolean isSurfaceCreated = false;
    SurfaceHolder.Callback cameraCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            isSurfaceCreated = true;
            initiateCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isSurfaceCreated = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);
        scannedImageView.setVisibility(View.VISIBLE);

        startActivityForResult(new Intent(this, ChooseActivity.class),AppConstant.Companion.getREQUEST_CODE());
//        Intent intent = new Intent(this, ScanActivity.class);
//        Bundle bundle = new Bundle();
//        intent.putExtra(ScanConstants.IS_GALLERY , getIntent().getBooleanExtra(ScanConstants.IS_GALLERY , false));
//        bundle.putParcelable(ScanConstants.SELECTED_BITMAP, getIntent().getExtras().getParcelable(ScanConstants.SELECTED_BITMAP));
//        intent.putExtras(bundle);
//        startActivityForResult(intent, REQUEST_CODE);
    }


    void init() {



        mSurfacePreview = findViewById(R.id.cameraPreview);
        holder = mSurfacePreview.getHolder();
        holder.addCallback(cameraCallback);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture(v);
            }
        });
    }

    private void initiateCamera() {

        if (this == null) {
            return;
        }

        if (!hasPermission(Manifest.permission.CAMERA)) {
            requestCameraPermission();
            isRequestedForPermission = true;
            return;
        } else {

            openCamera2(holder);

        }


//        else if (!checkOp(activity!!, OP_CAMERA)) {
//            if (!isRequestedForPermission) {
//                showOperationAlert()
//                isRequestedForPermission = true
//                return
//            }
//        }
//        if (!checkCameraHardware(activity)) {
//            startAnimation(mCircleRoadProgress, resultDelayInMillis, this)
//            mDiagnoseResult = DiagnoseResult(requestKey, DiagnoseFragment.RESULT_FEATURE_NOT_SUPPORTED, false)
//        } else if (null == mSurfacePreview) {
//            startAnimation(mCircleRoadProgress, resultDelayInMillis, this)
//            mDiagnoseResult = DiagnoseResult(requestKey, DiagnoseFragment.RESULT_INITIALIZATION_ERROR, false)
//
//        } else {
//
//            if (isSurfaceCreated) {
//                cancelAnimation(mCircleRoadProgress)
//                startAnimation(mCircleRoadProgress, testDurationInMillis, this)
//                openCamera2(holder)
//
//            }
//
//        }
    }


    private void requestCameraPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    protected boolean hasPermission(String permission) {
        return this.hasPermission(permission, -1);
    }

    protected boolean hasPermission(String permission, int opId) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }


    private void stopBackgroundThread() {
        if (mBackgroundThread == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBackgroundThread.quitSafely();
        } else {
            mBackgroundThread.quit();
        }
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (Throwable ignored) {
        }

    }




    private void openCamera2(SurfaceHolder holder) {


        try {
            mCamera = Camera.open(findCamera());
            if (mCamera != null) {

                Camera.Parameters mParam = mCamera.getParameters();
//                if (hasFlash() && cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {

              mParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                }
                mCamera.setDisplayOrientation(getCorrectCameraOrientation(mCameraInfo));
                mCamera.setPreviewDisplay(holder);
//                setCameraResolution(mParam);

                mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
                    @Override
                    public void onAutoFocusMoving(boolean start, Camera camera) {
                        Log.e("ddd" , "fff");

                    }
                });
                mCamera.setParameters(mParam);
                mCamera.startPreview();


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Integer findCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
           if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                mCameraInfo = info;
                return cameraId;
           }
        }
        return cameraId;
    }



    private void capture(View v) {
        takePicture(mCamera);
    }

    void takePicture(Camera camera) {

        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                getImg(data);
            }
        });




    }

    private void getImg(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//        if(bitmap.getHeight() < bitmap.getWidth()) {
//        Matrix matrix = new Matrix();
//
//        matrix.postRotate(90);
//
//        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//       bitmap = rotatedBitmap;
//        }
        Uri uri = createImageFile();
        Intent intent = new Intent(this, ScanActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SELECTED_BITMAP, uri);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE);

    }

    int getCorrectCameraOrientation(Camera.CameraInfo info) {
        int degrees = 0;
        rotation = getWindowManager().getDefaultDisplay().getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }


        int result;
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360;
//        } else {
            result = (info.orientation - degrees + 360) % 360;
//        }
        return result;
    }


    @Override
    protected void onPause() {
//        closeCamera();
//        stopBackgroundThread();
        super.onPause();
    }


    private class ScanButtonClickListener implements View.OnClickListener {

        private int preference;

        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }

        public ScanButtonClickListener() {
        }

        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    Uri fileUri;

    private Uri createImageFile() {
        clearTempImages();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new
                Date());
        File file = new File(ScanConstants.IMAGE_PATH, "IMG_" + timeStamp +
                ".jpg");

        try {
            OutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileUri = Uri.fromFile(file);

        return fileUri;
    }


    private void clearTempImages() {
        try {
            File tempFolder = new File(ScanConstants.IMAGE_PATH);
            for (File f : tempFolder.listFiles())
                f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                scannedImageView.setVisibility(View.VISIBLE);
                scannedImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            finish();
        }
    }

    private Bitmap convertByteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }


//    @Override
//    public void onBackPressed() {
//
//        if (scannedImageView.getVisibility() == View.VISIBLE) {
//            scannedImageView.setVisibility(View.GONE);
//            mSurfacePreview.setVisibility(View.VISIBLE);
//            button.setVisibility(View.VISIBLE);
//            openCamera2(holder);
//        } else {
//            super.onBackPressed();
//        }
//
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            mCamera.startPreview();
            mCamera.release();
            mCamera = null;
        } catch (Throwable ignored) {
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

}
