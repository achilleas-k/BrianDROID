package org.briansimulator.briandroid;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BDMainActivity extends Activity {

    public final static String EXTRA_MESSAGE = "org.briansimulator.briandroid.MESSAGE";
    private ListView simulationLV;
    private String[] simList;
    private ArrayAdapter arrayAdpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briandroid_main);


        simulationLV = (ListView) findViewById(R.id.simulationListView);
        simList = populateList();

        arrayAdpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, simList);
        simulationLV.setAdapter(arrayAdpt);
        // React to user clicks on item
        simulationLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                    long id) {
                String selectedSim = simList[position];
                startSimulationActivity(selectedSim);
            }
        });

    }

    public void startSimulationActivity(String simID) {
        // Send simulation object to the detail activity
        Intent simulationIntent = new Intent(this, SimulationActivity.class);
        simulationIntent.putExtra(EXTRA_MESSAGE, simID);
        startActivity(simulationIntent);
    }

    private String[] populateList() {
        // TODO: This method should read a specified directory (the simulation directory) and return the list of names
        // Currently the list is hardcoded since the class files are built into the app
        //File simulationDir = new File("Simulations");
        //simList = simulationDir.list();
        String[] list = {"COBA", "CUBA", "PitchOffline", "PitchOnline", "COBArs"};
        return list;
    }
}
