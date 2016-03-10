/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Basic activity class.
 *
 * <p>Responsible for rendering layout, and displaying some toasts to give buttons feedback.
 * There's nothing terribly interesting in this class. All the interesting stuff is in
 * res/layout/activity_main.xml and {@link DialView}.
 */
public class MainActivity extends Activity{

    private Button m_button1, m_button2;
    long m_curGroup = 106423876801L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        Manager.INSTANCE.init(this);

        // variables
        m_button1 = (Button) findViewById(R.id.button1);
        m_button2 = (Button) findViewById(R.id.button2);

        m_button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Manager.INSTANCE.createGroup("test", Manager.GroupInfo.MODE_OPEN);
            }
        });

        m_button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                uploadPicture();

                /*Packet_Share_Text packet = new Packet_Share_Text();
                packet.text = "CtoS Message";
                WiFiNetwork.INSTANCE.writeCtoS(packet);*/
            }
        });

    }// onCreate

    public static final int REQ_GALLERY_SELECT = 0;
    public static final int REQ_CAMERA_SELECT = 0;

    String cameraTempFilePath;

    void uploadPicture(){
        cameraTempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp_image.jpg";
        File imageFile = new File(cameraTempFilePath);
        Uri imageFileUri = Uri.fromFile(imageFile);

        Intent intent = new Intent();
        /*intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
        startActivityForResult(intent, REQ_CAMERA_SELECT);*/

        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQ_GALLERY_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data == null)
            return;

        String path = getPath(data.getData());
        StringTokenizer st = new StringTokenizer(path, "/");
        String filename = null;
        while(st.hasMoreTokens())
            filename = st.nextToken();
        copyFile(path, Manager.INSTANCE.getRealGroupPath(m_curGroup) + "/" + filename);

        if(resultCode == RESULT_OK) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            if (requestCode == REQ_CAMERA_SELECT) {
// (1,2,4,8 이렇게 지정함으로 원본사진의 반,1/4 로 옵션을 설정)

                Bitmap mImageBitmap = BitmapFactory.decodeFile(cameraTempFilePath, options);
// 비트맵에 값을 가져와서 사용한다

 /* Tiny Image Returned : 섬네일 파일 읽기
mBackBitmap = (Bitmap) data.getExtras().get("data");
ourImageView.setImageBitmap(mBackBitmap);
 */
            }
            else if (requestCode == REQ_GALLERY_SELECT) {

                try {
                    Uri uri = data.getData();
                    Bitmap mImageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
                }
                catch (IOException e){
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    private void copyFile(String from , String to){
        try {
            FileInputStream fis = new FileInputStream(from);
            FileOutputStream newfos = new FileOutputStream(to);
            int readcount=0;
            byte[] buffer = new byte[1024];
            while((readcount = fis.read(buffer,0,1024))!= -1){
                newfos.write(buffer,0,readcount);
            }
            newfos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
