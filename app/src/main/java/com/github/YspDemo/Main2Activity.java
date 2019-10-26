package com.github.YspDemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    private Button record;
    private TextureView textureView;
    private CameraManager cameraManager;
    private String cameraID;
    private static final String TAG = "maff";
    private CameraDevice.StateCallback deviceStateCall;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private CameraDevice cameraDevice;
    private Surface surface1;
    private boolean recording = false;
    ImageReader nv21Reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                    },
                    0);
        }
        record = findViewById(R.id.button_recorded);
        record.setOnClickListener(this);
        textureView = findViewById(R.id.texture);
        textureView.setDrawingCacheEnabled(false);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable: ");
                surface1 = new Surface(surface);
                nv21Reader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2);
                nv21Reader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        //nv21格式处理相关
                        Image image = reader.acquireLatestImage();
                        if (image != null) {
                            image.close();
                        }
                    }
                }, null);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIdList;//手机的相机列表
            cameraIdList = cameraManager.getCameraIdList();
            for (String s : cameraIdList) {//找到后置摄像头
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(s);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraID = s;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        deviceStateCall = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                final CaptureRequest.Builder captureRequest;
                try {
                    captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    captureRequest.addTarget(surface1);
                    captureRequest.addTarget(nv21Reader.getSurface());
                    cameraDevice.createCaptureSession(Arrays.asList(surface1, nv21Reader.getSurface()), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            CameraCaptureSession session1 = session;
                            try {
                                session1.setRepeatingRequest(captureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                        super.onCaptureCompleted(session, request, result);
                                    }
                                }, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        };
    }


    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            cameraManager.openCamera(cameraID, deviceStateCall, null);
            record.setText("停止录制");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCamera() {
        cameraDevice.close();
        record.setText("开始录制");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_recorded:
                if (!recording) {
                    recording = true;
                    openCamera();
                } else {
                    recording = false;
                    stopCamera();
                }
                break;
        }
    }
}
