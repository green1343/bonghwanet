package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.bonghwa.GroupInfo.ChatMsg;
import com.example.android.bonghwa.Manager;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private Activity m_activity;
    public ArrayList<ChatMsg> arr = new ArrayList<>();
    public ChatListAdapter(Activity act) {
        this.m_activity = act;
        mInflater = (LayoutInflater)m_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return arr.size();
    }
    @Override
    public Object getItem(int position) {
        return arr.get(position);
    }
    public long getItemId(int position){
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            int res = 0;
            res = R.layout.list_item_chatting;
            convertView = mInflater.inflate(res, parent, false);
        }
        ImageView imView = (ImageView)convertView.findViewById(R.id.vi_image);
        TextView title = (TextView)convertView.findViewById(R.id.vi_title);
        TextView content = (TextView)convertView.findViewById(R.id.vi_content);
        LinearLayout layout_view =  (LinearLayout)convertView.findViewById(R.id.vi_view);
        int resId = R.drawable.profile;
        imView.setBackgroundResource(resId);
        title.setText(Manager.INSTANCE.getUserName(arr.get(position).uploader));
        content.setText(arr.get(position).text);

		/*	버튼에 이벤트처리를 하기위해선 setTag를 이용해서 사용할 수 있습니다.
		 *
		 * 	Button btn 가 있다면, btn.setTag(position)을 활용해서 각 버튼들의 이벤트처리를 할 수 있습니다.
		 */
        layout_view.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            }
        });
        return convertView;
    }
}
