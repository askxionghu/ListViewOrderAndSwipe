package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * A custom button control that is used on the bottom view of widget that uses the JBHorizontalSwipe library.
 */
public class ButtonBottomView extends Button
{
  private final String TAG_LOG = "ButtonBottomView";

  private boolean ignoreMotionEvents = true;

  public ButtonBottomView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public ButtonBottomView(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Enables or disables the button from responding to touch events.
   * @param ignore If set to true, touch events will be ignored by the button.
   */
  public void setIgnoreMotionEvents(boolean ignore)
  {
    this.ignoreMotionEvents = ignore;
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event)
  {
    try
    {
      if (this.ignoreMotionEvents)
      {
        super.onTouchEvent(event);
        return false;
      }

      return super.onTouchEvent(event);
    }
    catch(Exception ex)
    {
      Log.e(TAG_LOG, "onTouchEvent: " + ex.getMessage());
      return false;
    }
  }
}
