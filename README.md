ListView Order and Swipe
========================
An Android app that demonstrates a listview that allows you to reorder items by dragging and deleting items by swiping them.

![ListViewOrderAndSwipe](https://github.com/JohannBlake/ListViewOrderAndSwipe/blob/master/Graphics/ListViewOrderAndSwipe-Demo.gif)



### Description

A common feature in many apps is having a list of items where you want to allow the user to reorder the items by dragging them to a new position within the list. Then there are lists where you want to allow the user to delete items by simply swiping them off the list. This app combines these two functions into a single listview.

The swipe-to-delete function mimics the functionality in Google's Gmail app. In the Gmail app, when you swipe an item in the list of emails in your inbox, the item is not immediately deleted. Instead, a view appears beneath the view that was swiped away and this view contains a label at the far left that reads "Deleted" and on the far right is an "Undo" button. While the item is deleted at this point, you do have the opportunity to undo the deletion either by tapping on the Undo button or swiping the item again to restore it to its previous state. This same functionality is incorporated into this app.

To reorder items, a "Move" icon is show on the far right of each item. It looks like a hamburger icon, that is typically shown in the upper left corner of many Android apps. The Google News app uses this same icon on the activity where you can reorder your topics. To reorder a list item, just press down on the icon and drag the row to the new location.

When you swipe an item to delete it, the bottom view (where the Undo button is shown) remains visible either until you delete another item, tap on another item, scroll the list, or press the Undo button.

The app is constructed in such a way that you can easily replace the bottom view with whatever kind of view you need in your listview to provide the functionality that is appropriate for your app.


### Demo

To try out the app, just install the demo app *ListViewOrderAndSwipe-Demo-x.x.x.apk* located in the root directory. 


### How it works

The app is based on three components that interact with each other. One of the components was written by Daniel Olshansky at Google to handle the reordering of list items. His code however requires performing a long tap on an item in order to initiate the drag functionality. Many users will find that unintuitive. Doing long presses on UI controls is a hidden functionality that most users probably won't even know exists in your app unless you explicitly tell them about it. The Google News app provides an icon instead where you press down and drag. This functionality replaces the long press functionality in Daniel's code.

Daniel's video can be viewed at: https://www.youtube.com/watch?v=_BZIvjMgH-Q

The second component is the swipe-to-delete functionality, written by Chet Haase, also from Google. However, only a portion of his code was incorporated in this app as it had no support for a bottom view when swiping a top view. Chet's code also deletes the item immediately making it impossible to allow the user to undo the deletion. Chet's code however does have a bug. If the listview has less items than the height of the listview can contain and you delete the last item, its background remains on the listview. This was fixed in this app.

The third component is a library called JBHorizontalSwipe that provides support for swiping list items. It acts as a controller and is not a widget. It can be used to swipe any kind of view out of its parent container revealing a view beneath it. It supports animating the swipe on finger-up, velocity swiping and swiping the top view back onto the screen either from the left or right. Another reason why this module was developed this way is to provide future support where you may want to swipe multiple items simultaneously.

One of the most important concepts in this app is that no scroll listeners are used, which would be problematic and cause undesirable side effects when attempting to coordinate vertical and horizontal swiping as well as having to deal with a bottom view that needs to deal with its own touch events.

Touch events are handled in two separate locations. One is in the listview (PersonListViewOrder) and the other is in the JBHorizontalSwipe controller. They coordinate touch events to provide a seemless touch experience for the user.

By keeping these three components separate, you can more easily customize the code to handle variations of re-ordering and swiping that are more applicable to your app.

To create a bottom view, take a look at the code in the person_item.xml layout file. 

``` xml
<?xml version="1.0" encoding="utf-8"?>
<com.example.android.listviewdragginganimation.CustomListItem
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/grey_600">

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:tag="BottomView"
            android:alpha="0">

            <com.example.android.listviewdragginganimation.ButtonBottomView
                android:id="@+id/btnUndo"
                android:layout_width="wrap_content"
                android:layout_height="match_parent"
                android:layout_alignParentRight="true"
                android:background="@drawable/listview_item_bottom_button"
                android:focusable="false"
                android:focusableInTouchMode="false"
                android:gravity="center"
                android:paddingLeft="20dp"
                android:paddingRight="20dp"
                android:text="UNDO"
                android:textColor="@color/orange_300"
                android:textSize="18sp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="match_parent"
                android:layout_alignParentLeft="true"
                android:layout_toLeftOf="@id/btnUndo"
                android:gravity="center_vertical"
                android:paddingLeft="20dp"
                android:text="Deleted"
                android:textColor="@color/white"
                android:textSize="18sp"/>

        </RelativeLayout>
    </LinearLayout>

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/person_listview_item"
        android:tag="TopView">

        <ImageView
            android:id="@+id/ivIcon"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_centerVertical="true"
            android:layout_marginBottom="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginRight="10dp"
            android:layout_marginTop="5dp"
            android:scaleType="fitXY"/>

        <ImageView
            android:id="@+id/ivDrag"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:layout_alignParentBottom="true"
            android:layout_alignParentRight="true"
            android:layout_alignParentTop="true"
            android:layout_centerVertical="true"
            android:layout_marginLeft="10dp"
            android:alpha=".54"
            android:minWidth="50dp"
            android:scaleType="centerInside"
            android:src="@drawable/ic_hamburger"
            android:tag="DragIcon"/>

        <TextView
            android:id="@+id/tvName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerVertical="true"
            android:layout_toLeftOf="@id/ivDrag"
            android:layout_toRightOf="@id/ivIcon"
            android:textSize="18sp"/>

    </RelativeLayout>

</com.example.android.listviewdragginganimation.CustomListItem>
```
The root view has to be a FrameLayout that gets extended. See CustomListItem.java for the custom FrameLayout.

There are two child views under the root view. The first one is used for the bottom view. Note the tag on its RelativeLayout is set to "BottomView". You must set this tag in order for JBHorizontalSwipe to recognize it as the bottom view. Likewise, notice the RelativeLayout with its tag set to "TopView". You need to set this as well for the top view to be identified. To bottom view also has its alpha set to 0 making it invisible. Don't set the visibility property to "INVISIBLE". Use the alpha property instead because that is how JBHorizontalSwipe handles displaying it.

The button used for undoing a deletion is a customized Button control called ButtonBottomView. You need to use the code in ButtonBottomView.java to make sure that touch events get handled properly.

Finally, an ImageView (ivDrag) is used to handle dragging a list item to a new position. You must set its tag to "DragIcon" in order for dragging to work.

Other than this, you can customize the top and bottom views according to your app's requirements. You should still stick with the layout for the list item as shown here. For instance, the first child view is a LinearLayout (this is the bottom view). This LinearLayout has a RelativeLayout. This is where your bottom view's controls can be placed. Don't eliminate the LinearLayout or the code will fail and keep its RelativeLayout if possible. The top view should only be a RelativeLayout.

### Acknowledgments

Thanks goes to Daniel Olshansky and Chet Haase at Google for providing the code that formed much of the basis for this app.

### MIT License

```
    The MIT License (MIT)

    Copyright (c) 2015 Johann Blake

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
```
