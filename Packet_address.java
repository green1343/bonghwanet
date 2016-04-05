package com.example.android.packet;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

//주소록 클래스
public class Packet_address extends Packet_New_User {

    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // setContentView(R.layout.main);

        ContentResolver cr = getContentResolver();

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,

                null, null, null, null);

        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {

                String phone_id = cur.getString(cur.getColumnIndex(

                        ContactsContract.Contacts._ID));

                String phone_name = cur.getString(cur.getColumnIndex(

                        ContactsContract.Contacts.DISPLAY_NAME));


                if (("1").equals(cur.getString(cur.getColumnIndex(

                        ContactsContract.Contacts.HAS_PHONE_NUMBER)))) {

                    Cursor pCur = cr.query(

                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,

                            null,

                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID

                                    + " = ?", new String[]{phone_id}, null);

                    int i = 0;

                    int pCount = pCur.getCount();

                    String[] phoneNum = new String[pCount];

                    String[] phoneType = new String[pCount];

                    while (pCur.moveToNext()) {

                        phoneNum[i] = pCur.getString(pCur.getColumnIndex(

                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        phoneType[i] = pCur.getString(pCur.getColumnIndex(

                                ContactsContract.CommonDataKinds.Phone.TYPE));

                        i++;

                    }
                }
            }
        }


    }


    //번호로 저장된 이름 얻을 수 있는 메소드..
    public static String getContactName(Context context, String phoneNumber) {

        ContentResolver cr = context.getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {

            return null;

        }

        String contactName = null;

        if(cursor.moveToFirst()) {

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

        }




        if(cursor != null && !cursor.isClosed()) {

            cursor.close();

        }




        return contactName;

    }

   // Context context;


}



