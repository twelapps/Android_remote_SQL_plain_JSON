package com.example.twelker.reminderdemo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
//import org.json.JSONException;
import org.json.simple.JSONObject;

public class MainActivity extends AppCompatActivity
        implements ReminderClickListener, LoaderManager.LoaderCallbacks<Cursor>, LocationListener {

    //Local variables
    private ReminderAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private EditText mNewReminderText;

    private LocationManager mLocationManager;

    //Database related local variables
    private Cursor mCursor;

    //Constants used when calling the detail activity
    public static final String INTENT_DETAIL_ROW_NUMBER = "Row number";

    //TODO to watch 6: use the local IP address, not "localhost" (may have a different meaning on different devices).
    final String MY_URL_STRING = "http://100.72.50.65:8888/";
    final int TIME_INTERVAL = 30000; //Milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize the local variables
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mNewReminderText = (EditText) findViewById(R.id.editText_main);

        getSupportLoaderManager().initLoader(0, null, this);

//        //Fill with some reminders
//        for (int i = 0; i < 20; i++) {
//            String temp = "Reminder " + Integer.toString(i);
//            Reminder tempReminder = new Reminder(temp);
//            mReminders.add(tempReminder);
//        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO to watch 7: "misuse" fab to read reminder data from the remote database and copy into snackbar message
                /********************************************************************
                 * Read the reminders with lat/lon/time from the SQL database
                 ********************************************************************/
                //Create an instance of the inner class ReadRemindersFromRemoteSql
                ReadRemindersFromRemoteSql task = new ReadRemindersFromRemoteSql();

                task.currentUrl = MY_URL_STRING;

                //And execute it
                task.execute();


//                //Get the user text from the textfield
//                String text = mNewReminderText.getText().toString();
//
//                //Check if some text has been added
//                if (!(TextUtils.isEmpty(text))) {
//
//                    //Add the reminder with given text to the database
//                //    mDataSource.createReminder(text);
//                    ContentValues values = new ContentValues();
//                    values.put(RemindersContract.ReminderEntry.COLUMN_NAME_REMINDER, text);
//                    getContentResolver().insert(RemindersProvider.CONTENT_URI, values);
//
//                    //Initialize the EditText for the next item
//                    mNewReminderText.setText("");
//                } else {
//                    //Show a message to the user if the text field is empty
//                    Snackbar.make(view, "Please enter some text in the textfield", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
            }
        });

        /*
Add a touch helper to the RecyclerView to recognize when a user swipes to delete a list entry.
An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
and uses callbacks to signal when a user is performing these actions.
*/
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
                            target) {
                        return false;
                    }

                    //Called when a user swipes left or right on a ViewHolder
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                        //Get the index corresponding to the selected position
                        mCursor.moveToPosition(viewHolder.getAdapterPosition());
                        long index = mCursor.getLong(mCursor.getColumnIndex(RemindersContract.ReminderEntry._ID));

                        //Delete the entry
                        Uri singleUri = ContentUris.withAppendedId(RemindersProvider.CONTENT_URI,index);
                        getContentResolver().delete(singleUri, null, null);
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        //Initiate the location manager instance
        mLocationManager=(LocationManager) getSystemService(LOCATION_SERVICE);

//Request updates from GPS; every <TIME_INTERVAL> ms; no distance parameter
//(if > 0, and there is no movement, no updates received)
//“this”: the place where the interface methods are implemented
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_INTERVAL, 0, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_INTERVAL, 0, this);
        }
        catch (SecurityException e) {
            Log.d("MainActivity", " onLocationChanged: " + "SecurityException: \n" + e.toString());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void reminderOnClick(long index) {
        Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
        intent.putExtra(INTENT_DETAIL_ROW_NUMBER, index);
        startActivity(intent);
    }

    @Override
    public void reminderOnLongClick(long id) {

        //Create an instance of the DetailFragment class
        final DetailFragment detailFragment = DetailFragment.newInstance(id);

        //Use a FragmentManager and transaction to add the fragment to the screen
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        //and here is the transaction to add the fragment to the screen
        fragmentManager.beginTransaction()
                .replace(R.id.detailFragment, detailFragment, "detailFragment")
                .addToBackStack(null)
                .commit();
    }

    protected void onPause() {
        super.onPause();

        mLocationManager.removeUpdates(this);
    }


    protected void onResume() {

        super.onResume();

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_INTERVAL, 0, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_INTERVAL, 0, this);
        }
        catch (SecurityException e) {
            Log.d("MainActivity", " onLocationChanged: " + "SecurityException: \n" + e.toString());
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        CursorLoader cursorLoader = new CursorLoader(this, RemindersProvider.CONTENT_URI, null,
                null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        this.mCursor = cursor;
        mAdapter = new ReminderAdapter(mCursor, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(mCursor);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

    @Override
    public void onLocationChanged(Location location) {

        double altitude = location.getAltitude();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double speed = location.getSpeed();
        long time = location.getTime(); //when the update was received
        String providerName = location.getProvider(); //Name of the provider, in case you have registered for multiple

        //Convert to a readable date and time
        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateToStr = format.format(time);

        //Add the reminder with lat/lon/time to the database
        //Lat/long 4th decimal is about 10 metres, fine for GPS.
        //"final": it will be used later in an inner method.

        ContentValues values = new ContentValues();
        final String data = "provider: " + providerName + "\nlatitude: " + String.format("%.4f", latitude) +
                "\nlongitude: " + String.format("%.4f", longitude) +
                "\ntime: " + dateToStr ;

        values.put(RemindersContract.ReminderEntry.COLUMN_NAME_REMINDER, data);
        getContentResolver().insert(RemindersProvider.CONTENT_URI, values);

        //TODO to watch 1: next to putting the data into the local db, put it into a remote db. First watch the php code.
        /********************************************************************
         * Add the reminder with lat/lon/time to an SQL database
         ********************************************************************/
        //Create an instance of the inner class AddReminderToRemoteSql
        AddReminderToRemoteSql task = new AddReminderToRemoteSql();

        task.currentUrl = MY_URL_STRING;

        //And execute it
        task.execute(new String[] { data });
    }

    //TODO to watch 2: create the task "AddReminderToRemoteSql" that submits a POST request to the remote server.
    //TODO to watch 3: mind the datatypes.
    private class AddReminderToRemoteSql extends AsyncTask<String, Void, Void> {

        //Class property
        public String currentUrl;

        @Override
        protected Void doInBackground(String... reminderData) {
            // All your networking logic should be here

            //TODO to watch 5: connect laptop & mobile device to same wifi for proper connection on subnetmake sure to use the local IP address, not 'localhost'.

            String dataRequestInput = "name=" + reminderData[0];
            HttpURLConnection conn = null;
            String response = "";
            BufferedReader reader = null;

            try {

                URL urlObj = new URL(currentUrl);

                //Create a client
                //Use Https in case of secure connection, otherwise Http
                conn = (HttpURLConnection) urlObj.openConnection();

                //REST API "POST" type
                conn.setRequestMethod("POST");

                //Data will be added
                conn.setDoOutput(true);

                // Write the data
                conn.getOutputStream().write(dataRequestInput.getBytes());

                Log.d("MainActivity ", "post response code " + conn.getResponseCode() + " ");

                //Prepare to read the output
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                response = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("MainActivity ", e.getMessage().toString());
                Log.d("MainActivity ", "POST error");
            } finally {
                try {
                    reader.close();
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (Exception ex) {
                }
            }
            Log.d("MainActivity ", "POST RESPONSE: " + response);
            return null;
        }
    }

    //TODO to watch 8: create the task "ReadRemindersFromRemoteSql" that submits a GET request to the remote server.
    private class ReadRemindersFromRemoteSql extends AsyncTask<Void, Void, List<String>> {

        //Class property
        public String currentUrl;

        @Override
        protected List<String> doInBackground(Void... voids) {
            // All your networking logic should be here

            //Create and initiate output
            ArrayList<String> reminders = new ArrayList<>();

            //Read data from remote sql and transform into arraylist

            //Create and initiate some local variables
            HttpURLConnection conn = null;
            String response = "";
            BufferedReader reader = null;

            try {
                URL urlObj = new URL(currentUrl + "index.php?name=*");

                //Create a client
                //Use Https in case of secure connection, otherwise Http
                conn = (HttpURLConnection) urlObj.openConnection();

                //REST API "GET" type
                conn.setRequestMethod("GET");

                //Proceed when the returncode is 200
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.d("MainActivity ", "run: return code from SQL server OK: " + conn.getResponseCode());

                    //Prepare to read the output
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line /*+ "\n"*/);
                    }

                    response = sb.toString();

                    JSONParser parser = new JSONParser();
                    Object object = parser.parse(response);
                    JSONArray json = (JSONArray) object;

                    for (int i = 0; i < json.size(); i++) {
                        //    Log.d("MainActivity", "element " + i + ": " + json.get(i));
                        //    JsonElement element = json.get(i);
                        //    Log.d("MainActivity", "element " + i + ": " + element);
                        JSONObject object2 = (JSONObject) json.get(i);
                        String data2 = object2.get("name").toString();
                        Log.d("MainActivity", "name element " + i + ": " + data2);

                        //Put in the local array of strings
                        reminders.add(data2);

                    }
                } else {
                    Log.d("MainActivity ", "run: return code from SQL server not OK: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("MainActivity ", e.getMessage());
                Log.d("MainActivity ", "GET error");
            } finally {
                try {
                    reader.close();
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (Exception ex) {}
            }
            Log.d("MainActivity ", "GET RESPONSE: " + response);

            return reminders;
        }

        protected void onPostExecute(List<String> reminders) {
            //Put all array elements into a snackbar message
            String msg = "";
            for (int i = reminders.size() - 1; i >= 0; i--) {
                msg = msg + "\n" +"\n" + Integer.toString(i+1) + ". " + reminders.get(i);
            }
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();

        }
    }


}
