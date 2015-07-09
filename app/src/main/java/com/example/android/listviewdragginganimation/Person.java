package com.example.android.listviewdragginganimation;

import android.graphics.Bitmap;

/**
 * Holds information about a person.
 */
public class Person
{
  public long id;
  public String name;
  public Bitmap bitmap;
  public boolean deleted;

  public Person(long id, String name, Bitmap bitmap)
  {
    this.id = id;
    this.name = name;
    this.bitmap = bitmap;
  }
}
