package com.e.tablettest;

import androidx.appcompat.app.AppCompatActivity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.TextView;

public class GaugeActivityMB extends AppCompatActivity implements GaugeTaskCallback {
    public static GaugeTaskCallback gaugeTaskCallback;

    AsyncGaugeTaskMB myGaugeTaskMB = null;

    TextView tvGaugeAddress;
    String txt = "";
    String[] params = new String[4];
    AngleIndicator ai1;

    ColorStateList textColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Keep the screen turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.gauge_activity_mb);

        gaugeTaskCallback = this;

        ai1 = findViewById(R.id.angleIndicatorMB1);
        tvGaugeAddress = findViewById(R.id.tvGaugeAddressMB);
        textColor = tvGaugeAddress.getTextColors();

        if (MainActivity.mbGaugeAddress.equals("")){
            txt = "Gauge PLC Address not set!";
            tvGaugeAddress.setText(txt);
        }
        else {
            String mbIPAddress, mbUnitID, timeout;

            mbIPAddress = MainActivity.mbIPAddress;
            mbUnitID = MainActivity.mbUnitID;
            timeout = MainActivity.timeout;

            mbIPAddress = mbIPAddress.replace(" ", "");
            mbUnitID = mbUnitID.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(mbIPAddress) || !TextUtils.isDigitsOnly(mbUnitID) || !TextUtils.isDigitsOnly(timeout)){
                txt = "PLC Parameter Error!";
                tvGaugeAddress.setText(txt);
            } else {
                txt = MainActivity.mbGaugeAddress;
                tvGaugeAddress.setText(txt);

                if (myGaugeTaskMB == null) {
                    myGaugeTaskMB = new AsyncGaugeTaskMB();
                }

                params[0] = "gateway=" + mbIPAddress + "&path=" + mbUnitID;
                params[1] = txt.substring(0, txt.indexOf(";"));
                params[2] = txt.substring(txt.indexOf(";") + 2);
                params[3] = timeout;

                myGaugeTaskMB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (myGaugeTaskMB != null){
            myGaugeTaskMB.cancel(true);
            myGaugeTaskMB = null;
        }
    }

    @Override
    public void UpdateGaugeValue(String value){
        if (value.startsWith("err") || value.equals("pending")){
            tvGaugeAddress.setTextColor(Color.RED);
            tvGaugeAddress.setText(value);
            ai1.setCurrentValue(0);
        } else {
            tvGaugeAddress.setTextColor(textColor);
            tvGaugeAddress.setText(txt);
            ai1.setCurrentValue(Float.parseFloat(value));
        }
    }
}
