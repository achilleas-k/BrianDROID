package org.briansimulator.briandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.briansimulator.briandroid.Simulations.COBA;
import org.briansimulator.briandroid.Simulations.Simulation;


public class BDMainActivity extends Activity {

    private ListView simulationLV;
    private Simulation[] simList;
    private ArrayAdapter arrayAdpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briandroid_main);


        simulationLV = (ListView) findViewById(R.id.simulationListView);
        simList = new Simulation[2];
        simList[0] = new COBA();
        simList[1] = new COBA();

        arrayAdpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, simList);
        simulationLV.setAdapter(arrayAdpt);
        // React to user clicks on item
        simulationLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                    long id) {
                //startSimulationActivity(id);
                CharSequence message = "SELECTED POS " + position + " WITH ID " + id;
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }

    public void startSimulationActivity(int loc) {
        // get simulation object from HashMap
        Simulation selectedSim = simList[loc];
        // Send simulation object to the detail activity
        Intent simulationIntent = new Intent(this, SimulationActivity.class);
    }


}
