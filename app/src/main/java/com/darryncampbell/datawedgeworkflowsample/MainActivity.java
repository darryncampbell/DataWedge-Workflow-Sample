package com.darryncampbell.datawedgeworkflowsample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "WorkflowSample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  todo
        //        DWUtilities.CreateDWProfile(this);
        //  todo register for this and display it (create the method first)
        DWUtilities.registerForNotifications(this, DWUtilities.NOTIFICATION_TYPE_SCANNER_STATUS);
        DWUtilities.registerForNotifications(this, DWUtilities.NOTIFICATION_TYPE_WORKFLOW_STATUS);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        filter.addAction(DWUtilities.NOTIFICATION_ACTION);
        registerReceiver(myBroadcastReceiver, filter);

        updateTextView(R.id.txtScannerStatus, "Waiting for Status");
        updateTextView(R.id.txtWorkflowStatus, "Waiting for Status");
        updateTextView(R.id.txtWorkflowResult, "Please Scan Barcode");
        updateImage(R.id.imgFreeformImageCaptureImage, null);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(myBroadcastReceiver);
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getResources().getString(R.string.activity_intent_filter_action)))
            {
                //  todo delete redundant code
                //  todo what if use content provider?

                updateTextView(R.id.txtTime, dateFormat.format(new Timestamp(System.currentTimeMillis())));

                //for (String key : intent.getExtras().keySet())
                //    Log.v(LOG_TAG, "key: " + key);
                String source = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
                Log.d(LOG_TAG, "Decode Source: " + source);
                updateTextView(R.id.txtSource, source);

                String dataString = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data_string));
                Log.d(LOG_TAG, "Decode Data String: " + dataString);
                updateTextView(R.id.txtDataString, dataString);

                String decodedMode = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_decoded_mode));
                Log.d(LOG_TAG, "Decode Mode: " + decodedMode);
                updateTextView(R.id.txtDecodeMode, decodedMode);

                String symbology = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
                Log.d(LOG_TAG, "Decode Symbology: " + symbology);
                updateTextView(R.id.txtSymbology, symbology);

                String imageUri = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_image_data));
                Log.d(LOG_TAG, "Decode ImageUri: " + imageUri);
                updateTextView(R.id.txtImageUri, imageUri);

                //String WorkflowName = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_workflow_name)); //  Not documented

                String data = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
                Log.d(LOG_TAG, "Decode Data: " + data);

                //String scannerIdentifier = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_scanner_identifier)); //  Not documented
                //ArrayList<byte[]> dataBytes = (ArrayList<byte[]>) intent.getSerializableExtra(getResources().getString(R.string.datawedge_intent_key_decode_data));
                //if (dataBytes != null)
                //{
                //    Log.d(LOG_TAG, "Decode Data Bytes (size): " + dataBytes.size());
                //    for (int j = 0; j < dataBytes.size(); j++)
                //    {
                //        byte[] rawBytes = dataBytes.get(0);
                //        String rawBytesAsString = "";
                //        for (int i = 0; i < rawBytes.length; i++)
                //            rawBytesAsString += " " + rawBytes[i];
                //        Log.d(LOG_TAG, "Decode Data Bytes (" + j + "): " + rawBytesAsString);
                //    }
                //}
                //Log.d(LOG_TAG, "Decode Scanner ID: " + scannerIdentifier);
                //Log.d(LOG_TAG, "Decode Workflow Name: " + WorkflowName);
                if (data != null)
                {
                    try {
                        JSONArray dataArray = new JSONArray(data);
                        updateTextView(R.id.txtWorkflowResult, dataArray.toString(2));
                        for (int i = 0; i < dataArray.length(); i++)
                        {
                            JSONObject workflowObject = dataArray.getJSONObject(i);
                            Log.v(LOG_TAG, "Workflow Object: " + workflowObject.toString());
                            if (workflowObject.has("string_data"))
                            {
                                Log.d(LOG_TAG, "Workflow " + i + " string_data: " + workflowObject.get("string_data")); //  Not an image
                                String label = workflowObject.getString("label");
                                if (label.equalsIgnoreCase(""))
                                    Log.d(LOG_TAG, "Free-form image capture");
                                else if (label.equalsIgnoreCase("License Plate"))
                                {
                                    //  todo and so on for Identification document, VIN, TIN, Meter
                                    //  hide freeform image capture image and replace with correct image
                                }
                            }
                            else
                            {
                                //  Image data
                                if (workflowObject.has("uri")) {
                                    String uriAsString = workflowObject.get("uri").toString();
                                    Log.d(LOG_TAG, "Workflow " + i + " uri: " + uriAsString);
                                    byte[] imageAsBytes = null;
                                    if (uriAsString != null)
                                        imageAsBytes = processUri(uriAsString);
                                    int width = workflowObject.getInt("width");
                                    int height = workflowObject.getInt("height");
                                    int stride = workflowObject.getInt("stride");
                                    int orientation = workflowObject.getInt("orientation");
                                    String imageFormat = workflowObject.getString("imageformat");

                                    Bitmap bitmap = null;
                                    if (imageAsBytes != null)
                                    {
                                        bitmap = ImageProcessing.getInstance().getBitmap(imageAsBytes, imageFormat, orientation, stride, width, height);
                                        updateImage(R.id.imgFreeformImageCaptureImage, bitmap);
                                    }else
                                        Log.w(LOG_TAG, "Error processing Image data");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.w(LOG_TAG, "JSON Exception parsing data: " + e.getMessage());
                    }
                }

                //  todo handle workflow data & content provider
            }
            else if (intent.getAction().equals(DWUtilities.NOTIFICATION_ACTION))
            {
                //  todo add scanner status
                //  todo write status to UI
                
                if (intent.hasExtra("com.symbol.datawedge.api.NOTIFICATION")) {
                    Bundle b = intent.getBundleExtra("com.symbol.datawedge.api.NOTIFICATION");
                    String NOTIFICATION_TYPE = b.getString("NOTIFICATION_TYPE");
                    if (NOTIFICATION_TYPE != null) {
                        switch (NOTIFICATION_TYPE) {
                            case DWUtilities.NOTIFICATION_TYPE_WORKFLOW_STATUS:
                                Log.d(LOG_TAG, "WORKFLOW_STATUS: status: " + b.getString("STATUS") + ", profileName: " + b.getString("PROFILE_NAME"));
                                updateTextView(R.id.txtWorkflowStatus, b.getString("STATUS"));
                                break;
                            case DWUtilities.NOTIFICATION_TYPE_SCANNER_STATUS:
                                Log.d(LOG_TAG, "SCANNER_STATUS: status: " + b.getString("STATUS") + ", profileName: " + b.getString("PROFILE_NAME"));
                                updateTextView(R.id.txtScannerStatus, b.getString("STATUS"));
                                break;
                        }
                    }
                }
            }
        }
    };

    private void updateTextView(int textViewId, String data)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView txt = findViewById(textViewId);
                txt.setText(data);
            }
        });
    }

    private void updateImage(int imageId, Bitmap bitmap)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                ImageView img = findViewById(imageId);
                if (bitmap == null)
                    img.setImageDrawable(getDrawable(android.R.drawable.btn_star));
                else
                    img.setImageBitmap(bitmap);
            }
        });
    }

    @SuppressLint("Range")
    private byte[] processUri(String uri)
    {
        Cursor cursor = getContentResolver().query(Uri.parse(uri),null,null,null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(cursor != null)
        {
            cursor.moveToFirst();
            try {
                baos.write(cursor.getBlob(cursor.getColumnIndex("raw_data")));
            } catch (IOException e) {
                Log.w(LOG_TAG, "Output Stream Write error " + e.getMessage());
            }
            String nextURI = cursor.getString(cursor.getColumnIndex("next_data_uri"));
            while (nextURI != null && !nextURI.isEmpty())
            {
                Cursor cursorNextData = getContentResolver().query(Uri.parse(nextURI),
                        null,null,null);
                if(cursorNextData != null)
                {
                    cursorNextData.moveToFirst();
                    try {
                        baos.write(cursorNextData.getBlob(cursorNextData.
                                getColumnIndex("raw_data")));
                    } catch (IOException e) {
                        Log.w(LOG_TAG, "Output Stream Write error " + e.getMessage());
                    }
                    nextURI = cursorNextData.getString(cursorNextData.
                            getColumnIndex("next_data_uri"));

                    cursorNextData.close();
                }

            }
            cursor.close();
        }
        return baos.toByteArray();
    }

}