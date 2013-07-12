package org.briansimulator.briandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.briansimulator.briandroid.Simulations.COBA;
import org.briansimulator.briandroid.Simulations.COBArs;
import org.briansimulator.briandroid.Simulations.CUBA;
import org.briansimulator.briandroid.Simulations.PitchOnline;
import org.briansimulator.briandroid.Simulations.PitchOffline;
import org.briansimulator.briandroid.Simulations.Simulation;

public class SimulationActivity extends Activity {

    TextView statusText;
    Button runButton;
    Simulation theSim;
    Context toastContext;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        statusText = (TextView) findViewById(R.id.statusTextView);
        runButton = (Button) findViewById(R.id.buttonRun);
        toastContext = getApplicationContext();

        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        String simulationName = intent.getStringExtra(BDMainActivity.EXTRA_MESSAGE);
        initSimulation(simulationName);
        runButton.setOnClickListener(runButtonListener);
    }

    private OnClickListener runButtonListener = new OnClickListener() {
        public void onClick(View v) {
            // start simulation
            if (theSim == null) {
                Toast.makeText(toastContext, "Something went horribly wrong.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                startSimulation();
            }
        }

    };

    void initSimulation(String ID) {
        if (ID.equals("COBA")) {
            theSim = new COBA(this);
            theSim.setup();
            theSim.setProgressView(statusText);
            Toast.makeText(toastContext, "Loaded simulation "+theSim.toString(), Toast.LENGTH_LONG).show();
        } else if (ID.equals("CUBA")) {
            theSim = new CUBA(this);
            theSim.setup();
            theSim.setProgressView(statusText);
            Toast.makeText(toastContext, "Loaded simulation "+theSim.toString(), Toast.LENGTH_LONG).show();
        } else if (ID.equals("PitchOffline")) {
            theSim = new PitchOffline(this);
            theSim.setup();
            theSim.setProgressView(statusText);
            Toast.makeText(toastContext, "Loaded simulation "+theSim.toString(), Toast.LENGTH_LONG).show();
        }  else if (ID.equals("PitchOnline")) {
            theSim = new PitchOnline(this);
            theSim.setup();
            theSim.setProgressView(statusText);
            Toast.makeText(toastContext, "Loaded simulation "+theSim.toString(), Toast.LENGTH_LONG).show();
        }  else if (ID.equals("COBArs")) {
            theSim = new COBArs(this);
            theSim.setup();
            theSim.setProgressView(statusText);
            Toast.makeText(toastContext, "Loaded simulation "+theSim.toString(), Toast.LENGTH_LONG).show();
        }
    }


    void startSimulation() {
        theSim.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
