package com.example.android.listviewdragginganimation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PersonAdapter extends ArrayAdapter<Person> implements JBHorizontalSwipe.IJBHorizontalSwipeAdapter
{
  private final String TAG = "PersonAdapter";
  private final String TAG_TOP_VIEW = "TopView";
  private final String TAG_BOTTOM_VIEW = "BottomView";

  private static final int MOVE_DURATION = 150;

  final int INVALID_ID = -1;

  private List<Person> items;
  private Context context;
  private PersonListViewOrder listview;
  private JBHorizontalSwipe jbHorizontalSwipe;
  private View selectedView;
  private Person personToRemove;
  private ListViewItemBackground listViewItemBackground;
  private MainActivity.IListItemControls iListItemControls;

  private HashMap<String, Integer> idMap = new HashMap<>();
  private HashMap<Long, Integer> listItemTopPosMap = new HashMap<>();

  public PersonAdapter(Context context, int textViewResourceId, List<Person> items, JBHorizontalSwipe jbHorizontalSwipe, PersonListViewOrder listview, MainActivity.IListItemControls iListItemControls)
  {
    super(context, textViewResourceId, items);

    this.items = items;
    this.context = context;
    this.jbHorizontalSwipe = jbHorizontalSwipe;
    this.iListItemControls = iListItemControls;
    this.listview = listview;
    this.listViewItemBackground = (ListViewItemBackground) listview.getParent();

    for (int i = 0; i < items.size(); ++i)
    {
      this.idMap.put(items.get(i).toString(), i);
    }
  }

  public void setListView(PersonListViewOrder listview)
  {
    this.listview = listview;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    try
    {
      View v = convertView;
      Person person = this.items.get(position);

      if (v == null)
      {
        LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.person_item, null);
      }

      CustomListItem customListItem = (CustomListItem) v;
      customListItem.setJBHeaderRef(this.jbHorizontalSwipe);

      v.setTag(person);
      View vTop = v.findViewWithTag(TAG_TOP_VIEW);
      View vBottom = v.findViewWithTag(TAG_BOTTOM_VIEW);

      ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);

      ivIcon.setImageBitmap(person.bitmap);

      TextView tvName = (TextView) v.findViewById(R.id.tvName);
      tvName.setText(person.name);

      vTop.setOnTouchListener(onTouchListenerTopView);

      if (person.deleted)
      {
        vTop.setX(vTop.getWidth());
        vBottom.setAlpha(1);
      }
      else
      {
        vTop.setX(0);
        vBottom.setAlpha(0);
      }

      ButtonBottomView btnUndo = (ButtonBottomView) v.findViewById(R.id.btnUndo);
      onItemSwiped(person, btnUndo);

      return v;
    }
    catch (Exception ex)
    {
      Log.e(TAG, "getView: " + ex.getMessage());
      return convertView;
    }
  }


  public void onItemSwiped(Person person, ButtonBottomView btnUndo)
  {
    try
    {
      if (person.deleted)
      {
        btnUndo.setIgnoreMotionEvents(false);
        btnUndo.setOnClickListener(onUndoClickListener);
      }
      else
      {
        btnUndo.setIgnoreMotionEvents(true);
        btnUndo.setOnClickListener(null);
      }
    }
    catch (Exception ex)
    {
      Log.e(TAG, "onItemSwiped: " + ex.getMessage());
    }
  }


  private View.OnClickListener onUndoClickListener = new View.OnClickListener()
  {
    @Override
    public void onClick(View v)
    {
      try
      {
        iListItemControls.onUndoClicked(v);

        View vRoot = getItemRootViewFromBottomControl(v);

        Person person = (Person) vRoot.getTag();
        person.deleted = false;

        View vTop = vRoot.findViewWithTag(TAG_TOP_VIEW);
        jbHorizontalSwipe.showTopView(vTop);
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onUndoClickListener.onClick: " + ex.getMessage());
      }
    }
  };


  private View getItemRootViewFromBottomControl(View v)
  {
    return (View) v.getParent().getParent().getParent();
  }


  /**
   * This method animates all other views in the ListView container (not including ignoreView)
   * into their final positions. It is called after ignoreView has been removed from the
   * adapter, but before layout has been run. The approach here is to figure out where
   * everything is now, then allow layout to run, then figure out where everything is after
   * layout, and then to run animations between all of those start/end positions.
   */
  public void animateRemoval(final View viewToRemove)
  {
    int position = listview.getPositionForView(viewToRemove);

    if (position == listview.getLastVisiblePosition())
      listViewItemBackground.hideBackground();
    else
      listViewItemBackground.showBackground(viewToRemove);

    int firstVisiblePosition = listview.getFirstVisiblePosition();

    for (int i = 0; i < listview.getChildCount(); ++i)
    {
      View child = listview.getChildAt(i);

      if (child != viewToRemove)
      {
        int pos = firstVisiblePosition + i;
        long itemId = getItemId(pos);
        listItemTopPosMap.put(itemId, child.getTop());
      }
    }
    // Delete the item from the adapter
    remove(getItem(position));

    final ViewTreeObserver observer = listview.getViewTreeObserver();
    observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
    {
      public boolean onPreDraw()
      {
        observer.removeOnPreDrawListener(this);
        boolean firstAnimation = true;
        int firstVisiblePosition = listview.getFirstVisiblePosition();

        for (int i = 0; i < listview.getChildCount(); ++i)
        {
          final View child = listview.getChildAt(i);
          int position = firstVisiblePosition + i;
          long itemId = getItemId(position);
          Integer startTop = listItemTopPosMap.get(itemId);
          int top = child.getTop();

          if (startTop != null)
          {
            if (startTop != top)
            {
              int delta = startTop - top;
              child.setTranslationY(delta);
              child.animate().setDuration(MOVE_DURATION).translationY(0);

              if (firstAnimation)
              {
                child.animate().withEndAction(new Runnable()
                {
                  public void run()
                  {
                    listViewItemBackground.hideBackground();
                    listview.setEnabled(true);
                  }
                });

                firstAnimation = false;
              }
            }
          }
          else
          {
            // Animate new views along with the others. The catch is that they did not
            // exist in the start state, so we must calculate their starting position
            // based on neighboring views.
            int childHeight = child.getHeight() + listview.getDividerHeight();
            startTop = top + (i > 0 ? childHeight : -childHeight);
            int delta = startTop - top;
            child.setTranslationY(delta);
            child.animate().setDuration(MOVE_DURATION).translationY(0);

            if (firstAnimation)
            {
              child.animate().withEndAction(new Runnable()
              {
                public void run()
                {
                  listViewItemBackground.hideBackground();
                  listview.setEnabled(true);
                }
              });

              firstAnimation = false;
            }
          }
        }

        listview.setPressed(false);
        listItemTopPosMap.clear();
        return true;
      }
    });
  }


  /*public void expand(final View v)
  {
    v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    final int targetHeight = v.getMeasuredHeight();

    v.getLayoutParams().height = 0;
    v.setVisibility(View.VISIBLE);

    Animation a = new Animation()
    {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t)
      {
        v.getLayoutParams().height = interpolatedTime == 1 ? ViewGroup.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
        v.requestLayout();
      }

      @Override
      public boolean willChangeBounds()
      {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }

  public void collapse(final View v)
  {
    final int initialHeight = v.getMeasuredHeight();

    Animation a = new Animation()
    {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t)
      {
        if (interpolatedTime == 1)
        {
          v.setVisibility(View.GONE);
          items.remove(personToRemove);
          notifyDataSetChanged();
        }
        else
        {
          v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
          v.requestLayout();
        }
      }

      @Override
      public boolean willChangeBounds()
      {
        return true;
      }
    };

    a.setAnimationListener(animListenerCollapsedRow);

    // 1dp/ms
    a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }*/


  private Animation.AnimationListener animListenerCollapsedRow = new Animation.AnimationListener()
  {
    @Override
    public void onAnimationEnd(Animation animation)
    {
      try
      {
//        items.remove(personToRemove);
//        notifyDataSetChanged();
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onAnimationEnd: " + ex.getMessage());
      }
    }

    @Override
    public void onAnimationRepeat(Animation animation)
    {

    }

    @Override
    public void onAnimationStart(Animation animation)
    {

    }
  };


  private View.OnTouchListener onTouchListenerTopView = new View.OnTouchListener()
  {
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
      try
      {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN)
          selectedView = v;

        return false;
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onCreate: " + ex.getMessage());
      }
      return false;
    }
  };


  @Override
  public View getSelectedView()
  {
    return this.selectedView;
  }

  @Override
  public int getCount()
  {
    return super.getCount();
  }

  @Override
  public long getItemId(int position)
  {
    if (position < 0 || position >= this.items.size())
    {
      return INVALID_ID;
    }

    Person person = this.items.get(position);

    long id = this.idMap.get(person.toString());

    return id;
  }


  public long getItemIdByPerson(Person person)
  {
    return this.idMap.get(person.toString());
  }

  @Override
  public boolean hasStableIds()
  {
    return true;
  }
}