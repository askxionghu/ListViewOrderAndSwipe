package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity
{
  private final String TAG = "MainActivity";

  private ArrayList<Person> persons = new ArrayList<>();
  private PersonAdapter adapterPerson;
  private PersonListViewOrder lvPersons;
  private JBHorizontalSwipe jbHorizontalSwipe;
  private Context context;

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
    public int onHeaderBeforeAnimation(boolean scrollingRight, float scrollDelta)
    {
      return 0;
    }

    @Override
    public void onHeaderAfterAnimation(boolean animatedRight, float scrollDelta)
    {

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
}
