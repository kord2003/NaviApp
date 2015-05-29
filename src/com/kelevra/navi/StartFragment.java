package com.kelevra.navi;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.kelevra.imagezoom.ImageViewTouch;
import com.kelevra.imagezoom.ImageViewTouchBase;
import com.kelevra.imagezoom.OnDisplayMatrixChangedListener;
import com.kelevra.imagezoom.OnLongPressListener;
import com.kelevra.navi.util.ScreenUtils;


/**
 * A placeholder fragment containing a simple view.
 */
public class StartFragment extends Fragment implements OnDisplayMatrixChangedListener, OnLongPressListener {

    public static final String TAG = StartFragment.class.getName();
    private ImageView ivOverlay;
    private ImageViewTouch ivtMap;
    private Bitmap overlayBitmap;
    private Canvas canvas;
    private Matrix prevDisplayMatrix;
    private PointF anchor1Point;
    private PointF anchor2Point;
    private LatLng latLng1;
    private LatLng latLng2;
    private LatLng currentLatLng;
    private Paint bigCirclePaint;
    private int bigCircleRadius;
    private Paint smallCirclePaint;
    private int smallCircleRadius;
    private Matrix latLngToScreenMatrix;
    private Matrix screenToLatLngMatrix;

    private AnchorInputState anchorInputState = AnchorInputState.ANCHORS_DONE;

    private static final String ARG_ROAD_EVENT = "arg_road_event";

    public StartFragment() {
    }

    public static StartFragment newInstance(String roadEvent) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        //args.putSerializable(ARG_ROAD_EVENT, roadEvent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            //roadEvent = (RoadEvent) args.getSerializable(ARG_ROAD_EVENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();
        ivOverlay = (ImageView) v.findViewById(R.id.ivOverlay);
        ivtMap = (ImageViewTouch) v.findViewById(R.id.ivtMap);
        ivtMap.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.screen);
        ivtMap.setImageBitmap(bitmap, null, -1f, 8f);
        ivtMap.setOnDisplayMatrixChangedListener(this);
        ivtMap.setLongClickable(true);
        ivtMap.setOnLongPressListener(this);

        latLng1 = new LatLng(35.5, 55.2);
        latLng2 = new LatLng(35.7, 55.3);
        anchor1Point = new PointF(0f, 0f);
        anchor2Point = new PointF(1080f, 540f);
        currentLatLng = new LatLng(35.55, 55.25);
        initBigCirclePaint();
        initSmallCirclePaint();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(anchorInputState != AnchorInputState.ANCHORS_DONE) {
            setLocation1();
        }
    }

    @Override
    public void onDisplayMatrixChanged(final ImageViewTouchBase view) {
        if(anchorInputState == AnchorInputState.ANCHORS_DONE) {
            Matrix newDisplayMatrix = ivtMap.getSuppMatrix();
            Log.d(TAG, "newDisplayMatrix = " + newDisplayMatrix.toShortString());
            if (prevDisplayMatrix == null || !prevDisplayMatrix.equals(newDisplayMatrix)) {
                prevDisplayMatrix = new Matrix(newDisplayMatrix);
                if (canvas == null) {
                    int screenWidth = ivtMap.getWidth();//ScreenUtils.getScreenSize(getActivity())[0];
                    int screenHeight = ivtMap.getHeight();//ScreenUtils.getScreenSize(getActivity())[1];
                    Log.d(TAG, "create overlayBitmap: " + screenWidth + ", " + screenHeight);
                    overlayBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_4444);
                    canvas = new Canvas(overlayBitmap);
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                PointF latLngPoint1 = getLatLngToScreenProjection(latLng1);
                PointF latLngPoint2 = getLatLngToScreenProjection(latLng2);
                PointF currentLatLngPoint = getLatLngToScreenProjection(currentLatLng);

                canvas.setMatrix(new Matrix(newDisplayMatrix));
                drawSmallCircle(latLngPoint1);
                drawSmallCircle(latLngPoint2);
                drawSmallCircle(currentLatLngPoint);
                ivOverlay.setImageDrawable(new BitmapDrawable(getResources(), overlayBitmap));
            }
        }
    }



    @Override
    public void onLongPress(ImageViewTouchBase view, PointF position) {
        //LatLng coordinates = getScreenToLatLngProjection(position);
        if(anchorInputState == AnchorInputState.ANCHOR_1_PENDING) {
            anchor1Point = new PointF(position.x, position.y);
            if(latLng1 != null && latLng2 != null) {
                anchorInputState = AnchorInputState.ANCHORS_DONE;
            } else if(latLng2 == null) {
                setLocation2();
            }
        } else if(anchorInputState == AnchorInputState.ANCHOR_2_PENDING) {
            anchor2Point = new PointF(position.x, position.y);
            if(latLng1 != null && latLng2 != null) {
                anchorInputState = AnchorInputState.ANCHORS_DONE;
            }
        }
        Toast.makeText(getActivity(), "position = " + position /*+ ", coordinates = " + coordinates*/, Toast.LENGTH_SHORT).show();
    }

    private Matrix calculateLatLngToScreenMatrix() {
        double viewPortWidth = ivtMap.getWidth();/*Math.abs(anchor1Point.x - anchor2Point.x)*/;
        double viewPortHeight = ivtMap.getHeight();/*Math.abs(anchor1Point.y - anchor2Point.y)*/;
        Log.d(TAG, "viewPortWidth = " + viewPortWidth + ", viewPortHeight = " + viewPortHeight);
        double mapWidth = Math.abs(latLng2.latitude - latLng1.latitude);
        double mapHeight = Math.abs(latLng2.longitude - latLng1.longitude);
        double screenAspectRatio = viewPortWidth / viewPortHeight;
        double mapAspectRatio = mapWidth / mapHeight;
        boolean shouldFitWidth = screenAspectRatio <= mapAspectRatio;
        float scaleValue = 0;
        if (shouldFitWidth) {
            scaleValue = (float) (viewPortWidth / mapWidth);
        } else {
            scaleValue = (float) (viewPortHeight / mapHeight);
        }
        double minLatitude = Math.min(latLng1.latitude, latLng2.latitude);
        float translateXValue = (float) (-scaleValue * minLatitude);
        float translateYValue = (float) (-scaleValue * latLng1.longitude);

        Matrix matrix = new Matrix();
        matrix.setValues(new float[]{scaleValue, 0, translateXValue, 0, scaleValue, translateYValue, 0, 0, 1});
        Log.d(TAG, "calculateLatLngToScreenMatrix() = " + matrix.toShortString());
        return matrix;
    }

    private Matrix calculateScreenToLatLngMatrix() {
        double viewPortWidth = ivtMap.getWidth();/*Math.abs(anchor1Point.x - anchor2Point.x);*/
        double viewPortHeight = ivtMap.getHeight();/*Math.abs(anchor1Point.y - anchor2Point.y);*/
        double mapWidth = latLng2.latitude - latLng1.latitude;
        double mapHeight = latLng2.longitude - latLng1.longitude;
        double screenAspectRatio = viewPortWidth / viewPortHeight;
        double mapAspectRatio = mapWidth / mapHeight;
        boolean shouldFitWidth = screenAspectRatio <= mapAspectRatio;
        float scaleValue = 0;
        if (shouldFitWidth) {
            scaleValue = (float) (mapWidth / viewPortWidth);
        } else {
            scaleValue = (float) (mapHeight / viewPortHeight);
        }
        float translateXValue = (float) (latLng1.latitude);
        float translateYValue = (float) (latLng1.longitude);

        Matrix matrix = new Matrix();
        matrix.setValues(new float[]{scaleValue, 0, translateXValue, 0, scaleValue, translateYValue, 0, 0, 1});
        Log.d(TAG, "calculateScreenToLatLngMatrix() = " + matrix.toShortString());
        return matrix;
    }

    private PointF getLatLngToScreenProjection(LatLng latLng) {
        float viewPortHeight = ivtMap.getHeight();
        //if(latLngToScreenMatrix == null) {
            latLngToScreenMatrix = calculateLatLngToScreenMatrix();
        //}
        Matrix pointFMatrix = new Matrix();
        pointFMatrix.setValues(new float[]{(float) latLng.latitude, 0, 0, (float) latLng.longitude, 0, 0, 1, 0, 0});
        Matrix product = new Matrix();
        product.setConcat(latLngToScreenMatrix, pointFMatrix);
        float[] values = new float[9];
        product.getValues(values);
        return new PointF(values[0], viewPortHeight - values[3]);
    }

    private LatLng getScreenToLatLngProjection(PointF pointF) {
        //if(screenToLatLngMatrix == null) {
            screenToLatLngMatrix = calculateScreenToLatLngMatrix();
        //}
        Matrix latLngMatrix = new Matrix();
        latLngMatrix.setValues(new float[]{(float) pointF.x, 0, 0, (float) pointF.y, 0, 0, 1, 0, 0});
        Matrix product = new Matrix();
        product.setConcat(screenToLatLngMatrix, latLngMatrix);
        float[] values = new float[9];
        product.getValues(values);
        return new LatLng(values[0], values[3]);
    }

    private void drawSmallCircle(PointF point) {
        Log.d(TAG, "drawSmallCircle(" + (point.x) + ", " + (point.y) + ")");
        canvas.drawCircle(point.x, point.y, smallCircleRadius, smallCirclePaint);
    }

    private void drawBigCircle(PointF point) {
        Log.d(TAG, "drawBigCircle(" + point.x + ", " + point.y + ")");
        canvas.drawCircle(point.x, point.y, bigCircleRadius, bigCirclePaint);
    }

    private void initBigCirclePaint() {
        bigCircleRadius = ScreenUtils.convertToPx(getActivity(), 24);
        bigCirclePaint = new Paint();
        bigCirclePaint.setColor(Color.RED);
        bigCirclePaint.setStrokeWidth(10);
        bigCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void initSmallCirclePaint() {
        smallCircleRadius = ScreenUtils.convertToPx(getActivity(), 4);
        smallCirclePaint = new Paint();
        smallCirclePaint.setColor(Color.BLUE);
        smallCirclePaint.setStrokeWidth(10);
        smallCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setLocation1() {
        Log.d(TAG, "setLocation1");
        anchorInputState = AnchorInputState.ANCHOR_1_PENDING;
        //String errorMessage = fragmentActivity.getString(R.string.DROPBOX_LOAD_FILE_ERROR);
        //FileLogger.appendLog(getActivity(), "DropboxFileErrorMessage: " + errorMessage);
        MessageDialog.newInstance("setLocation1", new MessageDialog.OnDismissLocationDialogListener() {
            @Override
            public void dismissLocationDialog(LatLng latLng) {
                if(latLng != null) {
                    latLng1 = new LatLng(latLng.latitude, latLng.longitude);
                }
            }
        }).show(getActivity().getFragmentManager(),
                null);
    }

    public void setLocation2() {
        Log.d(TAG, "setLocation2");
        anchorInputState = AnchorInputState.ANCHOR_2_PENDING;
        //String errorMessage = fragmentActivity.getString(R.string.DROPBOX_LOAD_FILE_ERROR);
        //FileLogger.appendLog(getActivity(), "DropboxFileErrorMessage: " + errorMessage);
        MessageDialog.newInstance("setLocation2", new MessageDialog.OnDismissLocationDialogListener() {
            @Override
            public void dismissLocationDialog(LatLng latLng) {
                if(latLng != null) {
                    latLng2 = new LatLng(latLng.latitude, latLng.longitude);
                }
            }
        }).show(getActivity().getFragmentManager(),
                null);
    }
}