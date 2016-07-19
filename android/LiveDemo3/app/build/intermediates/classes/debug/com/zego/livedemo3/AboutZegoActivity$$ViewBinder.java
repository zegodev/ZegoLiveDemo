// Generated code from Butter Knife. Do not modify!
package com.zego.livedemo3;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class AboutZegoActivity$$ViewBinder<T extends com.zego.livedemo3.AboutZegoActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558514, "field 'webView'");
    target.webView = finder.castView(view, 2131558514, "field 'webView'");
    view = finder.findRequiredView(source, 2131558513, "method 'back'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.back();
        }
      });
  }

  @Override public void unbind(T target) {
    target.webView = null;
  }
}
