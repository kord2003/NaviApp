package com.kelevra.navi.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {

    public static int convertToPx(Context context, int dp) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }

    @SuppressLint("NewApi")
    public static int[] getScreenSize(Activity context){
        Point size = new Point();
        WindowManager w = context.getWindowManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
            w.getDefaultDisplay().getSize(size);
            return new int[]{size.x, size.y};
        }else{
            Display d = w.getDefaultDisplay();
            //noinspection deprecation
            return new int[]{d.getWidth(), d.getHeight()};
        }
    }

}
