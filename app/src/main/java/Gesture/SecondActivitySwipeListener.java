package Gesture;

import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.example.ar2go.SecondActivity;

public class SecondActivitySwipeListener extends GestureDetector.SimpleOnGestureListener {
    private static int MIN_SWIPE_DISTANCE_Y = 100;

    private static int MAX_SWIPE_DISTANCE_Y = 1500;

    private SecondActivity activitiy = null;

    public SecondActivity getActivity() {
        return activitiy;
    }

    public void setActivitiy(SecondActivity activitiy) {
        this.activitiy = activitiy;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float deltaY = e1.getY() - e2.getY();

        float deltaYAbs = Math.abs(deltaY);

        if (deltaYAbs >= MIN_SWIPE_DISTANCE_Y && deltaYAbs <= MAX_SWIPE_DISTANCE_Y) {
            if(deltaY > 0){
                this.activitiy.startFreelance();
            }
        }
        return true;
    }
}
