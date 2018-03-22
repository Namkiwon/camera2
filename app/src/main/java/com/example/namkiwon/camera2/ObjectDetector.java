package com.example.namkiwon.camera2;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by namkiwon on 2018. 3. 15..
 */

public class ObjectDetector extends Object {
    private static final int INPUT_SIZE = 1440; //인식 시킬 이미지 사이즈
    private static final int  INPUT_SIZE_WIDTH = 1440/10; //인식 시킬 이미지 사이즈
    private static final int INPUT_SIZE_HEIGHT = 2072/10; //인식 시킬 이미지 사이즈
    private static final int IMAGE_MEAN = 0; //
    private static final float IMAGE_STD = 255.0f;
    private Context context;
//    private static final String INPUT_NAME = "";
//    private static final String OUTPUT_NAME = "";

    private static final String MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
    private static final String LABEL_FILE = "file:///android_asset/coco_labels_list.txt";

    public Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor(); // 익스큐터는 그래프 크기가 크기 때문에 스레드로 돌리기 위하여 사용



    public ObjectDetector(Context context , int INPUT_SIZE_WIDTH, int INPUT_SIZE_HEIGHT){
        this.context = context;
        //텐서플로우 초기화 및 그래프파일 메모리에 탑재
        initTensorFlowAndLoadModel();

    }

    public List<Classifier.Recognition> recognize_bitmap(Bitmap bitmap) {

        // 비트맵을 처음에 정의된 INPUT SIZE에 맞춰 스케일링
//        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        // classifier 의 recognizeImage 부분이 실제 inference 를 호출해서 인식작업을 하는 부분입니다.

        if(classifier == null) {
            Log.d("message","classifier is null");
            initTensorFlowAndLoadModel();
            }
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        // 결과값은 Classifier.Recognition 구조로 리턴되는데, 원래는 여기서 결과값을 배열로 추출가능
        // imgResult에는 분석에 사용된 비트맵을 뿌려줌.

        return results;
    }


    public void initTensorFlowAndLoadModel(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowObjectDetectionAPIModel.create(
                            context.getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE_WIDTH,
                            INPUT_SIZE_HEIGHT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}
