package com.e.tablettest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class PopUpAddressMB extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public PopUpAddressMB() {
        super();
    }

    private static final int intSpinnerMBIO = R.id.spinnerMBIO;
    private static final int intSpinnerMBDataType = R.id.spinnerMBDataType;
    private static final int intSpinnerMBStringLength = R.id.spinnerMBStringLength;

    SetTags setTags = MainActivity.setTags;

    DisplayMetrics dm = new DisplayMetrics();
    String callerName;
    EditText etMBAddress;
    ArrayAdapter<String> dataAdapter;
    Spinner spinMBIO, spinMBBit, spinMBDataType, spinMBStringLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_address_mb);

        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setLayout((int)(dm.widthPixels * .5), (int)(dm.heightPixels * .25));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        callerName = MainActivity.callerName;

        etMBAddress = findViewById(R.id.etMBAddress);

        spinMBIO = findViewById(R.id.spinnerMBIO);
        spinMBIO.setOnItemSelectedListener(this);

        spinMBDataType = findViewById(R.id.spinnerMBDataType);
        spinMBDataType.setOnItemSelectedListener(this);

        spinMBBit = findViewById(R.id.spinnerMBBit);

        spinMBStringLength = findViewById(R.id.spinnerMBStringLength);
        spinMBStringLength.setOnItemSelectedListener(this);

        if (callerName.equals("etMBTagGauge"))
            dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.modbusGaugeAddress));
        else
            dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.modbusAddress));

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();
        spinMBIO.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        String[] stringArray = new String[0];

        switch(parent.getId()){
            case intSpinnerMBIO:
                if (spinMBIO.getSelectedItem().toString().equals("CO") || spinMBIO.getSelectedItem().toString().equals("DI")){
                    stringArray = getResources().getStringArray(R.array.modbus_data_type_bool);
                    dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    spinMBDataType.setAdapter(dataAdapter);
                    spinMBBit.setEnabled(false);
                } else {
                    if (callerName.equals("etMBTagGauge")){
                        stringArray = getResources().getStringArray(R.array.gauge_data_type);
                        spinMBBit.setEnabled(false);
                    }
                    else {
                        stringArray = getResources().getStringArray(R.array.modbus_data_type);
                        spinMBBit.setEnabled(true);
                    }

                    dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    spinMBDataType.setAdapter(dataAdapter);
                }
                break;
            case intSpinnerMBDataType:
                spinMBStringLength.setEnabled(spinMBDataType.getSelectedItem().toString().equals("string"));

                if (spinMBBit.isEnabled()){
                    switch (spinMBDataType.getSelectedItem().toString()){
                        case "int8":
                        case "uint8":
                            stringArray = getResources().getStringArray(R.array.bits_8bit);
                            break;
                        case "int16":
                        case "uint16":
                            stringArray = getResources().getStringArray(R.array.bits_16bit);
                            break;
                        case "int32":
                        case "uint32":
                        case "float32":
                            stringArray = getResources().getStringArray(R.array.bits_32bit);
                            break;
                        case "int64":
                        case "uint64":
                        case "float64":
                            stringArray = getResources().getStringArray(R.array.bits_64bit);
                            break;
                        case "int128":
                        case "uint128":
                            stringArray = getResources().getStringArray(R.array.bits_128bit);
                            break;
                        case "string":
                            stringArray = new String[]{"None", "1"};
                            break;
                    }

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    spinMBBit.setAdapter(dataAdapter);
                    break;
                }
            case intSpinnerMBStringLength:
                int index = Integer.parseInt(spinMBStringLength.getSelectedItem().toString());
                stringArray = new String[index + 1];
                stringArray[0] = "None";

                for (int i = 1; i < index + 1; i++){
                    stringArray[i] = String.valueOf(i);
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dataAdapter.notifyDataSetChanged();
                spinMBBit.setAdapter(dataAdapter);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    protected void onPause() {
        super.onPause();

        View v = getCurrentFocus();

        if (v != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void sendMessageReturnTag(View v)
    {
        v.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        String addr = (etMBAddress.getText().toString());

        if (!TextUtils.isEmpty(addr)){
            int addrValue = Integer.parseInt(addr);

            if (addrValue < 65535){
                if (spinMBStringLength.isEnabled()){
                    if (spinMBBit.getSelectedItem().toString().equals("None"))
                        setTags.UpdateTags(callerName, (spinMBIO.getSelectedItem().toString().toLowerCase() + addrValue + "; " + spinMBDataType.getSelectedItem() + "; " + spinMBStringLength.getSelectedItem()));
                    else
                        setTags.UpdateTags(callerName, (spinMBIO.getSelectedItem().toString().toLowerCase() + addrValue + "/" + spinMBBit.getSelectedItem().toString() + "; " + spinMBDataType.getSelectedItem() + "; " + spinMBStringLength.getSelectedItem()));
                } else if (spinMBBit.isEnabled() && !spinMBBit.getSelectedItem().toString().equals("None")){
                    setTags.UpdateTags(callerName, (spinMBIO.getSelectedItem().toString().toLowerCase() + addrValue + "/" + spinMBBit.getSelectedItem().toString() + "; " + spinMBDataType.getSelectedItem()));
                } else {
                    setTags.UpdateTags(callerName, (spinMBIO.getSelectedItem().toString().toLowerCase() + addrValue + "; " + spinMBDataType.getSelectedItem()));
                }
            } else {
                setTags.UpdateTags(callerName, "");
            }
        } else {
            setTags.UpdateTags(callerName, "");
        }

        this.finish();
    }
}
