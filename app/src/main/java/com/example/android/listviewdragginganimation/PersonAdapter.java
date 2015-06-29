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
import android.widget.TextView;

import com.johannblake.widgets.jbhorizonalswipelib.JBHorizontalSwipe;

import java.util.HashMap;
import java.util.List;

public class PersonAdapter extends ArrayAdapter<Person> implements JBHorizontalSwipe.IJBHorizontalSwipeAdapter
{
  private final String TAG = "PersonAdapter";
  final int INVALID_ID = -1;

  private List<Person> items;
  private Context context;
  private PersonListViewOrder listview;
  private JBHorizontalSwipe jbHorizontalSwipe;
  private boolean disableAdapter;

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

      if (v == null)
      {
        LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.person_item, null);
      }

      CustomListItem customListItem = (CustomListItem) v;
      customListItem.setJBHeaderRef(this.jbHorizontalSwipe);

      Person person = this.items.get(position);
      v.setTag(person);

      ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);

      ivIcon.setImageBitmap(person.bitmap);

      TextView tvName = (TextView) v.findViewById(R.id.tvName);
      tvName.setText(person.name);

      //v.setOnClickListener(onItemClick);

      return v;
    }
    catch (Exception ex)
    {
      Log.e(TAG, "onCreate: " + ex.getMessage());
      return convertView;
    }
  }


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

  private View.OnClickListener onItemClick = new View.OnClickListener()
  {
    @Override
    public void onClick(View v)
    {
      int x = 0;
      x++;
    }
  };


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