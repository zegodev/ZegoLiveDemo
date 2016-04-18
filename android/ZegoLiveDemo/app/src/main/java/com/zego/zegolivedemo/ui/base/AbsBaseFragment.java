package com.zego.zegolivedemo.ui.base;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.zego.zegolivedemo.utils.ToastUtils;

import butterknife.ButterKnife;


/**
 * Fragment基类.
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

    protected ToastUtils mToastUtils;


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

    /**
     * 获取页面的标签, 即子类的名称， 必须返回.
     *
     * @return 返回页面的标签
     */
    protected abstract String getPageTag();

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


    /**
     * 初始化父类中的变量.
     */
    private void initBaseVariables() {
        mParentActivity = getActivity();
        mResources = getResources();
        mHandler = new Handler();
        mToastUtils = ToastUtils.genToastUtils(mParentActivity);
    }
}
