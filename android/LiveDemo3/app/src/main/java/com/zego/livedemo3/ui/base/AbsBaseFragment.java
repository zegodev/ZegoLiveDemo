package com.zego.livedemo3.ui.base;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by wubiao on 2015/12/29
 *
 * Des: fragment 基类
 */
public abstract class AbsBaseFragment extends Fragment {

    protected Resources mResources;

    /**
     * 宿主activity.
     */
    protected Activity mParentActivity;

    /**
     * 主线程绑定的Handler.
     */
    protected Handler mHandler;

    /**
     * 页面布局.
     */
    protected View mRootView;


    /**
     * 获取内容页面的布局.
     *
     * @return 返回内容页面的布局
     */
    protected abstract int getContentViewLayout();

    /**
     * 初始化从外部传递过来的数据.
     */
    protected abstract void initExtraData();

    /**
     * 初始化子类中的变量.
     */
    protected abstract void initVariables();

    /**
     * 初始化子类中的控件.
     */
    protected abstract void initViews();

    /**
     * 加载数据.
     */
    protected abstract void loadData();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExtraData();
        initBaseVariables();
        initVariables();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getContentViewLayout(), container, false);
            // 初始化View注入
            ButterKnife.bind(this, mRootView);

            initViews();
            loadData();
        } else {
            ViewGroup viewGroup = (ViewGroup) mRootView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mRootView);
            }
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 初始化父类中的变量.
     */
    private void initBaseVariables() {
        mParentActivity = getActivity();
        mResources = getResources();
        mHandler = new Handler();
    }
}
