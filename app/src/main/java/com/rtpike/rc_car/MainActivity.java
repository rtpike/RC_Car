package com.rtpike.rc_car;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.russ.set.rc_car.R;


public class MainActivity extends AppCompatActivity {

    TcpClient mTcpClient = null;
    ConnectTask mTask = null;
    private Handler mHandler;
    Toolbar toolbar;
    FloatingActionButton fab;

    Boolean FPressed = false;
    Boolean RPressed = false;
    Boolean RLPressed = false;

    static final int CONNECT_ERROR = 0x0;
    static final int CONNECTED     = 0x1;
    static final int DISCONNECTED  = 0x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg){
                final View view = findViewById(android.R.id.content).getRootView();
                switch (msg.what) {
                    case CONNECTED:
                        Log.d("UI Handler", "In Handler's shutdown");
                        toolbar.setTitle("Connected");
                        fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
                        Snackbar.make(view, "Connecting to car over WiFi myEddy ", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    /*
                    views = new RemoteViews(context.getPackageName(), R.layout.activity_main);
                    widget = new ComponentName(context, MainActivity.class);
                    awManager = AppWidgetManager.getInstance(context);
                    views.setTextViewText(R.id.state, "Shutting PC...");
                    awManager.updateAppWidget(widget, views);
                    */
                        break;
                    case CONNECT_ERROR:
                        Log.d("UI Handler", "Error connecting");
                        toolbar.setTitle("Error connecting");
                        fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
                        Snackbar.make(view, "Error connecting...", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        break;
                    case DISCONNECTED:
                        Log.d("UI Handler", "disconnecting");
                        toolbar.setTitle("Disconnected");
                        fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
                        Snackbar.make(view, "Disconnected from to car", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        break;
                    default:
                        Log.d("UI Handler", "unknown command");
                        break;
                }
            }
        };


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mTask == null) {   //start TCP service
                    mTask = new ConnectTask();
                    mTask.view = view;
                    mTask.execute("");

                    //Snackbar.make(view, "Connecting to car over WiFi myEddy: ", Snackbar.LENGTH_LONG)
                    //        .setAction("Action", null).show();

                    //TODO: check for errors
                    //toolbar.setTitle("Connected");
                    //fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));

                } else {  //Disconnect
                    //fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
                    //mTcpClient.getInetAddr().toString();
                    if (mTcpClient != null) {
                        mTcpClient.stopClient();
                    }
                    //mTcpClient = null;
                    //mTask.cancel(true);
                    mTask = null;
                    //Snackbar.make(view, "Disconnected from to car", Snackbar.LENGTH_LONG)
                    //       .setAction("Action", null).show();
                    //toolbar.setTitle("Disconnected");
                }

   /*             if (mTcpClient != null && mTcpClient.isConnected()) { //Connected
                    fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
                }*/

                //TcpClient client = new TcpClient();
                //client.run();


/*                switch (item.getItemId()) {
                    case R.id.connect:

                        String username = PreferencesManager.getInstance().getUserName();
                        // check if we have the username saved in the preferences, if not, notify the user to write one down
                        if (username != null) {
                            // connect to the server
                            new ConnectTask().execute("");
                        } else {
                            Toast.makeText(this, "Please got to preferences and set a username first!", Toast.LENGTH_LONG).show();
                        }

                        return true;
                    case R.id.disconnect:

                        if (mTcpClient == null) {
                            return true;
                        }

                        // disconnect
                        mTcpClient.stopClient();
                        mTcpClient = null;

*/

            }
        });


        View forwardButtonTouch = (View) findViewById(R.id.forward_button);
        forwardButtonTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!RPressed) { //Reversed already pressed
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        forwardButton(v);
                        FPressed = true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        stop(v);  //Stop
                        FPressed = false;
                    }
                }
                return true;
            }
        });

        View reverseButtonTouch = (View) findViewById(R.id.reverse_button);
        reverseButtonTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!FPressed) { //Forward already pressed
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        reverseButton(v);
                        RPressed = true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        stop(v);  //Stop
                        RPressed = false;
                    }
                }
                return true;
            }
        });

        View leftButtonTouch = (View) findViewById(R.id.left_button);
        leftButtonTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftButton(v);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    center(v);  //Stop
                }
                return true;
            }
        });

        View rightButtonTouch = (View) findViewById(R.id.right_button);
        rightButtonTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    rightButton(v);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    center(v);  //Stop
                }
                return true;
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mTcpClient != null) {
            mTcpClient.stopClient();
        }
        //mTcpClient = null;
        //mTask.cancel(true);
        //mTask = null;
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

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void forwardButton(View view) {
        //Snackbar.make(view, "forwardButton", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();
        if (mTcpClient != null) mTcpClient.sendMessage("F");
    }

    public void stop(View view) {
        if (mTcpClient != null) mTcpClient.sendMessage("S");
    }

    public void reverseButton(View view) {
        //Snackbar.make(view, "reverseButton", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();
        if (mTcpClient != null) mTcpClient.sendMessage("V");
    }

    public void leftButton(View view) {
        //Snackbar.make(view, "leftButton", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();
        if (mTcpClient != null) mTcpClient.sendMessage("E");
    }

    public void center(View view) {
        if (mTcpClient != null) mTcpClient.sendMessage("C");
    }

    public void rightButton(View view) {
        //Snackbar.make(view, "rightButton", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();
        if (mTcpClient != null && mTcpClient.isConnected()) mTcpClient.sendMessage("T");
        //Error message
    }


    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        View view;

        private static final String COMMAND = "shutdown -s";
        private static final String TAG = "ShutdownAsyncTask";

        /**
         * ShutdownAsyncTask constructor with handler passed as argument. The UI is updated via handler.
         * In doInBackground(...) method, the handler is passed to TCPClient object.
         *
         * @param mHandler Handler object that is retrieved from MainActivity class and passed to TCPClient
         *                 class for sending messages and updating UI.
         */
//        public ShutdownAsyncTask(Handler mHandler) {
//            this.mHandler = mHandler;
//        }

        @Override
        protected TcpClient doInBackground(String... message) {

            Log.i("ConnectTask", "doInBackground: started.");
            try {
                //we create a TCPClient object and
                mTcpClient = new TcpClient(mHandler, new TcpClient.OnMessageReceived() {
                    @Override
                    //here the messageReceived method is implemented
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                        publishProgress(message);
                    }
                });

                mTcpClient.run();
            } catch (Exception e) {
                Log.e("TCP Client", "ERROR: " + e.toString());

            }
            return null;
        }
/*
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//
//            //in the arrayList we add the messaged received from server
//            arrayList.add(values[0]);
//            // notify the adapter that the data set has changed. This means that new message received
//            // from server was added to the list
//            mAdapter.notifyDataSetChanged();
//        }

        protected void onPostExecute(Long result) {
            showDialog("Downloaded " + result + " bytes");
        }
*/

        /**
         * Update list ui after process finished.
         */
        protected void onPostExecute(String file_url) {
            //TODO update list ui here, use
            //
            Log.i("ConnectTask", "onPostExecute: started.");
//            if(mTcpClient != null && mTcpClient.isConnected()) {
//                String ipaddr = mTcpClient.getInetAddr().toString();
//                Snackbar.make(view, "Connected to car over WiFi: " + ipaddr, Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//
//                //TODO: check for errors
//                toolbar.setTitle("Connected to "+ ipaddr);
//                fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
//
//            } else {  //Disconnect

            //TODO: change myEddy to variable
            Snackbar.make(view, "ERROR: Can't connect to myEddy: ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            fab.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
            //mTcpClient.getInetAddr().toString();
            if (mTcpClient != null) {
                mTcpClient.stopClient();
            }
            mTcpClient = null;
            //mTask.cancel(true);
            //mTask = null;
//            }

//            activity.runOnUiThread(new Runnable() {
//                public void run() {
//                    /**
//                     * Updating parsed JSON data into ListView
//                     */
//                    ListAdapter adapter = new SimpleAdapter(...);
//                    // updating listview
//                    ((ListActivity) activity).setListAdapter(adapter);
//                }
//            });

        }

    }


    /*
    Write to pins
    L + pin
    H + pin

    Input/Output mode, pins
    O + pin
    I + pin

    Read digital input
    R + pin

    Read analog pin
    A + pin

    */


}