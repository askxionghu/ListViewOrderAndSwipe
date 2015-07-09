/*
TODO: Remove deleted item if listview scrolled or another row starts to be moved.
      Swipe a deleted item back into view from either left or right

 */

package com.example.android.listviewdragginganimation;

import android.animation.Animator;
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
  private ViewGroup vgSwiped;

  private boolean removePrevDeleted;
  private Person prevDeletedPerson;


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
      this.adapterPerson = new PersonAdapter(this, R.layout.person_item, persons, this.jbHorizontalSwipe, this.lvPersons, new IListItemControls()
      {
        @Override
        public void onUndoClicked(View v)
        {
          removePrevDeleted = false;
          prevDeletedPerson = null;
        }
      });

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
    public void onTopViewVisibilityChange(View vTop, boolean visible)
    {
      try
      {
        vgSwiped = (ViewGroup) vTop.getParent();
        final Person person = (Person) vgSwiped.getTag();
        person.deleted = !visible;
        removePrevDeleted = false;

        if ((person == prevDeletedPerson) && !person.deleted)
          prevDeletedPerson = null;

        if ((person.deleted) && (prevDeletedPerson != null) && (person != prevDeletedPerson))
          removePrevDeleted = true;

        View vBottom = vgSwiped.findViewWithTag(TAG_BOTTOM_VIEW);
        PropertyValuesHolder pvhAlphaCurrent;

        ButtonBottomView btnUndo = (ButtonBottomView) vBottom.findViewById(R.id.btnUndo);
        adapterPerson.onItemSwiped(person, btnUndo);

        if (person.deleted)
          pvhAlphaCurrent = PropertyValuesHolder.ofFloat("alpha", 0, 1);
        else
          pvhAlphaCurrent = PropertyValuesHolder.ofFloat("alpha", 1, 0);

        ObjectAnimator animatorView = ObjectAnimator.ofPropertyValuesHolder(vBottom, pvhAlphaCurrent);
        animatorView.setInterpolator(new LinearInterpolator());
        animatorView.setDuration(300);

        animatorView.addListener(new Animator.AnimatorListener()
        {
          @Override
          public void onAnimationStart(Animator animation)
          {

          }

          @Override
          public void onAnimationEnd(Animator animation)
          {
            try
            {
              if (removePrevDeleted)
              {
                int pos = (int) adapterPerson.getPosition(prevDeletedPerson);

                if ((pos >= lvPersons.getFirstVisiblePosition()) && (pos <= lvPersons.getLastVisiblePosition()))
                {
                  View vPrevDeleted = lvPersons.getChildAt(pos - lvPersons.getFirstVisiblePosition());
                  adapterPerson.animateRemoval(vPrevDeleted);
                }
                else
                {
                  adapterPerson.remove(prevDeletedPerson);
                }
              }

              if (person.deleted)
                prevDeletedPerson = person;
            }
            catch (Exception ex)
            {
              Log.e(TAG, "onTopViewVisibilityChange.onAnimationEnd: " + ex.getMessage());
            }
          }

          @Override
          public void onAnimationCancel(Animator animation)
          {

          }

          @Override
          public void onAnimationRepeat(Animator animation)
          {

          }
        });

        animatorView.start();
      }
      catch (Exception ex)
      {
        Log.e(TAG, "onTopViewVisibilityChange: " + ex.getMessage());
      }
    }
  };


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


  interface IListItemControls
  {
    void onUndoClicked(View v);
  }
}
