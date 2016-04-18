package com.zego.zegolivedemo.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.zego.zegoavkit.ZegoAVKitCommon;
import com.zego.zegolivedemo.LiveRoomActivity;
import com.zego.zegolivedemo.R;
import com.zego.zegolivedemo.ZegoApiManager;
import com.zego.zegolivedemo.ui.base.AbsBaseFragment;
import com.zego.zegolivedemo.ui.widget.SelectImgsPopupWindow;
import com.zego.zegolivedemo.utils.ExecutorUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.OnClick;

public class FragmentPublish extends AbsBaseFragment {

    public static final int REQUEST_CODE_GET_IMAGE_FROM_ALBUMS = 1001;

    public static final int REQUEST_CODE_GET_IMAGE_FROM_CAMERA = 1002;

    private Bitmap mBitmapCover;

    private String mFilePath;

    @Bind(R.id.edt_publish_title)
    public EditText edtPublishTitle;

    @Bind(R.id.iv_cover)
    public ImageView ivCover;

    private SelectImgsPopupWindow mSelectImgsPopWindow;

    @Override
    protected int getContentViewLayout() {
        return R.layout.fragment_publish;
    }

    @Override
    protected void initExtraData() {

    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initViews() {
        mSelectImgsPopWindow = new SelectImgsPopupWindow(mParentActivity, new SelectImgsPopupWindow.OnSelectImgsListenner() {
            @Override
            public void OnGetImgFromAlbums() {
                getImageFromAlbum();
            }

            @Override
            public void OnGetImgFromCamera() {
                getImageFromCamera();
            }
        });
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected String getPageTag() {
        return null;
    }

    @OnClick(R.id.iv_cover)
    public void selectCoverImage() {
        mSelectImgsPopWindow.setAnimationStyle(android.support.v7.appcompat.R.style.Base_Animation_AppCompat_DropDownUp);
        mSelectImgsPopWindow.showAtLocation(mParentActivity.findViewById(R.id.main), Gravity.RIGHT | Gravity.BOTTOM, 0, 0);
    }

    @OnClick(R.id.btn_start_publish)
    public void startPublish() {

        String title = edtPublishTitle.getText().toString();

        if (title.length() == 0) {
            title = "Live";
        }

        ExecutorUtil.getInstance().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (mBitmapCover != null) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    mBitmapCover.compress(Bitmap.CompressFormat.JPEG, 10, output);
                    // mBitmapCover.recycle();
                    byte[] result = output.toByteArray();
                    try {
                        output.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // "/storage/emulated/0/Pictures/Picture_01_Shark.jpg"

                    // 上传封面
//                    int ret = ZegoApiManager.getInstance().getZegoAVApi().setPublishExtraData(ZegoAVKitCommon.ZegoCustomDataType.File, "cover", null, "/storage/emulated/0/Pictures/Picture_01_Shark.jpg");
                    int ret = ZegoApiManager.getInstance().getZegoAVApi().setPublishExtraData(ZegoAVKitCommon.ZegoCustomDataType.Data, "cover", result, "");
                    Log.d("test", "test");
                }
            }
        });

        Intent intent = new Intent(getActivity(), LiveRoomActivity.class);
        intent.putExtra(LiveRoomActivity.INTENT_KEY_IS_PLAY, false);
        intent.putExtra(LiveRoomActivity.INTENT_KEY_PUBLISH_TITLE, title);

        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_GET_IMAGE_FROM_ALBUMS:
            case REQUEST_CODE_GET_IMAGE_FROM_CAMERA:
                Uri uri = data.getData();
                if (uri == null) {
                    Bundle bundle = data.getExtras();
                    mBitmapCover = (Bitmap) bundle.get("data");
                } else {
                    try {
                        mFilePath = (new File(data.getData().getPath())).getAbsolutePath();
                        mBitmapCover = MediaStore.Images.Media.getBitmap(mParentActivity.getContentResolver(), data.getData());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ivCover.setImageBitmap(mBitmapCover);
                break;
        }
    }

    /**
     * 从相册获取图片.
     */
    private void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_GET_IMAGE_FROM_ALBUMS);
    }

    /**
     * 拍照获取相片.
     */
    protected void getImageFromCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(getImageByCamera, REQUEST_CODE_GET_IMAGE_FROM_CAMERA);
        } else {
            Toast.makeText(mParentActivity, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }
    }
}
