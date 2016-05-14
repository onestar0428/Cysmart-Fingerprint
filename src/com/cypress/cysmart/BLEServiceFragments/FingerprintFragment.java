package com.cypress.cysmart.BLEServiceFragments;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cypress.cysmart.BLEConnectionServices.BluetoothLeService;
import com.cypress.cysmart.CommonUtils.Constants;
import com.cypress.cysmart.CommonUtils.GattAttributes;
import com.cypress.cysmart.CommonUtils.Logger;
import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.R;

import java.util.List;

/**
 * Created by Michael Tien on 2016-05-11.
 */
public class FingerprintFragment extends Fragment {
    // GATT service and characteristics
    private static BluetoothGattService mCurrentservice;
    private static BluetoothGattCharacteristic mFingerprintCharacteristic;

    // Data view variables
//    private ImageView mRGBcanavs;
    //private ImageView mcolorpicker;
    private ViewGroup mViewContainer;
//    private TextView mTextred;
//    private TextView mTextgreen;
//    private TextView mTextblue;
//    private TextView mTextalpha;
//    private ImageView mColorindicator;
//    private SeekBar mIntensityBar;
    private RelativeLayout mParentRelLayout;
    private ImageView mFingerprint;
    private Button mStartButton;
    private Button mStopButton;
    private byte[] fingerprint_values;

    //ProgressDialog
    private ProgressDialog mProgressDialog;

    // Data variables
//    private float mWidth;
//    private float mHeight;
//    private String mHexRed;
//    private String mHexGreen;
//    private String mHexBlue;
    private View mRootView;
    // Flag
    private boolean mIsReaded = false;
    private Bitmap mBitmap;
//    private int mRed, mGreen, mBlue, mIntensity;
    /**
     * BroadcastReceiver for receiving the GATT server status
     */
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDING) {
                    // Bonding...
                    Logger.i("Bonding is in process....");
                    Utils.bondingProgressDialog(getActivity(), mProgressDialog, true);
                } else if (state == BluetoothDevice.BOND_BONDED) {
                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + BluetoothLeService.getmBluetoothDeviceName() + "|"
                            + BluetoothLeService.getmBluetoothDeviceAddress() + "]" +
                            getResources().getString(R.string.dl_commaseparator) +
                            getResources().getString(R.string.dl_connection_paired);
                    Logger.datalog(dataLog);
                    Utils.bondingProgressDialog(getActivity(), mProgressDialog, false);
                    getGattData();

                } else if (state == BluetoothDevice.BOND_NONE) {
                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + BluetoothLeService.getmBluetoothDeviceName() + "|"
                            + BluetoothLeService.getmBluetoothDeviceAddress() + "]" +
                            getResources().getString(R.string.dl_commaseparator) +
                            getResources().getString(R.string.dl_connection_unpaired);
                    Logger.datalog(dataLog);
                    Utils.bondingProgressDialog(getActivity(), mProgressDialog, false);
                }
            } else
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (extras.containsKey(Constants.EXTRA_FINGEPRINT_VALUE)) {
                    fingerprint_values = extras
                            .getByteArray(Constants.EXTRA_FINGEPRINT_VALUE);
                    //CapsenseServiceProximity.displayLiveData(received_proximity_rate);
                    // display it
                    updateFingerprintImage();
                }
            }

        }

    };

    public FingerprintFragment create(BluetoothGattService currentservice) {
        mCurrentservice = currentservice;
        FingerprintFragment fragment = new FingerprintFragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fingerpint_sensor, container,
                    false);
        getActivity().getActionBar().setTitle(R.string.fingerprint_sensor);
        setUpControls();
        setHasOptionsMenu(true);
        return mRootView;
    }

    /**
     * Method to set up the GAMOT view
     */
    void setUpControls() {
        mParentRelLayout = (RelativeLayout) mRootView.findViewById(R.id.parent);
        mParentRelLayout.setClickable(true);
        mFingerprint = (ImageView) mRootView.findViewById(R.id.fingerprintImageView);
        mStartButton = (Button) mRootView.findViewById(R.id.start_scan_btn);
        mStartButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFingerprintNotification(true);
            }
        });

        mStopButton = (Button) mRootView.findViewById(R.id.stop_scan_btn);
        mStopButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFingerprintNotification(false);
            }
        });
//        mRGBcanavs = (ImageView) mRootView.findViewById(R.id.imgrgbcanvas);
//        mcolorpicker = (ImageView) mRootView.findViewById(R.id.imgcolorpicker);
//        mTextalpha = (TextView) mRootView.findViewById(R.id.txtintencity);
//        mTextred = (TextView) mRootView.findViewById(R.id.txtred);
//        mTextgreen = (TextView) mRootView.findViewById(R.id.txtgreen);
//        mTextblue = (TextView) mRootView.findViewById(R.id.txtblue);
//        mColorindicator = (ImageView) mRootView
//                .findViewById(R.id.txtcolorindicator);
        mViewContainer = (ViewGroup) mRootView.findViewById(R.id.viewgroup);
//        mIntensityBar = (SeekBar) mRootView.findViewById(R.id.intencitychanger);
        mProgressDialog = new ProgressDialog(getActivity());
//        BitmapDrawable mBmpdwbl = (BitmapDrawable) mRGBcanavs.getDrawable();
//        mBitmap = mBmpdwbl.getBitmap();
//        Drawable d = getResources().getDrawable(R.drawable.gamut);
//        mRGBcanavs.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_MOVE
//                        || event.getAction() == MotionEvent.ACTION_DOWN
//                        || event.getAction() == MotionEvent.ACTION_UP) {
//
//                    float x = event.getX();
//                    float y = event.getY();
//                    if (x >= 0 && y >= 0) {
//
//                        int x1 = (int) x;
//                        int y1 = (int) y;
//                        if (x < mBitmap.getWidth() && y < mBitmap.getHeight()) {
//                            int p = mBitmap.getPixel(x1, y1);
//                            if (p != 0) {
//                                if (x > mRGBcanavs.getMeasuredWidth())
//                                    x = mRGBcanavs.getMeasuredWidth();
//                                if (y > mRGBcanavs.getMeasuredHeight())
//                                    y = mRGBcanavs.getMeasuredHeight();
//                                setwidth(1.f / mRGBcanavs.getMeasuredWidth()
//                                        * x);
//                                setheight(1.f - (1.f / mRGBcanavs
//                                        .getMeasuredHeight() * y));
//                                mRed = Color.red(p);
//                                mGreen = Color.green(p);
//                                mBlue = Color.blue(p);
//                                UIupdation();
//                                mIsReaded = false;
//                                moveTarget();
//                                return true;
//                            }
//                        }
//                    }
//                }
//                return false;
//            }
//        });

//        mIntensity = mIntensityBar.getProgress();
//        mTextalpha.setText(String.format("0x%02x", mIntensity));
//        // Seek bar progress change listener
//        mIntensityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            public void onProgressChanged(SeekBar seekBar, int progress,
//                                          boolean fromUser) {
//
//                mIntensity = progress;
//                UIupdation();
//                mIsReaded = false;
//
//            }
//
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                mIsReaded = false;
//                BluetoothLeService.writeCharacteristicRGB(mFingerprintCharacteristic,
//                        mRed, mGreen, mBlue, mIntensity);
//
//            }
//        });
    }

    @Override
    public void onResume() {
        getActivity().registerReceiver(mGattUpdateReceiver,
                Utils.makeGattUpdateIntentFilter());
        getGattData();
        Utils.setUpActionBar(getActivity(),
                getResources().getString(R.string.fingerprint_sensor));
        super.onResume();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }


    private void UIupdation() {
//        String hexColor = String
//                .format("#%02x%02x%02x%02x", mIntensity, mRed, mGreen, mBlue);
//        mColorindicator.setBackgroundColor(Color.parseColor(hexColor));
//        mTextalpha.setText(String.format("0x%02x", mIntensity));
//        mHexRed = String.format("0x%02x", mRed);
//        mHexGreen = String.format("0x%02x", mGreen);
//        mHexBlue = String.format("0x%02x", mBlue);
//        mTextred.setText(mHexRed);
//        mTextblue.setText(mHexBlue);
//        mTextgreen.setText(mHexGreen);
//        mTextalpha.setText(String.format("0x%02x", mIntensity));
//        try {
//            Logger.i("Writing value-->" + mRed + " " + mGreen + " " + mBlue + " " + mIntensity);
//            BluetoothLeService.writeCharacteristicRGB(mFingerprintCharacteristic, mRed,
//                    mGreen, mBlue, mIntensity);
//        } catch (Exception e) {
//
//        }

    }

    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        List<BluetoothGattCharacteristic> gattCharacteristics = mCurrentservice
                .getCharacteristics();
        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
            String uuidchara = gattCharacteristic.getUuid().toString();
            if (uuidchara.equalsIgnoreCase(GattAttributes.FINGERPRINT) //|| (uuidchara.equalsIgnoreCase(GattAttributes.FINGERPRINT_CUSTOM))
                    ) {
                mFingerprintCharacteristic = gattCharacteristic;
                setFingerprintNotification(true);
                break;
            }
        }
    }
    void setFingerprintNotification(boolean flag){
        if ( mFingerprintCharacteristic != null)
            BluetoothLeService.setCharacteristicNotification(mFingerprintCharacteristic, flag);
    }
    /**
     * Moving the color picker object
     */

//    void moveTarget() {
//        float x = getwidth() * mRGBcanavs.getMeasuredWidth();
//        float y = (1.f - getheigth()) * mRGBcanavs.getMeasuredHeight();
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mcolorpicker
//                .getLayoutParams();
//        layoutParams.leftMargin = (int) (mRGBcanavs.getLeft() + x
//                - Math.floor(mcolorpicker.getMeasuredWidth() / 2) - mViewContainer
//                .getPaddingLeft());
//        layoutParams.topMargin = (int) (mRGBcanavs.getTop() + y
//                - Math.floor(mcolorpicker.getMeasuredHeight() / 2) - mViewContainer
//                .getPaddingTop());
//        mcolorpicker.setLayoutParams(layoutParams);
//    }

//    private float getwidth() {
//        return mWidth;
//    }
//
//    private float getheigth() {
//        return mHeight;
//    }
//
//    private void setwidth(float sat) {
//        mWidth = sat;
//    }
//
//    private void setheight(float val) {
//        mHeight = val;
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.global, menu);
        MenuItem graph = menu.findItem(R.id.graph);
        MenuItem log = menu.findItem(R.id.log);
        MenuItem search = menu.findItem(R.id.search);
        MenuItem pairCache = menu.findItem(R.id.pairing);
        if (Utils.getBooleanSharedPreference(getActivity(), Constants.PREF_PAIR_CACHE_STATUS)) {
            pairCache.setChecked(true);
        } else {
            pairCache.setChecked(false);
        }
        search.setVisible(false);
        graph.setVisible(false);
        log.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRootView = inflater.inflate(R.layout.fingerpint_sensor, null);
            ViewGroup rootViewG = (ViewGroup) getView();
            // Remove all the existing views from the root view.
            rootViewG.removeAllViews();
            rootViewG.addView(mRootView);
            setUpControls();
//            setDefaultColorPickerPositionColor();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            LayoutInflater inflater = (LayoutInflater) getActivity()
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            mRootView = inflater.inflate(R.layout.rgb_view_portrait, null);
//            ViewGroup rootViewG = (ViewGroup) getView();
//            // Remove all the existing views from the root view.
//            rootViewG.removeAllViews();
//            rootViewG.addView(mRootView);
//            setUpControls();
//            setDefaultColorPickerPositionColor();
//
//        }
    }
    private static Bitmap getBitmapFromPgm(byte[] grays, int width, int height){

        // Create pixel array, and expand 8 bit gray to ARGB_8888
        int[] pixels = new int[width  * height];
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = grays[i] & 0xff;
                pixels[i] = 0xff000000 | gray << 16 | gray << 8 | gray;
                i++;
            }
        }
        Bitmap pgm = Bitmap.createBitmap(pixels, width, height, android.graphics.Bitmap.Config.ARGB_8888);
        return pgm;
    }
    private byte value_offset;
    private void updateFingerprintImage() {

        int index = (int)(fingerprint_values[0] & 0xff) + (int)((fingerprint_values[1] << 8) & 0xff);
        if ( index != 95 && index != 191) return;
        byte[] fp = new byte[192*192];
        int k = 0;
        for ( int i = 0; i < 192; i ++ ) {
            for ( int j = 0; j < 192; j ++) {
                fp[k++] = (byte)(i + j + value_offset);
            }
        }
        value_offset+=30;
        Bitmap pgm= getBitmapFromPgm( fp, 192, 192);
        mFingerprint.setImageBitmap(pgm);
        // this is background thread, use postInvalidate
        mFingerprint.postInvalidate();
    }
}
