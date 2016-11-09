package com.zego.livedemo3.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.livedemo3.R;

import java.util.LinkedList;

/**
 * Created by 赵晨璞 on 2016/6/19.
 *RecyclerView适配器
 */

public  class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.MyViewHolder> {

    private Context mContext;

    private LinkedList<String> mDatas;

    private LayoutInflater mLayoutInflater;

    public LogListAdapter(Context context, LinkedList<String> datas) {
        mContext=context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public LogListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.item_log_list, parent, false));
    }

    @Override
    public void onBindViewHolder(LogListAdapter.MyViewHolder holder, int position) {
        holder.tv.setText(mDatas.get(position));
    }

    @Override
    public int getItemCount()
    {
        return mDatas.size();
    }

    public void setDatas(LinkedList<String> datas){
        mDatas = datas;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        private TextView tv;

        public MyViewHolder(View view)
        {
            super(view);
            tv = (TextView) view.findViewById(R.id.tv);
        }
    }



}
