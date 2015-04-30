package com.kelevra.navi;

import com.google.android.gms.maps.model.LatLng;
import com.kelevra.imagezoom.ImageViewTouch;
import com.kelevra.imagezoom.ImageViewTouchBase;
import com.kelevra.imagezoom.OnDisplayMatrixChangedListener;
import com.kelevra.imagezoom.OnLongPressListener;
import com.kelevra.navi.util.ScreenUtils;
import com.kelevra.navi.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements OnDisplayMatrixChangedListener, OnLongPressListener {
    private static final String TAG = FullscreenActivity.class.getName();
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;


    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private ImageView ivOverlay;
    private ImageViewTouch ivtMap;
    private Bitmap overlayBitmap;
    private Canvas canvas;
    private Matrix prevDisplayMatrix;
    private LatLng minLatLng;
    private LatLng maxLatLng;
    private LatLng currentLatLng;
    private Paint bigCirclePaint;
    private int bigCircleRadius;
    private Paint smallCirclePaint;
    private int smallCircleRadius;
    private Matrix transformationMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        ivOverlay = (ImageView) findViewById(R.id.ivOverlay);
        ivtMap = (ImageViewTouch) findViewById(R.id.ivtMap);
        ivtMap.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.screen);
        ivtMap.setImageBitmap(bitmap, null, -1f, 8f);
        ivtMap.setOnDisplayMatrixChangedListener(this);
        ivtMap.setLongClickable(true);
        ivtMap.setOnLongPressListener(this);

        minLatLng = new LatLng(35.5, 55.2);
        maxLatLng = new LatLng(35.7, 55.3);
        currentLatLng = new LatLng(35.55, 55.25);
        calculateTransformationMatrix();
        initBigCirclePaint();
        initSmallCirclePaint();
        getLatLngToScreenProjection(minLatLng);
        getLatLngToScreenProjection(maxLatLng);
        getLatLngToScreenProjection(currentLatLng);

        now render it!

        final View controlsView = findViewById(R.id.fullscreen_content_controls);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, ivtMap, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        ivtMap.setDoubleTapListener(new ImageViewTouch.OnImageViewTouchDoubleTapListener() {
            @Override
            public void onDoubleTap() {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);



    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (AUTO_HIDE) {
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            }
            return true;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onDisplayMatrixChanged(final ImageViewTouchBase view) {
        ivOverlay.post(new Runnable() {
            @Override
            public void run() {
                Matrix newDisplayMatrix = ivtMap.getDisplayMatrix();
                if(prevDisplayMatrix == null || !prevDisplayMatrix.equals(newDisplayMatrix)) {
                    prevDisplayMatrix = new Matrix(newDisplayMatrix);
                    Log.d(TAG, "newDisplayMatrix = " + newDisplayMatrix.toShortString());
                    if (canvas == null) {
                        overlayBitmap = Bitmap.createBitmap(view.getWidth()/2, view.getHeight()/2, Bitmap.Config.ARGB_4444);
                        canvas = new Canvas(overlayBitmap);
                    }
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, bigCircleRadius, bigCirclePaint);

                    float[] values = new float[9];
                    newDisplayMatrix.getValues(values);
                    values[Matrix.MTRANS_X] /= 2;
                    values[Matrix.MTRANS_Y] /= 2;
                    newDisplayMatrix.setValues(values);
                    canvas.setMatrix(new Matrix(newDisplayMatrix));
                    ivOverlay.setImageDrawable(new BitmapDrawable(getResources(), overlayBitmap));
                }
            }
        });
    }

    private void initBigCirclePaint() {
        bigCircleRadius = ScreenUtils.convertToPx(FullscreenActivity.this, 24);
        bigCirclePaint = new Paint();
        bigCirclePaint.setColor(Color.RED);
        bigCirclePaint.setStrokeWidth(10);
        bigCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void initSmallCirclePaint() {
        smallCircleRadius = ScreenUtils.convertToPx(FullscreenActivity.this, 4);
        smallCirclePaint = new Paint();
        smallCirclePaint.setColor(Color.BLUE);
        smallCirclePaint.setStrokeWidth(10);
        smallCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void onLongPress(ImageViewTouchBase view, PointF position) {
        Toast.makeText(this, "position = " + position, Toast.LENGTH_SHORT).show();



        //ok now calculate matrix projection
    }

    private void calculateTransformationMatrix() {
        double screenWidth = ScreenUtils.getScreenSize(this)[0];
        double screenHeight = ScreenUtils.getScreenSize(this)[1];
        double mapWidth = maxLatLng.latitude - minLatLng.latitude;
        double mapHeight = maxLatLng.longitude - minLatLng.longitude;
        double screenAspectRatio = screenWidth/screenHeight;
        double mapAspectRatio = mapWidth/mapHeight;
        boolean shouldFitWidth = screenAspectRatio <= mapAspectRatio;
        float scaleValue = 0;
        if(shouldFitWidth) {
            scaleValue = (float) (screenWidth/mapWidth);
        } else {
            scaleValue = (float) (screenHeight/mapHeight);
        }
        float translateXValue = (float) (-scaleValue*minLatLng.latitude);
        float translateYValue = (float) (-scaleValue*minLatLng.longitude);

        transformationMatrix = new Matrix();
        transformationMatrix.setValues(new float[]{scaleValue, 0, translateXValue, 0, scaleValue, translateYValue, 0, 0, 1});
        Log.d(TAG, "transformationMatrix = " + transformationMatrix.toShortString());
    }

    private PointF getLatLngToScreenProjection(LatLng latLng) {
        Matrix latLngMatrix = new Matrix();
        latLngMatrix.setValues(new float[]{(float) latLng.latitude, 0, 0, (float) latLng.longitude, 0, 0, 1, 0, 0});
        Matrix product = new Matrix();
        product.setConcat(transformationMatrix, latLngMatrix);
        Log.d(TAG, "product = " + product.toShortString());
        return new PointF();
    }
}