package com.example.android.listviewdragginganimation;

import android.graphics.Bitmap;

/**
 * Created by Johann on 6/24/15.
 */
public class Person
{
  public long id;
  public String name;
  public Bitmap bitmap;
  public boolean deleted;
  public boolean remove;

  public Person(long id, String name, Bitmap bitmap)
  {
    this.id = id;
    this.name = name;
    this.bitmap = bitmap;
  }
}
