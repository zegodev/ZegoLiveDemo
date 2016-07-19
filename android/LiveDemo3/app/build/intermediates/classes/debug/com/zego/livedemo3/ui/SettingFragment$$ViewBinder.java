// Generated code from Butter Knife. Do not modify!
package com.zego.livedemo3.ui;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SettingFragment$$ViewBinder<T extends com.zego.livedemo3.ui.SettingFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558557, "field 'tvsdkVersion'");
    target.tvsdkVersion = finder.castView(view, 2131558557, "field 'tvsdkVersion'");
    view = finder.findRequiredView(source, 2131558558, "field 'etUserAccount'");
    target.etUserAccount = finder.castView(view, 2131558558, "field 'etUserAccount'");
    view = finder.findRequiredView(source, 2131558559, "field 'etUserName'");
    target.etUserName = finder.castView(view, 2131558559, "field 'etUserName'");
    view = finder.findRequiredView(source, 2131558560, "field 'spinnerResolutions'");
    target.spinnerResolutions = finder.castView(view, 2131558560, "field 'spinnerResolutions'");
    view = finder.findRequiredView(source, 2131558561, "field 'tvResolution'");
    target.tvResolution = finder.castView(view, 2131558561, "field 'tvResolution'");
    view = finder.findRequiredView(source, 2131558562, "field 'seekbarResolution'");
    target.seekbarResolution = finder.castView(view, 2131558562, "field 'seekbarResolution'");
    view = finder.findRequiredView(source, 2131558563, "field 'tvFps'");
    target.tvFps = finder.castView(view, 2131558563, "field 'tvFps'");
    view = finder.findRequiredView(source, 2131558564, "field 'seekBarFps'");
    target.seekBarFps = finder.castView(view, 2131558564, "field 'seekBarFps'");
    view = finder.findRequiredView(source, 2131558565, "field 'tvBitrate'");
    target.tvBitrate = finder.castView(view, 2131558565, "field 'tvBitrate'");
    view = finder.findRequiredView(source, 2131558566, "field 'seekBarBitrate'");
    target.seekBarBitrate = finder.castView(view, 2131558566, "field 'seekBarBitrate'");
    view = finder.findRequiredView(source, 2131558567, "field 'tvDemoVersion' and method 'showHideOperation'");
    target.tvDemoVersion = finder.castView(view, 2131558567, "field 'tvDemoVersion'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.showHideOperation();
        }
      });
    view = finder.findRequiredView(source, 2131558571, "field 'llytHideOperation'");
    target.llytHideOperation = finder.castView(view, 2131558571, "field 'llytHideOperation'");
    view = finder.findRequiredView(source, 2131558572, "field 'tbTestEnv'");
    target.tbTestEnv = finder.castView(view, 2131558572, "field 'tbTestEnv'");
    view = finder.findRequiredView(source, 2131558573, "field 'etAppID'");
    target.etAppID = finder.castView(view, 2131558573, "field 'etAppID'");
    view = finder.findRequiredView(source, 2131558574, "field 'etAppKey'");
    target.etAppKey = finder.castView(view, 2131558574, "field 'etAppKey'");
    view = finder.findRequiredView(source, 2131558556, "field 'scrollView'");
    target.scrollView = finder.castView(view, 2131558556, "field 'scrollView'");
    view = finder.findRequiredView(source, 2131558570, "method 'showAdvaaced'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.showAdvaaced();
        }
      });
    view = finder.findRequiredView(source, 2131558568, "method 'uploadLog'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.uploadLog();
        }
      });
    view = finder.findRequiredView(source, 2131558569, "method 'openAboutPage'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.openAboutPage();
        }
      });
  }

  @Override public void unbind(T target) {
    target.tvsdkVersion = null;
    target.etUserAccount = null;
    target.etUserName = null;
    target.spinnerResolutions = null;
    target.tvResolution = null;
    target.seekbarResolution = null;
    target.tvFps = null;
    target.seekBarFps = null;
    target.tvBitrate = null;
    target.seekBarBitrate = null;
    target.tvDemoVersion = null;
    target.llytHideOperation = null;
    target.tbTestEnv = null;
    target.etAppID = null;
    target.etAppKey = null;
    target.scrollView = null;
  }
}
