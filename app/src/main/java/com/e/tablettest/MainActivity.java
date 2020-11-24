package com.e.tablettest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.sun.jna.Platform;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SetTags,AdapterView.OnItemSelectedListener,ABTaskCallback,MBTaskCallback,WriteTaskCallback,GetCLGXTagsTaskCallback {
    public static ABTaskCallback ABtaskCallback;
    public static MBTaskCallback MBtaskCallback;
    public static GetCLGXTagsTaskCallback GetCLGXTagstaskCallback;
    public static WriteTaskCallback WritetaskCallback;
    public static SetTags setTags;

    private static final String TAG = "Main Activity";

    public static String cpu = "", callerName = "", boolDisplay = "";
    public static boolean cbSwapBytesChecked, cbSwapWordsChecked;

    AsyncReadTaskAB myTaskAB = null;
    AsyncReadTaskModbus myTaskMB = null;
    AsyncTaskGetCLGXTags myTaskGetCLGXTags = null;
    AsyncWriteTaskAB myWriteTaskAB = null;
    AsyncWriteTaskModbus myWriteTaskMB = null;

    EditText tvAB1, tvAB2, tvAB3, tvAB4, tvAB5, tvAB6, tvAB7, tvAB8, tvMB1, tvMB2, tvMB3, tvMB4, tvMB5, tvMB6, tvMB7, tvMB8;
    EditText etAB1, etAB2, etAB3, etAB4, etAB5, etAB6, etAB7, etAB8, etMB1, etMB2, etMB3, etMB4, etMB5, etMB6, etMB7, etMB8;
    EditText etABIP, etABPath, etMBIP, etMBUnitID, etTimeout, etProgram;
    ToggleButton btnAB, btnMB;
    Button btnGetCLGXTags, btnWriteABCaller, btnWriteAB1, btnWriteAB2, btnWriteAB3, btnWriteAB4, btnWriteAB5, btnWriteAB6, btnWriteAB7, btnWriteAB8;
    Button btnWriteMBCaller, btnWriteMB1, btnWriteMB2, btnWriteMB3, btnWriteMB4, btnWriteMB5, btnWriteMB6, btnWriteMB7, btnWriteMB8;
    Spinner spinABCPU, spinMBCPU, spinCLGXTags, spinBooleanDisplay;
    CheckBox cbSwapBytes, cbSwapWords;
    ColorStateList textColor;
    TextWatcher tcListener;
    boolean clearingTags;

    public static class ABAddressInfo
    {
        EditText etABTag;
        EditText etABTagValue;
        Button btnWriteAB;

        ABAddressInfo(){}
    }

    private List<ABAddressInfo> ABAddressList = new ArrayList<>();

    public static class MBAddressInfo
    {
        EditText etMBTag;
        EditText etMBTagValue;
        Button btnWriteMB;

        MBAddressInfo(){}
    }

    private static List<MBAddressInfo> MBAddressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set landscape screen mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Keep the screen turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        spinABCPU = findViewById(R.id.spinnerABCPU);
        spinABCPU.setOnItemSelectedListener(this);

        spinMBCPU = findViewById(R.id.spinnerModbus);

        spinCLGXTags = findViewById(R.id.spinnerCLGXTags);
        spinCLGXTags.setOnItemSelectedListener(this);

        spinBooleanDisplay = findViewById(R.id.spinnerBooleanDisplay);
        spinBooleanDisplay.setOnItemSelectedListener(this);

        tcListener = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!clearingTags){
                    // Enable or disable the Read/Write display boxes
                    boolean textHasValue = !charSequence.toString().equals("");

                    switch (callerName){
                        case "etABTag1":
                            tvAB1.setEnabled(textHasValue);
                            break;
                        case "etABTag2":
                            tvAB2.setEnabled(textHasValue);
                            break;
                        case "etABTag3":
                            tvAB3.setEnabled(textHasValue);
                            break;
                        case "etABTag4":
                            tvAB4.setEnabled(textHasValue);
                            break;
                        case "etABTag5":
                            tvAB5.setEnabled(textHasValue);
                            break;
                        case "etABTag6":
                            tvAB6.setEnabled(textHasValue);
                            break;
                        case "etABTag7":
                            tvAB7.setEnabled(textHasValue);
                            break;
                        case "etABTag8":
                            tvAB8.setEnabled(textHasValue);
                            break;
                        case "etMBTag1":
                            tvMB1.setEnabled(textHasValue);
                            break;
                        case "etMBTag2":
                            tvMB2.setEnabled(textHasValue);
                            break;
                        case "etMBTag3":
                            tvMB3.setEnabled(textHasValue);
                            break;
                        case "etMBTag4":
                            tvMB4.setEnabled(textHasValue);
                            break;
                        case "etMBTag5":
                            tvMB5.setEnabled(textHasValue);
                            break;
                        case "etMBTag6":
                            tvMB6.setEnabled(textHasValue);
                            break;
                        case "etMBTag7":
                            tvMB7.setEnabled(textHasValue);
                            break;
                        case "etMBTag8":
                            tvMB8.setEnabled(textHasValue);
                            break;
                    }

                    callerName = "";
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        tvAB1 = findViewById(R.id.tvABTagValue1);
        tvAB2 = findViewById(R.id.tvABTagValue2);
        tvAB3 = findViewById(R.id.tvABTagValue3);
        tvAB4 = findViewById(R.id.tvABTagValue4);
        tvAB5 = findViewById(R.id.tvABTagValue5);
        tvAB6 = findViewById(R.id.tvABTagValue6);
        tvAB7 = findViewById(R.id.tvABTagValue7);
        tvAB8 = findViewById(R.id.tvABTagValue8);
        tvMB1 = findViewById(R.id.tvMBTagValue1);
        tvMB2 = findViewById(R.id.tvMBTagValue2);
        tvMB3 = findViewById(R.id.tvMBTagValue3);
        tvMB4 = findViewById(R.id.tvMBTagValue4);
        tvMB5 = findViewById(R.id.tvMBTagValue5);
        tvMB6 = findViewById(R.id.tvMBTagValue6);
        tvMB7 = findViewById(R.id.tvMBTagValue7);
        tvMB8 = findViewById(R.id.tvMBTagValue8);

        textColor = tvAB1.getTextColors();

        etAB1 = findViewById(R.id.etABTag1);
        etAB1.addTextChangedListener(tcListener);
        etAB2 = findViewById(R.id.etABTag2);
        etAB2.addTextChangedListener(tcListener);
        etAB3 = findViewById(R.id.etABTag3);
        etAB3.addTextChangedListener(tcListener);
        etAB4 = findViewById(R.id.etABTag4);
        etAB4.addTextChangedListener(tcListener);
        etAB5 = findViewById(R.id.etABTag5);
        etAB5.addTextChangedListener(tcListener);
        etAB6 = findViewById(R.id.etABTag6);
        etAB6.addTextChangedListener(tcListener);
        etAB7 = findViewById(R.id.etABTag7);
        etAB7.addTextChangedListener(tcListener);
        etAB8 = findViewById(R.id.etABTag8);
        etAB8.addTextChangedListener(tcListener);
        etMB1 = findViewById(R.id.etMBTag1);
        etMB1.addTextChangedListener(tcListener);
        etMB2 = findViewById(R.id.etMBTag2);
        etMB2.addTextChangedListener(tcListener);
        etMB3 = findViewById(R.id.etMBTag3);
        etMB3.addTextChangedListener(tcListener);
        etMB4 = findViewById(R.id.etMBTag4);
        etMB4.addTextChangedListener(tcListener);
        etMB5 = findViewById(R.id.etMBTag5);
        etMB5.addTextChangedListener(tcListener);
        etMB6 = findViewById(R.id.etMBTag6);
        etMB6.addTextChangedListener(tcListener);
        etMB7 = findViewById(R.id.etMBTag7);
        etMB7.addTextChangedListener(tcListener);
        etMB8 = findViewById(R.id.etMBTag8);
        etMB8.addTextChangedListener(tcListener);

        btnWriteAB1 = findViewById(R.id.btnWriteABTag1);
        btnWriteAB2 = findViewById(R.id.btnWriteABTag2);
        btnWriteAB3 = findViewById(R.id.btnWriteABTag3);
        btnWriteAB4 = findViewById(R.id.btnWriteABTag4);
        btnWriteAB5 = findViewById(R.id.btnWriteABTag5);
        btnWriteAB6 = findViewById(R.id.btnWriteABTag6);
        btnWriteAB7 = findViewById(R.id.btnWriteABTag7);
        btnWriteAB8 = findViewById(R.id.btnWriteABTag8);
        btnWriteMB1 = findViewById(R.id.btnWriteMBTag1);
        btnWriteMB2 = findViewById(R.id.btnWriteMBTag2);
        btnWriteMB3 = findViewById(R.id.btnWriteMBTag3);
        btnWriteMB4 = findViewById(R.id.btnWriteMBTag4);
        btnWriteMB5 = findViewById(R.id.btnWriteMBTag5);
        btnWriteMB6 = findViewById(R.id.btnWriteMBTag6);
        btnWriteMB7 = findViewById(R.id.btnWriteMBTag7);
        btnWriteMB8 = findViewById(R.id.btnWriteMBTag8);

        btnAB = findViewById(R.id.toggleButtonAB);
        btnMB = findViewById(R.id.toggleButtonMB);
        btnGetCLGXTags = findViewById(R.id.btnGetCLGXTags);

        etABIP = findViewById(R.id.etABIPAddress);
        etABPath = findViewById(R.id.etABPath);
        etProgram = findViewById(R.id.etCLGXProgram);
        etTimeout = findViewById(R.id.etTimeout);
        etMBIP = findViewById(R.id.etMBIPAddress);
        etMBUnitID = findViewById(R.id.etMBUnitID);

        cbSwapBytes = findViewById(R.id.cbSwapBytes);
        cbSwapWords = findViewById(R.id.cbSwapWords);

        cbSwapBytesChecked = cbSwapBytes.isChecked();
        cbSwapWordsChecked = cbSwapWords.isChecked();

        ABtaskCallback = this;
        MBtaskCallback = this;
        GetCLGXTagstaskCallback = this;
        WritetaskCallback = this;
        setTags = this;

        String osArch = "OS Arch : " + System.getProperty("os.arch");
        String platformArch = "Platform Arch : " + Platform.ARCH;

        EditText et0 = findViewById(R.id.txtOSArch);
        et0.setText(osArch);
        EditText et1 = findViewById(R.id.txtPlatformArch);
        et1.setText(platformArch);

        //Hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myTaskAB != null){
            myTaskAB.cancel(true);
            myTaskAB = null;
        }

        if (myTaskMB != null){
            myTaskMB.cancel(true);
            myTaskMB = null;
        }

        if (myWriteTaskAB != null){
            myWriteTaskAB.cancel(true);
            myWriteTaskAB = null;
        }

        if (myWriteTaskMB != null){
            myWriteTaskMB.cancel(true);
            myWriteTaskMB = null;
        }

        if (myTaskGetCLGXTags != null){
            myTaskGetCLGXTags.cancel(true);
            myTaskGetCLGXTags = null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        switch(parent.getId()){
            case R.id.spinnerABCPU:
                if (spinABCPU.getSelectedItem().toString().equals("controllogix") ||
                        spinABCPU.getSelectedItem().toString().equals("logixpccc") ||
                        spinABCPU.getSelectedItem().toString().equals("njnx")){
                    btnGetCLGXTags.setEnabled(true);
                    btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
                    spinCLGXTags.setEnabled(true);
                    etProgram.setEnabled(true);
                    String ipAddress = "192.168.1.21";
                    etABIP.setText(ipAddress);
                    etABPath.setText("1,3");
                } else {
                    btnGetCLGXTags.setEnabled(false);
                    btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
                    spinCLGXTags.setEnabled(false);
                    etProgram.setEnabled(false);
                    String ipAddress = "192.168.1.10";
                    etABIP.setText(ipAddress);
                    etABPath.setText("");
                }
                break;
            case R.id.spinnerCLGXTags:
                if (!(spinCLGXTags.getSelectedItem().toString().startsWith("*") || spinCLGXTags.getSelectedItem().toString().startsWith("Failed") ||
                        spinCLGXTags.getSelectedItem().toString().equals("Controller + Program Tags"))) {

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Selected CLGX Tag", spinCLGXTags.getSelectedItem().toString());
                    clipboard.setPrimaryClip(clip);
                }
                break;
            case R.id.spinnerBooleanDisplay:
                boolDisplay = spinBooleanDisplay.getSelectedItem().toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void sendMessageGetCLGXTags(View v){
        String[] params = new String[3];
        String ipaddress, path, program, timeout;

        ipaddress = etABIP.getText().toString();
        path = etABPath.getText().toString();
        program = etProgram.getText().toString();
        timeout = etTimeout.getText().toString();

        ipaddress = ipaddress.replace(" ", "");
        path = path.replace(" ", "");
        program = program.replace(" ", "");
        timeout = timeout.replace(" ", "");

        if (TextUtils.isEmpty(ipaddress) || TextUtils.isEmpty(path) || TextUtils.isEmpty(program) || !TextUtils.isDigitsOnly(timeout)){
            return;
        }

        params[0] = "gateway=" + ipaddress + "&path=" + path;
        params[1] = program;
        params[2] = timeout;

        if (myTaskGetCLGXTags == null) {
            myTaskGetCLGXTags = new AsyncTaskGetCLGXTags();
        } else {
            return;
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.ab_tags_please_wait));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();
        spinCLGXTags.setAdapter(dataAdapter);

        myTaskGetCLGXTags.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        btnGetCLGXTags.setEnabled(false);
        btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        Log.v(TAG, "GetCLGXTags Task is " + myTaskGetCLGXTags.getStatus().toString());
    }

    @SuppressWarnings("unchecked")
    public void sendMessageToggleAB(View v){
        if (btnAB.getText().equals(btnAB.getTextOn())){
            if (myTaskAB == null) {
                myTaskAB = new AsyncReadTaskAB();
            } else {
                btnAB.setText(btnAB.getTextOff());
                return;
            }

            if (TextUtils.isEmpty(etAB1.getText()) && TextUtils.isEmpty(etAB2.getText()) &&
                    TextUtils.isEmpty(etAB3.getText()) && TextUtils.isEmpty(etAB4.getText()) &&
                    TextUtils.isEmpty(etAB5.getText()) && TextUtils.isEmpty(etAB6.getText()) &&
                    TextUtils.isEmpty(etAB7.getText()) && TextUtils.isEmpty(etAB8.getText())){

                btnAB.setText(btnAB.getTextOff());
                myTaskAB = null;
                return;
            }

            ArrayList<ArrayList<String>> params = new ArrayList<>();
            ArrayList<String> tagItems = new ArrayList<>(), plcAddresses = new ArrayList<>(), callerIDs = new ArrayList<>();

            String cpu, ipaddress, path, timeout;

            cpu = spinABCPU.getSelectedItem().toString();
            ipaddress = etABIP.getText().toString();
            path = etABPath.getText().toString();
            timeout = etTimeout.getText().toString();

            ipaddress = ipaddress.replace(" ", "");
            path = path.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
                myTaskAB = null;
                return;
            }

            tagItems.add("gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu);
            tagItems.add(timeout);

            params.add(tagItems);

            if (!TextUtils.isEmpty(etAB1.getText())){
                plcAddresses.add(etAB1.getText().toString());
                callerIDs.add("tvABTagValue1");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB1;
                ABinfo.etABTagValue = tvAB1;
                ABinfo.btnWriteAB = btnWriteAB1;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB2.getText())){
                plcAddresses.add(etAB2.getText().toString());
                callerIDs.add("tvABTagValue2");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB2;
                ABinfo.etABTagValue = tvAB2;
                ABinfo.btnWriteAB = btnWriteAB2;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB3.getText())){
                plcAddresses.add(etAB3.getText().toString());
                callerIDs.add("tvABTagValue3");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB3;
                ABinfo.etABTagValue = tvAB3;
                ABinfo.btnWriteAB = btnWriteAB3;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB4.getText())){
                plcAddresses.add(etAB4.getText().toString());
                callerIDs.add("tvABTagValue4");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB4;
                ABinfo.etABTagValue = tvAB4;
                ABinfo.btnWriteAB = btnWriteAB4;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB5.getText())){
                plcAddresses.add(etAB5.getText().toString());
                callerIDs.add("tvABTagValue5");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB5;
                ABinfo.etABTagValue = tvAB5;
                ABinfo.btnWriteAB = btnWriteAB5;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB6.getText())){
                plcAddresses.add(etAB6.getText().toString());
                callerIDs.add("tvABTagValue6");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB6;
                ABinfo.etABTagValue = tvAB6;
                ABinfo.btnWriteAB = btnWriteAB6;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB7.getText())){
                plcAddresses.add(etAB7.getText().toString());
                callerIDs.add("tvABTagValue7");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB7;
                ABinfo.etABTagValue = tvAB7;
                ABinfo.btnWriteAB = btnWriteAB7;
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB8.getText())){
                plcAddresses.add(etAB8.getText().toString());
                callerIDs.add("tvABTagValue8");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB8;
                ABinfo.etABTagValue = tvAB8;
                ABinfo.btnWriteAB = btnWriteAB8;
                ABAddressList.add(ABinfo);
            }

            for (ABAddressInfo abi: ABAddressList){
                abi.etABTag.setInputType(InputType.TYPE_NULL);
                abi.etABTag.setClickable(false);
                abi.etABTagValue.setInputType(InputType.TYPE_NULL);
                abi.btnWriteAB.setEnabled(false);
                abi.btnWriteAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
            }

            spinABCPU.setEnabled(false);
            etABIP.setEnabled(false);
            etABPath.setEnabled(false);
            etTimeout.setEnabled(false);

            params.add(plcAddresses);
            params.add(callerIDs);

            myTaskAB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

            btnAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));

            Log.v(TAG, "AB Task is " + myTaskAB.getStatus().toString());
        } else {
            if (myTaskAB != null){
                myTaskAB.cancel(true);
                myTaskAB = null;
            }

            for (ABAddressInfo abi: ABAddressList){
                abi.etABTag.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                abi.etABTag.setClickable(true);
                abi.etABTagValue.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                abi.etABTagValue.setText("");
                abi.btnWriteAB.setEnabled(true);
                abi.btnWriteAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
            }

            ABAddressList.clear();

            spinABCPU.setEnabled(true);
            etABIP.setEnabled(true);
            etABPath.setEnabled(true);
            if (btnMB.getText().equals(btnMB.getTextOff())){
                etTimeout.setEnabled(true);
            }

            btnAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

            Log.v(TAG, "AB Task is Cancelled");
        }
    }

    @SuppressWarnings("unchecked")
    public void sendMessageToggleMB(View v) {
        if (btnMB.getText().equals(btnMB.getTextOn())){
            if (myTaskMB == null) {
                myTaskMB = new AsyncReadTaskModbus();
            } else {
                btnMB.setText(btnMB.getTextOff());
                return;
            }

            if (TextUtils.isEmpty(etMB1.getText()) && TextUtils.isEmpty(etMB2.getText()) &&
                    TextUtils.isEmpty(etMB3.getText()) && TextUtils.isEmpty(etMB4.getText()) &&
                    TextUtils.isEmpty(etMB5.getText()) && TextUtils.isEmpty(etMB6.getText()) &&
                    TextUtils.isEmpty(etMB7.getText()) && TextUtils.isEmpty(etMB8.getText())){

                btnMB.setText(btnMB.getTextOff());
                myTaskMB = null;
                return;
            }

            ArrayList<ArrayList<String>> params = new ArrayList<>();
            ArrayList<String> tagItems = new ArrayList<>(), plcAddresses = new ArrayList<>(), callerIDs = new ArrayList<>();

            String ipaddress, unitID, timeout;

            ipaddress = etMBIP.getText().toString();
            unitID = etMBUnitID.getText().toString();
            timeout = etTimeout.getText().toString();

            ipaddress = ipaddress.replace(" ", "");
            unitID = unitID.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
                myTaskMB = null;
                return;
            }

            tagItems.add("gateway=" + ipaddress + "&path=" + unitID);
            tagItems.add(timeout);

            params.add(tagItems);

            if (!TextUtils.isEmpty(etMB1.getText())){
                plcAddresses.add(etMB1.getText().toString());
                callerIDs.add("tvMBTagValue1");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB1;
                MBinfo.etMBTagValue = tvMB1;
                MBinfo.btnWriteMB = btnWriteMB1;
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB2.getText())){
                plcAddresses.add(etMB2.getText().toString());
                callerIDs.add("tvMBTagValue2");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB2;
                MBinfo.etMBTagValue = tvMB2;
                MBinfo.btnWriteMB = btnWriteMB2;
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB3.getText())){
                plcAddresses.add(etMB3.getText().toString());
                callerIDs.add("tvMBTagValue3");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB3;
                MBinfo.etMBTagValue = tvMB3;
                MBinfo.btnWriteMB = btnWriteMB3;
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB4.getText())){
                plcAddresses.add(etMB4.getText().toString());
                callerIDs.add("tvMBTagValue4");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB4;
                MBinfo.etMBTagValue = tvMB4;
                MBinfo.btnWriteMB = btnWriteMB4;
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB5.getText())){
                plcAddresses.add(etMB5.getText().toString());
                callerIDs.add("tvMBTagValue5");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB5;
                MBinfo.etMBTagValue = tvMB5;
                MBinfo.btnWriteMB = btnWriteMB5;
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB6.getText())){
                plcAddresses.add(etMB6.getText().toString());
                callerIDs.add("tvMBTagValue6");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB6;
                MBinfo.etMBTagValue = tvMB6;
                MBinfo.btnWriteMB = btnWriteMB6;
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB7.getText())){
                plcAddresses.add(etMB7.getText().toString());
                callerIDs.add("tvMBTagValue7");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB7;
                MBinfo.etMBTagValue = tvMB7;
                MBinfo.btnWriteMB = btnWriteMB7;
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB8.getText())){
                plcAddresses.add(etMB8.getText().toString());
                callerIDs.add("tvMBTagValue8");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB8;
                MBinfo.etMBTagValue = tvMB8;
                MBinfo.btnWriteMB = btnWriteMB8;
                MBAddressList.add(MBinfo);
            }

            for (MBAddressInfo mbi: MBAddressList){
                mbi.etMBTag.setInputType(InputType.TYPE_NULL);
                mbi.etMBTag.setClickable(false);
                mbi.etMBTagValue.setInputType(InputType.TYPE_NULL);
                mbi.btnWriteMB.setEnabled(false);
                mbi.btnWriteMB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
            }

            spinMBCPU.setEnabled(false);
            etMBIP.setEnabled(false);
            etMBUnitID.setEnabled(false);
            etTimeout.setEnabled(false);
            cbSwapBytes.setEnabled(false);
            cbSwapWords.setEnabled(false);

            params.add(plcAddresses);
            params.add(callerIDs);

            myTaskMB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

            btnMB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));

            Log.v(TAG, "Modbus Task is " + myTaskMB.getStatus().toString());
        } else {
            if (myTaskMB != null){
                myTaskMB.cancel(true);
                myTaskMB = null;
            }

            for (MBAddressInfo mbi: MBAddressList){
                mbi.etMBTag.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                mbi.etMBTag.setClickable(true);
                mbi.etMBTagValue.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                mbi.etMBTagValue.setText("");
                mbi.btnWriteMB.setEnabled(true);
                mbi.btnWriteMB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
            }

            MBAddressList.clear();

            spinMBCPU.setEnabled(true);
            etMBIP.setEnabled(true);
            etMBUnitID.setEnabled(true);
            if (btnAB.getText().equals(btnAB.getTextOff())){
                etTimeout.setEnabled(true);
            }
            cbSwapBytes.setEnabled(true);
            cbSwapWords.setEnabled(true);

            btnMB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

            Log.v(TAG, "Modbus Task is Cancelled");
        }
    }

    public void sendMessageWriteAB(View v) {
        if (myWriteTaskAB == null) {
            // Clear the label indicating write success/failure
            ((TextView)findViewById(R.id.labelWriteMessage)).setText("");

            myWriteTaskAB = new AsyncWriteTaskAB();
        } else {
            return;
        }

        String cpu, ipaddress, path, timeout;

        cpu = spinABCPU.getSelectedItem().toString();
        ipaddress = etABIP.getText().toString();
        path = etABPath.getText().toString();
        timeout = etTimeout.getText().toString();

        ipaddress = ipaddress.replace(" ", "");
        path = path.replace(" ", "");
        timeout = timeout.replace(" ", "");

        if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
            myWriteTaskAB = null;
            return;
        }

        String[] params = new String[4];

        if (cpu.equals("controllogix") || cpu.equals("logixpccc") || cpu.equals("njnx")){
            params[0] = "gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu;
        } else {
            params[0] = "gateway=" + ipaddress + "&cpu=" + cpu;
        }
        params[0] = "gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu;
        params[1] = timeout;

        switch(v.getId()){
            case R.id.btnWriteABTag1:
                if (TextUtils.isEmpty(etAB1.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB1.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB1.getText().toString();
                        params[3] = tvAB1.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag2:
                if (TextUtils.isEmpty(etAB2.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB2.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB2.getText().toString();
                        params[3] = tvAB2.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag3:
                if (TextUtils.isEmpty(etAB3.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB3.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB3.getText().toString();
                        params[3] = tvAB3.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag4:
                if (TextUtils.isEmpty(etAB4.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB4.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB4.getText().toString();
                        params[3] = tvAB4.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag5:
                if (TextUtils.isEmpty(etAB5.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB5.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB5.getText().toString();
                        params[3] = tvAB5.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag6:
                if (TextUtils.isEmpty(etAB6.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB6.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB6.getText().toString();
                        params[3] = tvAB6.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag7:
                if (TextUtils.isEmpty(etAB7.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB7.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB7.getText().toString();
                        params[3] = tvAB7.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteABTag8:
                if (TextUtils.isEmpty(etAB8.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvAB8.getText())){
                        myWriteTaskAB = null;
                        return;
                    } else {
                        params[2] = etAB8.getText().toString();
                        params[3] = tvAB8.getText().toString();
                    }
                }
                break;
        }

        btnWriteABCaller = (Button)v;
        btnWriteABCaller.setEnabled(false);
        btnWriteABCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        myWriteTaskAB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        Log.v(TAG, "AB Write Task is " + myWriteTaskAB.getStatus().toString());
    }

    public void sendMessageWriteMB(View v) {
        ((TextView)findViewById(R.id.labelWriteMessage)).setText("");

        if (myWriteTaskMB == null) {
            myWriteTaskMB = new AsyncWriteTaskModbus();
        } else {
            return;
        }

        String ipaddress, unitID, timeout;

        ipaddress = etMBIP.getText().toString();
        unitID = etMBUnitID.getText().toString();
        timeout = etTimeout.getText().toString();

        ipaddress = ipaddress.replace(" ", "");
        unitID = unitID.replace(" ", "");
        timeout = timeout.replace(" ", "");

        if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
            myWriteTaskMB = null;
            return;
        }

        String[] params = new String[4];

        params[0] = "gateway=" + ipaddress + "&path=" + unitID;
        params[1] = timeout;

        switch(v.getId()){
            case R.id.btnWriteMBTag1:
                if (TextUtils.isEmpty(etMB1.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB1.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB1.getText().toString();
                        params[3] = tvMB1.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteMBTag2:
                if (TextUtils.isEmpty(etMB2.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB2.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB2.getText().toString();
                        params[3] = tvMB2.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteMBTag3:
                if (TextUtils.isEmpty(etMB3.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB3.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB3.getText().toString();
                        params[3] = tvMB3.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteMBTag4:
                if (TextUtils.isEmpty(etMB4.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB4.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB4.getText().toString();
                        params[3] = tvMB4.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteMBTag5:
                if (TextUtils.isEmpty(etMB5.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB5.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB5.getText().toString();
                        params[3] = tvMB5.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteMBTag6:
                if (TextUtils.isEmpty(etMB6.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB6.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB6.getText().toString();
                        params[3] = tvMB6.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteMBTag7:
                if (TextUtils.isEmpty(etMB7.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB7.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB7.getText().toString();
                        params[3] = tvMB7.getText().toString();
                    }
                }
                break;
            case R.id.btnWriteMBTag8:
                if (TextUtils.isEmpty(etMB8.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    if (TextUtils.isEmpty(tvMB8.getText())){
                        myWriteTaskMB = null;
                        return;
                    } else {
                        params[2] = etMB8.getText().toString();
                        params[3] = tvMB8.getText().toString();
                    }
                }
                break;
        }

        btnWriteMBCaller = (Button)v;
        btnWriteMBCaller.setEnabled(false);
        btnWriteMBCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        myWriteTaskMB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        Log.v(TAG, "Modbus Write Task is " + myWriteTaskMB.getStatus().toString());
    }

    public void sendMessageClearABTags(View v) {
        clearingTags = true;

        if (!(etAB1.getInputType() == InputType.TYPE_NULL) && !etAB1.getText().toString().equals("")){
            etAB1.setText("");
            tvAB1.setText("");
            tvAB1.setEnabled(false);
        }

        if (!(etAB2.getInputType() == InputType.TYPE_NULL) && !etAB2.getText().toString().equals("")){
            etAB2.setText("");
            tvAB2.setText("");
            tvAB2.setEnabled(false);
        }

        if (!(etAB3.getInputType() == InputType.TYPE_NULL) && !etAB3.getText().toString().equals("")){
            etAB3.setText("");
            tvAB3.setText("");
            tvAB3.setEnabled(false);
        }

        if (!(etAB4.getInputType() == InputType.TYPE_NULL) && !etAB4.getText().toString().equals("")){
            etAB4.setText("");
            tvAB4.setText("");
            tvAB4.setEnabled(false);
        }

        if (!(etAB5.getInputType() == InputType.TYPE_NULL) && !etAB5.getText().toString().equals("")){
            etAB5.setText("");
            tvAB5.setText("");
            tvAB5.setEnabled(false);
        }

        if (!(etAB6.getInputType() == InputType.TYPE_NULL) && !etAB6.getText().toString().equals("")){
            etAB6.setText("");
            tvAB6.setText("");
            tvAB6.setEnabled(false);
        }

        if (!(etAB7.getInputType() == InputType.TYPE_NULL) && !etAB7.getText().toString().equals("")){
            etAB7.setText("");
            tvAB7.setText("");
            tvAB7.setEnabled(false);
        }

        if (!(etAB8.getInputType() == InputType.TYPE_NULL) && !etAB8.getText().toString().equals("")){
            etAB8.setText("");
            tvAB8.setText("");
            tvAB8.setEnabled(false);
        }

        clearingTags = false;
    }

    public void sendMessageClearMBTags(View v) {
        clearingTags = true;

        if (!(etMB1.getInputType() == InputType.TYPE_NULL) && !etMB1.getText().toString().equals("")){
            etMB1.setText("");
            tvMB1.setText("");
            tvMB1.setEnabled(false);
        }

        if (!(etMB2.getInputType() == InputType.TYPE_NULL) && !etMB2.getText().toString().equals("")){
            etMB2.setText("");
            tvMB2.setText("");
            tvMB2.setEnabled(false);
        }

        if (!(etMB3.getInputType() == InputType.TYPE_NULL) && !etMB3.getText().toString().equals("")){
            etMB3.setText("");
            tvMB3.setText("");
            tvMB3.setEnabled(false);
        }

        if (!(etMB4.getInputType() == InputType.TYPE_NULL) && !etMB4.getText().toString().equals("")){
            etMB4.setText("");
            tvMB4.setText("");
            tvMB4.setEnabled(false);
        }

        if (!(etMB5.getInputType() == InputType.TYPE_NULL) && !etMB5.getText().toString().equals("")){
            etMB5.setText("");
            tvMB5.setText("");
            tvMB5.setEnabled(false);
        }

        if (!(etMB6.getInputType() == InputType.TYPE_NULL) && !etMB6.getText().toString().equals("")){
            etMB6.setText("");
            tvMB6.setText("");
            tvMB6.setEnabled(false);
        }

        if (!(etMB7.getInputType() == InputType.TYPE_NULL) && !etMB7.getText().toString().equals("")){
            etMB7.setText("");
            tvMB7.setText("");
            tvMB7.setEnabled(false);
        }

        if (!(etMB8.getInputType() == InputType.TYPE_NULL) && !etMB8.getText().toString().equals("")){
            etMB8.setText("");
            tvMB8.setText("");
            tvMB8.setEnabled(false);
        }

        clearingTags = false;
    }

    public void sendMessagePopUpAddressAB(View v)
    {
        callerName = getResources().getResourceEntryName(v.getId());
        cpu = spinABCPU.getSelectedItem().toString();

        Intent intent = new Intent(MainActivity.this, PopUpAddressAB.class);
        startActivity(intent);
    }

    public void sendMessagePopUpAddressMB(View v)
    {
        callerName = getResources().getResourceEntryName(v.getId());

        Intent intent = new Intent(MainActivity.this, PopUpAddressMB.class);
        startActivity(intent);
    }

    public void sendMessageScreenClean(View v)
    {
        Intent intent = new Intent(MainActivity.this, ScreenClean.class);
        startActivity(intent);
    }

    public void sendMessageCheckBoxSwapBytesWords(View v)
    {
        CheckBox cb = (CheckBox)v;

        if (cb.isChecked()){
            if (cb.getText().equals("Swap Bytes")){
                cbSwapBytesChecked = true;
            } else {
                cbSwapWordsChecked = true;
            }
        } else {
            if (cb.getText().equals("Swap Bytes")){
                cbSwapBytesChecked = false;
            } else {
                cbSwapWordsChecked = false;
            }
        }
    }

    @Override
    public void UpdateABUI(String callerID, String value) {
        TextView tv = findViewById(getResources().getIdentifier(callerID, "id", getPackageName()));

        if (value.startsWith("err") || value.equals("pending")){
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(textColor);
        }
        tv.setText(value);
    }

    @Override
    public void UpdateMBUI(String callerID, String value) {
        TextView tv = findViewById(getResources().getIdentifier(callerID, "id", getPackageName()));

        if (value.startsWith("err") || value.equals("pending")){
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(textColor);
        }
        tv.setText(value);
    }

    @Override
    public void UpdateGetCLGXTagsUI(List<String> values) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();
        spinCLGXTags.setAdapter(dataAdapter);

        if (myTaskGetCLGXTags != null){
            myTaskGetCLGXTags.cancel(true);
            myTaskGetCLGXTags = null;
        }

        btnGetCLGXTags.setEnabled(true);
        btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
    }

    @Override
    public void UpdateTags(String callerId, String value) {
        ((EditText)findViewById(getResources().getIdentifier(callerId, "id", getPackageName()))).setText(value);
        callerName = "";
    }

    @Override
    public void WriteUpdateUI(String value) {
        if (value.startsWith("AB")){
            if (myWriteTaskAB != null){
                myWriteTaskAB.cancel(true);
                myWriteTaskAB = null;
            }

            btnWriteABCaller.setEnabled(true);
            btnWriteABCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
        } else {
            if (myWriteTaskMB != null){
                myWriteTaskMB.cancel(true);
                myWriteTaskMB = null;
            }

            btnWriteMBCaller.setEnabled(true);
            btnWriteMBCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
        }

        ((TextView)findViewById(R.id.labelWriteMessage)).setText(value.substring(3));
    }
}
