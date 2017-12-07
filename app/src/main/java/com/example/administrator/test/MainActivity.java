package com.example.administrator.test;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 2017年12月7日20:39:00
 * 短信备份与恢复,完美兼容安卓7.0版本
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();
    }

    private void initPermission() {
        ArrayList<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] permissions = new String[permissionList.size()];
        permissions = permissionList.toArray(permissions);
        if (permissions.length > 0) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (permissions.length == 2) {
                    if (grantResults.length > 0 && grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED && grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "you allow 2 permission", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (permissions.length == 1) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "you allow 1 permission", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;
        }
    }

    public void smsBackup(View view) {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        try {
            xmlSerializer.setOutput(new FileOutputStream(Environment.getExternalStorageDirectory() +
                    "/smsbackup.xml"), "utf-8");
            xmlSerializer.startDocument("utf-8", true);
            xmlSerializer.startTag(null, "smss");

            ContentResolver contentResolver = getContentResolver();
            Uri uri = Uri.parse("content://sms/");
            Cursor cursor = contentResolver.query(uri, new String[]{"address", "body", "date", "type"},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    xmlSerializer.startTag(null, "sms");

                    xmlSerializer.startTag(null, "address");
                    xmlSerializer.text(cursor.getString(0));
                    xmlSerializer.endTag(null, "address");

                    xmlSerializer.startTag(null, "body");
                    xmlSerializer.text(cursor.getString(1));
                    xmlSerializer.endTag(null, "body");

                    xmlSerializer.startTag(null, "date");
                    xmlSerializer.text(cursor.getString(2));
                    xmlSerializer.endTag(null, "date");

                    xmlSerializer.startTag(null, "type");
                    xmlSerializer.text(cursor.getString(3));
                    xmlSerializer.endTag(null, "type");

                    xmlSerializer.endTag(null, "sms");
                }
                cursor.close();
            }

            xmlSerializer.endTag(null, "smss");
            xmlSerializer.endDocument();
            Toast.makeText(this, "备份成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void smsRestore(View view) {
        ArrayList<SmsBean> list = null;
        SmsBean smsBean = null;
        XmlPullParser xmlPullParser = Xml.newPullParser();
        try {
            xmlPullParser.setInput(new FileInputStream(Environment.getExternalStorageDirectory() +
                    "/smsbackup.xml"), "utf-8");

            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String currentTag = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("smss".equals(currentTag)) {
                            list = new ArrayList<>();
                        } else if ("sms".equals(currentTag)) {
                            smsBean = new SmsBean();
                        } else if ("address".equals(currentTag)) {
                            smsBean.address = xmlPullParser.nextText();
                        } else if ("body".equals(currentTag)) {
                            smsBean.body = xmlPullParser.nextText();
                        } else if ("date".equals(currentTag)) {
                            smsBean.date = xmlPullParser.nextText();
                        } else if ("type".equals(currentTag)) {
                            smsBean.type = xmlPullParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("sms".equals(currentTag)) {
                            list.add(smsBean);
                        } else if ("smss".equals(currentTag)) {
                            // 打印测试结果
                            for (SmsBean smsBean1 : list) {
                                Log.d(TAG, "smsRestore: " + smsBean1);
                            }
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }
}
