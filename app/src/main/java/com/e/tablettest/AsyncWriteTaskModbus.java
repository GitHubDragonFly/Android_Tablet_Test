package com.e.tablettest;

import android.os.AsyncTask;
import android.util.Log;
import org.libplctag.Tag;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

@SuppressWarnings("deprecation")
public class AsyncWriteTaskModbus extends AsyncTask<String, Void, String> {
    private static final String TAG = "Modbus Write Activity";

    public String value = "";
    private int elem_size, elem_count = 1, strLength;
    private Tag MBWriteMaster = new Tag();

    WriteTaskCallback WritetaskCallback = MainActivity.WritetaskCallback;

    @Override
    protected String doInBackground(String... params) {
        Log.v(TAG,"On doInBackground...");

        String gateway_unitId = params[0];
        int timeout = Integer.parseInt(params[1]);
        int tag_id = -1, bitIndex = -1;

        boolean swapBytes = MainActivity.cbSwapBytesChecked;
        boolean swapWords = MainActivity.cbSwapWordsChecked;

        while (!isCancelled()){
            String name = params[2].substring(0, params[2].indexOf(';'));

            if (name.startsWith("di") || name.startsWith("ir")){
                value = "MB Write Failed";
                publishProgress();
                MBWriteMaster.close(tag_id);
                Log.v(TAG,"doInBackground Finished");
                return "FINISHED";
            }

            if (name.contains("/")){
                bitIndex = Integer.parseInt(name.substring(name.indexOf('/') + 1));
                name = name.substring(0, name.indexOf('/'));
            }

            String dataType;

            if (params[2].indexOf(';') < params[2].lastIndexOf(';'))
                dataType = params[2].substring(params[2].indexOf(';') + 2, params[2].lastIndexOf(';'));
            else
                dataType = params[2].substring(params[2].indexOf(';') + 2);

            if (dataType.equals("string")){
                strLength = Integer.parseInt(params[2].substring(params[2].lastIndexOf(';') + 2));
                if (bitIndex > 0)
                    bitIndex --;
            }

            switch (dataType) {
                case "bool":
                    elem_size = 1;
                    break;
                case "int8":
                case "uint8":
                case "int16":
                case "uint16":
                    elem_size = 2;
                    break;
                case "int32":
                case "uint32":
                case "float32":
                case "bool array":
                    elem_size = 4;
                    break;
                case "int64":
                case "uint64":
                case "float64":
                    elem_size = 8;
                    break;
                case "int128":
                case "uint128":
                    elem_size = 16;
                    break;
                case "string":
                    elem_size = 2;
                    elem_count = (int)Math.ceil((float)strLength / (float)elem_size);
                    break;
            }

            String tagMBString = "protocol=modbus_tcp&";
            tagMBString += gateway_unitId + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name + "&elem_type=" + dataType;

            tag_id = MBWriteMaster.TagCreate(tagMBString, timeout);

            while (MBWriteMaster.getStatus(tag_id) == 1){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (MBWriteMaster.getStatus(tag_id) == 0){
                MBWriteMaster.read(tag_id, timeout);

                if (bitIndex > -1){
                    int BitValueToWrite = 0;

                    if (!dataType.equals("string")){
                        if (params[3].equals("1") || params[3].equals("true") || params[3].equals("True")){
                            BitValueToWrite = 1;
                        } else if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False")){
                            BitValueToWrite = 0;
                        } else {
                            value = "MB Write Failed";
                            publishProgress();
                            MBWriteMaster.close(tag_id);
                            Log.v(TAG,"doInBackground Finished");
                            return "FINISHED";
                        }
                    }

                    if (dataType.equals("string")){
                        byte[] bytes = new byte[elem_size * elem_count];
                        byte[] tempBytes = new byte[0];

                        for (int z = 0; z < bytes.length; z++) {
                            bytes[z] = (byte) MBWriteMaster.getUInt8(tag_id, z);
                        }

                        try {
                            tempBytes = params[3].getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        if (tempBytes.length > 1){
                            value = "MB Write Failed";
                            publishProgress();
                            MBWriteMaster.close(tag_id);
                            Log.v(TAG,"doInBackground Finished");
                            return "FINISHED";
                        } else {
                            System.arraycopy(tempBytes, 0, bytes, bitIndex, tempBytes.length);

                            if (swapBytes && swapWords){
                                for (int z = 0; z < bytes.length; z++) {
                                    MBWriteMaster.setUInt8(tag_id, z, bytes[z]);
                                }

                            } else {
                                byte[] swappedBytes = SwapCheck(bytes);

                                for (int z = 0; z < swappedBytes.length; z++) {
                                    MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                }
                            }
                        }
                    } else if (dataType.equals("int128") || dataType.equals("uint128")){
                        if (swapBytes && swapWords){
                            MBWriteMaster.setBit(tag_id, bitIndex, BitValueToWrite);
                        } else {
                            byte[] bytes = new byte[elem_size * elem_count];

                            for (int z = 0; z < bytes.length; z++) {
                                bytes[z] = (byte) MBWriteMaster.getUInt8(tag_id, z);
                            }

                            byte[] swappedBytes = SwapCheck(bytes);

                            int byte2change = (int)Math.floor((float)bitIndex / 8F);

                            if (BitValueToWrite == 1)
                                swappedBytes[byte2change] |= 1 << (bitIndex - (byte2change * 8));
                            else
                                swappedBytes[byte2change] &= ~(1 << (bitIndex - (byte2change * 8)));

                            for (int z = 0; z < swappedBytes.length; z++) {
                                MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                            }
                        }
                    } else {
                        MBWriteMaster.setBit(tag_id, bitIndex, BitValueToWrite);
                    }
                } else {
                    switch (dataType){
                        case "int8":
                            MBWriteMaster.setInt8(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "uint8":
                            MBWriteMaster.setUInt8(tag_id, 0, Short.parseShort(params[3]));
                            break;
                        case "int16":
                            MBWriteMaster.setInt16(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "uint16":
                            MBWriteMaster.setUInt16(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "int32":
                            MBWriteMaster.setInt32(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "uint32":
                            MBWriteMaster.setUInt32(tag_id, 0, Long.parseLong(params[3]));
                            break;
                        case "int64":
                            MBWriteMaster.setInt64(tag_id, 0, Long.parseLong(params[3]));
                            break;
                        case "uint64":
                            MBWriteMaster.setUInt64(tag_id, 0, new BigInteger(params[3]));
                            break;
                        case "int128":
                            MBWriteMaster.setInt128(tag_id, 0, new BigInteger(params[3]));
                            break;
                        case "uint128":
                            MBWriteMaster.setUInt128(tag_id, 0, new BigInteger(params[3]));
                            break;
                        case "float32":
                            MBWriteMaster.setFloat32(tag_id, 0, Float.parseFloat(params[3]));
                            break;
                        case "float64":
                            MBWriteMaster.setFloat64(tag_id, 0, Double.parseDouble(params[3]));
                            break;
                        case "bool":
                            MBWriteMaster.setBit(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "string":
                            byte[] bytes = new byte[elem_size * elem_count];
                            byte[] tempBytes = new byte[0];

                            try {
                                tempBytes = params[3].getBytes("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            System.arraycopy(tempBytes, 0, bytes, 0, tempBytes.length);

                            if (swapBytes && swapWords){
                                for (int z = 0; z < bytes.length; z++) {
                                    MBWriteMaster.setUInt8(tag_id, z, bytes[z]);
                                }

                            } else {
                                byte[] swappedBytes = SwapCheck(bytes);

                                for (int z = 0; z < swappedBytes.length; z++) {
                                    MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                }
                            }
                            break;
                    }
                }

                MBWriteMaster.write(tag_id, timeout);

                value = "MB Write Success";
            } else {
                value = "MB Write Failed";
            }

            publishProgress();
        }

        MBWriteMaster.close(tag_id);

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

        WritetaskCallback.WriteUpdateUI(value);
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
    }

    private byte[] SwapCheck(byte[] bytes)
    {
        if (!MainActivity.cbSwapWordsChecked && bytes.length > 3)
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
}
