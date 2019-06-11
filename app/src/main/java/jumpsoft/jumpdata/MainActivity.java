package jumpsoft.jumpdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private SensorManager manager;
    private Sensor pressureSensor;
    private PowerManager.WakeLock wakeLock;


    TextView altiView;
    TextView spdView;
    TextView vmaxView;
    TextView vavgView;
    TextView exitView;
    TextView dplyView;
    TextView timeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "jumpData:wakelock");
        //wakeLock.acquire(70000000); //TODO wakelock timeout

        manager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        if (manager != null) {
            pressureSensor = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
        PressureListener listener = new PressureListener();
        manager.registerListener(listener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);


        altiView = (TextView) findViewById(R.id.altiView);
        spdView = (TextView) findViewById(R.id.spdView);
        vmaxView = (TextView) findViewById(R.id.vmaxView);
        vavgView = (TextView) findViewById(R.id.vavgView);
        exitView = (TextView) findViewById(R.id.exitView);
        dplyView = (TextView) findViewById(R.id.dplyView);
        timeView = (TextView) findViewById(R.id.timeView);

        SQLiteOpenHelper sqLiteOpenHelper = new LogBookDatabaseHelper(getApplicationContext(), "Logbook.db", null, 1);
        SQLiteDatabase logbookDB = sqLiteOpenHelper.getWritableDatabase(); // todo close database
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class PressureListener implements SensorEventListener {

        float pressureValue;
        float rawPressureValue;
        float groundPressure;
        float deltaTime;
        float deltaHeight;
        float altitude;
        float maxSpeed = 0;
        float exitAlti = 0; // first logged when hi speed
        float dplyAlti; // last logged when speed low at altitude
        float avgSpeed;
        float speed;
        long time;
        long falltime; // last - first logged
        int medianCounter = 0;
        boolean groundPressureSet = false;
        boolean freefalling = false;
        boolean dataWritten = false;

        ArrayList<Float> medianList = new ArrayList<>();
        ArrayList<Long> speedTimeList = new ArrayList<>();
        ArrayList<Float> speedAltitudeList = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#####");


        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if (!groundPressureSet) {
                groundPressure = sensorEvent.values[0];
                groundPressureSet = true;
            }

            rawPressureValue = sensorEvent.values[0];
            medianList.add(rawPressureValue);

            medianCounter++;
            if (medianCounter == 3) {
                Collections.sort(medianList);
                pressureValue = medianList.get(1);
                altitude = manager.getAltitude(groundPressure, pressureValue);
                if (altitude < 0) {
                    groundPressureSet = false;
                }
                //Speed calculation
                time = sensorEvent.timestamp;
                speedTimeList.add(time);
                speedAltitudeList.add(altitude);
                if (speedTimeList.size() > 2) {
                    deltaTime = ((speedTimeList.get(speedTimeList.size() - 1)
                            - speedTimeList.get(speedTimeList.size() - 2))) / 1000000;
                    deltaHeight = (speedAltitudeList.get(speedAltitudeList.size() - 2)
                            - speedAltitudeList.get(speedAltitudeList.size() - 1));
                    speed = (deltaHeight / deltaTime) * 1000;

                    if (maxSpeed < speed) {
                        maxSpeed = speed;
                    }

                    if (speed > 10 && altitude > 50) {
                        logData(time, altitude);
                        freefalling = true;
                    } else {
                        freefalling = false;
                    }
                    if (logTime.size() > 2 && !freefalling) { // logtime array is empty when !freefalling, runs only once
                        updateViews(logTime, logAlti);
                    }
                    speedTimeList.remove(0);
                    speedAltitudeList.remove(0);
                }
                medianCounter = 0;
                medianList.clear();
            }

            altiView.setText(String.valueOf(df.format(altitude) + " m"));
            spdView.setText(String.valueOf(df.format(speed) + " m/s"));


            Log.d("pressure", String.valueOf(sensorEvent.values[0]) + " " + deltaTime + " " + deltaHeight);
        } // onsensorchanged

        ArrayList<Long> logTime = new ArrayList<>();
        ArrayList<Float> logAlti = new ArrayList<>();

        void logData(long time, float altitude) {
            logTime.add(time);
            logAlti.add(altitude);
        }

        void updateViews(ArrayList<Long> logTime, ArrayList<Float> logAlti) {
            exitAlti = logAlti.get(0);
            dplyAlti = logAlti.get(logAlti.size() - 1);
            falltime = (logTime.get(logTime.size() - 1) - logTime.get(0)) / 1000000000;
            avgSpeed = (exitAlti - dplyAlti) / falltime;

            vmaxView.setText(String.valueOf(df.format(maxSpeed)));
            vavgView.setText(String.valueOf(df.format(avgSpeed)));
            exitView.setText(String.valueOf(df.format(exitAlti)));
            dplyView.setText(String.valueOf(df.format(dplyAlti)));
            timeView.setText(String.valueOf(df.format(falltime)));


            logAlti.clear();
            logTime.clear();

            // todo database update here
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}
