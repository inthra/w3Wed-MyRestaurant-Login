package com.epicodus.myrestaurants.adapters;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import com.epicodus.myrestaurants.models.Restaurant;
import com.epicodus.myrestaurants.ui.RestaurantDetailActivity;
import com.epicodus.myrestaurants.util.ItemTouchHelperAdapter;
import com.epicodus.myrestaurants.util.OnStartDragListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Guest on 7/18/16.
 */
public class FirebaseRestaurantListAdapter extends FirebaseRecyclerAdapter<Restaurant, FirebaseRestaurantViewHolder> implements ItemTouchHelperAdapter {
    private DatabaseReference mRef;
    private OnStartDragListener mOnStartDragListener;
    private Context mContext;
    private ChildEventListener mChildEventListener;
    private ArrayList<Restaurant> mRestaurants = new ArrayList<>();

    public FirebaseRestaurantListAdapter (Class<Restaurant> modelClass, int modelLayout, Class<FirebaseRestaurantViewHolder> viewHolderClass, Query ref, OnStartDragListener onStartDragListener, Context context) {
        // the constructor requires the class of the data that will populate the RecyclerView, the layout we will inflate for each item, the ViewHolder class, and the database reference or query. We also add the OnStartDragListener and the context to the constructor. The context will be needed when we eventually create an intent to navigate to the detail activity.

        super(modelClass, modelLayout, viewHolderClass, ref);
        mRef = ref.getRef();
        mOnStartDragListener = onStartDragListener;
        mContext = context;

        mChildEventListener = mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mRestaurants.add(dataSnapshot.getValue(Restaurant.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void populateViewHolder(final FirebaseRestaurantViewHolder viewHolder, Restaurant model, int position) {
        viewHolder.bindRestaurant(model);
        viewHolder.mRestaurantImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mOnStartDragListener.onStartDrag(viewHolder);
                }
                return false;
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(mContext, RestaurantDetailActivity.class);
                intent.putExtra("position", viewHolder.getAdapterPosition());
                intent.putExtra("restaurants", Parcels.wrap(mRestaurants));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // override methods from the ItemTouchHelperAdapter interface
        Collections.swap(mRestaurants, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
        // this is a java method, it get starting and final position
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        mRestaurants.remove(position);
        // override methods from the ItemTouchHelperAdapter interface
        getRef(position).removeValue();
    }

    private void setIndexInFirebase() {
        // this method is used in the cleanup, when we leave the saved restaurant activity
        for (Restaurant restaurant : mRestaurants) {
            int index = mRestaurants.indexOf(restaurant);
            DatabaseReference ref = getRef(index);
            restaurant.setIndex(Integer.toString(index));
            ref.setValue(restaurant);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        setIndexInFirebase();
        mRestaurants.clear();
        mRef.removeEventListener(mChildEventListener);
    }

}
