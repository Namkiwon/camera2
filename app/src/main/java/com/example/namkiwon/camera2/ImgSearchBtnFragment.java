package com.example.namkiwon.camera2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by namkiwon on 2018. 3. 22..
 */

public class ImgSearchBtnFragment extends Fragment {
    private String type;
    public Button btn;

    public ImgSearchBtnFragment(String type){
        this.type = type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_imgsearchbtn,null);

        btn = (Button) view.findViewById(R.id.imgSerchBtn);
        btn.setText(type);
        btn.setOnClickListener(bListener);
        return view;
    }

    Button.OnClickListener bListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.imgSerchBtn:
                    if(type.equals("google")){
                        Log.d("type",type);
                    }else if(type.equals("naver")){
                        Log.d("type",type);
                    }
                    break;
            }
        }
    };
}
