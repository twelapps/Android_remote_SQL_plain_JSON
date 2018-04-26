package com.example.twelker.reminderdemo;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by twelh on 9/18/17.
 */

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {


    private Cursor mCursor;
    final private ReminderClickListener mReminderClickListener;

    public ReminderAdapter(Cursor cursor, ReminderClickListener mReminderClickListener) {
        this.mCursor = cursor;
        this.mReminderClickListener = mReminderClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView= (TextView) itemView.findViewById(android.R.id.text1);
        }
    }


    @Override
    public ReminderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);

        // Return a new holder instance
        ReminderAdapter.ViewHolder viewHolder = new ReminderAdapter.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ReminderAdapter.ViewHolder holder, final int position) {

        // Move the mCursor to the position of the item to be displayed
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null
        // Update the view holder with the information needed to display
        final long index=
                mCursor.getLong(mCursor.getColumnIndex(RemindersContract.ReminderEntry._ID));
        String name = mCursor.getString(mCursor.getColumnIndex(RemindersContract.ReminderEntry.COLUMN_NAME_REMINDER));


        holder.textView.setText(name);

        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReminderClickListener.reminderOnClick(index);
            }
        });

        holder.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mReminderClickListener.reminderOnLongClick(index);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return (mCursor == null ? 0 : mCursor.getCount());
    }

    public void swapCursor(Cursor newCursor) {

        if (mCursor != null) mCursor.close();

        mCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

}
