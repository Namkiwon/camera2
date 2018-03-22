package com.example.namkiwon.camera2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ObjectDetector detector;
    private List<Classifier.Recognition> detectResult;
    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private RelativeLayout surfaceLayout;
    private AutoFitTextureView mCameraTextureView;
    private Preview mPreview;
    private Button takePicture;

    private OverLayView r;
    private Boolean isStart = false;


    Activity mainActivity = this;

    private static final String TAG = "MAINACTIVITY";

    static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraTextureView = (AutoFitTextureView) findViewById(R.id.cameraTextureView);
        surfaceLayout = (RelativeLayout) findViewById(R.id.surface);

        mPreview = new Preview(this, mCameraTextureView,surfaceLayout);
        takePicture = (Button) findViewById(R.id.capture);
        takePicture.setOnClickListener(bListener);
        mPreview.setSurfaceTextureListener();


//        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
//                new IntentFilter("messageReceiver"));

    }


    Button.OnClickListener bListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.capture :
                    mPreview.takePicture();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            mCameraTextureView = (AutoFitTextureView) findViewById(R.id.cameraTextureView);
                            mPreview = new Preview(mainActivity, mCameraTextureView,surfaceLayout ) ;
                            Log.d(TAG,"mPreview set");
                        } else {
                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.onPause();
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) ||
                    shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(MainActivity.this,
                        "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[] {PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

//===============================================================================================================


//    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String m = intent.getStringExtra("isCameraReady");
//            if(m != null)
//            {
//                //what you want to do
//                if(m.equals("camera is ready")) {
//                    detectThread.start();
//                }else if(m.equals("camera is close")){
//                    detectThread.interrupt();
//                }
//            }
//        }
//    };
//
//    class DetectThread extends Thread{
//        @Override
//        public void run() {
//            while(true) {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Bitmap a = mCameraTextureView.getBitmap();
//                if(detector.classifier != null) {
//                    doDetection(a);
//                    Message msg = handler.obtainMessage();
//                    handler.sendMessage(msg);
//                }else{
//                    detector.initTensorFlowAndLoadModel();
//                }
//            }
//        }
//
//        public void doDetection(Bitmap photoBitmap){
//            double maxArea = 0.0;
//            RectF maxRect = null;
//            String txt = "";
//            //비트맵을 워하는 사이즈로 변경
//            photoBitmap = Bitmap.createScaledBitmap(photoBitmap, mCameraTextureView.getWidth()/10, mCameraTextureView.getHeight()/10, false);
//            detectResult = detector.recognize_bitmap(photoBitmap);
//            Log.d("result one", detectResult.toString());
//            for(int i = 0 ; i < 5; i++){
//                /** 아이템중 디텍드되지 못한 id는 내림차순으로 시작한다**/
//                if(Integer.parseInt(detectResult.get(i).getId()) > 4) break;
//                txt = txt + detectResult.get(i).toString()+"\n";
//                RectF rect= detectResult.get(i).getLocation();
//                Log.d("left",String.valueOf(rect.left));
//                Log.d("right",String.valueOf(rect.right));
//                Log.d("top",String.valueOf(rect.top));
//                Log.d("bottom",String.valueOf(rect.bottom));
//                double objectArea = (rect.right - rect.left)*(rect.bottom-rect.top);
//
//                if(detectResult.get(i).getId().equals("0") || objectArea > maxArea) {
//                    maxArea = objectArea;
//                    maxRect = rect;
//                    Log.d("maxRect",String.valueOf(objectArea));
//                }
//            }
//        Log.d("maxRect",maxRect.toString());
//
//            r = new OverLayView(MainActivity.this,maxRect,photoBitmap);
//            r.invalidate();
//        }
//
//        final  Handler handler = new Handler() {
//            public void handleMessage(Message msg) {
//                surfaceLayout.removeAllViews();
//                surfaceLayout.addView(r);
////                DetectThread a = new DetectThread();
////                a.start();
//            }
//        };
//
//    }
//

}




