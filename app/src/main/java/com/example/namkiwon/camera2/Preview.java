package com.example.namkiwon.camera2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import static android.support.v4.app.ActivityCompat.requestPermissions;
import static android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale;

/**
 * Created by namkiwon on 2018. 3. 16..
 */


public class Preview extends Thread {

    private final static String TAG = "Preview : ";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,90);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }
    private Size mPreviewSize;
    private Context mContext;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private AutoFitTextureView mTextureView;
    private StreamConfigurationMap map;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CaptureRequest.Builder captureRequestBuilder;



    private ObjectDetector detector;
    private List<Classifier.Recognition> detectResult;
    private OverLayView r;
    private RelativeLayout surfaceLayout;
    private static DetectTask detectTask;
    private ImageReader mImageReader;
    private Boolean isCameraOpen  = true;




    private String imageFileName;
    private String imgPath = "";

    public Preview(Context context, AutoFitTextureView textureView, RelativeLayout surfaceLayout) {
        mContext = context;
        mTextureView = textureView;
        detector  = new ObjectDetector(mContext,mTextureView.getWidth()/10, mTextureView.getHeight()/10 );
        this.surfaceLayout = surfaceLayout;
    }



    private String getBackFacingCameraId(CameraManager cManager) {
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openCamera() {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = getBackFacingCameraId(manager);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

//
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_CAMERA);
                return;
            }
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        Log.e(TAG, "openCamera X");

    }




    public TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                              int width, int height) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureAvailable, width="+width+",height="+height);
            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureDestroyed");
//            detectTask.cancel(true);

            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TODO Auto-generated method stub
        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            startPreview();
            isCameraOpen = true;
            if(mTextureView.isAvailable()){
                Log.e(TAG, "Detect Thread Start");
                detectTask = new DetectTask(mTextureView,mContext,surfaceLayout);
                detectTask.execute();

            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onError");
        }

    };


    protected void createCameraPreview() {
        try {

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(mContext, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void startPreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return");

        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if(null == texture) {
            Log.e(TAG,"texture is null, return");
            return;
        }

        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        HandlerThread thread = new HandlerThread("CameraPreview");
//        thread.start();
//        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
//=============================
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //==============================

    public void setSurfaceTextureListener()
    {
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        startBackgroundThread();
        setSurfaceTextureListener();
    }



    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPause");
        stopBackgroundThread();
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                Log.d(TAG, "CameraDevice Close");
            }
            if(mImageReader != null){
                mImageReader.close();
                mImageReader =null;
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }


    protected void takePicture() {

        if(null == mCameraDevice) {
            Log.e(TAG, "mCameraDevice is null, return");
            return;
        }

        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader imageReader= ImageReader.newInstance(width, height, ImageFormat.JPEG, 3);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(imageReader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            final String imageFileName = timeStamp+".jpg";
            final File storageDir = new File( Environment.getExternalStorageDirectory(),"Pictures/nam");
            Log.d("dir",storageDir.getAbsolutePath());
            if (!storageDir.exists()) {
                storageDir.mkdirs();
                Log.e("directory", "Directory not created");
            }
            File imageFile = null;
            imageFile = new File(storageDir.getAbsoluteFile(), imageFileName);
            imgPath = imageFile.getAbsolutePath();

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {

                    Image image = null;
                    try {

                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];

                        Log.d("byte lengh",String.valueOf(bytes.length));
                        Bitmap cropedBitmap = cropImage(image);
//                        Bitmap oldBitmap = convertYUV420888ToNV21(image);
//                        oldBitmap = Bitmap.createScaledBitmap(oldBitmap, mTextureView.getWidth(), mTextureView.getHeight(), false);
//
//                        RectF rect = doDetection(oldBitmap);
//                        float height = oldBitmap.getHeight()/mTextureView.getHeight()*(rect.bottom -rect.top)*10;
//                        Log.d("test", String.valueOf(height));
//
//                        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap,(int)rect.left*10,(int)rect.top*10,((int)rect.right - (int)rect.left)*10,((int)rect.bottom - (int)rect.top)*10);
////                        buffer.get(bytes);
//                        Log.d("left",String.valueOf((int)rect.left*10));
//                        Log.d("right",String.valueOf((int)rect.top*10));
//                        Log.d("bytes",String.valueOf(oldBitmap.getWidth()));
//                        Log.d("bytes",String.valueOf(oldBitmap.getHeight()));

                        Log.d("bytes",String.valueOf(bytes.length));
                        saveBitmap(cropedBitmap);
//                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                            reader.close();
                        }
                    }

                }
                private void saveBitmap(Bitmap bitmap) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(imgPath);
                        Log.d("path",imgPath);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                        Log.d("save","finish");
                    }catch (Exception e){
                        e.printStackTrace();

                    }finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(imgPath);
                        Log.d("path",imgPath);
//                        output.write(bytes);
                        Log.d("save","finish");
                    }catch (Exception e){
                        e.printStackTrace();

                    }finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            imageReader.setOnImageAvailableListener(readerListener, backgroudHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    Log.e(TAG,"capture comlete");
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(mContext, "Saved:"+imgPath, Toast.LENGTH_SHORT).show();
                    startPreview();
                }
            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        startBackgroundThread();
    }

    public Bitmap cropImage(Image image){
            double maxArea = 0.0;
            RectF maxRect = null;

            /**
             Camera2 에서 캡쳐한 사진은 YUV 형식으로 저장되는데 이를 비트맵으로 변환하기 위해서는 NV21형식으로 변환 후 Bitmap으로 변경하여야 한다.
             이를 수행하는 메서드가 convertYUV420888ToNV21() 메서드 이고 변환된 비트맵을 반환한다
             **/
            Bitmap photoBitmap = convertYUV420888ToNV21(image);
            /**
             변환된 비트맵은 사이즈가 바뀔 수 있는데 이를 화면크기로 재구성한다
             이 재구성의 목적은 tensorflow로 얻은 RectF 값이 화면크기에 맞춰져 있기 때문이다
             이를 통해 Detect 된 부분을 정확하게 자를 수 있다
             **/
            photoBitmap = Bitmap.createScaledBitmap(photoBitmap, mTextureView.getWidth(), mTextureView.getHeight(), false);
            /** 비트맵을 원하는 사이즈로 변경 >> 이유는 TensorFlow로 너무 큰 이미지를 보내게 되면 인식하는 시간이 길어지기 때문에 1/10의 크기로 줄여서 보낸다 **/
            Bitmap bufBitmap = Bitmap.createScaledBitmap(photoBitmap, mTextureView.getWidth()/10, mTextureView.getHeight()/10, false);
            detectResult = detector.recognize_bitmap(bufBitmap);
            Log.d("result one", detectResult.toString());
            /** TensorFlow mobile 모델을 통해 디텍트된 5가지중 가장 크기가 큰 Object를 뽑기위한 For문**/
            for(int i = 0 ; i < 5; i++){
                /** 아이템중 디텍드되지 못한 id는 내림차순으로 시작한다**/
                if(Integer.parseInt(detectResult.get(i).getId()) > 4) break;
                RectF rect= detectResult.get(i).getLocation();
                Log.d("left",String.valueOf(rect.left));
                Log.d("right",String.valueOf(rect.right));
                Log.d("top",String.valueOf(rect.top));
                Log.d("bottom",String.valueOf(rect.bottom));
                double objectArea = (rect.right - rect.left)*(rect.bottom-rect.top);

                if(detectResult.get(i).getId().equals("0") || objectArea > maxArea) {
                    maxArea = objectArea;
                    maxRect = rect;
                    Log.d("maxRect",String.valueOf(objectArea));
                }
            }

        photoBitmap = Bitmap.createBitmap(photoBitmap,(int)maxRect.left*10,(int)maxRect.top*10,((int)maxRect.right - (int)maxRect.left)*10,((int)maxRect.bottom - (int)maxRect.top)*10);
        return photoBitmap;
    }

    private Bitmap convertYUV420888ToNV21(Image imgYUV420) {// Converting YUV_420_888 data to YUV_420_SP (NV21).
        byte[] data;
        ByteBuffer buffer0 = imgYUV420.getPlanes()[0].getBuffer();
//        ByteBuffer buffer2 = imgYUV420.getPlanes()[2].getBuffer();
        int buffer0_size = buffer0.remaining();
//        int buffer2_size = buffer2.remaining();
        data = new byte[buffer0_size ];
        buffer0.get(data, 0, buffer0_size);
//        buffer2.get(data, buffer0_size, buffer2_size);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        return bitmap;}
}


