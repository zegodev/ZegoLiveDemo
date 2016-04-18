package com.zego.zegolivedemo.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zego.zegolivedemo.R;

public class LazyAdapter extends BaseAdapter {

    private static LayoutInflater inflater=null;
    private ArrayList<HashMap<String, String>> data;

    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        Activity activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.my_listitem, null);

        TextView title = (TextView)vi.findViewById(R.id.ItemTitle); // 标题
        TextView artist = (TextView)vi.findViewById(R.id.ItemText); // 歌手名
        TextView duration = (TextView)vi.findViewById(R.id.duration); // 时长
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.ImageViewCover); // 缩略图

        HashMap<String, String> item = data.get(position);

        Integer i = Integer.parseInt(item.get("startTime"));
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
        String timeString = format.format(new Date(i * 1000L));

        // 设置ListView的相关值
        title.setText(item.get("title"));
        artist.setText(item.get("userName"));
        duration.setText(timeString);

        String imageURL = item.get("coverImageURL");
        ImageLoader.getInstance().displayImage(imageURL, thumb_image);
        return vi;
    }
}
