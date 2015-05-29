package com.kelevra.navi;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sharlukovich on 29.05.2015.
 */
public class MessageDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = MessageDialog.class.getName();

    private String message = "";

    private OnDismissLocationDialogListener mListener;

    private EditText edtLatitude;
    private EditText edtLongitude;
    private LatLng latLng;

    public static MessageDialog newInstance(String message) {
        MessageDialog dlg = new MessageDialog();
        dlg.message = message;
        return dlg;
    }

    public static MessageDialog newInstance(String message, OnDismissLocationDialogListener listener) {
        MessageDialog dlg = new MessageDialog();
        dlg.message = message;
        dlg.mListener = listener;
        return dlg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.dlgCustom);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // view
        View view = inflater.inflate(R.layout.dialog_location, container);
        view.findViewById(R.id.btnOk).setOnClickListener(this);
        view.findViewById(R.id.btnCancel).setOnClickListener(this);
        if(message != null) {
            ((TextView) view.findViewById(R.id.tvMessage)).setText(message);
        }
        edtLatitude = (EditText) view.findViewById(R.id.edtLatitude);
        edtLongitude = (EditText) view.findViewById(R.id.edtLongitude);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOk:
                String latitudeString = edtLatitude.getText().toString();
                String longitudeString = edtLatitude.getText().toString();
                try {
                    float latitude = Float.valueOf(latitudeString);
                    float longitude = Float.valueOf(longitudeString);
                    if (latitude > 90 || latitude < -90) {
                        Toast.makeText(getActivity(), getString(R.string.ERROR_MESSAGE_LATITUDE_VALUE), Toast.LENGTH_SHORT).show();
                    } else if (longitude > 180 || longitude < -180) {
                        Toast.makeText(getActivity(), getString(R.string.ERROR_MESSAGE_LONGITUDE_VALUE), Toast.LENGTH_SHORT).show();
                    } else {
                        latLng = new LatLng(latitude, longitude);
                        dismiss();
                    }
                } catch (NumberFormatException nfe) {
                    Toast.makeText(getActivity(), getString(R.string.ERROR_MESSAGE_GPS_FORMAT), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btnCancel:
                latLng = null;
                dismiss();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.dismissLocationDialog(latLng);
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        try {
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            //if (BuildConfig.DEBUG) Log.e(TAG, "Error. Activity has been destroyed");
            e.printStackTrace();
        }
    }

    public interface OnDismissLocationDialogListener {
        public void dismissLocationDialog(LatLng latLng);
    }
}
