package com.e.tablettest;

import android.util.Log;
import android.os.AsyncTask;
import org.libplctag.Tag;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class AsyncReadTaskModbus  extends AsyncTask<ArrayList<ArrayList<String>>, Void, String> {

    private static final String TAG = "Modbus Read Activity";

    String value = "", tempValue = "", callerID = "", gateway_path = "", name = "", dataType = "";
    int tag_id, timeout, number_of_addresses, elem_size, elem_count, strLength;
    HashMap<String, Integer> dict = new HashMap<>();
    private Tag MBMaster = new Tag();

    private String byteOrder = null;
    private String[] int16byteOrder = new String[]{"int16_byte_order=10", "int16_byte_order=01"};
    private String[] int32byteOrder = new String[]{"int32_byte_order=3210", "int32_byte_order=2301",
            "int32_byte_order=1032", "int32_byte_order=0123"};
    private String[] int64byteOrder = new String[]{"int64_byte_order=76543210", "int64_byte_order=67452301",
            "int64_byte_order=10325476", "int64_byte_order=01234567"};

    MBTaskCallback MBtaskCallback = MainActivity.MBtaskCallback;

    @Override
    @SafeVarargs
    protected final String doInBackground(ArrayList<ArrayList<String>>... params) {
        Log.v(TAG,"On doInBackground...");

        timeout = Integer.parseInt(params[0].get(0).get(1));
        number_of_addresses = params[0].get(1).size();
        gateway_path = params[0].get(0).get(0);

        String[] values = new String[number_of_addresses];
        String[] tags = new String[number_of_addresses];
        String[] dType = new String[number_of_addresses];
        int[] bitIndex = new int[number_of_addresses];
        Arrays.fill(values, "");
        Arrays.fill(tags, "");
        Arrays.fill(dType, "");
        Arrays.fill(bitIndex, -1);

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
                        strLength = Integer.parseInt(fullString.substring(fullString.lastIndexOf(';') + 1));
                        if (bitIndex[i] > 0)
                            bitIndex[i] --;
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

                            if (swapBytes){
                                byteOrder = int16byteOrder[0];
                            } else {
                                byteOrder = int16byteOrder[1];
                            }

                            break;
                        case "int32":
                        case "uint32":
                        case "float32":
                            elem_size = 2;
                            elem_count = 2;

                            if (swapBytes){
                                if (swapWords){
                                    byteOrder = int32byteOrder[0];
                                } else {
                                    byteOrder = int32byteOrder[2];
                                }
                            } else {
                                if (swapWords){
                                    byteOrder = int32byteOrder[1];
                                } else {
                                    byteOrder = int32byteOrder[3];
                                }
                            }

                            break;
                        case "int64":
                        case "uint64":
                        case "float64":
                            elem_size = 2;
                            elem_count = 4;

                            if (swapBytes){
                                if (swapWords){
                                    byteOrder = int64byteOrder[0];
                                } else {
                                    byteOrder = int64byteOrder[2];
                                }
                            } else {
                                if (swapWords){
                                    byteOrder = int64byteOrder[1];
                                } else {
                                    byteOrder = int64byteOrder[3];
                                }
                            }

                            break;
                        case "int128":
                        case "uint128":
                            elem_size = 2;
                            elem_count = 8;
                            break;
                        case "string":
                            elem_size = 2;
                            elem_count = (int)Math.ceil((float)strLength / (float)elem_size);

                            if (swapBytes){
                                byteOrder = int16byteOrder[0];
                            } else {
                                byteOrder = int16byteOrder[1];
                            }

                            break;
                    }

                    String tagModbusString = "protocol=modbus_tcp&";

                    if (byteOrder != null){
                        tagModbusString += gateway_path + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name + "&elem_type=" + dataType + "&" + byteOrder;
                    } else {
                        tagModbusString += gateway_path + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name + "&elem_type=" + dataType;
                    }

                    tag_id = MBMaster.TagCreate(tagModbusString, timeout);

                    while (MBMaster.getStatus(tag_id) == 1){
                        try {
                            Thread.sleep(10);
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

                            if (bitIndex[i] > -1){
                                switch (dType[i]) {
                                    case "int8":
                                    case "uint8":
                                    case "int16":
                                    case "uint16":
                                        tempValue = ExtractInt16Bit((short) MBMaster.getUInt16(id, 0), bitIndex[i]);
                                        break;
                                    case "int32":
                                    case "uint32":
                                    case "float32":
                                        tempValue = ExtractInt32Bit((int) MBMaster.getUInt32(id, 0), bitIndex[i]);
                                        break;
                                    case "int64":
                                    case "uint64":
                                    case "float64":
                                        tempValue = ExtractInt64Bit(MBMaster.getUInt64(id, 0).longValue(), bitIndex[i]);
                                        break;
                                    case "int128":
                                    case "uint128":
                                        byte[] biBytes = new byte[elem_size * elem_count];

                                        for (int z = 0; z < biBytes.length; z++) {
                                            biBytes[z] = (byte) MBMaster.getUInt8(id, z);
                                        }
                                        tempValue = ExtractInt128Bit(SwapCheck(biBytes), bitIndex[i]);

                                        break;
                                    case "string":
                                        byte[] bytes = new byte[elem_size * elem_count];

                                        for (int z = 0; z < bytes.length; z++) {
                                            bytes[z] = (byte) MBMaster.getUInt8(id, z);
                                        }

                                        if (!swapBytes && swapWords){
                                            try {
                                                tempValue = new String(new byte[] {SwapCheck(bytes)[bitIndex[i]]}, "UTF-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            try {
                                                tempValue = new String(new byte[] {bytes[bitIndex[i]]}, "UTF-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        break;
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
                                    case "int64":
                                        tempValue = String.valueOf(MBMaster.getInt64(id,0));
                                        break;
                                    case "uint64":
                                        tempValue = String.valueOf(MBMaster.getUInt64(id,0));
                                        break;
                                    case "int128":
                                        byte[] biBytes = new byte[elem_size * elem_count];

                                        for (int z = 0; z < biBytes.length; z++) {
                                            biBytes[z] = (byte) MBMaster.getUInt8(id, z);
                                        }

                                        byte[] biSwapped = SwapCheck(biBytes);
                                        tempValue = String.valueOf(BitConverterInt128(BigInteger2binaryString(biSwapped)));

                                        break;
                                    case "uint128":
                                        byte[] ubiBytes = new byte[elem_size * elem_count];

                                        for (int z = 0; z < ubiBytes.length; z++) {
                                            ubiBytes[z] = (byte) MBMaster.getUInt8(id, z);
                                        }

                                        byte[] ubiSwapped = SwapCheck(ubiBytes);
                                        tempValue = String.valueOf(BitConverterUInt128(BigInteger2binaryString(ubiSwapped)));

                                        break;
                                    case "float32":
                                        tempValue = String.valueOf(MBMaster.getFloat32(id,0));
                                        break;
                                    case "float64":
                                        tempValue = String.valueOf(MBMaster.getFloat64(id,0));
                                        break;
                                    case "bool":
                                        int val = MBMaster.getBit(id,0);

                                        if (MainActivity.boolDisplay.equals("One : Zero")){
                                            tempValue = String.valueOf(val);
                                        } else if (MainActivity.boolDisplay.equals("On : Off")){
                                            if (val == 1){
                                                tempValue = "On";
                                            } else {
                                                tempValue = "Off";
                                            }
                                        } else {
                                            if (val == 1){
                                                tempValue = "True";
                                            } else {
                                                tempValue = "False";
                                            }
                                        }
                                        break;
                                    case "string":
                                        byte[] bytes = new byte[elem_size * elem_count];

                                        for (int z = 0; z < bytes.length; z++) {
                                            bytes[z] = (byte) MBMaster.getUInt8(id, z);
                                        }

                                        byte[] swappedBytes = SwapCheck(bytes);

                                        try {
                                            tempValue = new String(swappedBytes, "UTF-8");
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

                            tag_id = MBMaster.TagCreate(tags[i], timeout);

                            while (MBMaster.getStatus(tag_id) == 1){
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (MBMaster.getStatus(tag_id) == 0){
                                dict.put(tags[i], tag_id);
                            }
                        }
                    }
                }

                // If the value has changed then publish progress on UI thread
                if (!values[i].equals(tempValue)){
                    values[i] = tempValue;
                    value = tempValue;
                    callerID = params[0].get(2).get(i);

                    publishProgress();
                }

                // Optional routine to slow down the communication
                //try {
                //    Thread.sleep(100);
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                //}
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

    private String ExtractInt128Bit(byte[] value, int BitToReturn)
    {
        char[] bitString = BigInteger2binaryString(value);

        if (bitString[127 - BitToReturn] == '0')
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "0";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "Off";
            else
                return "False";
        }
        else
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "1";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "On";
            else
                return "True";
        }
    }

    private String ExtractInt64Bit(long value, int BitToReturn)
    {
        char[] bitString = String.format("%64s", Long.toBinaryString(value)).replace(' ', '0').toCharArray();

        if (bitString[63 - BitToReturn] == '0')
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "0";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "Off";
            else
                return "False";
        }
        else
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "1";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "On";
            else
                return "True";
        }
    }

    private String ExtractInt32Bit(int value, int BitToReturn)
    {
        char[] bitString = String.format("%32s", Integer.toBinaryString(value)).replace(' ', '0').toCharArray();

        if (bitString[31 - BitToReturn] == '0')
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "0";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "Off";
            else
                return "False";
        }
        else
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "1";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "On";
            else
                return "True";
        }
    }

    private String ExtractInt16Bit(short value, int BitToReturn)
    {
        char[] bitString = String.format("%16s", Integer.toBinaryString(0xFFFF & value)).replace(' ', '0').toCharArray();

        if (bitString[15 - BitToReturn] == '0')
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "0";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "Off";
            else
                return "False";
        }
        else
        {
            if (MainActivity.boolDisplay.equals("One : Zero"))
                return "1";
            else if (MainActivity.boolDisplay.equals("On : Off"))
                return "On";
            else
                return "True";
        }
    }
}
