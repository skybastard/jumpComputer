package jumpsoft.jumpdata;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.os.Bundle;
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
    SensorManager manager;
    Sensor pressureSensor;

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

        manager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        PressureListener listener = new PressureListener();
        manager.registerListener(listener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);


        altiView = (TextView) findViewById(R.id.altiView);
        spdView = (TextView) findViewById(R.id.spdView);
        vmaxView = (TextView) findViewById(R.id.vmaxView);
        vavgView = (TextView) findViewById(R.id.vavgView);
        exitView = (TextView) findViewById(R.id.exitView);
        dplyView = (TextView) findViewById(R.id.dplyView);
        timeView = (TextView) findViewById(R.id.timeView);
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
        float exitAlti = 0;
        float dplyAlti;
        float speed;
        long time;
        long falltime;
        int medianCounter = 0;
        boolean groundPressureSet = false;

        ArrayList<Float> medianList = new ArrayList<>();
        ArrayList<Long> speedTimeList = new ArrayList<>();
        ArrayList<Float> speedAltitudeList = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#####");



        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if (groundPressureSet == false) {
                groundPressure = Math.round(sensorEvent.values[0]);
                groundPressureSet = true;
            }

            rawPressureValue = sensorEvent.values[0];
            medianList.add(rawPressureValue);

            medianCounter++;
            if (medianCounter == 5) {
                Collections.sort(medianList);
                pressureValue = medianList.get(2);
                altitude = manager.getAltitude(groundPressure, pressureValue);

                //Speed calculation
                time = sensorEvent.timestamp;
                speedTimeList.add(time);
                speedAltitudeList.add(altitude);
                if (speedTimeList.size() > 2) {
                    deltaTime = ((speedTimeList.get(speedTimeList.size() - 1)
                            - speedTimeList.get(speedTimeList.size() - 2))) / 1000000;
                    deltaHeight = (speedAltitudeList.get(speedAltitudeList.size() - 2)
                            - speedAltitudeList.get(speedAltitudeList.size() - 1));
                    speed = Math.abs((deltaHeight / deltaTime) * 1000);
                    speedTimeList.remove(0);
                    speedAltitudeList.remove(0);
                }
                medianCounter = 0;
                medianList.clear();
            }
            altiView.setText(String.valueOf( df.format(altitude) + " m"));
            spdView.setText(String.valueOf( df.format(speed) + " m/s"));
            Log.d("pressure", String.valueOf(sensorEvent.values[0]) + " "+ deltaTime +" "+deltaHeight);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }


}
