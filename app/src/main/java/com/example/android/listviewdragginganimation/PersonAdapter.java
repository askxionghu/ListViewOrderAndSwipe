/**
 * This code is partially based upon code written by Chet Haase at Google.
 * Chet's video can be viewed at:
 * <p/>
 * https://www.youtube.com/watch?v=YCHNAi9kJI4
 * <p/>
 * More specifically, the animateRemoval method was incorporated into this module
 * with slight modifications that include a bugfix that was in Chet's original code.
 * See the animateRemoval method for details on the bug.
 * <p/>
 * The primary difference between this code and Chet's is that the swiping of a list
 * item doesn't automatically delete the item. Instead, each list item consists
 * of both a top and bottom view. When the top view is swiped out of view, the bottom
 * view is revealed which contains a label on the far left that reads "Deleted" and
 * a button on the far right that is labeled "Undo". Pressing the Undo button undeletes
 * the item and swipes the top view back into place. This is the same functionality
 * as seen in the Gmail app when deleting email items.
 */

package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

import java.util.HashMap;
import java.util.List;

/**
 * The adapter used by a listview (PersonListViewOrder).
 */
public class PersonAdapter extends ArrayAdapter<Person> implements JBHorizontalSwipe.IJBHorizontalSwipeAdapter {
    private final String TAG_LOG = "PersonAdapter";
    private final String TAG_TOP_VIEW = "TopView";
    private final String TAG_BOTTOM_VIEW = "BottomView";

    private static final int MOVE_DURATION = 150;

    private final int INVALID_ID = -1;

    private List<Person> mItems;
    private Context mContext;
    private PersonListViewOrder mListview;
    private JBHorizontalSwipe mJBHorizontalSwipe;
    private View mSelectedView;
    private ListViewItemBackground mListViewItemBackground;
    private MainActivity.IListItemControls mIListItemControls;

    private HashMap<String, Integer> mIDHashMap = new HashMap<>();
    private HashMap<Long, Integer> mListItemTopPosMap = new HashMap<>();

    public PersonAdapter(Context context, int textViewResourceId, List<Person> items, JBHorizontalSwipe jbHorizontalSwipe, PersonListViewOrder listview, MainActivity.IListItemControls iListItemControls) {
        super(context, textViewResourceId, items);

        mItems = items;
        mContext = context;
        mJBHorizontalSwipe = jbHorizontalSwipe;
        mIListItemControls = iListItemControls;
        mListview = listview;
        mListViewItemBackground = (ListViewItemBackground) listview.getParent();

        // Store an id for each list item. This ensures that items can be
        // retrieved even when their visual location within the list changes.
        for (int i = 0; i < items.size(); ++i) {
            mIDHashMap.put(items.get(i).toString(), i);
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Person person = mItems.get(position);

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.person_item, null);
        }

        // Each item needs to store a reference to the JBHorizontalSwipe object to manage being swiped by the user.
        CustomListItem customListItem = (CustomListItem) v;
        customListItem.setJBHeaderRef(mJBHorizontalSwipe);

        v.setTag(person);
        View vTop = v.findViewWithTag(TAG_TOP_VIEW);
        View vBottom = v.findViewWithTag(TAG_BOTTOM_VIEW);

        ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);

        ivIcon.setImageBitmap(person.bitmap);

        TextView tvName = (TextView) v.findViewById(R.id.tvName);
        tvName.setText(person.name);

        vTop.setOnTouchListener(onTouchListenerTopView);

        // Unlike typical listviews that contain a single view for each item,
        // this listview contains items where each item can have a top and a bottom
        // view, so the state of the top/bottom needs to be set as each view
        // gets recycled.

        if (person.deleted) {
            vTop.setX(vTop.getWidth());
            vBottom.setAlpha(1);
        } else {
            vTop.setX(0);
            vBottom.setAlpha(0);
        }

        // Setup a handler to handle the user tapping on the Undo button. The Undo
        // button might get selected while swiping the top view, so set its Pressed
        // state to false to prevent it from being selected when it gets displayed.
        ButtonBottomView btnUndo = (ButtonBottomView) v.findViewById(R.id.btnUndo);
        btnUndo.setPressed(false);
        onItemSwiped(person, btnUndo);

        return v;
    }


    /**
     * Enables or disables the handling of Undo button when the top or bottom view is visible.
     *
     * @param person  The Person associated with the swiped item.
     * @param btnUndo The view representing the Undo button.
     */
    public void onItemSwiped(Person person, ButtonBottomView btnUndo) {
        if (person.deleted) {
            btnUndo.setIgnoreMotionEvents(false);
            btnUndo.setOnClickListener(onUndoClickListener);
        } else {
            // When the item is not deleted, it is important to prevent motion events from reaching
            // the Undo button and causing an Undo event to occur.
            btnUndo.setIgnoreMotionEvents(true);
            btnUndo.setOnClickListener(null);
        }
    }


    /**
     * Event handler to handle a user tapping on the Undo button.
     */
    private View.OnClickListener onUndoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIListItemControls.onUndoClicked(v);

            View vRoot = getItemRootViewFromBottomControl(v);

            Person person = (Person) vRoot.getTag();
            person.deleted = false;

            View vTop = vRoot.findViewWithTag(TAG_TOP_VIEW);
            mJBHorizontalSwipe.showTopView(vTop);
        }
    };


    /**
     * Returns the root view given a view (control) on the bottom view.
     *
     * @param v A control on the bottom view.
     * @return The root view of the item is returned.
     */
    private View getItemRootViewFromBottomControl(View v) {
        return (View) v.getParent().getParent().getParent();
    }


    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    public void animateRemoval(final View viewToRemove) {
        int position = mListview.getPositionForView(viewToRemove);

        // The following line is a bugfix from Google's original code.
        // If the listview doesn't have enough items to fil the height
        // of the listview and the last item is deleted, the bottom view
        // will remain visible as a snapshot. The hideBackground removes
        //

        if (position == mListview.getLastVisiblePosition())
            mListViewItemBackground.hideBackground();
        else
            mListViewItemBackground.showBackground(viewToRemove);

        int firstVisiblePosition = mListview.getFirstVisiblePosition();

        for (int i = 0; i < mListview.getChildCount(); ++i) {
            View child = mListview.getChildAt(i);

            if (child != viewToRemove) {
                int pos = firstVisiblePosition + i;
                long itemId = getItemId(pos);
                mListItemTopPosMap.put(itemId, child.getTop());
            }
        }

        // Delete the item from the adapter. NOTE: calling "remove" forces the listview
        // to update its UI after animateRemoval returns. This means that all the
        // items in the code below that get animated will be working on a set of items
        // that is one less than total amount that was available prior to calling remove.
        remove(getItem(position));

        final ViewTreeObserver observer = mListview.getViewTreeObserver();

        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = mListview.getFirstVisiblePosition();

                for (int i = 0; i < mListview.getChildCount(); ++i) {
                    final View child = mListview.getChildAt(i);
                    child.setPressed(false);
                    int position = firstVisiblePosition + i;
                    long itemId = getItemId(position);
                    Integer startTop = mListItemTopPosMap.get(itemId);
                    int top = child.getTop();

                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);

                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        mListViewItemBackground.hideBackground();
                                        mListview.setEnabled(true);
                                    }
                                });

                                firstAnimation = false;
                            }
                        }
                    } else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + mListview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);

                        if (firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {
                                    mListViewItemBackground.hideBackground();
                                    mListview.setEnabled(true);
                                }
                            });

                            firstAnimation = false;
                        }
                    }
                }

                mListview.setPressed(false);
                mListItemTopPosMap.clear();
                return true;
            }
        });
    }


    /**
     * Handles touch events for a list item.
     */
    private View.OnTouchListener onTouchListenerTopView = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction() & MotionEvent.ACTION_MASK;

            if (action == MotionEvent.ACTION_DOWN)
                mSelectedView = v;

            return false;
        }
    };


    @Override
    public View getSelectedView() {
        return mSelectedView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mItems.size()) {
            return INVALID_ID;
        }

        Person person = mItems.get(position);
        return mIDHashMap.get(person.toString());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}