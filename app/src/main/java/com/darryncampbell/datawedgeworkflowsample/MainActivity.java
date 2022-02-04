package com.darryncampbell.datawedgeworkflowsample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "WorkflowSample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DWUtilities.CreateDWProfile(this);
        DWUtilities.registerForNotifications(this, DWUtilities.NOTIFICATION_TYPE_SCANNER_STATUS);
        DWUtilities.registerForNotifications(this, DWUtilities.NOTIFICATION_TYPE_WORKFLOW_STATUS);

        Intent initiatingIntent = getIntent();
        if (initiatingIntent != null)
        {
            String action = initiatingIntent.getAction();
            if (action.equalsIgnoreCase(getResources().getString(R.string.activity_intent_filter_action)))
            {
                //  Received a barcode through StartActivity
                processScan(getApplicationContext(), initiatingIntent);
            }
        }
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

        //  todo reinstate
        //updateTextView(R.id.txtScannerStatus, "Waiting for Status");
        //updateTextView(R.id.txtWorkflowStatus, "Waiting for Status");
        //updateTextView(R.id.txtWorkflowResult, "Please Scan Barcode");
        //updateTextView(R.id.txtDataString, "Please Scan Barcode");
        //updateImage(R.id.img, null);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(myBroadcastReceiver);
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getResources().getString(R.string.activity_intent_filter_action))) {
                processScan(context, intent);
            }
            else if (intent.getAction().equals(DWUtilities.NOTIFICATION_ACTION))
            {
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

    private void processScan(Context context, Intent intent)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (intent.getAction().equals(getResources().getString(R.string.activity_intent_filter_action)))
        {
            updateTextView(R.id.txtDataString, "Please Scan Barcode");
            updateTextView(R.id.txtWorkflowResult, "Please Scan Barcode");

            //  Note: Intent Output plugin setting for 'Use Content providers' is not used for Workflow or barcode highlighting
            updateTextView(R.id.txtTime, dateFormat.format(new Timestamp(System.currentTimeMillis())));

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

            String data = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
            Log.d(LOG_TAG, "Decode Data: " + data);
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
                            Log.d(LOG_TAG, "Workflow " + i + " string_data: " + workflowObject.get("string_data"));
                            String label = workflowObject.getString("label");
                            if (label.equalsIgnoreCase("lastName"))
                            {
                                parseIdentityDocument(dataArray);
                                break;
                            }
                            else if (label.equalsIgnoreCase("License Plate Number"))
                            {
                                Log.d(LOG_TAG, "License Plate");
                                updateTextView(R.id.txtOCRResult, "License Plate: " + workflowObject.get("string_data"));
                                updateTextView(R.id.lblImage, "License Plate Image");
                            }
                            else if (label.equalsIgnoreCase("VIN Number"))
                            {
                                Log.d(LOG_TAG, "VIN (Vehicle Identification Number)");
                                updateTextView(R.id.txtOCRResult, "Vehicle ID: " + workflowObject.get("string_data"));
                                updateTextView(R.id.lblImage, "Vehicle ID Image");
                            }
                            else if (label.equalsIgnoreCase("TIN Number"))
                            {
                                Log.d(LOG_TAG, "TIN (Tyre Identification Number)");
                                updateTextView(R.id.txtOCRResult, "Tyre ID: " + workflowObject.get("string_data"));
                                updateTextView(R.id.lblImage, "Tyre ID Image");
                            }
                            else if (label.equalsIgnoreCase("Meter Reading"))
                            {
                                Log.d(LOG_TAG, "Meter (e.g. Gas meter)");
                                updateTextView(R.id.txtOCRResult, "Meter: " + workflowObject.get("string_data"));
                                updateTextView(R.id.lblImage, "Meter Image");
                            }
                            else{
                                Log.d(LOG_TAG, "Freeform image capture");
                                updateTextView(R.id.lblImage, "Freeform Image Capture Image");
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
                                    updateImage(R.id.img, bitmap);
                                }else
                                    Log.w(LOG_TAG, "Error processing Image data");
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.w(LOG_TAG, "JSON Exception parsing data: " + e.getMessage());
                }
            }
        }
    }

    private void parseIdentityDocument(JSONArray dataArray) throws JSONException {
        Log.d(LOG_TAG, "Identification Document");

        String lastName = "?";
        String firstName = "?";
        //  parse identity document (https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/workflow-input/#ocrresult)
        for (int i = 0; i < dataArray.length(); i++)
        {
            JSONObject workflowObject = dataArray.getJSONObject(i);
            Log.v(LOG_TAG, "Workflow Object: " + workflowObject.toString());
            if (workflowObject.has("string_data")) {
                String label = workflowObject.getString("label");
                if (label.equalsIgnoreCase("lastName")) {
                    lastName = workflowObject.getString("string_data");
                }
                else if (label.equalsIgnoreCase("firstName"))
                    firstName = workflowObject.getString("string_data");
            }
        }
        updateTextView(R.id.txtOCRResult, "First Name: " + firstName + ", Last Name: " + lastName);
        updateTextView(R.id.lblImage, "Identity Document Image");
    }

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