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
- The [Workflow Programmer's Guide](https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/workflow-input/)
- The [Changes to the Notification API](https://techdocs.zebra.com/datawedge/11-2/guide/api/registerfornotification/) for Workflow
- A [Code Walkthrough](https://youtu.be/dDCnVpmVbD0) by Zebra engineering on YouTube


## Barcode Highliting

You can highlight barcodes in the current field of view which meet your specified criteria.  Optionally, you can then choose to also decode these barcodes and return them to the application.

![Applictaion](https://github.com/darryncampbell/DataWedge-Workflow-Sample/raw/main/media/barcode_highlighting.png)

**Possible uses:**

- Provide onscreen feedback to let the operator know which barcodes are being captured
- Help the operator find an item by highlighting the barcode

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
7. Reporting rules and highlighting rules have different conditions, meaning you can choose to report different barcodes that what are being highlighted, though it is recommended to keep these rule the same.
8. Specify the reporting action to have these barcodes returned to the application.

![Barcode Highlighting Configuration](https://github.com/darryncampbell/DataWedge-Workflow-Sample/raw/main/media/dw_barcode_highlighting_conditions_identifier.png)


###Video Demos of Barcode Highlighting

**The following video shows barcode highlighting via the Imager:**

[![DataWedge Barcode Highlighting via Imager](https://img.youtube.com/vi/YZExLp9WvKo/0.jpg)](https://www.youtube.com/watch?v=YZExLp9WvKo)

Notice how the different symbologies or barcode lengths were assigned different colours, as defined by the rules.  

Also notice how data is returned in a JSON array output to the text field without modification in this app.

**The following video shows barcode highlighting via the Camera:**

[![DataWedge Barcode Highlighting via Camera](https://img.youtube.com/vi/E5ZkZtUNX_k/0.jpg)](https://www.youtube.com/watch?v=E5ZkZtUNX_k)

The video also shows how the highlighting rules are configured with DataWedge.  

The rules are configured to highlight and report EAN13 barcodes of any length and with any contents.

###Some additional notes:

- Reporting will only report barcodes meeting the specified criteria currently highlighted in the viewfinder.  If you want to capture barcodes outside the viewfinder, for example, if you are waving the device across multiple barcodes, then you should use the 'Freeform Image Capture' Workflow.
- Captured Barcodes are reported in the same way as "Workflow" data capture.  I.e. the Intent plugin will report the result through `com.symbol.datawedge.data`
- If multiple barcodes are captured, the data will be concatenated, i.e. `com.symbol.datawedge.data_string` will return all data without any separators.  It is recommended to use `com.symbol.datawedge.data` instead. 
- If you switch scanners when configuring DataWedge, your highlighting rules will be lost (with this initial release)
- As stated in the documentation, the keystroke output will concatenate all the data without any separators.  This means the Intent plugin is really the only viable way to receive data. 


## Freeform Image Capture

Camera: https://youtu.be/BWAvUeLFnQI
Imager: https://youtu.be/hwTDCuvcjYk

## OCR: License Plates, VIN, TIN, Meters

## OCR: Identity Documents

## Notes
- Intent Output plugin setting for 'Use Content providers' is not used for Workflow or barcode highlighting
- Similarly, although the principle is similar do not use the [content provider](https://techdocs.zebra.com/datawedge/latest/guide/programmers-guides/content-provider/) programming guide to extract images from the workflow data, instead use the [workflow programmer's guide](https://techdocs.zebra.com/datawedge/11-2/guide/programmers-guides/workflow-input/)
- The "OCR params" under "Scanner Configuration" is something separate and not related to the OCR feature of workflow.


