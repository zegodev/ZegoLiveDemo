package com.zego.livedemo3.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zego.livedemo3.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des:
 */
public class NavigationBar extends LinearLayout implements View.OnClickListener{

    private LinearLayout mRootView;

    private String[] mTabTitles;

    private List<RelativeLayout> mTabs;

    private int mColorNormal;

    private int mColorSelected;

    private NavigationBarListener mNavigationBarListener;

    public NavigationBar(Context context) {
        super(context);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources resources = context.getResources();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationBar);
        mTabTitles =  resources.getStringArray(
                typedArray.getResourceId(R.styleable.NavigationBar_tabTitles, R.array.navigation_bar_titles));
        typedArray.recycle();

        mColorNormal = resources.getColor(R.color.text_black);
        mColorSelected = resources.getColor(R.color.colorAccent);

        initViews(context);
    }

    private void initViews(Context context){
        mRootView = (LinearLayout) (LayoutInflater.from(context).inflate(R.layout.view_navigation_bar, this)).findViewById(R.id.llyt_root);

        mTabs = new ArrayList<>();
        int tabIndex = 0;
        for(int i = 0, size = mRootView.getChildCount(); i < size; i++){
            if(mRootView.getChildAt(i) instanceof RelativeLayout){
                RelativeLayout tab = (RelativeLayout) mRootView.getChildAt(i);
                mTabs.add(tab);
                TextView tv = getTextViewFromTab(tab);
                if(tv != null){
                    tv.setText(mTabTitles[tabIndex]);
                    tv.setTextColor(mColorNormal);
                }
                tab.setTag(tabIndex);
                tab.setOnClickListener(this);
                tabIndex++;
            }
        }
    }


    @Override
    public void onClick(View v) {
        Integer tabIndex = (Integer) v.getTag();
        selectTab(tabIndex);
        if(mNavigationBarListener != null){
            mNavigationBarListener.onTabSelect(tabIndex);
        }
    }

    private void resetTabs(){
        for(RelativeLayout tab : mTabs){
            TextView tv = getTextViewFromTab(tab);
            if(tv != null){
                tv.setTextColor(mColorNormal);
            }
        }
    }


    private TextView getTextViewFromTab(RelativeLayout tab){
        TextView tv = null;

        for(int i = 0, size = tab.getChildCount(); i < size; i++){
            if(tab.getChildAt(i) instanceof TextView){
                tv = (TextView) tab.getChildAt(i);
                break;
            }
        }

        return tv;
    }

    public void selectTab(int tabIndex){
        if(tabIndex >= 0 && tabIndex <= mTabs.size()){
            resetTabs();
            getTextViewFromTab(mTabs.get(tabIndex)).setTextColor(mColorSelected);
        }
    }

    public void setNavigationBarListener(NavigationBarListener listener){
        mNavigationBarListener = listener;
    }

    public interface NavigationBarListener{
        void onTabSelect(int tabIndex);
    }
}
