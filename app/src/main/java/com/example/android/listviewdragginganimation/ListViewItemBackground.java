package com.example.android.listviewdragginganimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class ListViewItemBackground extends FrameLayout
{

  boolean mShowing = false;
  int mOpenAreaTop, mOpenAreaBottom, mOpenAreaHeight;
  boolean mUpdateBounds = false;

  Bitmap bmRowSnapshot;
  Drawable drawableBackground;

  public ListViewItemBackground(Context context)
  {
    super(context);
  }

  public ListViewItemBackground(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public ListViewItemBackground(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
  }


  public void showBackground(View v)
  {
    setWillNotDraw(false);
    mOpenAreaTop = v.getTop();
    mOpenAreaHeight = v.getHeight();
    mShowing = true;
    mUpdateBounds = true;

    this.bmRowSnapshot = loadBitmapFromView(v);
  }

  public static Bitmap loadBitmapFromView(View v)
  {
    Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
    v.draw(c);
    return b;
  }

  public void hideBackground()
  {
    setWillNotDraw(true);
    mShowing = false;
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    /*
     * The canvas takes up the entire area of the listview. The row to be deleted has a snapshot image of itself
     * drawn on top of this canvas at the top of the canvas. After drawing the snapsot, the canvas is moved down
     * to the position where the row's top position is located, giving the effect that the row is still visible
     * when in reality only a snapshot is being shown.
     */

    if (mShowing)
    {
      if (mUpdateBounds)
      {
        this.drawableBackground = new BitmapDrawable(getResources(), this.bmRowSnapshot);
        this.drawableBackground.setBounds(0, 0, getWidth(), mOpenAreaHeight);
      }

      canvas.save();
      canvas.translate(0, mOpenAreaTop);
      this.drawableBackground.draw(canvas);
      canvas.restore();
    }
  }
}
