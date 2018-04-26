package com.example.twelker.reminderdemo;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

public class UpdateActivity extends AppCompatActivity {

    private EditText mReminderView;
    private long mID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mReminderView = (EditText) findViewById(R.id.editText_update);

        //Obtain parameters provided by MainActivity
        mID = getIntent().getLongExtra(MainActivity.INTENT_DETAIL_ROW_NUMBER, -1);

        Uri singleUri = ContentUris.withAppendedId(RemindersProvider.CONTENT_URI,mID);
        Cursor mCursor =   getContentResolver().query (singleUri,null,null, null, null);


        if (mCursor != null)
            mCursor.moveToFirst();
        mReminderView.setText(mCursor.getString(mCursor.getColumnIndex(RemindersContract.ReminderEntry.COLUMN_NAME_REMINDER)));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updatedReminderText = mReminderView.getText().toString();
                if (!TextUtils.isEmpty(updatedReminderText)) {

                    // Update the Content Provider database
                    ContentValues values = new ContentValues();
                    values.put(RemindersContract.ReminderEntry.COLUMN_NAME_REMINDER,updatedReminderText);
                    Uri singleUri = ContentUris.withAppendedId(RemindersProvider.CONTENT_URI,mID);
                    getContentResolver().update(singleUri, values, null, null);

                    finish();
                } else {
                    Snackbar.make(view, "Enter some data", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


            }
        });
    }

}
