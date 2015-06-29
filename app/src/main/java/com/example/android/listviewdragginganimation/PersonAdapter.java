package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

import java.util.HashMap;
import java.util.List;

public class PersonAdapter extends ArrayAdapter<Person> implements JBHorizontalSwipe.IJBHorizontalSwipeAdapter
{
  private final String TAG = "PersonAdapter";
  private final String TAG_TOP_VIEW = "TopView";
  final int INVALID_ID = -1;

  private List<Person> items;
  private Context context;
  private PersonListViewOrder listview;
  private JBHorizontalSwipe jbHorizontalSwipe;
  private boolean disableAdapter;
  private View selectedView;

  HashMap<String, Integer> idMap = new HashMap<>();

  public PersonAdapter(Context context, int textViewResourceId, List<Person> items, JBHorizontalSwipe jbHorizontalSwipe)
  {
    super(context, textViewResourceId, items);

    this.items = items;
    this.context = context;
    this.jbHorizontalSwipe = jbHorizontalSwipe;

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

      ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);

      ivIcon.setImageBitmap(person.bitmap);

      TextView tvName = (TextView) v.findViewById(R.id.tvName);
      tvName.setText(person.name);

      View vTop = v.findViewWithTag(TAG_TOP_VIEW);
      vTop.setOnTouchListener(onTouchListenerTopView);

      return v;
    }
    catch (Exception ex)
    {
      Log.e(TAG, "onCreate: " + ex.getMessage());
      return convertView;
    }
  }


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
  public boolean isEnabled(int position)
  {
    if (this.disableAdapter)
      return false;
    else
      return true;
  }


  @Override
  public void setDisable(boolean disable)
  {
    this.disableAdapter = disable;
  }

  @Override
  public View getSelectedView()
  {
    return this.selectedView;
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

  @Override
  public boolean hasStableIds()
  {
    return true;
  }
}