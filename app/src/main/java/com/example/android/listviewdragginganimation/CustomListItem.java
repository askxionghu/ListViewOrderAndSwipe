package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

/**
 * Created by Johann on 6/27/15.
 */
public class CustomListItem extends RelativeLayout
{
  private JBHorizontalSwipe jbHorizontalSwipe;

  public CustomListItem(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public CustomListItem(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev)
  {
    if (this.jbHorizontalSwipe != null)
      this.jbHorizontalSwipe.onScrollerDispatchTouchEventListener(this, ev);

    return super.dispatchTouchEvent(ev);
  }

  public void setJBHeaderRef(JBHorizontalSwipe jbHorizontalSwipe)
  {
    this.jbHorizontalSwipe = jbHorizontalSwipe;
  }
}
