// Generated code from Butter Knife. Do not modify!
package com.zego.livedemo3;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MainActivity$$ViewBinder<T extends com.zego.livedemo3.MainActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558530, "field 'toolBar'");
    target.toolBar = finder.castView(view, 2131558530, "field 'toolBar'");
    view = finder.findRequiredView(source, 2131558531, "field 'drawerLayout'");
    target.drawerLayout = finder.castView(view, 2131558531, "field 'drawerLayout'");
    view = finder.findRequiredView(source, 2131558532, "field 'navigationBar'");
    target.navigationBar = finder.castView(view, 2131558532, "field 'navigationBar'");
    view = finder.findRequiredView(source, 2131558533, "field 'viewPager'");
    target.viewPager = finder.castView(view, 2131558533, "field 'viewPager'");
  }

  @Override public void unbind(T target) {
    target.toolBar = null;
    target.drawerLayout = null;
    target.navigationBar = null;
    target.viewPager = null;
  }
}
