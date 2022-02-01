package com.darryncampbell.datawedgeworkflowsample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DWUtilities {
    private static final String PROFILE_NAME = "Workflow Sample";
    private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
    public static final String NOTIFICATION_ACTION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    public static final String NOTIFICATION_TYPE_SCANNER_STATUS = "SCANNER_STATUS";
    public static final String NOTIFICATION_TYPE_WORKFLOW_STATUS = "WORKFLOW_STATUS";

    public static void CreateDWProfile(Context context)
    {
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, PROFILE_NAME);

        //  Now configure that created profile to apply to our application
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", PROFILE_NAME);
        profileConfig.putString("PROFILE_ENABLED", "true"); //  Seems these are all strings
        profileConfig.putString("CONFIG_MODE", "UPDATE");

        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true");
        Bundle barcodeProps = new Bundle();
        barcodeProps.putString("configure_all_scanners", "true");
        barcodeProps.putString("scanner_input_enabled", "false");  //  todo why is this 'false' not applying?  Change to scanner_selection auto?  And remove configure_all_scanners?
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", context.getPackageName());      //  Associate the profile with this app
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);

        //  You can only configure one plugin at a time, we have done the barcode input, now do the intent output
        profileConfig.remove("PLUGIN_CONFIG");
        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", context.getResources().getString(R.string.activity_intent_filter_action));
        intentProps.putString("intent_delivery", "2");  //  SendBroadcast
        intentConfig.putBundle("PARAM_LIST", intentProps);
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);

        //  Disable keyboard output
        profileConfig.remove("PLUGIN_CONFIG");
        Bundle keystrokeConfig = new Bundle();
        keystrokeConfig.putString("PLUGIN_NAME", "KEYSTROKE");
        keystrokeConfig.putString("RESET_CONFIG", "true");
        Bundle keystrokeProps = new Bundle();
        keystrokeProps.putString("keystroke_output_enabled", "false");
        keystrokeConfig.putBundle("PARAM_LIST", keystrokeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", keystrokeConfig);
        sendDataWedgeIntentWithExtra(context, ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
    }

    private static void sendDataWedgeIntentWithExtra(Context context, String action, String extraKey, String extraValue)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        context.sendBroadcast(dwIntent);
    }

    private static void sendDataWedgeIntentWithExtra(Context context, String action, String extraKey, Bundle extras)
    {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        context.sendBroadcast(dwIntent);
    }

    public static void registerForNotifications(Context context, String notificationType)
    {
        Bundle b = new Bundle();
        b.putString("com.symbol.datawedge.api.APPLICATION_NAME", context.getPackageName());
        b.putString("com.symbol.datawedge.api.NOTIFICATION_TYPE", notificationType);
        Intent i = new Intent();
        i.setAction(ACTION_DATAWEDGE);
        i.putExtra("com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION", b);
        context.sendBroadcast(i);
    }
}

