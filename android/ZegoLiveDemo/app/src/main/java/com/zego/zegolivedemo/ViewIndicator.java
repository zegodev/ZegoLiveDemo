package com.zego.zegolivedemo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ViewIndicator extends LinearLayout implements OnClickListener {
	private int mDefaultIndicator = 0; // 默认的选定View

	private static int mCurIndicator; // 当前选定View

	private static View[] mIndicators; // View集合

	private OnIndicateListener mOnIndicateListener; // 对应的监听器
	// 对应的图标Tag
	private static final String TAG_ICON_0 = "icon_tag_0";
	private static final String TAG_ICON_1 = "icon_tag_1";
	private static final String TAG_ICON_2 = "icon_tag_2";
	private static final String TAG_ICON_3 = "icon_tag_3";
	private static final String TAG_ICON_4 = "icon_tag_4";
	// 对应的文字Tag
	private static final String TAG_TEXT_0 = "text_tag_0";
	private static final String TAG_TEXT_1 = "text_tag_1";
	private static final String TAG_TEXT_2 = "text_tag_2";
	private static final String TAG_TEXT_3 = "text_tag_3";
	private static final String TAG_TEXT_4 = "text_tag_4";
	// 未选中状态
	private static final int COLOR_UNSELECT = Color.argb(100, 0xff, 0xff, 0xff);
	// 选中状态
	private static final int COLOR_SELECT = Color.WHITE;

	// 构造函数
	public ViewIndicator(Context context) {
		super(context);
	}

	public ViewIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCurIndicator = mDefaultIndicator;
		setOrientation(LinearLayout.HORIZONTAL);// 水平布局
		init();
	}

	/**
	 * 菜单视图布局
	 *
	 * @param iconResID
	 *            图片资源ID
	 * @param stringResID
	 *            文字资源ID
	 * @param stringColor
	 *            颜色资源ID
	 * @param iconTag
	 *            图片标签
	 * @param textTag
	 *            文字标签
	 * @return
	 */
	private View createIndicator(int iconResID, int stringResID,
								 int stringColor, String iconTag, String textTag) {
		// 实例一个LinearLayout
		LinearLayout view = new LinearLayout(getContext());
		view.setOrientation(LinearLayout.VERTICAL);// 垂直布局
		// 设置宽高和权重
		view.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		view.setGravity(Gravity.CENTER_HORIZONTAL);
		view.setBackgroundResource(R.mipmap.main_tab_item_bg_normal);
		// 实例一个ImageView
		ImageView iconView = new ImageView(getContext());
		// 设置与该ImageView视图相关联的标记
		iconView.setTag(iconTag);
		// 设置宽高和权重
		iconView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		iconView.setImageResource(iconResID);// 设置图片资源
		// 实例一个TextView
		TextView textView = new TextView(getContext());
		// 设置与该TextView视图相关联的标记
		textView.setTag(textTag);
		// 设置宽高和权重
		textView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		// 设置文字颜色
		textView.setTextColor(stringColor);
		// 设置文字大小
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		// 设置文字资源
		textView.setText(stringResID);
		// 添加视图到布局中
		view.addView(iconView);
		view.addView(textView);
		// 返回布局视图
		return view;

	}

	/**
	 * 初始化视图
	 */
	private void init() {
		mIndicators = new View[5];// 5个View
		// 第一个为默认选中的
		// 主页main_tab_item_bg_normal
		mIndicators[0] = createIndicator(R.mipmap.main_tab_item_home_focus,
				R.string.tab_item_home, COLOR_SELECT, TAG_ICON_0, TAG_TEXT_0);
		mIndicators[0].setBackgroundResource(R.drawable.main_tab_item_bg);
		mIndicators[0].setTag(Integer.valueOf(0));
		mIndicators[0].setOnClickListener(this);
		addView(mIndicators[0]);
		// 分类
		mIndicators[1] = createIndicator(
				R.mipmap.main_tab_item_replay_normal,
				R.string.tab_item_replay, COLOR_UNSELECT, TAG_ICON_1,
				TAG_TEXT_1);
		mIndicators[1].setBackgroundResource(R.drawable.main_tab_item_bg);
		mIndicators[1].setTag(Integer.valueOf(1));
		mIndicators[1].setOnClickListener(this);
		addView(mIndicators[1]);
		// 下载
		mIndicators[2] = createIndicator(R.mipmap.main_tab_item_publish_normal,
				R.string.tab_item_publish, COLOR_UNSELECT, TAG_ICON_2, TAG_TEXT_2);
		mIndicators[2].setBackgroundResource(R.drawable.main_tab_item_bg);
		mIndicators[2].setTag(Integer.valueOf(2));
		mIndicators[2].setOnClickListener(this);
		addView(mIndicators[2]);
		// 我的
		mIndicators[3] = createIndicator(R.mipmap.main_tab_item_hot_normal,
				R.string.tab_item_hot, COLOR_UNSELECT, TAG_ICON_3, TAG_TEXT_3);
		mIndicators[3].setBackgroundResource(R.drawable.main_tab_item_bg);
		mIndicators[3].setTag(Integer.valueOf(3));
		mIndicators[3].setOnClickListener(this);
		addView(mIndicators[3]);
		// 设置
		mIndicators[4] = createIndicator(
				R.mipmap.main_tab_item_profile_normal,
				R.string.tab_item_profile, COLOR_UNSELECT, TAG_ICON_4,
				TAG_TEXT_4);
		mIndicators[4].setBackgroundResource(R.drawable.main_tab_item_bg);
		mIndicators[4].setTag(Integer.valueOf(4));
		mIndicators[4].setOnClickListener(this);
		addView(mIndicators[4]);
	}

	public static void setIndicator(int which) {
		// /////////////////清除之前的状态/////////////////////////////////
		// mIndicators[mCurIndicator].setBackgroundResource(R.drawable.main_tab_item_bg_normal);
		ImageView prevIcon;
		TextView prevText;
		switch (mCurIndicator) {
			case 0:
				prevIcon = (ImageView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_ICON_0);
				prevIcon.setImageResource(R.drawable.main_tab_item_home);
				prevText = (TextView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_TEXT_0);
				prevText.setTextColor(COLOR_UNSELECT);
				break;
			case 1:
				prevIcon = (ImageView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_ICON_1);
				prevIcon.setImageResource(R.drawable.main_tab_item_replay);
				prevText = (TextView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_TEXT_1);
				prevText.setTextColor(COLOR_UNSELECT);
				break;
			case 2:
				prevIcon = (ImageView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_ICON_2);
				prevIcon.setImageResource(R.drawable.main_tab_item_publish);
				prevText = (TextView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_TEXT_2);
				prevText.setTextColor(COLOR_UNSELECT);
				break;
			case 3:
				prevIcon = (ImageView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_ICON_3);
				prevIcon.setImageResource(R.drawable.main_tab_item_hot);
				prevText = (TextView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_TEXT_3);
				prevText.setTextColor(COLOR_UNSELECT);
				break;
			case 4:
				prevIcon = (ImageView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_ICON_4);
				prevIcon.setImageResource(R.drawable.main_tab_item_profile);
				prevText = (TextView) mIndicators[mCurIndicator]
						.findViewWithTag(TAG_TEXT_4);
				prevText.setTextColor(COLOR_UNSELECT);
				break;
		}
		// /////////////////更新前状态/////////////////////////////////
		// mIndicators[which].setBackgroundResource(R.drawable.main_tab_item_bg_focus);
		ImageView currIcon;
		TextView currText;
		/**
		 * 设置选中状态
		 */
		switch (which) {
			case 0:
				currIcon = (ImageView) mIndicators[which]
						.findViewWithTag(TAG_ICON_0);
				currIcon.setImageResource(R.mipmap.main_tab_item_home_focus);
				currText = (TextView) mIndicators[which]
						.findViewWithTag(TAG_TEXT_0);
				currText.setTextColor(COLOR_SELECT);
				break;
			case 1:
				currIcon = (ImageView) mIndicators[which]
						.findViewWithTag(TAG_ICON_1);
				currIcon.setImageResource(R.mipmap.main_tab_item_replay_focus);
				currText = (TextView) mIndicators[which]
						.findViewWithTag(TAG_TEXT_1);
				currText.setTextColor(COLOR_SELECT);
				break;
			case 2:
				currIcon = (ImageView) mIndicators[which]
						.findViewWithTag(TAG_ICON_2);
				currIcon.setImageResource(R.mipmap.main_tab_item_publish_focus);
				currText = (TextView) mIndicators[which]
						.findViewWithTag(TAG_TEXT_2);
				currText.setTextColor(COLOR_SELECT);
				break;
			case 3:
				currIcon = (ImageView) mIndicators[which]
						.findViewWithTag(TAG_ICON_3);
				currIcon.setImageResource(R.mipmap.main_tab_item_hot_focus);
				currText = (TextView) mIndicators[which]
						.findViewWithTag(TAG_TEXT_3);
				currText.setTextColor(COLOR_SELECT);
				break;
			case 4:
				currIcon = (ImageView) mIndicators[which]
						.findViewWithTag(TAG_ICON_4);
				currIcon.setImageResource(R.mipmap.main_tab_item_profile_focus);
				currText = (TextView) mIndicators[which]
						.findViewWithTag(TAG_TEXT_4);
				currText.setTextColor(COLOR_SELECT);
				break;
		}

		mCurIndicator = which;
	}

	public interface OnIndicateListener {
		public void onIndicate(View v, int which);
	}

	public void setOnIndicateListener(OnIndicateListener listener) {
		mOnIndicateListener = listener;
	}

	@Override
	public void onClick(View v) {
		if (mOnIndicateListener != null) {
			int tag = (Integer) v.getTag();
			switch (tag) {
				case 0:
					if (mCurIndicator != 0) {
						mOnIndicateListener.onIndicate(v, 0);
						setIndicator(0);
					}
					break;
				case 1:
					if (mCurIndicator != 1) {
						mOnIndicateListener.onIndicate(v, 1);
						setIndicator(1);
					}
					break;
				case 2:
					if (mCurIndicator != 2) {
						mOnIndicateListener.onIndicate(v, 2);
						setIndicator(2);
					}
					break;
				case 3:
					if (mCurIndicator != 3) {
						mOnIndicateListener.onIndicate(v, 3);
						setIndicator(3);
					}
					break;
				case 4:
					if (mCurIndicator != 4) {
						mOnIndicateListener.onIndicate(v, 4);
						setIndicator(4);
					}
					break;
				default:
					break;
			}
		}
	}
}
