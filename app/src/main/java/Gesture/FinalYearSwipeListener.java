package Gesture;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.example.ar2go.FinalYearActivity;

public class FinalYearSwipeListener extends GestureDetector.SimpleOnGestureListener {
    private static int MIN_SWIPE_DISTANCE_Y = 100;

    private static int MAX_SWIPE_DISTANCE_Y = 1000;

    private FinalYearActivity activitiy = null;

    public FinalYearActivity getActivity() {
        return activitiy;
    }

    public void setActivitiy(FinalYearActivity activitiy) {
        this.activitiy = activitiy;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float deltaY = e1.getY() - e2.getY();

        float deltaYAbs = Math.abs(deltaY);

        if (deltaYAbs >= MIN_SWIPE_DISTANCE_Y && deltaYAbs <= MAX_SWIPE_DISTANCE_Y) {
            if (deltaY > 0) {
                this.activitiy.showOneArt();
            } else if (deltaY < 0) {
                this.activitiy.showAllArt();
            }

        }
        return true;
    }
}

