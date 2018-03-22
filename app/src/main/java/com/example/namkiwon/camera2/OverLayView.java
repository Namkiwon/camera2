package com.example.namkiwon.camera2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class OverLayView extends View {

    private RectF rect;
    private Paint paint;
    private Bitmap photoBitmap;

    public OverLayView(Context context, RectF rect, Bitmap photoBitmap) {
        super(context);
        this.photoBitmap = photoBitmap;
        this.rect = rect;
    }

//    public RectView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//
//
//    }

    /**
     * 뷰가 화면에 디스플레이 될때 자동으로 호출
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint(); // 페인트 객체 생성
        paint.setColor(Color.RED); // 빨간색으로 설정
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
//        canvas.drawBitmap(photoBitmap,new Matrix(),null);
        RectF r = new RectF();
        r.left = rect.left*10;
        r.right = rect.right*10;
        r.bottom = rect.bottom*10;
        r.top = rect.top*10;
        canvas.drawRect(r, paint);
        // 좌표값과 페인트 객체를 이용해서 사각형을 그리는 drawRect()
    }

    /**
     * 터치 이벤트를 처리
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Toast.makeText(super.getContext(), "MotionEvent.ACTION_DOWN : " +
                    event.getX() + ", " + event.getY(), Toast.LENGTH_SHORT).show();
        }
        return super.onTouchEvent(event);
    }
}