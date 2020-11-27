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

    SetTags setTags = MainActivity.setTags;

    DisplayMetrics dm = new DisplayMetrics();
    String callerName;
    EditText etMBAddr;
    Spinner spinMBIO, spinMBBit, spinMBDataType, spinMBStringLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_address_mb);

        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setLayout((int)(dm.widthPixels * .5), (int)(dm.heightPixels * .25));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        callerName = MainActivity.callerName;

        etMBAddr = findViewById(R.id.etMBAddress);

        spinMBIO = findViewById(R.id.spinnerMBIO);
        spinMBIO.setOnItemSelectedListener(this);

        spinMBDataType = findViewById(R.id.spinnerMBDataType);
        spinMBDataType.setOnItemSelectedListener(this);

        spinMBBit = findViewById(R.id.spinnerMBBit);

        spinMBStringLength = findViewById(R.id.spinnerMBStringLength);
        spinMBStringLength.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        String[] stringArray = new String[0];

        switch(parent.getId()){
            case R.id.spinnerMBIO:
                if (spinMBIO.getSelectedItem().toString().equals("CO") ||
                        spinMBIO.getSelectedItem().toString().equals("DI")){
                    stringArray = getResources().getStringArray(R.array.modbus_data_type_bool);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    spinMBDataType.setAdapter(dataAdapter);
                    spinMBBit.setEnabled(false);
                } else {
                    stringArray = getResources().getStringArray(R.array.modbus_data_type);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    spinMBDataType.setAdapter(dataAdapter);
                    spinMBBit.setEnabled(true);
                }
                break;
            case R.id.spinnerMBDataType:
                if (spinMBDataType.getSelectedItem().toString().equals("string")){
                    spinMBStringLength.setEnabled(true);
                } else {
                    spinMBStringLength.setEnabled(false);
                }

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
            case R.id.spinnerMBStringLength:
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

        String addr = (etMBAddr.getText().toString());

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
