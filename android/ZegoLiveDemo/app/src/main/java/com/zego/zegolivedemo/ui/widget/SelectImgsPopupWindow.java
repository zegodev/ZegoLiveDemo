package com.zego.zegolivedemo.ui.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.zego.zegolivedemo.R;

/**
 * Copyright © 2016 Zego. All rights reserved.
 *
 * des: 底部弹出框, 用于选择图片.
 */
public class SelectImgsPopupWindow extends PopupWindow implements View.OnClickListener{

    private Button mBtnAlbums;

    private Button mBtnCamera;

    private Button mBtnCancel;

    private View mMenuView;

    private OnSelectImgsListenner mOnSelectImgsListenner;

    public SelectImgsPopupWindow(Context context, OnSelectImgsListenner listenner) {

        mMenuView = LayoutInflater.from(context).inflate(R.layout.dialog_select_images, null);
        mOnSelectImgsListenner = listenner;

        mBtnAlbums = (Button) mMenuView.findViewById(R.id.btn_albums);
        mBtnCamera = (Button) mMenuView.findViewById(R.id.btn_camera);
        mBtnCancel = (Button) mMenuView.findViewById(R.id.btn_cancel);

        mBtnCancel.setOnClickListener(this);
        mBtnAlbums.setOnClickListener(this);
        mBtnCamera.setOnClickListener(this);

        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_albums:
                if(mOnSelectImgsListenner != null){
                    mOnSelectImgsListenner.OnGetImgFromAlbums();
                }
                break;
            case R.id.btn_camera:
                if(mOnSelectImgsListenner != null){
                    mOnSelectImgsListenner.OnGetImgFromCamera();
                }
                break;
            case R.id.btn_cancel:
                break;
        }
        dismiss();
    }

    public interface OnSelectImgsListenner {
        /**
         * 从相册获取图片.
         */
        void OnGetImgFromAlbums();

        /**
         * 从相机拍照获取照片.
         */
        void OnGetImgFromCamera();
    }
}
