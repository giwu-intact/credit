package com.example.giwu.credit;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.TextView;



import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements SensorEventListener {

    private static Context context;
    private TextView txt;
    private SensorManager sensorManager;
    private Boolean activityRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        displayContacts();
        try {
           displayLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }
        displayCallLog();
        displaySMS();
        displayCalendarEvents();
        displayAccounts();
        displaySensors();
        displayPhoneStatus();
        displayUSB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            txt = (TextView) findViewById(R.id.steps);
            txt.setText("Step Counter Not Found!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {
            txt = (TextView) findViewById(R.id.steps);
            txt.setText("Step Counter: "+String.valueOf(event.values[0])+'\n');
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void displayContacts() {
        TextView txt = (TextView)findViewById(R.id.contacts);
        String msg = "Displaying contacts..." + '\n';

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

            long contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

            long emailId = 0;
            Cursor cur = getContentResolver()
                    .query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{"_id"},
                            "contact_id = " + contactId,
                            null,null);
            if(cur != null && cur.moveToFirst()){
                emailId = cur.getLong(0);
            }
            msg += name + " " + number + " " + emailId + "\n";
        }
        txt.setText(msg);
    }

    public void displayLocation() throws IOException {
        txt = (TextView)findViewById(R.id.loc);
        LocationHandler locationHandler = new LocationHandler(context,txt);
    }

    public void displayCallLog() {
        TextView txt = (TextView)findViewById(R.id.calllog);
        String msg = "Displaying call log..." + '\n';

        Uri allCalls = Uri.parse("content://call_log/calls");
        Cursor c = getContentResolver().query(allCalls, null, null, null, null);
        while(c.moveToNext()) {
            String num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));// for  number
            String name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name
            String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));// for duration
            String type = c.getString(c.getColumnIndex(CallLog.Calls.TYPE));
            msg += num + ", " + name + ", " + duration + ", " + type + "\n";
        }
        txt.setText(msg);
    }

    public void displaySMS(){
        String sms = "Displaying SMS... " + "\n";
        Uri uriSMSURI = Uri.parse("content://sms/inbox");
        Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);

        while (cur.moveToNext()) {
            String address = cur.getString(cur.getColumnIndex("address"));
            String body = cur.getString(cur.getColumnIndexOrThrow("body"));
            sms += "Number: " + address + " .Message: " + body + '\n';

        }
        TextView txt = (TextView)findViewById(R.id.sms);
        txt.setText(sms);
    }

    public void displayCalendarEvents() {
        String msg = "Displaying calendar events..."+"\n";
        Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/calendars");
        String[] FIELDS = {
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Calendars.VISIBLE
        };
        Cursor cursor = getContentResolver().query(CALENDAR_URI, FIELDS, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String displayName = cursor.getString(1);
            String color = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR));
            Boolean selected = !cursor.getString(3).equals("0");
            msg += name + " " + displayName + " " + color + " " + selected + '\n';
        }
        TextView txt = (TextView)findViewById(R.id.events);
        txt.setText(msg);
    }


    public void displayAccounts() {
        TextView txt = (TextView)findViewById(R.id.accounts);
        String msg = "Displaying accounts..." + '\n';
        AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = am.getAccounts();
        for(Account account:accounts) {
            String name = account.name;
            String type = account.type;
            msg += name + ", " + type + "\n";
        }
        txt.setText(msg);
    }

    public void displaySensors() {
        txt = (TextView)findViewById(R.id.sensors);
        PackageManager manager = getPackageManager();
        boolean hasTemp = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE);
        boolean hasStepCounter = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);
        txt.setText("Has Temp Sensor: " + hasTemp + '\n' + "Has Step Counter: " + hasStepCounter + '\n');
    }

    public void displayPhoneStatus() {
        TextView txt = (TextView)findViewById(R.id.identity);
        String msg = "Displaying phone status..." + '\n';
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        msg += "Call State: ";
        switch(tm.getCallState()) {
            case(0):
                msg += "Idle \n";
                break;
            case(1):
                msg += "Ringing \n";
                break;
            case(2):
                msg += "Offhook \n";
                break;
        }

        msg += "Location: " + tm.getCellLocation() + '\n';

        msg += "Data activity: ";
        switch(tm.getDataActivity()) {
            case(0):
                msg += "None \n";
                break;
            case(1):
                msg += "In \n";
                break;
            case(2):
                msg += "Out \n";
                break;
            case(3):
                msg += "Inout \n";
                break;
            case(4):
                msg += "Dormant \n";
                break;
        }

        msg += "Data state: ";
        switch(tm.getDataState()) {
            case(0):
                msg += "Disconnected \n";
                break;
            case(1):
                msg += "Connecting \n";
                break;
            case(2):
                msg += "Connected \n";
                break;
            case(3):
                msg += "Suspended \n";
                break;
        }

        msg += "Device ID: " + tm.getDeviceId() + '\n';
        msg += "Device Software Version: " + tm.getDeviceSoftwareVersion() + '\n';
        msg += "Network Country Info: " + tm.getNetworkCountryIso() + '\n';
        msg += "Network Operator: " + tm.getNetworkOperator() + '\n';
        msg += "Network Operator Name: " + tm.getNetworkOperatorName() + '\n';

        msg += "Network Type: ";
        switch(tm.getNetworkType()) {
            case(0):
                msg += "Unknown \n";
                break;
            case(1):
                msg += "GPRS \n";
                break;
            case(2):
                msg += "EDGE \n";
                break;
            case(3):
                msg += "UMTS \n";
                break;
            case(4):
                msg += "CDMA: Either IS95A or IS95B \n";
                break;
            case(5):
                msg += "EVDO revision 0 \n";
                break;
            case(6):
                msg += "EVDO revision A \n";
                break;
            case(7):
                msg += "1xRTT \n";
                break;
            case(8):
                msg += "HSDPA \n";
                break;
            case(9):
                msg += "HSUPA \n";
                break;
            case(10):
                msg += "HSPA \n";
                break;
            case(11):
                msg += "iDen \n";
                break;
            case(12):
                msg += "EVDO revision B \n";
                break;
            case(13):
                msg += "LTE \n";
                break;
            case(14):
                msg += "eHRPD \n";
                break;
            case(15):
                msg += "HSPA+ \n";
                break;
            case(16):
                msg += "Suspended \n";
                break;
        }

        msg += "Phone Type: ";
        switch(tm.getPhoneType()) {
            case(0):
                msg += "No phone radio \n";
                break;
            case(1):
                msg += "GSM \n";
                break;
            case(2):
                msg += "CDMA \n";
                break;
            case(3):
                msg += "SIP \n";
                break;
        }

        msg += "SIM Country ISO: " + tm.getNetworkOperatorName() + '\n';
        msg += "SIM Operator Name: " + tm.getNetworkOperatorName() + '\n';
        msg += "SIM Serial Number: " + tm.getNetworkOperatorName() + '\n';

        msg += "SIM State: ";
        switch(tm.getSimState()) {
            case(0):
                msg += "Unknown \n";
                break;
            case(1):
                msg += "Absent \n";
                break;
            case(2):
                msg += "Locked: Require the user's SIM PIN to unlock \n";
                break;
            case(3):
                msg += "Locked: Require the user's SIM PUK to unlock \n";
                break;
            case(4):
                msg += "Locked: Require a network PIN to unlock \n";
                break;
            case(5):
                msg += "Ready \n";
                break;
        }

        String isRoaming = (tm.isNetworkRoaming())?"Yes":"No";
        msg += "Is network roaming: " + isRoaming + '\n';
        txt.setText(msg);
    }

    public void displayUSB() {
        TextView txt = (TextView)findViewById(R.id.usb);
        String msg = "Displaying USB..." + '\n';

        msg += "External Files: \n";
        File dirFiles[] = Environment.getExternalStorageDirectory().listFiles();
        if (dirFiles.length != 0) {
            // loops through the array of files, outputing the name to console
            for (int ii = 0; ii < dirFiles.length; ii++) {
                msg += "File Name: " + dirFiles[ii].toString() + '\n';
            }
        }

        msg += "Internal Files: \n";
        File inFiles[] = Environment.getDataDirectory().listFiles();
        if (inFiles != null) {
            // loops through the array of files, outputing the name to console
            for (int ii = 0; ii < inFiles.length; ii++) {
                msg += "File Name: " + inFiles[ii].toString() + '\n';
            }
        }
        txt.setText(msg);
    }
}
