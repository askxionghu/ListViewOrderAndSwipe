package com.johannblake.widgets.jbhorizonalswipelib;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

/**
 * Created by Johann on 6/27/15.
 */
public class JBHorizontalSwipe
{
  private final String LOG_TAG = "JBHorizontalSwipe";
  private final String TAG_TOP_VIEW = "TopView";

  private boolean fingerUp;
  private float scrollDelta;
  private float motionEventPrevX;
  private boolean scrollingRight;
  private View vScroller;
  private IJBHorizontalSwipe ijbHorizontalSwipe;
  private ObjectAnimator animatorView;
  private boolean animating;
  private boolean cancelAnimation;

  public final static int ANIMATE_USING_DEFAULT = 0;
  public final static int ANIMATE_LEFT = 1;
  public final static int ANIMATE_RIGHT = 2;

  public JBHorizontalSwipe(IJBHorizontalSwipe ijbHorizontalSwipe)
  {
    this.ijbHorizontalSwipe = ijbHorizontalSwipe;
  }


  /**
   * Receives motion events from the scroller. Scrollers must implement the dispatchTouchEvent method and call this
   * method from there.
   *
   * @param v     Indicates the scroller that sent the motion event.
   * @param event The motion event that was sent.
   */

  public void onScrollerDispatchTouchEventListener(View v, MotionEvent event)
  {
    try
    {
      if (event.getAction() == MotionEvent.ACTION_DOWN)
        this.vScroller = v;
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onScrollerDispatchTouchEventListener: " + ex.toString());
    }
  }

  public void onRootDispatchTouchEventListener(MotionEvent event)
  {
    try
    {
      if (event.getAction() == MotionEvent.ACTION_UP)
      {
        // Reposition the top view if necessary.
        this.fingerUp = true;

        View vTopView = this.vScroller.findViewWithTag(TAG_TOP_VIEW);

        if ((vTopView.getX() > 0) || (scrollDelta != 0))
          processViewPosition(vTopView);

        this.vScroller = null;
      }
      else if (event.getAction() == MotionEvent.ACTION_DOWN)
      {
        this.fingerUp = false;
        this.motionEventPrevX = event.getX();
      }

      if ((event.getAction() == MotionEvent.ACTION_MOVE) && (this.vScroller != null))
      {
        // Adjust the position of the view.
        this.scrollingRight = event.getX() > this.motionEventPrevX;
        this.scrollDelta = Math.abs(event.getX() - this.motionEventPrevX);
        this.motionEventPrevX = event.getX();

        if (this.scrollDelta < 10)
          return;

        if (this.scrollDelta != 0)
          repositionTopView();
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "onRootDispatchTouchEventListener: " + ex.toString());
    }
  }


  private void repositionTopView()
  {
    try
    {
      Log.i(LOG_TAG, "scrollingRight: " + this.scrollingRight + " scrollDelta: " + this.scrollDelta + " fingerUp: " + fingerUp);

      View rlTopView = this.vScroller.findViewWithTag(TAG_TOP_VIEW);

      // Hide the top view if the user was flinging it to the right.
      if (this.scrollingRight && (this.scrollDelta > 50) && fingerUp)
      {
        animateViewRight(rlTopView);
        return;
      }

      // Show the top view if the user was flinging it to the left.
      if (!this.scrollingRight && (this.scrollDelta > 50) && fingerUp)
      {
        animateViewLeft(rlTopView);
        return;
      }

      if (animating || fingerUp)
        return;

      if (this.scrollingRight)
      {
        float x = rlTopView.getX() + this.scrollDelta;

        if (x > this.vScroller.getWidth())
          x = this.vScroller.getWidth();

        rlTopView.setX(x);
      }
      else
      {
        float x = rlTopView.getX() - this.scrollDelta;

        if (x < 0)
          x = 0;

        rlTopView.setX(x);
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "repositionTopView: " + ex.toString());
    }
  }


  /**
   * This is where the decision is made to either display or hide the view.
   */
  private void processViewPosition(View vTopView)
  {
    try
    {
      if (this.fingerUp)
      {
        if (this.vScroller == null)
          return;

        // Animate the header up or down if the client has requested it.
        if (this.ijbHorizontalSwipe != null)
        {
          int animateDirection = this.ijbHorizontalSwipe.onHeaderBeforeAnimation(this.scrollingRight, this.scrollDelta);

          if (animateDirection == ANIMATE_LEFT)
          {
            animateViewLeft(vTopView);
            return;
          }
          else if (animateDirection == ANIMATE_RIGHT)
          {
            animateViewRight(vTopView);
            return;
          }
        }

        if (this.scrollingRight && (this.scrollDelta > 50))
        {
          animateViewRight(vTopView);
          return;
        }

        if (!this.scrollingRight && (this.scrollDelta > 50))
        {
          animateViewLeft(vTopView);
          return;
        }

        // The view has moved less than half of its width
        if (vTopView.getX() < this.vScroller.getWidth() / 2)
        {
          animateViewLeft(vTopView);
          return;
        }
        else
        {
          animateViewRight(vTopView);
          return;
        }
      }
      else
      {
        if (this.animatorView != null)
          this.animatorView.cancel();
      }
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "processViewPosition: " + ex.toString());
    }
  }


  /**
   * Animates the view to the left to be hidden.
   */
  public void animateViewLeft(View vTopVIew)
  {
    try
    {
      if (this.animatorView != null)
        this.animatorView.cancel();

      if (vTopVIew.getX() == 0)
        return;

      animating = true;
      PropertyValuesHolder pvhXBar = PropertyValuesHolder.ofFloat("x", vTopVIew.getX(), 0);
      this.animatorView = ObjectAnimator.ofPropertyValuesHolder(vTopVIew, pvhXBar);
      this.animatorView.setInterpolator(new LinearInterpolator());
      this.animatorView.setDuration(200);
      this.animatorView.addListener(animListener);
      this.cancelAnimation = false;
      this.animatorView.start();

      if (this.ijbHorizontalSwipe != null)
        this.ijbHorizontalSwipe.onHeaderAfterAnimation(false, this.scrollDelta);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "animateViewLeft: " + ex.toString());
    }
  }


  /**
   * Animates the view to the right to be visible.
   */
  public void animateViewRight(View vTopVIew)
  {
    try
    {
      if (this.animatorView != null)
        this.animatorView.cancel();

      if (vTopVIew.getX() == this.vScroller.getWidth() - 1)
        return;

      animating = true;
      PropertyValuesHolder pvhXBar = PropertyValuesHolder.ofFloat("x", vTopVIew.getX(), vTopVIew.getWidth());
      this.animatorView = ObjectAnimator.ofPropertyValuesHolder(vTopVIew, pvhXBar);
      this.animatorView.setInterpolator(new LinearInterpolator());
      this.animatorView.setDuration(200);
      this.animatorView.addListener(animListener);
      this.cancelAnimation = false;
      this.animatorView.start();

      if (this.ijbHorizontalSwipe != null)
        this.ijbHorizontalSwipe.onHeaderAfterAnimation(true, this.scrollDelta);
    }
    catch (Exception ex)
    {
      Log.e(LOG_TAG, "animateViewRight: " + ex.toString());
    }
  }


  /**
   * The animation listener. Needed to know when the animation should be canceled.
   */
  private Animator.AnimatorListener animListener = new Animator.AnimatorListener()
  {
    @Override
    public void onAnimationStart(Animator animation)
    {
      if (cancelAnimation)
        animatorView.cancel();
    }

    @Override
    public void onAnimationEnd(Animator animation)
    {
      animating = false;
      cancelAnimation = false;
    }

    @Override
    public void onAnimationCancel(Animator animation)
    {
      cancelAnimation = false;
    }

    @Override
    public void onAnimationRepeat(Animator animation)
    {
    }
  };


  public interface IJBHorizontalSwipe
  {
    void onReposition(float x, boolean scrollingRight, float scrollDelta);

    int onHeaderBeforeAnimation(boolean scrollingRight, float scrollDelta);

    void onHeaderAfterAnimation(boolean animatedRight, float scrollDelta);
  }
}
