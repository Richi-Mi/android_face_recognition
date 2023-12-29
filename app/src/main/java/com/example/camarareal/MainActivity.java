package com.example.camarareal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;
    CascadeClassifier cascadeClassifier;
    Mat gray, rgb, traspose_gray, traspose_rgb;
    MatOfRect rects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();

        cameraBridgeViewBase = findViewById( R.id.cameraView );

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                rgb = new Mat();
                gray = new Mat();
                rects = new MatOfRect();
            }

            @Override
            public void onCameraViewStopped() {
                rgb.release();
                gray.release();
                rects.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                // TODO: Procces input frame

                //rgb = inputFrame.rgba();
                gray = inputFrame.gray();

                //traspose_gray = gray.t();
                //traspose_rgb  = rgb.t();

                cascadeClassifier.detectMultiScale( gray, rects, 1.1, 2);

                for( Rect rect : rects.toList() ) {
                    Mat submat = gray.submat( rect );
                    Imgproc.blur( submat, submat, new Size(10,10));
                    Imgproc.rectangle( gray, rect, new Scalar( 0, 255, 0), 10 );

                    submat.release();
                }

                return gray;
            }
        });

        if (OpenCVLoader.initDebug()) {
            cameraBridgeViewBase.setCameraPermissionGranted();
            cameraBridgeViewBase.enableView();


            try {
                InputStream inputStream = getResources().openRawResource( R.raw.lbpcascade_frontalface );
                File file = new File( getDir("cascade", MODE_PRIVATE), "lbpcascade_frontalface.xml");
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] data = new byte[4096];
                int read_bytes;

                while (( read_bytes = inputStream.read(data)) != -1 ) {
                    fileOutputStream.write( data, 0, read_bytes );
                }

                cascadeClassifier = new CascadeClassifier( file.getAbsolutePath() );
                if( cascadeClassifier.empty() ) cascadeClassifier = null;


                inputStream.close();
                fileOutputStream.close();
                file.delete();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void getPermission() {
        if( checkSelfPermission( android.Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions( new String[] { Manifest.permission.CAMERA }, 2 );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 2) {
            if( grantResults[0] != PackageManager.PERMISSION_GRANTED ) {
                getPermission();
            }
        }
    }
    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList( cameraBridgeViewBase );
    }
}