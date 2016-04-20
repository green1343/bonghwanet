package com.example.android.basicaccessibility.Emergency;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.android.basicaccessibility.R;

/**
 * Created by User on 2016-04-20.
 */
public class Show_smsActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //XML
        setContentView(R.layout.show_sms);

        TextView smsDate = (TextView) findViewById(R.id.smsDate);
        TextView originNum = (TextView) findViewById(R.id.originNum);
        TextView originText = (TextView) findViewById(R.id.originText);

        Intent smsIntent = getIntent();

        String originNumber = smsIntent.getStringExtra("originNum");
        String originDate = smsIntent.getStringExtra("smsDate");
        String originSmsText = smsIntent.getStringExtra("originText");

        originNum.setText(originNumber);
        smsDate.setText(originDate);
        originText.setText(originSmsText);
    }

}
