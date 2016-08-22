package com.zego.livedemo3.ui.fragments;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zego.biz.BizRoom;
import com.zego.livedemo3.R;
import com.zego.livedemo3.interfaces.OnUpdateRoomListListener;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.ui.activities.PlayActivity;
import com.zego.livedemo3.ui.adapters.ListRoomAdapter;
import com.zego.livedemo3.ui.adapters.SpaceItemDecoration;
import com.zego.livedemo3.ui.base.AbsBaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class RoomListFragment extends AbsBaseFragment {

    @Bind(R.id.srl)
    public SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.rlv_room_list)
    public RecyclerView rlvRoomList;

    @Bind(R.id.tv_hint_pull_refresh)
    public TextView tvHintPullRefresh;

    private ArrayList<BizRoom> mListRoom;

    private ListRoomAdapter mListRoomAdapter;

    private LinearLayoutManager mLinearLayoutManager;

    private int mStartPageIndex  = 0;

    public static final int PAGE_SIZE = 20;

    public static RoomListFragment newInstance(){
        return new RoomListFragment();
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.fragment_room_list;
    }

    @Override
    protected void initExtraData() {

    }

    @Override
    protected void initVariables() {

        BizLivePresenter.getInstance().setUpdateRoomListListener(new OnUpdateRoomListListener() {
            @Override
            public void onUpdateRoomList(List<BizRoom> listRoom) {
                mStartPageIndex += listRoom.size();
                mListRoom.addAll(listRoom);

                if(mListRoom.size() == 0){
                    tvHintPullRefresh.setVisibility(View.VISIBLE);
                }else {
                    tvHintPullRefresh.setVisibility(View.INVISIBLE);
                }
                mListRoomAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, mHandler);

        mListRoom = new ArrayList<>();
        mListRoomAdapter = new ListRoomAdapter(mParentActivity, mListRoom);
        mListRoomAdapter.setHasMoreData(false);
        mListRoomAdapter.setOnItemClickListener(new ListRoomAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                BizRoom roomInfo = mListRoom.get(position);
                PlayActivity.actionStart(mParentActivity, roomInfo.roomKey, roomInfo.serverKey, roomInfo.listStream);
            }
        });
    }

    @Override
    protected void initViews() {
        mLinearLayoutManager = new LinearLayoutManager(mParentActivity);
        rlvRoomList.setLayoutManager(mLinearLayoutManager);
        rlvRoomList.addItemDecoration(new SpaceItemDecoration(mResources.getDimensionPixelSize(R.dimen.dimen_5)));

        // 设置 进度条的颜色变化，最多可以设置4种颜色
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark,  android.R.color.holo_orange_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新, 数据清零
                mStartPageIndex = 0;
                mListRoom.clear();

                BizLivePresenter.getInstance().getRoomList(mStartPageIndex, PAGE_SIZE);
            }
        });

        rlvRoomList.setAdapter(mListRoomAdapter);

    }

    @Override
    protected void loadData() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                BizLivePresenter.getInstance().getRoomList(mStartPageIndex, PAGE_SIZE);
            }
        });
    }
}
