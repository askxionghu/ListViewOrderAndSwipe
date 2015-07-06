package com.example.android.listviewdragginganimation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends ActionBarActivity
{
  private final String TAG = "MainActivity";
  private final String TAG_BOTTOM_VIEW = "BottomView";

  private ArrayList<Person> persons = new ArrayList<>();
  private PersonAdapter adapterPerson;
  private PersonListViewOrder lvPersons;
  private JBHorizontalSwipe jbHorizontalSwipe;
  private Context context;
  private boolean refreshList;
  private Person prevDeletePerson;
  private ViewGroup vgSwiped;
  private HashMap<Long, Integer> listItemTopPosMap = new HashMap<>();

  private static final int MOVE_DURATION = 5000;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    try
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      this.context = this;

      this.jbHorizontalSwipe = new JBHorizontalSwipe(ijbHorizontalSwipe);

      this.persons.add(new Person(getNewId(), "Ben", BitmapFactory.decodeResource(getResources(), R.drawable.ic_ben)));
      this.persons.add(new Person(getNewId(), "Brad", BitmapFactory.decodeResource(getResources(), R.drawable.ic_brad)));
      this.persons.add(new Person(getNewId(), "Bradley", BitmapFactory.decodeResource(getResources(), R.drawable.ic_bradley)));
      this.persons.add(new Person(getNewId(), "Bruce", BitmapFactory.decodeResource(getResources(), R.drawable.ic_bruce)));
      this.persons.add(new Person(getNewId(), "Chris", BitmapFactory.decodeResource(getResources(), R.drawable.ic_chris)));
      this.persons.add(new Person(getNewId(), "Christian", BitmapFactory.decodeResource(getResources(), R.drawable.ic_christian)));
      this.persons.add(new Person(getNewId(), "Denzel", BitmapFactory.decodeResource(getResources(), R.drawable.ic_denzel)));
      this.persons.add(new Person(getNewId(), "George", BitmapFactory.decodeResource(getResources(), R.drawable.ic_george)));
      this.persons.add(new Person(getNewId(), "Hugh", BitmapFactory.decodeResource(getResources(), R.drawable.ic_hugh)));
      this.persons.add(new Person(getNewId(), "Johnny", BitmapFactory.decodeResource(getResources(), R.drawable.ic_johnny)));
      this.persons.add(new Person(getNewId(), "Leo", BitmapFactory.decodeResource(getResources(), R.drawable.ic_leo)));
      this.persons.add(new Person(getNewId(), "Liam", BitmapFactory.decodeResource(getResources(), R.drawable.ic_liam)));
      this.persons.add(new Person(getNewId(), "Matt", BitmapFactory.decodeResource(getResources(), R.drawable.ic_matt)));
      this.persons.add(new Person(getNewId(), "Matthew", BitmapFactory.decodeResource(getResources(), R.drawable.ic_matthew)));
      this.persons.add(new Person(getNewId(), "Morgan", BitmapFactory.decodeResource(getResources(), R.drawable.ic_morgan)));
      this.persons.add(new Person(getNewId(), "Russell", BitmapFactory.decodeResource(getResources(), R.drawable.ic_russell)));
      this.persons.add(new Person(getNewId(), "Tom", BitmapFactory.decodeResource(getResources(), R.drawable.ic_tom)));
      this.persons.add(new Person(getNewId(), "Will", BitmapFactory.decodeResource(getResources(), R.drawable.ic_will)));

      this.lvPersons = (PersonListViewOrder) findViewById(R.id.lvPersons);
      this.lvPersons.setPersonList(this.persons);
      this.adapterPerson = new PersonAdapter(this, R.layout.person_item, persons, this.jbHorizontalSwipe);
      this.adapterPerson.setListView(this.lvPersons);
      this.lvPersons.setAdapter(this.adapterPerson);

      this.lvPersons.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
          try
          {
            Person person = (Person) view.getTag();
            Toast.makeText(context, person.name, Toast.LENGTH_SHORT).show();
          }
          catch (Exception ex)
          {
            Log.e(TAG, "onItemClick: " + ex.getMessage());
          }
        }
      });

/*      this.lvPersons.setOnScrollListener(new AbsListView.OnScrollListener()
      {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
          try
          {
            if (refreshList)
            {
              adapterPerson.notifyDataSetChanged();
              refreshList = false;
            }
          }
          catch (Exception ex)
          {
            Log.e(TAG, "onScroll: " + ex.getMessage());
          }
        }
      });*/

    }
    catch (Exception ex)
    {
      Log.e(TAG, "onCreate: " + ex.getMessage());
    }
  }

  private JBHorizontalSwipe.IJBHorizontalSwipe ijbHorizontalSwipe = new JBHorizontalSwipe.IJBHorizontalSwipe()
  {
    @Override
    public void onReposition(float x, boolean scrollingRight, float scrollDelta)
    {

    }

    @Override
    public void onSwipeAnimationCompleted(View v)
    {
      try
      {
        View vCurrent = (ViewGroup) v.getParent();
        animateRemoval(lvPersons, vCurrent);
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onSwipeAnimationCompleted: " + ex.getMessage());
      }
    }

    @Override
    public void onTopViewVisibilityChange(View vTop, boolean visible)
    {
      try
      {
        vgSwiped = (ViewGroup) vTop.getParent();
        final Person person = (Person) vgSwiped.getTag();
        person.deleted = !visible;
//
//        View vBottom = vgSwiped.findViewWithTag(TAG_BOTTOM_VIEW);
//        PropertyValuesHolder pvhAlphaCurrent;
//
//        if (person.deleted)
//          pvhAlphaCurrent = PropertyValuesHolder.ofFloat("alpha", 0, 1);
//        else
//          pvhAlphaCurrent = PropertyValuesHolder.ofFloat("alpha", 1, 0);
//
//        ObjectAnimator animatorView = ObjectAnimator.ofPropertyValuesHolder(vBottom, pvhAlphaCurrent);
//        animatorView.setInterpolator(new LinearInterpolator());
//        animatorView.setDuration(300);
//        animatorView.start();


//        if (person.deleted)
//        {
//          if (prevDeletePerson != null)
//
//
//            prevDeletePerson.remove = true;
//
//          prevDeletePerson = person;
//        }
//        else
//        {
//
//        }

        //adapterPerson.notifyDataSetChanged();

        refreshList = true;
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onTopViewVisibilityChange: " + ex.getMessage());
      }
    }
  };


  /**
   * This method animates all other views in the ListView container (not including ignoreView)
   * into their final positions. It is called after ignoreView has been removed from the
   * adapter, but before layout has been run. The approach here is to figure out where
   * everything is now, then allow layout to run, then figure out where everything is after
   * layout, and then to run animations between all of those start/end positions.
   */
  private void animateRemoval(final ListView listview, View viewToRemove)
  {
    int firstVisiblePosition = listview.getFirstVisiblePosition();

    for (int i = 0; i < listview.getChildCount(); ++i)
    {
      View child = listview.getChildAt(i);

      if (child != viewToRemove)
      {
        int position = firstVisiblePosition + i;
        long itemId = this.adapterPerson.getItemId(position);
        listItemTopPosMap.put(itemId, child.getTop());
      }
    }
    // Delete the item from the adapter
    int position = listview.getPositionForView(viewToRemove);
    this.adapterPerson.remove(this.adapterPerson.getItem(position));

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
          long itemId = adapterPerson.getItemId(position);
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
                    //mBackgroundContainer.hideBackground();
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
                  //mBackgroundContainer.hideBackground();
                  listview.setEnabled(true);
                }
              });

              firstAnimation = false;
            }
          }
        }

        listItemTopPosMap.clear();
        return true;
      }
    });
  }


  public boolean dispatchTouchEvent(MotionEvent ev)
  {
    if (this.jbHorizontalSwipe != null)
      this.jbHorizontalSwipe.onRootDispatchTouchEventListener(ev);

    return super.dispatchTouchEvent(ev);
  }


  private long getNewId()
  {
    Random r = new Random();
    long id = r.nextLong();
    return id;
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings)
    {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
