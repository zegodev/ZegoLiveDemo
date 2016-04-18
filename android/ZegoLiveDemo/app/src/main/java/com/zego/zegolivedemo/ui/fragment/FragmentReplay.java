package com.zego.zegolivedemo.ui.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;


import com.zego.zegoavkit.ZegoAVApi;
import com.zego.zegoavkit.ZegoAVRoomInfo;
import com.zego.zegoavkit.ZegoStreamInfo;
import com.zego.zegoavkit.callback.ZegoAVReplayListCallback;
import com.zego.zegolivedemo.R;
import com.zego.zegolivedemo.ZegoApiManager;
import com.zego.zegolivedemo.entity.ReplayVideo;
import com.zego.zegolivedemo.ui.activity.PlayerActivity;
import com.zego.zegolivedemo.ui.adapter.ReplayListAdapter;
import com.zego.zegolivedemo.ui.adapter.SpaceItemDecoration;
import com.zego.zegolivedemo.ui.base.AbsBaseFragment;
import com.zego.zegolivedemo.ui.widget.EndlessRecyclerOnScrollListener;
import com.zego.zegolivedemo.utils.ExecutorUtil;
import com.zego.zegolivedemo.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class FragmentReplay extends AbsBaseFragment {

    public static final String TAG = "FragmentReplay";

    private ZegoAVApi mZegoAVApi;

    private ReplayListAdapter mReplayListAdapter;

    private LinearLayoutManager mLinearLayoutManager;

    private List<ReplayVideo> mVideoList;

    private int mStartPageIndex = 0;

    private static final int PAGE_SIZE = 10;

    @Bind(R.id.srl)
    public SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.rcv_video_list)
    public RecyclerView recyclerView;



    @Override
    protected int getContentViewLayout() {
        return R.layout.fragment_replay;
    }

    @Override
    protected void initExtraData() {

    }

    @Override
    protected void initVariables() {

        mZegoAVApi = ZegoApiManager.getInstance().getZegoAVApi();

        mVideoList = new ArrayList<>();

        setCallback();

        mReplayListAdapter = new ReplayListAdapter(mParentActivity, mVideoList);
        mReplayListAdapter.setHasMoreData(true);
        mReplayListAdapter.setOnItemClickListener(new ReplayListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                PlayerActivity.actionStart(mParentActivity, mVideoList.get(position).getVideoUrl());
            }
        });
    }

    @Override
    protected void initViews() {

        mLinearLayoutManager = new LinearLayoutManager(mParentActivity);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.addItemDecoration(new SpaceItemDecoration(mResources.getDimensionPixelSize(R.dimen.dimen_5)));

        // 设置 进度条的颜色变化，最多可以设置4种颜色
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_red_light,
                android.R.color.holo_red_light, android.R.color.holo_red_light);

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        recyclerView.setAdapter(mReplayListAdapter);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadMoreData();
            }
        });
    }

    @Override
    protected void loadData() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        }, 500);

    }

    @Override
    protected String getPageTag() {
        return TAG;
    }

    /**
     * 下拉刷新.
     */
    private void refreshData(){
        mStartPageIndex = 0;
        mVideoList.clear();
        requstData();
    }

    /**
     * 上拉加载更多。
     */
    private void loadMoreData(){
        mStartPageIndex += PAGE_SIZE;
        requstData();
    }

    /**
     * 获取数据.
     */
    private void requstData(){
        mZegoAVApi.refreshReplayList();
        mZegoAVApi.getReplayList(mStartPageIndex, PAGE_SIZE);
    }

    /**
     * 设置api的回调，用于拉取数据.
     */
    private void setCallback(){
        ZegoApiManager.getInstance().getZegoAVApi().setReplayListCallback(new ZegoAVReplayListCallback() {
            @Override
            public void onGetReplayList(int errorCode, int totalCount, final int beginIndex, final ZegoAVRoomInfo[] roomInfoList) {
                ExecutorUtil.getInstance().getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (roomInfoList != null) {
                            for (ZegoAVRoomInfo zegoAVRoomInfo : roomInfoList) {
                                ArrayList<ZegoStreamInfo> zegoStreamInfos = zegoAVRoomInfo.getStreamInfoList();
                                if (zegoStreamInfos != null && zegoStreamInfos.size() > 0) {
                                    ZegoStreamInfo streamInfo = zegoStreamInfos.get(0);
                                    ReplayVideo video = new ReplayVideo();
                                    // 视频创建时间
                                    video.setPublishTime(TimeUtils.getRelativeTime(mResources, zegoAVRoomInfo.createdTime, true));
                                    // 视频标题
                                    video.setVideoTitle(streamInfo.streamTitle);
                                    // 视频截图地址
                                    video.setScreenShotUrl(streamInfo.screenShotUrl);
                                    // 发布本视频的用户的userName
                                    video.setUserName(streamInfo.userName);
                                    // 发布本视频的用户的userID
                                    video.setUserId(streamInfo.userID);
                                    if (streamInfo.replayInfo != null) {
                                        video.setVideoTime((streamInfo.replayInfo.endTime - streamInfo.replayInfo.beginTime) + "");
                                        if (streamInfo.replayInfo.urlList != null) {
                                            video.setVideoUrl(streamInfo.replayInfo.urlList[0]);
                                        }
                                    }
                                    mVideoList.add(video);
                                }
                            }
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mReplayListAdapter.notifyDataSetChanged();
                                        swipeRefreshLayout.setRefreshing(false);
                                    }
                                }, 1000);
                            }
                        });
                    }
                });
            }
        });
    }
}
