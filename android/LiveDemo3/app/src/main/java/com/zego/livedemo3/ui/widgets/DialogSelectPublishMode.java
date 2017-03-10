package com.zego.livedemo3.ui.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.zego.livedemo3.R;

/**
 * Copyright © 2016 Zego. All rights reserved.
 * des: 选择直播模式.
 */

public class DialogSelectPublishMode extends DialogFragment implements View.OnClickListener {

    private OnSelectPublishModeListener mOnSelectPublishModeListener;

    public void setOnSelectPublishModeListener(OnSelectPublishModeListener listener){
        mOnSelectPublishModeListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_fragment_select_publish_mode, null);

        view.findViewById(R.id.tv_select_single_anchor).setOnClickListener(this);
        view.findViewById(R.id.tv_select_more_anchors).setOnClickListener(this);
        view.findViewById(R.id.tv_select_mix_stream).setOnClickListener(this);
        view.findViewById(R.id.tv_select_game_living).setOnClickListener(this);
        view.findViewById(R.id.tv_cancel).setOnClickListener(this);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);

        return dialog;
    }

    @Override
    public void onClick(View v) {
            if(mOnSelectPublishModeListener != null){
                switch (v.getId()) {
                    case R.id.tv_select_single_anchor:
                        mOnSelectPublishModeListener.onSingleAnchorSelect();
                        break;
                    case R.id.tv_select_more_anchors:
                        mOnSelectPublishModeListener.onMoreAnchorsSelect();
                        break;
                    case R.id.tv_select_mix_stream:
                        mOnSelectPublishModeListener.onMixStreamSelect();
                        break;
                    case R.id.tv_select_game_living:
                        mOnSelectPublishModeListener.onGameLivingSelect();
                        break;
                    case R.id.tv_cancel:
                        break;
                }

                dismiss();
            }
    }


    public interface OnSelectPublishModeListener{
        void onSingleAnchorSelect();
        void onMoreAnchorsSelect();
        void onMixStreamSelect();
        void onGameLivingSelect();
    }

}
