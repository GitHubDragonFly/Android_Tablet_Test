package com.e.tablettest;

import android.os.AsyncTask;
import android.util.Log;

import org.libplctag.Tag;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class AsyncReadTaskModbus  extends AsyncTask<ArrayList<ArrayList<String>>, Void, String> {

    private static final String TAG = "Modbus Read Activity";

    String value = "", tempValue = "", callerID = "", gateway_path = "", name = "", dataType = "";
    int tag_id, timeout, number_of_addresses, elem_size, elem_count, stringLength;
    HashMap<String, Integer> dict = new HashMap<>();
    private final Tag MBMaster = new Tag();

    private String byteOrder = null;
    private final String[] int16byteOrder = new String[]{"int16_byte_order=10", "int16_byte_order=01"};
    private final String[] int32byteOrder = new String[]{"int32_byte_order=3210", "int32_byte_order=2301",
            "int32_byte_order=1032", "int32_byte_order=0123"};
    private final String[] int64byteOrder = new String[]{"int64_byte_order=76543210", "int64_byte_order=67452301",
            "int64_byte_order=10325476", "int64_byte_order=01234567"};
    private final String[] float32byteOrder = new String[]{"float32_byte_order=3210", "float32_byte_order=2301",
            "float32_byte_order=1032", "float32_byte_order=0123"};
    private final String[] float64byteOrder = new String[]{"float64_byte_order=76543210", "float64_byte_order=67452301",
            "float64_byte_order=10325476", "float64_byte_order=01234567"};

    MBTaskCallback MBtaskCallback = MainActivity.MBtaskCallback;

    @Override
    @SafeVarargs
    protected final String doInBackground(ArrayList<ArrayList<String>>... params) {
        Log.v(TAG,"On doInBackground...");

        timeout = Integer.parseInt(params[0].get(0).get(1));
        number_of_addresses = params[0].get(1).size();
        gateway_path = params[0].get(0).get(0);

        String[] tags = new String[number_of_addresses];
        String[] dType = new String[number_of_addresses];
        int[] bitIndex = new int[number_of_addresses];
        int[] strLength = new int[number_of_addresses];
        Arrays.fill(tags, "");
        Arrays.fill(dType, "");
        Arrays.fill(bitIndex, -1);
        Arrays.fill(strLength, -1);

        boolean swapBytes = MainActivity.cbSwapBytesChecked;
        boolean swapWords = MainActivity.cbSwapWordsChecked;

        while (!isCancelled()){
            for (int i = 0; i < number_of_addresses; i++) {
                if (dict.size() != number_of_addresses){
                    String fullString = params[0].get(1).get(i);
                    fullString = fullString.replace(" ", "");

                    name = fullString.substring(0, fullString.indexOf(';'));

                    if (name.contains("/")){
                        bitIndex[i] = Integer.parseInt(name.substring(name.indexOf('/') + 1));
                        name = name.substring(0, name.indexOf('/'));
                    }

                    if (fullString.indexOf(';') < fullString.lastIndexOf(';'))
                        dataType = fullString.substring(fullString.indexOf(';') + 1, fullString.lastIndexOf(';'));
                    else
                        dataType = fullString.substring(fullString.indexOf(';') + 1);

                    if (dataType.equals("string")){
                        stringLength = Integer.parseInt(fullString.substring(fullString.lastIndexOf(';') + 1));
                        if (bitIndex[i] > 0)
                            bitIndex[i] -= 1;
                    }

                    switch (dataType) {
                        case "bool":
                            elem_size = 1;
                            elem_count = 1;
                            break;
                        case "int8":
                        case "uint8":
                        case "int16":
                        case "uint16":
                            elem_size = 2;
                            elem_count = 1;

                            if (swapBytes)
                                byteOrder = int16byteOrder[1];
                            else
                                byteOrder = int16byteOrder[0];

                            break;
                        case "int32":
                        case "uint32":
                            elem_size = 2;
                            elem_count = 2;

                            if (swapBytes){
                                if (swapWords)
                                    byteOrder = int32byteOrder[3];
                                else
                                    byteOrder = int32byteOrder[2];
                            } else {
                                if (swapWords)
                                    byteOrder = int32byteOrder[1];
                                else
                                    byteOrder = int32byteOrder[0];
                            }

                            break;
                        case "float32":
                            elem_size = 2;
                            elem_count = 2;

                            if (swapBytes){
                                if (swapWords)
                                    byteOrder = float32byteOrder[3];
                                else
                                    byteOrder = float32byteOrder[2];
                            } else {
                                if (swapWords)
                                    byteOrder = float32byteOrder[1];
                                else
                                    byteOrder = float32byteOrder[0];
                            }

                            break;
                        case "int64":
                        case "uint64":
                            elem_size = 2;
                            elem_count = 4;

                            if (swapBytes){
                                if (swapWords)
                                    byteOrder = int64byteOrder[3];
                                else
                                    byteOrder = int64byteOrder[2];
                            } else {
                                if (swapWords)
                                    byteOrder = int64byteOrder[1];
                                else
                                    byteOrder = int64byteOrder[0];
                            }

                            break;
                        case "float64":
                            elem_size = 2;
                            elem_count = 4;

                            if (swapBytes){
                                if (swapWords)
                                    byteOrder = float64byteOrder[3];
                                else
                                    byteOrder = float64byteOrder[2];
                            } else {
                                if (swapWords)
                                    byteOrder = float64byteOrder[1];
                                else
                                    byteOrder = float64byteOrder[0];
                            }

                            break;
                        case "int128":
                        case "uint128":
                            elem_size = 2;
                            elem_count = 8;
                            break;
                        case "string":
                            elem_size = 2;
                            elem_count = (int)Math.ceil((float)stringLength / 2F);
                            strLength[i] = elem_size * elem_count;
                            break;
                    }

                    String tagModbusString = "protocol=modbus_tcp&";

                    if (byteOrder != null){
                        tagModbusString += gateway_path + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name + "&" + byteOrder;
                    } else {
                        tagModbusString += gateway_path + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name;
                    }

                    tag_id = MBMaster.TagCreate(tagModbusString, timeout);

                    while (MBMaster.getStatus(tag_id) == 1){
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (MBMaster.getStatus(tag_id) == 0){
                        dict.put(tagModbusString, tag_id);
                        tags[i] = tagModbusString;
                        dType[i] = dataType;
                    } else {
                        if (MBMaster.getStatus(tag_id) == 1)
                            tempValue = "pending";
                        else
                            tempValue = "err " + MBMaster.getStatus(tag_id);
                    }
                }

                if (!tags[i].equals("")){
                    Integer id = dict.get(tags[i]);

                    if (id != null){
                        if (MBMaster.getStatus(id) == 0){
                            MBMaster.read(id, timeout);

                            if (MBMaster.getStatus(id) == 0){
                                tempValue = "";

                                if (bitIndex[i] > -1){
                                    if (dType[i].equals("string")){
                                        byte[] bytes = new byte[strLength[i]];

                                        for (int z = 0; z < bytes.length; z++) {
                                            bytes[z] = (byte) MBMaster.getUInt8(id, z);
                                        }

                                        byte[] swappedBytes = SwapCheck(bytes);

                                        try {
                                            tempValue = (new String(swappedBytes, "UTF-8")).trim();
                                            tempValue = String.valueOf(tempValue.charAt(bitIndex[i]));
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (dType[i].equals("int128") || dType[i].equals("uint128")){
                                        byte[] bytes = new byte[16];

                                        for (int z = 0; z < bytes.length; z++) {
                                            bytes[z] = (byte) MBMaster.getUInt8(id, z);
                                        }

                                        byte[] swappedBytes = SwapCheck(bytes);

                                        boolean bit = (new BigInteger(swappedBytes).testBit(bitIndex[i]));

                                        tempValue = BooleanDisplay(bit ? '1' : '0');
                                    } else{
                                        switch (dType[i]){
                                            case "int8":
                                            case "uint8":
                                            case "int16":
                                            case "uint16":
                                                if (swapBytes){
                                                    String stringPart0 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 0))).replace(' ', '0')).reverse().toString();
                                                    String stringPart1 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 1))).replace(' ', '0')).reverse().toString();
                                                    String binaryString = (stringPart1 + stringPart0);
                                                    tempValue = BooleanDisplay(binaryString.charAt(bitIndex[i]));
                                                } else {
                                                    tempValue = BooleanDisplay(String.valueOf(MBMaster.getBit(id, bitIndex[i])).charAt(0));
                                                }
                                                break;
                                            case "int32":
                                            case "uint32":
                                            case "float32":
                                                if (swapBytes || swapWords){
                                                    String stringPart0 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 0))).replace(' ', '0')).reverse().toString();
                                                    String stringPart1 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 1))).replace(' ', '0')).reverse().toString();
                                                    String stringPart2 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 2))).replace(' ', '0')).reverse().toString();
                                                    String stringPart3 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 3))).replace(' ', '0')).reverse().toString();
                                                    String binaryString;

                                                    if (swapBytes){
                                                        if (swapWords)
                                                            binaryString = (stringPart3 + stringPart2 + stringPart1 + stringPart0);
                                                        else
                                                            binaryString = (stringPart1 + stringPart0 + stringPart3 + stringPart2);
                                                    } else
                                                        binaryString = (stringPart2 + stringPart3 + stringPart0 + stringPart1);

                                                    tempValue = BooleanDisplay(binaryString.charAt(bitIndex[i]));
                                                } else {
                                                    tempValue = BooleanDisplay(String.valueOf(MBMaster.getBit(id, bitIndex[i])).charAt(0));
                                                }
                                                break;
                                            case "int64":
                                            case "uint64":
                                            case "float64":
                                                if (swapBytes || swapWords){
                                                    String stringPart0 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 0))).replace(' ', '0')).reverse().toString();
                                                    String stringPart1 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 1))).replace(' ', '0')).reverse().toString();
                                                    String stringPart2 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 2))).replace(' ', '0')).reverse().toString();
                                                    String stringPart3 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 3))).replace(' ', '0')).reverse().toString();
                                                    String stringPart4 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 4))).replace(' ', '0')).reverse().toString();
                                                    String stringPart5 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 5))).replace(' ', '0')).reverse().toString();
                                                    String stringPart6 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 6))).replace(' ', '0')).reverse().toString();
                                                    String stringPart7 = new StringBuilder(String.format("%8s", Integer.toBinaryString(0xFF & MBMaster.getUInt8(id, 7))).replace(' ', '0')).reverse().toString();
                                                    String binaryString;

                                                    if (swapBytes){
                                                        if (swapWords)
                                                            binaryString = (stringPart7 + stringPart6 + stringPart5 + stringPart4 + stringPart3 + stringPart2 + stringPart1 + stringPart0);
                                                        else
                                                            binaryString = (stringPart1 + stringPart0 + stringPart3 + stringPart2 + stringPart5 + stringPart4 + stringPart7 + stringPart6);
                                                    } else
                                                        binaryString = (stringPart6 + stringPart7 + stringPart4 + stringPart5 + stringPart2 + stringPart3 + stringPart0 + stringPart1);

                                                    tempValue = BooleanDisplay(binaryString.charAt(bitIndex[i]));
                                                } else {
                                                    tempValue = BooleanDisplay(String.valueOf(MBMaster.getBit(id, bitIndex[i])).charAt(0));
                                                }
                                                break;
                                        }
                                    }
                                } else {
                                    switch (dType[i]){
                                        case "int8":
                                            if (swapBytes)
                                                tempValue = String.valueOf(MBMaster.getInt8(id,1));
                                            else
                                                tempValue = String.valueOf(MBMaster.getInt8(id,0));
                                            break;
                                        case "uint8":
                                            if (swapBytes)
                                                tempValue = String.valueOf(MBMaster.getUInt8(id,1));
                                            else
                                                tempValue = String.valueOf(MBMaster.getUInt8(id,0));
                                            break;
                                        case "int16":
                                            tempValue = String.valueOf(MBMaster.getInt16(id,0));
                                            break;
                                        case "uint16":
                                            tempValue = String.valueOf(MBMaster.getUInt16(id,0));
                                            break;
                                        case "int32":
                                            tempValue = String.valueOf(MBMaster.getInt32(id,0));
                                            break;
                                        case "uint32":
                                            tempValue = String.valueOf(MBMaster.getUInt32(id,0));
                                            break;
                                        case "float32":
                                            tempValue = String.valueOf(MBMaster.getFloat32(id,0));
                                            break;
                                        case "int64":
                                            tempValue = String.valueOf(MBMaster.getInt64(id,0));
                                            break;
                                        case "uint64":
                                            tempValue = String.valueOf(MBMaster.getUInt64(id,0));
                                            break;
                                        case "float64":
                                            tempValue = String.valueOf(MBMaster.getFloat64(id,0));
                                            break;
                                        case "int128":
                                        case "uint128":
                                            byte[] bytes = new byte[16];

                                            for (int z = 0; z < bytes.length; z++) {
                                                bytes[z] = (byte) MBMaster.getUInt8(id, z);
                                            }

                                            byte[] swappedBytes = SwapCheck(bytes);

                                            if (dType[i].equals("int128"))
                                                tempValue = String.valueOf(BitConverterInt128(BigInteger2binaryString(swappedBytes)));
                                            else
                                                tempValue = String.valueOf(BitConverterUInt128(BigInteger2binaryString(swappedBytes)));

                                            break;
                                        case "bool":
                                            tempValue = BooleanDisplay(String.valueOf(MBMaster.getBit(id,0)).charAt(0));
                                            break;
                                        case "string":
                                            byte[] strBytes = new byte[strLength[i]];

                                            for (int z = 0; z < strBytes.length; z++) {
                                                strBytes[z] = (byte) MBMaster.getUInt8(id, z);
                                            }

                                            byte[] strSwappedBytes = SwapCheck(strBytes);

                                            try {
                                                tempValue = (new String(strSwappedBytes, "UTF-8")).trim();
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                            break;
                                    }
                                }
                            } else {
                                if (MBMaster.getStatus(id) == 1)
                                    tempValue = "pending";
                                else
                                    tempValue = "err " + MBMaster.getStatus(id);

                                MBMaster.close(id);
                                dict.remove(tags[i]);
                            }
                        } else {
                            if (MBMaster.getStatus(id) == 1)
                                tempValue = "pending";
                            else
                                tempValue = "err " + MBMaster.getStatus(id);

                            MBMaster.close(id);
                            dict.remove(tags[i]);
                        }
                    }
                }
                // Publish progress on UI thread continuously, controlled with the sleep time.

                value = tempValue.trim();

                callerID = params[0].get(2).get(i);

                publishProgress();

                // Slow down the communication to give time for UI to update all the values.
                // Adjust the sleep time if necessary.
                try {
                    TimeUnit.MILLISECONDS.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.v(TAG,"doInBackground Finished");

        return "FINISHED";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.v(TAG,"On PreExecute...");
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate();

        MBtaskCallback.UpdateMBUI(callerID, value);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.v(TAG,"On PostExecute...");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.v(TAG,"On Cancelled...");

        if (dict != null && MBMaster != null){
            for (String key : dict.keySet()){
                Integer keyIntVal = dict.get(key);
                if (keyIntVal != null)
                    MBMaster.close(keyIntVal);
            }
        }
    }

    private byte[] SwapCheck(byte[] bytes)
    {
        if (MainActivity.cbSwapWordsChecked && bytes.length > 3)
        {
            for (int i = 0; i < bytes.length / 2; i++)
            {
                byte tempByte = bytes[i];
                bytes[i] = bytes[bytes.length - i - 1];
                bytes[bytes.length - i - 1] = tempByte;
            }
        }

        if (!MainActivity.cbSwapBytesChecked)
        {
            for (int i = 0; i < bytes.length; i += 2)
            {
                byte tempByte = bytes[i];
                bytes[i] = bytes[i + 1];
                bytes[i + 1] = tempByte;
            }
        }

        return bytes;
    }

    private char[] BigInteger2binaryString(byte[] BigIntegerBytes)
    {
        char[] bitString = new char[128];

        for (int i = 0; i < BigIntegerBytes.length; i++){
            char[] tempBits = String.format("%8s", Integer.toBinaryString(0xFF & BigIntegerBytes[i])).replace(' ', '0').toCharArray();
            System.arraycopy(tempBits, 0, bitString, i * 8, tempBits.length);
        }

        return bitString;
    }

    private BigInteger BitConverterInt128(char[] binaryString)
    {
        BigInteger Int128 = new BigInteger("0");
        BigInteger base = new BigInteger("2");
        BigInteger biMin = new BigInteger("-170141183460469231731687303715884105728");

        for (int i = 0; i < binaryString.length - 1; i++)
        {
            if (binaryString[127 - i] == '1')
                Int128 = Int128.add(base.pow(i));
        }

        if (binaryString[0] == '1')
            Int128 = biMin.add(Int128);

        return Int128;
    }

    private BigInteger BitConverterUInt128(char[] binaryString)
    {
        BigInteger UInt128 = new BigInteger("0");
        BigInteger base = new BigInteger("2");

        for (int i = 0; i < binaryString.length; i++)
        {
            if (binaryString[127 - i] == '1')
                UInt128 = UInt128.add(base.pow(i));
        }

        return UInt128;
    }

    private String BooleanDisplay(char bitValue){
        if (MainActivity.boolDisplay.equals("One : Zero")){
            if (bitValue == '1')
                return "1";
            else
                return "0";
        } else if (MainActivity.boolDisplay.equals("On : Off")){
            if (bitValue == '1'){
                return "On";
            } else {
                return "Off";
            }
        } else {
            if (bitValue == '1'){
                return "True";
            } else {
                return "False";
            }
        }
    }
}
