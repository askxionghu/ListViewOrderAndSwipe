/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Johann Blake
 *
 * https://www.linkedin.com/in/johannblake
 * https://plus.google.com/+JohannBlake
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.johannblake.widgets.jbhorizonalswipelib;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ListView;

/**
 * This is a library that allows you to create horizontally swiped views in Android where the swiped
 * view is over top another view. Typically, this will be used to swipe listview items but can be used
 * for any kind of UI widget where you need to reveal a lower view.
 * <p/>
 * Google's Gmail app allows you to swipe an email's summary in a listview causing the item to be delete.
 * When swiped, a view is exposed below the swiped view that shows the label "Deleted" on the far left
 * and a button on the far right labeled "Undo". You can undo the delete either by swiping the top view
 * back into place or tapping on the Undo button. This module provides the functionality to accomplish
 * the same feat.
 */
public class JBHorizontalSwipe {
    private final String LOG_TAG = "JBHorizontalSwipe";
    private final String TAG_TOP_VIEW = "TopView";

    private boolean mFingerUp;
    private float mScrollDeltaX;
    private float mScrollDeltaY;
    private float mMotionEventPrevX;
    private float mMotionEventPrevY;
    private boolean mScrollingRight;
    private View mScrollerView;
    private IJBHorizontalSwipe mIJBHorizontalSwipe;
    private ObjectAnimator mAnimatorView;
    private boolean mAnimating;
    private boolean mCancelAnimation;
    private float mInitialLeft;
    private boolean mTopViewChanged;
    private boolean mTopViewVisible;

    public final static int ANIMATE_POSITION_LEFT_VISIBLE = 0;
    public final static int ANIMATE_POSITION_LEFT_INVISIBLE = 1;
    public final static int ANIMATE_POSITION_RIGHT_VISIBLE = 2;
    public final static int ANIMATE_POSITION_RIGHT_INVISIBLE = 3;

    public JBHorizontalSwipe(IJBHorizontalSwipe ijbHorizontalSwipe) {
        mIJBHorizontalSwipe = ijbHorizontalSwipe;
    }


    /**
     * Receives motion events from the scroller. Scrollers must implement the dispatchTouchEvent method and call this
     * method from there.
     *
     * @param v     Indicates the scroller that sent the motion event.
     * @param event The motion event that was sent.
     */

    public void onScrollerDispatchTouchEventListener(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mScrollerView = v;
            View vTop = mScrollerView.findViewWithTag(TAG_TOP_VIEW);
            mInitialLeft = vTop.getX();
        }
    }

    public void onRootDispatchTouchEventListener(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Reposition the top view if necessary.
            mFingerUp = true;

            if (mScrollerView != null) {
                View vTop = mScrollerView.findViewWithTag(TAG_TOP_VIEW);

                if ((vTop != null) && (vTop.getX() != 0))
                    processViewPosition(vTop);

                mScrollerView = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mFingerUp = false;
            mMotionEventPrevX = event.getX();
            mMotionEventPrevY = event.getY();
        }

        if ((event.getAction() == MotionEvent.ACTION_MOVE) && (mScrollerView != null)) {
            // Adjust the position of the view.
            mScrollingRight = event.getX() > mMotionEventPrevX;
            mScrollDeltaX = Math.abs(event.getX() - mMotionEventPrevX);
            mScrollDeltaY = Math.abs(event.getY() - mMotionEventPrevY);
            mMotionEventPrevX = event.getX();
            mMotionEventPrevY = event.getY();

            View vTop = mScrollerView.findViewWithTag(TAG_TOP_VIEW);

            if (((mScrollDeltaX > 10) && (mScrollDeltaY < 10)) || ((vTop != null) && (vTop.getX() != 0))) {
                IJBHorizontalSwipeTouch ijbHorizontalSwipeTouch = (IJBHorizontalSwipeTouch) mScrollerView.getParent();
                ijbHorizontalSwipeTouch.setDisableScrolling(true);

                ListView listview = (ListView) vTop.getParent().getParent();
                listview.requestDisallowInterceptTouchEvent(true);

                repositionTopView();
            }
        }
    }


    /**
     * Repositions the top view when the user scrolls it horizontally.
     */
    private void repositionTopView() {
        View vTop = mScrollerView.findViewWithTag(TAG_TOP_VIEW);

        if (mAnimating || mFingerUp)
            return;

        if (mScrollingRight) {
            float x = vTop.getX() + mScrollDeltaX;

            if (vTop.getX() >= (vTop.getWidth() - 1))
                vTop.setX(-(vTop.getWidth() - 1));
            else
                vTop.setX(x);
        } else {
            float x = vTop.getX() - mScrollDeltaX;

            if (vTop.getX() <= -(vTop.getWidth() - 1))
                vTop.setX(vTop.getWidth());
            else
                vTop.setX(x);
        }

        // Change the alpha of the top view as it is being scrolled making it dimmer as it moves off the screen.
        float alpha = (vTop.getWidth() - Math.abs(vTop.getX() - mScrollDeltaX)) / vTop.getWidth();
        vTop.setAlpha(alpha);
    }


    /**
     * This is where the decision is made to either display or hide the view.
     */
    private void processViewPosition(View vTop) {
        if (mFingerUp) {
            if (mScrollerView == null)
                return;

            if (mScrollingRight && (mScrollDeltaX > 50) && (vTop.getX() > 0)) {
                animateView(vTop, ANIMATE_POSITION_RIGHT_INVISIBLE);
                return;
            }

            if (mScrollingRight && (mScrollDeltaX > 50) && (vTop.getX() < 0)) {
                animateView(vTop, ANIMATE_POSITION_RIGHT_VISIBLE);
                return;
            }

            if (!mScrollingRight && (mScrollDeltaX > 50) && (vTop.getX() > 0)) {
                animateView(vTop, ANIMATE_POSITION_LEFT_VISIBLE);
                return;
            }

            if (!mScrollingRight && (mScrollDeltaX > 50) && (vTop.getX() < 0)) {
                animateView(vTop, ANIMATE_POSITION_LEFT_INVISIBLE);
                return;
            }

            // View was moved to the right of its origin.
            if ((mInitialLeft == 0) && (vTop.getX() > 0) && (vTop.getX() < mScrollerView.getWidth() / 3)) {
                animateView(vTop, ANIMATE_POSITION_LEFT_VISIBLE);
                return;
            } else if ((mInitialLeft == 0) && (vTop.getX() >= mScrollerView.getWidth() / 3)) {
                animateView(vTop, ANIMATE_POSITION_RIGHT_INVISIBLE);
                return;
            } else if ((mInitialLeft == 0) && (vTop.getX() > -mScrollerView.getWidth() / 3)) {
                animateView(vTop, ANIMATE_POSITION_RIGHT_VISIBLE);
                return;
            } else if (mInitialLeft == 0) {
                animateView(vTop, ANIMATE_POSITION_LEFT_INVISIBLE);
                return;
            } else if ((mInitialLeft > 0) && (vTop.getX() >= mScrollerView.getWidth() * 2 / 3)) {
                animateView(vTop, ANIMATE_POSITION_RIGHT_INVISIBLE);
                return;
            } else if (mInitialLeft > 0) {
                animateView(vTop, ANIMATE_POSITION_LEFT_VISIBLE);
                return;
            } else if ((mInitialLeft < 0) && (vTop.getX() > -mScrollerView.getWidth() * 2 / 3)) {
                animateView(vTop, ANIMATE_POSITION_RIGHT_VISIBLE);
                return;
            } else {
                animateView(vTop, ANIMATE_POSITION_LEFT_INVISIBLE);
                return;
            }
        } else {
            if (mAnimatorView != null)
                mAnimatorView.cancel();
        }
    }


    /**
     * Makes the top view visible by animating it onto the screen from either
     * the left or right side of the container depending on where its current
     * left position is located.
     *
     * @param vTop The top view to make visible.
     */
    public void showTopView(View vTop) {
        if (vTop.getX() < 0)
            animateView(vTop, ANIMATE_POSITION_RIGHT_VISIBLE);
        else
            animateView(vTop, ANIMATE_POSITION_LEFT_VISIBLE);

        mTopViewVisible = true;
        mTopViewChanged = true;
    }

    /**
     * Shows or hides the top view by animating on to or off of the screen.
     *
     * @param vTop     The top view.
     * @param position Use ANIMATE_POSITION_LEFT_INVISIBLE to slide it off the screen in the left
     *                 direction. Use ANIMATE_POSITION_RIGHT_INVISIBLE to slide it off the screen
     *                 in the right direction. Set it to ANIMATE_POSITION_LEFT_VISIBLE or
     *                 ANIMATE_POSITION_RIGHT_VISIBLE to slide it on to the screen.
     */
    public void animateView(View vTop, int position) {
        if (mAnimatorView != null)
            mAnimatorView.cancel();

        float left;

        switch (position) {
            case ANIMATE_POSITION_LEFT_INVISIBLE:
                left = -vTop.getWidth();
                break;

            case ANIMATE_POSITION_RIGHT_INVISIBLE:
                left = vTop.getWidth();
                break;

            default:
                left = 0;
                break;
        }

        mAnimating = true;
        PropertyValuesHolder pvhXBar = PropertyValuesHolder.ofFloat("x", vTop.getX(), left);
        mAnimatorView = ObjectAnimator.ofPropertyValuesHolder(vTop, pvhXBar);
        mAnimatorView.setInterpolator(new LinearInterpolator());
        mAnimatorView.setDuration(200);
        mAnimatorView.addListener(animListener);
        mCancelAnimation = false;
        mAnimatorView.start();

        if ((mIJBHorizontalSwipe != null) && (left != mInitialLeft)) {
            mTopViewChanged = true;
            mTopViewVisible = (position == ANIMATE_POSITION_LEFT_VISIBLE) || (position == ANIMATE_POSITION_RIGHT_VISIBLE);
        } else
            mTopViewChanged = false;

    }


    /**
     * The animation listener. Needed to know when the animation should be canceled.
     */
    private Animator.AnimatorListener animListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (mCancelAnimation)
                mAnimatorView.cancel();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimating = false;
            mCancelAnimation = false;
            View v = (View) mAnimatorView.getTarget();
            v.setAlpha(1);

            if ((mIJBHorizontalSwipe != null) && mTopViewChanged)
                mIJBHorizontalSwipe.onTopViewVisibilityChange(v, mTopViewVisible);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mCancelAnimation = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };


    public interface IJBHorizontalSwipe {
        void onReposition(float x, boolean scrollingRight, float scrollDelta);

        void onTopViewVisibilityChange(View vTop, boolean visible);
    }

    public interface IJBHorizontalSwipeTouch {
        void setDisableScrolling(boolean disable);
    }

    public interface IJBHorizontalSwipeAdapter {
        View getSelectedView();
    }
}
