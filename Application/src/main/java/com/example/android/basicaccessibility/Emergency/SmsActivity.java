package com.example.android.basicaccessibility.Emergency;

/**
 * Created by yoo on 2016-04-20.
 */
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.basicaccessibility.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsActivity extends Activity {
    Context mContext;
    EditText smsNumber, smsTextContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_service);     //activity_main 대신 sms xml 넣으면됨

        mContext = this;

        smsNumber = (EditText) findViewById(R.id.smsNumber);  //
        smsTextContext = (EditText) findViewById(R.id.smsText); //
    }

    public void sendSMS(View v){
        String smsNum = smsNumber.getText().toString();
        String smsText = smsTextContext.getText().toString();

        if (smsNum.length()>0 && smsText.length()>0){
            sendSMS(smsNum, smsText);
        }else{
            Toast.makeText(this, "모두 입력해 주세요", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSMS(String smsNumber, String smsText){
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        /**
         * SMS가 발송될때 실행
         * When the SMS massage has been sent
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        /**
         * SMS가 도착했을때 실행
         * When the SMS massage has been delivered
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(mContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(mContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }



    public void onReceive(Context mContext, Intent intent) {
        String action =  intent.getAction();

        if("android.provider.Telephony.SMS_RECEIVED".equals(action)){
            /**
             * SMS메세지 파싱
             */
            Bundle bundle = intent.getExtras();
            Object messages[] = (Object[])bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[messages.length];

            for(int i = 0; i < messages.length; i++) {
                /**
                 * PDU포멧의 SMS를 변환합니다
                 */
                smsMessage[i] = SmsMessage.createFromPdu((byte[])messages[i]);
            }

            /**
             * 날짜 형식을 우리나라에 맞도록 변환합니다
             */
            Date curDate = new Date(smsMessage[0].getTimestampMillis());
            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.KOREA);

            String originDate = mDateFormat.format(curDate);
            String origNumber = smsMessage[0].getOriginatingAddress();
            String Message = smsMessage[0].getMessageBody().toString();

           Intent showSMSIntent = new Intent(mContext, Show_smsActivity.class);
            showSMSIntent.putExtra("originNum", origNumber);
            showSMSIntent.putExtra("smsDate", originDate);
            showSMSIntent.putExtra("originText", Message);

            showSMSIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mContext.startActivity(showSMSIntent);
        }
    }






}
