package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

/**
 * A custom FrameLayout that intercept touch events and routes them the a JBHorizontalSwipe controller.
 */
public class CustomListItem extends FrameLayout
{
  private JBHorizontalSwipe mJBHorizontalSwipe;

  public CustomListItem(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public CustomListItem(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
  }


  @Override
  public boolean onTouchEvent(MotionEvent ev)
  {
    if (this.mJBHorizontalSwipe != null)
      this.mJBHorizontalSwipe.onScrollerDispatchTouchEventListener(this, ev);

    return super.onTouchEvent(ev);
  }

  /**
   * Sets a reference to a JBHorizontalSwipe controller.
   * @param jbHorizontalSwipe The instance of a JBHorizontalSwipe controller.
   */
  public void setJBHeaderRef(JBHorizontalSwipe jbHorizontalSwipe)
  {
    this.mJBHorizontalSwipe = jbHorizontalSwipe;
  }
}
