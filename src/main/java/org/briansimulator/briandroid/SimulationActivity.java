package org.briansimulator.briandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.briansimulator.briandroid.Simulations.COBA;
import org.briansimulator.briandroid.Simulations.Simulation;

public class SimulationActivity extends Activity {


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        TextView statusText = (TextView) findViewById(R.id.statusTextView);
        statusText.setTextSize(40);
        statusText.setText("FOO");

        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        String simulationName = intent.getStringExtra(BDMainActivity.EXTRA_MESSAGE);
        Toast.makeText(this, "STARTING SIMULATION "+simulationName, Toast.LENGTH_LONG).show();
        Simulation theSim;
        if (simulationName.equals("COBA")) {
            theSim = new COBA();
            theSim.setup();
            theSim.setProgressView(statusText);
            Toast.makeText(this, "RUNNING", Toast.LENGTH_LONG).show();
            theSim.run();
        }
        finish();
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
