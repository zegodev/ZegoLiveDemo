package com.zego.livedemo3.ui.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zego.livedemo3.R;
import com.zego.livedemo3.ui.adapters.LogListAdapter;

import java.util.LinkedList;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class ViewLogList extends LinearLayout {

    private LinearLayout mRootView;

    private RecyclerView mRecyclerView;

    private TextView mTvBack;

    private LogListCallback mLogListCallback;

    private LogListAdapter mLogListAdapter;

    public ViewLogList(Context context) {
        super(context);
    }

    public ViewLogList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewLogList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initViews(context);
    }

    private void initViews(Context context) {
        mRootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.activity_log_list, this);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mTvBack = (TextView) mRootView.findViewById(R.id.tv_back);
        mTvBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLogListCallback != null){
                    mLogListCallback.onCallback();
                }
            }
        });
    }

    public void initList(Context context, LinkedList<String> datas){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mLogListAdapter = new LogListAdapter(context, datas);
        mRecyclerView.setAdapter(mLogListAdapter);
    }

    public void setDatas(LinkedList<String> datas){
        mLogListAdapter.setDatas(datas);
    }

    public void setLogListCallback(LogListCallback callback) {
        mLogListCallback = callback;
    }

    public interface LogListCallback {
        void onCallback();
    }
}
