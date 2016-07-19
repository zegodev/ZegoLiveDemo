// Generated code from Butter Knife. Do not modify!
package com.zego.livedemo3.ui;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class PublishFragment$$ViewBinder<T extends com.zego.livedemo3.ui.PublishFragment> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558547, "field 'tbEnableFrontCam'");
    target.tbEnableFrontCam = finder.castView(view, 2131558547, "field 'tbEnableFrontCam'");
    view = finder.findRequiredView(source, 2131558548, "field 'tbEnableTorch'");
    target.tbEnableTorch = finder.castView(view, 2131558548, "field 'tbEnableTorch'");
    view = finder.findRequiredView(source, 2131558550, "field 'spFilters'");
    target.spFilters = finder.castView(view, 2131558550, "field 'spFilters'");
    view = finder.findRequiredView(source, 2131558551, "field 'spBeauties'");
    target.spBeauties = finder.castView(view, 2131558551, "field 'spBeauties'");
    view = finder.findRequiredView(source, 2131558552, "field 'etPublishTitle'");
    target.etPublishTitle = finder.castView(view, 2131558552, "field 'etPublishTitle'");
    view = finder.findRequiredView(source, 2131558544, "field 'svPreview'");
    target.svPreview = finder.castView(view, 2131558544, "field 'svPreview'");
    view = finder.findRequiredView(source, 2131558553, "method 'startPublishing'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.startPublishing();
        }
      });
    view = finder.findRequiredView(source, 2131558516, "method 'hideInputWindow'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.hideInputWindow();
        }
      });
  }

  @Override public void unbind(T target) {
    target.tbEnableFrontCam = null;
    target.tbEnableTorch = null;
    target.spFilters = null;
    target.spBeauties = null;
    target.etPublishTitle = null;
    target.svPreview = null;
  }
}
