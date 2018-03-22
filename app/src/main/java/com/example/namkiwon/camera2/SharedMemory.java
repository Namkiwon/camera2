package com.example.namkiwon.camera2;

/**
 * Created by namkiwon on 2018. 3. 20..
 */
public class SharedMemory {
    private static SharedMemory sharedMemory = null;

    public static synchronized SharedMemory getinstance(){
        if(sharedMemory == null){
            sharedMemory = new SharedMemory();
        }
        return sharedMemory;
    }
    private OverLayView rView;

    public OverLayView getrView() {
        return rView;
    }

    public void setrView(OverLayView rView) {
        this.rView = rView;
    }
}