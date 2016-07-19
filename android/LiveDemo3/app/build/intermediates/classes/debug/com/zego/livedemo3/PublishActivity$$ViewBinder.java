// Generated code from Butter Knife. Do not modify!
package com.zego.livedemo3;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class PublishActivity$$ViewBinder<T extends com.zego.livedemo3.PublishActivity> extends com.zego.livedemo3.BaseDisplayActivity$$ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    super.bind(finder, target, source);

    View view;
    view = finder.findRequiredView(source, 2131558525, "method 'doPublish'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.doPublish();
        }
      });
  }

  @Override public void unbind(T target) {
    super.unbind(target);

  }
}
