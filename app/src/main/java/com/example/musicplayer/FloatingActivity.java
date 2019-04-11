package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class FloatingActivity extends Service {

    private WindowManager wm;
    private LinearLayout ll;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public void onCreate(){

        super.onCreate();
        wm= (WindowManager) getSystemService(WINDOW_SERVICE);
        ll= new LinearLayout(this);
        LinearLayout.LayoutParams llparameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(128, 255, 0, 0));
        ll.setLayoutParams(llparameters);
        final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(400, 100, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x=0;
        parameters.y=0;
        parameters.gravity = Gravity.CENTER | Gravity.CENTER;
        wm.addView(ll, parameters);

        ll.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams updatedParameters= parameters;
            int x, y;
            float touchedX, touchedY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        x= updatedParameters.x;
                        y= updatedParameters.y;
                        touchedX = event.getRawX();
                        touchedY = event.getRawY();
                        break;

                    case  MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x+(event.getRawX() - touchedX));
                        updatedParameters.y = (int) (y+(event.getRawY() - touchedY));

                        wm.updateViewLayout(ll, updatedParameters);


                    default:
                        break;
                }
                return false;
            }
        });



    }

}
