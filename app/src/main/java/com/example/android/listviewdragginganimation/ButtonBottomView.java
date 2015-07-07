package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by Johann on 7/7/15.
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

  public void setIgnoreMotionEvents(boolean ignore)
  {
    this.ignoreMotionEvents = ignore;
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event)
  {
    try
    {
//      int action = event.getAction() & MotionEvent.ACTION_MASK;
//
//      switch (action)
//      {
//        case MotionEvent.ACTION_DOWN:
//          break;
//      }

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
