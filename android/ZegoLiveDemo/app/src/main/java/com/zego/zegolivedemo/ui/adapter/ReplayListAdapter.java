package com.zego.zegolivedemo.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zego.zegolivedemo.R;
import com.zego.zegolivedemo.entity.ReplayVideo;
import com.zego.zegolivedemo.ui.widget.CirImageView;
import com.zego.zegolivedemo.ui.widget.MaterialProgressBarSupport;

import java.util.List;

/**
 * Created by Mark on 2016/3/14
 *
 * Des: 回播列表适配器.
 */
public class ReplayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_FOOTER = Integer.MIN_VALUE;
    public static final int TYPE_ITEM = 0;
    private boolean hasMoreData = true;//设置是否可以继续加载数据

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<ReplayVideo> mVideoList;
    private ImageLoader mImageLoader;
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public ReplayListAdapter(Context context, List<ReplayVideo> videoList) {
        mContext = context;
        mVideoList = videoList;
        mLayoutInflater = LayoutInflater.from(context);
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_FOOTER){
            return new FooterViewHolder(mLayoutInflater.inflate(R.layout.item_view_load_more, parent, false));
        }else {
            return new ReplayListHolder(mLayoutInflater.inflate(R.layout.item_video, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof FooterViewHolder){
            if(hasMoreData){
                ((FooterViewHolder) holder).mProgressView.setVisibility(View.VISIBLE);
                ((FooterViewHolder) holder).mProgressView.startProgress();
                //((FooterViewHolder) holder).mProgressView.setIndeterminate(true);
                ((FooterViewHolder) holder).mTextView.setText(R.string.app_loading_more);
            } else {
                ((FooterViewHolder) holder).mProgressView.stopProgress();
                ((FooterViewHolder) holder).mProgressView.setVisibility(View.GONE);
                //((FooterViewHolder) holder).mProgressView.st;
                ((FooterViewHolder) holder).mTextView.setText(R.string.app_no_more_data);
            }
        }else {
            ReplayVideo video = mVideoList.get(position);
            mImageLoader.displayImage(video.getScreenShotUrl(), ((ReplayListHolder)holder).ivScreenShot);
            ((ReplayListHolder)holder).tvUserName.setText(video.getUserName());
            ((ReplayListHolder)holder).tvPublishTime.setText(video.getPublishTime());

            if(mOnItemClickListener != null){
                ((ReplayListHolder)holder).rlytItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.OnItemClick(((ReplayListHolder)holder).rlytItem, ((ReplayListHolder)holder).getLayoutPosition());
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == mVideoList.size()){
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if(mVideoList == null || mVideoList.size() == 0){
            return 0;
        }
        return  mVideoList.size() + 1;
    }


    public static class ReplayListHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlytItem;
        CirImageView civAvatar;
        TextView tvUserName;
        TextView tvPublishTime;
        ImageView ivScreenShot;

        public ReplayListHolder(View itemView) {
            super(itemView);
            rlytItem = (RelativeLayout)itemView.findViewById(R.id.rlyt_item);
            civAvatar = (CirImageView) itemView.findViewById(R.id.civ_avatar);
            tvUserName = (TextView) itemView.findViewById(R.id.tv_username);
            tvPublishTime = (TextView) itemView.findViewById(R.id.tv_publish_time);
            ivScreenShot = (ImageView) itemView.findViewById(R.id.iv_screenshot);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public final MaterialProgressBarSupport mProgressView;
        public final TextView mTextView;

        public FooterViewHolder(View view) {
            super(view);
            mProgressView = (MaterialProgressBarSupport) view.findViewById(R.id.progress_view);
            mTextView = (TextView) view.findViewById(R.id.tv_content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }

    public void setHasMoreData(boolean hasMoreData) {
        if(this.hasMoreData != hasMoreData) {
            this.hasMoreData = hasMoreData;
            notifyDataSetChanged();
        }
    }
}
