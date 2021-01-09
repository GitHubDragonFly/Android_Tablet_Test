package com.e.tablettest;

import android.os.AsyncTask;
import org.libplctag.Tag;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class AsyncGaugeTaskMB extends AsyncTask<String, Void, String> {
    GaugeTaskCallback gaugeTaskCallback = GaugeActivityMB.gaugeTaskCallback;

    public String gaugeValue = "";

    HashMap<String, Integer> dict = new HashMap<>();
    private final Tag GaugeMaster = new Tag();

    int tag_id, timeout, elem_size, elem_count;

    private String byteOrder = null;
    private final String[] int16byteOrder = new String[]{"int16_byte_order=10", "int16_byte_order=01"};
    private final String[] int32byteOrder = new String[]{"int32_byte_order=3210", "int32_byte_order=2301",
            "int32_byte_order=1032", "int32_byte_order=0123"};
    private final String[] int64byteOrder = new String[]{"int64_byte_order=76543210", "int64_byte_order=67452301",
            "int64_byte_order=10325476", "int64_byte_order=01234567"};

    @Override
    protected String doInBackground(String... params) {
        String gateway_unitID = params[0];
        String name = params[1];
        String dataType = params[2];
        String tag = "";

        timeout = Integer.parseInt(params[3]);

        boolean swapBytes = MainActivity.cbSwapBytesChecked;
        boolean swapWords = MainActivity.cbSwapWordsChecked;

        while (!isCancelled()){
            String tempValue = "";

            if (dict.size() != 1){
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
                }

                String tagMBString = "protocol=modbus_tcp&";

                if (byteOrder != null){
                    tagMBString += gateway_unitID + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name + "&" + byteOrder;
                } else {
                    tagMBString += gateway_unitID + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name;
                }

                tag_id = GaugeMaster.TagCreate(tagMBString, timeout);

                while (GaugeMaster.getStatus(tag_id) == 1){
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (GaugeMaster.getStatus(tag_id) == 0){
                    dict.put(tagMBString, tag_id);
                    tag = tagMBString;
                } else {
                    if (GaugeMaster.getStatus(tag_id) == 1)
                        tempValue = "pending";
                    else
                        tempValue = "err " + GaugeMaster.getStatus(tag_id);

                    dict.clear();
                }
            }

            if (!tag.equals("")){
                Integer id = dict.get(tag);

                if (id != null){
                    if (GaugeMaster.getStatus(id) == 0){
                        GaugeMaster.read(id, timeout);
                        if (GaugeMaster.getStatus(id) == 0){
                            switch (dataType){
                                case "int8":
                                    if (swapBytes)
                                        tempValue = String.valueOf(GaugeMaster.getInt8(id,1));
                                    else
                                        tempValue = String.valueOf(GaugeMaster.getInt8(id,0));
                                    break;
                                case "uint8":
                                    if (swapBytes)
                                        tempValue = String.valueOf(GaugeMaster.getUInt8(id,1));
                                    else
                                        tempValue = String.valueOf(GaugeMaster.getUInt8(id,0));
                                    break;
                                case "int16":
                                    tempValue = String.valueOf(GaugeMaster.getInt16(id,0));
                                    break;
                                case "uint16":
                                    tempValue = String.valueOf(GaugeMaster.getUInt16(id,0));
                                    break;
                                case "int32":
                                    tempValue = String.valueOf(GaugeMaster.getInt32(id,0));
                                    break;
                                case "uint32":
                                    tempValue = String.valueOf(GaugeMaster.getUInt32(id,0));
                                    break;
                                case "float32":
                                    tempValue = String.valueOf(GaugeMaster.getFloat32(id,0));
                                    break;
                                case "int64":
                                    tempValue = String.valueOf(GaugeMaster.getInt64(id,0));
                                    break;
                                case "uint64":
                                    tempValue = String.valueOf(GaugeMaster.getUInt64(id,0));
                                    break;
                                case "float64":
                                    tempValue = String.valueOf(GaugeMaster.getFloat64(id,0));
                                    break;
                            }
                        } else {
                            if (GaugeMaster.getStatus(id) == 1)
                                tempValue = "pending";
                            else
                                tempValue = "err " + GaugeMaster.getStatus(id);

                            GaugeMaster.close(id);
                            dict.remove(tag);
                            tag = "";
                        }

                    } else {
                        if (GaugeMaster.getStatus(id) == 1)
                            tempValue = "pending";
                        else
                            tempValue = "err " + GaugeMaster.getStatus(id);

                        GaugeMaster.close(id);
                        dict.remove(tag);
                        tag = "";
                    }
                }
            }

            // Publish progress on UI thread continuously, controlled with thread's sleep time.

            gaugeValue = tempValue.trim();

            publishProgress();

            // Adjust the sleep time if necessary.
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return "FINISHED";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate();

        gaugeTaskCallback.UpdateGaugeValue(gaugeValue);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
