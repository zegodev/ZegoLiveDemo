package com.zego.zegolivedemo.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.zego.zegolivedemo.R;

public class FragmentHot extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_hot, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        WebView wv = (WebView)getActivity().findViewById(R.id.webView);
        wv.loadUrl("http://www.zego.im");
    }
}
