*Please be aware that this application / sample is provided as-is for demonstration purposes without any guarantee of support*
=========================================================

# DataWedge Workflow Sample

Sample of Zebra DataWedge's "Workflow Input" Plugin (OCR and Image Capture) as well as the "Barcode Highligting" feature.

These features are only available in DataWedge 11.2 to:

- Highlight barcodes in the field of view which match specified properties
- Capture multiple barcodes in the field of view
- Image capture
- Optical Character recognition of ID cards, tyres (TIN), vehicle IDs (VIN), License plates, meters (as in gas or electric meters).
- Support for all these features with **both the camera and imager**.  Image capture via the imager has been a long standing requirement that has now been delivered.

OCR is a licensed feature, see the [licensing process](https://techdocs.zebra.com/licensing/process/
) for more information.

Documentation for these features are spread over multiple sections on the techdocs portal:

- The [Workflow Input Plugin](https://techdocs.zebra.com/datawedge/11-2/guide/input/workflow/)
- The [Barcode Highlighting Programmer's Guide](https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/barcode-highlight/)
- The [Workflow Programmer's Guide](https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/workflow-input/)
- The [Changes to the Notification API](https://techdocs.zebra.com/datawedge/11-2/guide/api/registerfornotification/) for Workflow
- A [Code Walkthrough](https://youtu.be/dDCnVpmVbD0) by Zebra engineering on YouTube


## Barcode Highlighting

You can highlight barcodes in the current field of view which meet your specified criteria.  Optionally, you can then choose to also decode these barcodes and return them to the application.

![Applictaion](https://github.com/darryncampbell/DataWedge-Workflow-Sample/raw/main/media/barcode_highlighting.png)

Example showing barcode highlighting configured to set PDF417 barcodes to orange and Code128 barcodes less than 13 characters to blue.

### Possible uses for Barcode Highlighting:

- Provide onscreen feedback to let the operator know which barcodes are being captured
- Help the operator find an item by highlighting the barcode

### How to configure Barcode Highlighting:

Barcode highlighting is part of the *Barcode Input* plugin, not the separate Workflow Input plugin.

To configure barcode highlighting, configure your DataWedge profile as follows:

1. Enable Barcode Highlighting in the Barcode input plugin
2. Press the ellipsis to bring up additional parameters
3. Specify the **highlighting** rules:
   - Create a different rule for each barcode type or colour you want to show
   - You can have multiple rules with different colours
4. Specify the conditions for the rule: 
   - The max / min length of the barcode
   - Any string it might contain
   - The barcode symbology.
5. Rules are executed in priority order from top to bottom
6. Specify the **reporting** rules.  If you do not specify the reporting rule, your barcode(s) will not be sent to your app.
7. Reporting rules and highlighting rules have different conditions, meaning you can choose to report different barcodes than what are being highlighted, though it is recommended to keep these rule the same.
8. Specify the reporting action to have these barcodes returned to the application.

![Barcode Highlighting Configuration](https://github.com/darryncampbell/DataWedge-Workflow-Sample/raw/main/media/dw_barcode_highlighting_conditions_identifier.png)

### How to use Barcode Highlighting

- The hardware or software trigger will initiate the scanning session
- When a barcode meeting the specified criteria is seen in the viewfinder, it will be highlighted.
- The next trigger will end the session and return decoded barcodes back to the app, if configured to do so.

### Video Demos of Barcode Highlighting

**The following video shows barcode highlighting via the Imager:**

[![DataWedge Barcode Highlighting via Imager](https://img.youtube.com/vi/YZExLp9WvKo/0.jpg)](https://www.youtube.com/watch?v=YZExLp9WvKo)

Notice how the different symbologies or barcode lengths were assigned different colours, as defined by the rules.  

Also notice how data is returned in a JSON array output to the text field without modification in this app.

**The following video shows barcode highlighting via the Camera:**

[![DataWedge Barcode Highlighting via Camera](https://img.youtube.com/vi/E5ZkZtUNX_k/0.jpg)](https://www.youtube.com/watch?v=E5ZkZtUNX_k)

The video also shows how the highlighting rules are configured with DataWedge.  The rules are configured to highlight and report EAN13 barcodes of any length and with any contents.

### Coding and Barcode Highlighting: Receiving Data

Although part of the Barcode input plugin, barcode highlighting borrows a lot of its logic from the Workflow plugin.  What that means for you as a developer is you should follow the [Workflow programmer's guide](https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/workflow-input/) to extract data returned from the Intent output plugin, **though there will be no image data**.

```java
//  Given the data returned via 'intent'
String data = intent.getStringExtra("com.symbol.datawedge.decode_data");
JSONArray dataArray = new JSONArray(data);
for (int i = 0; i < dataArray.length(); i++)
{
  JSONObject workflowObject = dataArray.getJSONObject(i);
  if (workflowObject.has("string_data"))
  {
    String label = workflowObject.getString("label");
    if (label.equalsIgnoreCase(""))
    {
      //  Each data decoded barcode is stored in workflowObject.getString("string_data")       
      //  Symbology returned in workflowObject.getString("barcodetype")

    }
  }
}

```

### Coding and Barcode Highlighting: Configuring DataWedge

There are 2 ways to configure barcode highlighting in code

**1. At Runtime:**

A new API has been been introduced in DataWedge 11.2, [Switch Data Capture](https://techdocs.zebra.com/datawedge/11-2/guide/api/switchdatacapture/) to allow you to switch from 'regular' scanning, to barcode highlighting.  A full code example is given in the [help docs](https://techdocs.zebra.com/datawedge/11-2/guide/api/switchdatacapture/#switchbetweenbarcodescanningandhighlighting) but as a high level summary:

```java
Intent i = new Intent();
i.putExtra("com.symbol.datawedge.api.SWITCH_DATACAPTURE", "BARCODE");
//  Add highlighting rules
Bundle paramList = new Bundle();
  paramList.putString("scanner_selection_by_identifier", "INTERNAL_IMAGER");
  paramList.putString("barcode_highlighting_enabled", "true");
Bundle rule1 = new Bundle();
  rule1.putString("rule_name", "Rule1");
Bundle rule1Criteria = new Bundle();
Bundle bundleContains1 = new Bundle();
  bundleContains1.putString("criteria_key", "contains");
  bundleContains1.putString("criteria_value", "090986");
  ArrayList<Bundle> identifierParamList = new ArrayList<>();
    identifierParamList.add(bundleContains1);
    rule1Criteria.putParcelableArrayList("identifier", identifierParamList);
//  Similar logic for criteria
...
ArrayList<Bundle> ruleList = new ArrayList<>();
  ruleList.add(rule1);
Bundle ruleBundlebarcodeOverlay = new Bundle();
  ruleBundlebarcodeOverlay.putString("rule_param_id", "barcode_overlay");
  ruleBundlebarcodeOverlay.putParcelableArrayList("rule_list", ruleList);
  ArrayList<Bundle> ruleParamList = new ArrayList<>();
    ruleParamList.add(ruleBundlebarcodeOverlay);
    paramList.putParcelableArrayList("rules", ruleParamList);
i.putExtra("PARAM_LIST", paramList);
sendBroadcast(i);
```

I strongly recommend you copy / paste the example from techdocs and modify as needed

**2. Persistently:**

A new section has been added to the existing SetConfig API for [Barcode Highlighting Parameters](https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#barcodehighlightingparameters).  The format passed to SetConfig is very similar to that passed to the new 'Switch Data Capture' API, i.e. create a nested bundle structure for rules, actions and criteria.

For a full example of barcode highlighting through SetConfig please see the [Techdocs example](https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#setbarcodehighlightingparameters)

Again, I strongly recommend you copy / paste the example and modify as needed

### Coding and Barcode Highlighting: Registering for change

The [RegisterForNotification](https://techdocs.zebra.com/datawedge/11-2/guide/api/registerfornotification/) API has been updated to report the status of the workflow plugin and since barcode highlighting is implemented by the workflow plugin (though presented separately on the UI) it follows the same lifecycle.

Register to receive the Workflow notifications.  See the [RegisterForNotification](https://techdocs.zebra.com/datawedge/11-2/guide/api/registerfornotification/) docs or this app for more detailed code:

```java
Bundle b = new Bundle();
b.putString("com.symbol.datawedge.api.APPLICATION_NAME", getPackageName());
b.putString("com.symbol.datawedge.api.NOTIFICATION_TYPE", "WORKFLOW_STATUS");
```

Process the received notification

```java
case "WORKFLOW_STATUS":
  Log.d(LOG_TAG, "WORKFLOW_STATUS: status: " + b.getString("STATUS");
  break;
```

**Be aware**: Any Intent API sent to DataWedge before the 'PLUGIN_READY' status will lead to undefined behaviour.

### Some additional notes for Barcode Highlighting:

- Reporting will only report barcodes meeting the specified criteria currently highlighted in the viewfinder.  **If you want to capture barcodes outside the viewfinder, for example, if you are waving the device across multiple barcodes, then you should use the 'Freeform Image Capture' Workflow**.
- Captured Barcodes are reported in the same way as "Workflow" data capture.  I.e. the Intent plugin will report the result through `com.symbol.datawedge.data`
- The order the results are given will be the order in which the barcodes were recognised by the decoding algorithm, this is not something the user can influence.  An `identifier` field is returned in the JSONObject for each barcode captured.
- If multiple barcodes are captured, the value returned in `com.symbol.datawedge.data_string` will be concatenated without any separators.  It is strongly recommended to use `com.symbol.datawedge.data` instead. 
- If you switch scanners when configuring DataWedge, your highlighting rules will be lost (with this initial release)
- As stated in the documentation, the keystroke output will concatenate all the data without any separators.  This means the Intent plugin is really the only viable way to receive data. 


## Freeform Image Capture

Even if your device does not have a camera, you can now capture an image using the barcode scanner.  Any barcodes that have been seen during the capture session will be returned to your app along with the image (even if they are no longer in frame). 

### Possible uses of Freeform Image Capture

- Capture an image through the device imager (scanner), without a camera.
- Capture proof of delivery and the tracking barcode in a single step

### How to configure Freeform Image Capture

Freeform Image capture is delivered through a new DataWedge input plugin called 'Workflow'.  This is separate from the standard 'Barcode' input plugin.

To configure freeform image capture, configure your DataWedge profile as follows:

1. Enable the Workflow input plugin.  If doing this through the DataWedge UI, you will be prompted that you cannot have both the Barcode and Workflow input plugins active.
2. Scroll down to the 'Image Capture' section of the plugin and enable it.
3. Press the ellipsis to bring up additional parameters
4. Choose the input source, either camera or imager
5. Set the session timeout, in ms.  This is the length of time the scanning session will be held open until cancelled, if the trigger was not pulled.
6. Select 'Decode And Highlight Barcodes' to return data.  Although set to 'Highlight' only by default, most use cases will also want to return data.
7. Set the additional feedback parameters, such as haptic feedback, LED notification and decode audio feedback.  This is the feedback that will be given whenever a new barcode is seen in the viewfinder, so as you pan across multiple barcodes your device will inform you whenever it sees a barcode. 

![Freeform Image Configuration](https://github.com/darryncampbell/DataWedge-Workflow-Sample/raw/main/media/dw_freeform_image_capture_config_1.png)

### How to use Freeform Image Capture

- The hardware or software trigger will initiate the data acquisition session
- As the system sees barcodes, it will highlight them to let the user know it has been seen.  Note: it is NOT possible to change the highlight colour with freeform image capture.
- The next trigger press will capture the image, so the user can step back if need be, to ensure the entire object is within view.
- If the 'Decode / Highlight' option is turned on, it will return the image and the barcodes that had been highlighted.

### Video Demos of Freeform Image Capture

**The following video shows freeform image capture with barcode highlighting via the Camera:**

This shows panning across many barcodes during a long scanning session.

[![DataWedge freeform image capture with barcode highlighting via Camera](https://img.youtube.com/vi/BWAvUeLFnQI/0.jpg)](https://www.youtube.com/watch?v=BWAvUeLFnQI)

**The following video shows freeform image capture with barcode highlighting via the Imager:**

This shows a brief scanning session with many barcodes in view.

[![DataWedge freeform image capture with barcode highlighting via Camera](https://img.youtube.com/vi/hwTDCuvcjYk/0.jpg)](https://www.youtube.com/watch?v=hwTDCuvcjYk)


### Coding and Freeform Image Capture: Receiving Data

The [Workflow programmer's guide](https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/workflow-input/) gives a lot of detail how to parse workflow return types.  The code applicable to freeform image capture is given below

```java
//  Given the data returned via 'intent'
String data = intent.getStringExtra("com.symbol.datawedge.decode_data");
JSONArray dataArray = new JSONArray(data);
for (int i = 0; i < dataArray.length(); i++)
{
  JSONObject workflowObject = dataArray.getJSONObject(i);
  if (workflowObject.has("string_data"))
  {
    String label = workflowObject.getString("label");
    if (label.equalsIgnoreCase(""))
    {
      //  Each data decoded barcode is stored in workflowObject.getString("string_data")      
      //  Symbology returned in workflowObject.getString("barcodetype")
    }
  }
  else
  {
      //  string_data is absent, therefore this is an image
      if (workflowObject.has("uri")) {
        String uriAsString = workflowObject.get("uri").toString();
        byte[] imageAsBytes = null;
        imageAsBytes = processUri(uriAsString); //  Extract data from content provider
        int width = workflowObject.getInt("width");
        int height = workflowObject.getInt("height");
        int stride = workflowObject.getInt("stride");
        int orientation = workflowObject.getInt("orientation");
        String imageFormat = workflowObject.getString("imageformat");
        //  See the sample or programmer's guide for getBitmap(...)
        Bitmap bitmap = 
          ImageProcessing.getInstance().getBitmap(imageAsBytes, imageFormat, orientation, stride, width, height);
        //  Show bitmap on UI
      }
   }
}

```

### Coding and Freeform Image Capture: Configuring DataWedge

There are two ways to configure freeform image capture in code.  This section is similar to the earlier section talking about configuring DataWedge for barcode highlighting but some of the detail is different.

**1. At Runtime:**

As discussed in the "Coding and Barcode Highlighting: Configuring DataWedge" section, a new API has been introduced in DataWedge 11.2, [Switch Data Capture](https://techdocs.zebra.com/datawedge/11-2/guide/api/switchdatacapture/) to allow you to switch from 'regular' scanning to any of the workflow input options.  
   
The full code example in the [help docs for Switch Data Capture](https://techdocs.zebra.com/datawedge/11-2/guide/api/switchdatacapture/#switchbetweenworkflowoptions) refers exclusively to ID cards.  For a freeform image capture example you can copy the format provided in the [Set Config Example](https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#setfreeformimagecaptureconfiguration):

```java
Intent i = new Intent();
i.putExtra("com.symbol.datawedge.api.SWITCH_DATACAPTURE", "WORKFLOW");
Bundle paramList = new Bundle();
paramList.putString("workflow_name","free_form_capture");
paramList.putString("workflow_input_source","2");
Bundle paramSet1 = new Bundle();
paramSet1.putString("module","BarcodeTrackerModule");
Bundle moduleAParams = new Bundle();
moduleContainerDecoderModule.putString("session_timeout", "16000");
moduleContainerDecoderModule.putString("illumination", "off");
moduleContainerDecoderModule.putString("decode_and_highlight_barcodes", "1");
paramSet1.putBundle("module_params",moduleAParams);
//  Feedback params omitted from this code sample
ArrayList<Bundle> paramSetList = new ArrayList<>();
paramSetList.add(paramSet1);
paramList.putParcelableArrayList("workflow_params", paramSetList);
i.putExtra("PARAM_LIST", paramList);
sendBroadcast(i);
```

**2. Persistently:**
   
A new section has been added to the existing SetConfig API for [Workflow Input Parameters](https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#workflowinputparameters).  

There is a [dedicated SetConfig example](https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#setfreeformimagecaptureconfiguration) for freeform image capture.

### Coding and Freeform Image Capture: Registering for change

Registering for change in the workflow plugin status was covered earlier in the "Coding and Barcode Highlighting: Registering for change" section.  The code will be identical


### Some additional notes for Freeform Image Capture:

- The order the results are given will be the order in which the barcodes were seen by the decoding algorithm, this is will correspond to the order the user passed over them.
- Captured Barcodes are reported via the Intent plugin as `com.symbol.datawedge.data`
- If multiple barcodes are captured, the data will be concatenated, i.e. `com.symbol.datawedge.data_string` will return all data without any separators.  It is recommended to use `com.symbol.datawedge.data` instead. 
- As stated in the documentation, the keystroke output will concatenate all the data without any separators.  This means the Intent plugin is really the only viable way to receive data. 

## OCR: License Plates, VIN, TIN, Meters

https://techdocs.zebra.com/licensing/process/


https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#setlicenseplateconfiguration
https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#setvehicleidentificationnumbervinconfiguration
https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#settireidentificationnumbertinconfiguration
https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#setmeterconfiguration

## OCR: Identity Documents

https://techdocs.zebra.com/datawedge/11-2/guide/api/setconfig/#setidentificationdocumentconfiguration


## Not to be confused with...

**Even users familiar with DataWedge may confuse some of these new features with existing ones.  Below is a list of similar, though unrelated capabilities**

- The Intent Output plugin setting for 'Use Content providers' is not used for Workflow or barcode highlighting
- Similarly, although the principle is similar do not use the [content provider](https://techdocs.zebra.com/datawedge/latest/guide/programmers-guides/content-provider/) programming guide to extract images from the workflow data, instead use the [workflow programmer's guide](https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/workflow-input/)
- The "OCR params" under "Scanner Configuration" is something separate and not related to the OCR feature of workflow.


