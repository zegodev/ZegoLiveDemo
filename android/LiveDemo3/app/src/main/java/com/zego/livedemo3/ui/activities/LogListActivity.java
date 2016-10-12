package com.zego.livedemo3.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.zego.livedemo3.R;
import com.zego.livedemo3.ui.activities.moreanchors.MorAnchorsBaseLiveActivity;
import com.zego.livedemo3.ui.adapters.LogListAdapter;
import com.zego.livedemo3.ui.base.AbsBaseActivity;
import com.zego.livedemo3.utils.PreferenceUtil;

import java.util.LinkedList;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class LogListActivity extends AbsBaseActivity {

    @Bind(R.id.recyclerView)
    public RecyclerView recyclerView;

    private LogListAdapter mLogListAdapter;

    private LinkedList<String> mLinkedListData;

    public static void actionStart(Activity activity){
        Intent intent = new Intent(activity, LogListActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_log_list;
    }

    @Override
    protected void initExtraData(Bundle savedInstanceState) {

    }

    @Override
    protected void initVariables(Bundle savedInstanceState) {
        mLinkedListData = (LinkedList<String>) PreferenceUtil.getInstance().getObjectFromString(MorAnchorsBaseLiveActivity.KEY_LIST_LOG);
        if (mLinkedListData == null) {
            mLinkedListData = new LinkedList<>();
        }
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLogListAdapter = new LogListAdapter(this, mLinkedListData);
        recyclerView.setAdapter(mLogListAdapter);
    }

    @Override
    protected void loadData(Bundle savedInstanceState) {

    }

    @OnClick(R.id.tv_back)
    public void back(){
        finish();
    }
}
