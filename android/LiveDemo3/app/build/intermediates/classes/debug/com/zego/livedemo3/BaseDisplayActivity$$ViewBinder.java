// Generated code from Butter Knife. Do not modify!
package com.zego.livedemo3;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class BaseDisplayActivity$$ViewBinder<T extends com.zego.livedemo3.BaseDisplayActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558523, "method 'openLogList'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.openLogList();
        }
      });
    view = finder.findRequiredView(source, 2131558524, "method 'publishSettings'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.publishSettings();
        }
      });
    view = finder.findRequiredView(source, 2131558526, "method 'close'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.close();
        }
      });
  }

  @Override public void unbind(T target) {
  }
}
