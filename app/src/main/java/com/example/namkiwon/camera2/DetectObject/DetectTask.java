package com.example.namkiwon.camera2.DetectObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RelativeLayout;

import com.example.namkiwon.camera2.TensorFlowMobile.Classifier;
import com.example.namkiwon.camera2.DetectObject.ObjectDetector;
import com.example.namkiwon.camera2.Views.AutoFitTextureView;
import com.example.namkiwon.camera2.Views.OverLayView;

import java.util.List;

/**
 * Created by namkiwon on 2018. 3. 21..
 */

public class DetectTask extends AsyncTask<Void,Void,Void> {

    private ObjectDetector detector;
    private List<Classifier.Recognition> detectResult;
    private OverLayView r;
    private AutoFitTextureView textureView;
    private  Context context;
    private  RelativeLayout surfaceLayout;
    private Runnable runnable;

    public DetectTask( AutoFitTextureView textureView, Context context, RelativeLayout surfaceLayout){
        this.textureView = textureView;
        this.context = context;
        this.surfaceLayout = surfaceLayout;
        detector = new ObjectDetector(context,textureView.getWidth()/10, textureView.getHeight()/10 );
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while(true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Bitmap a = textureView.getBitmap();
            if(detector.classifier != null) {
                doDetection(a);
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }else{
                detector.initTensorFlowAndLoadModel();
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }

    public void doDetection(Bitmap photoBitmap){
        double maxArea = 0.0;
        RectF maxRect = null;
        String txt = "";
        //비트맵을 워하는 사이즈로 변경
        photoBitmap = Bitmap.createScaledBitmap(photoBitmap, textureView.getWidth()/10, textureView.getHeight()/10, false);
        detectResult = detector.recognize_bitmap(photoBitmap);
        Log.d("result one", detectResult.toString());
        for(int i = 0 ; i < 5; i++){
            /** 아이템중 디텍드되지 못한 id는 내림차순으로 시작한다**/
            if(Integer.parseInt(detectResult.get(i).getId()) > 4) break;
            txt = txt + detectResult.get(i).toString()+"\n";
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
        Log.d("maxRect",maxRect.toString());

        r = new OverLayView(context,maxRect,photoBitmap);
        r.invalidate();
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            surfaceLayout.removeAllViews();
            surfaceLayout.addView(r);
//                DetectThread a = new DetectThread();
//                a.start();
        }
    };
}
