package org.briansimulator.briandroid;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.briansimulator.briandroid.Simulations.COBA;
import org.briansimulator.briandroid.Simulations.Simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BDMainActivity extends ListActivity {


    public static List<Map<String, Simulation>> simList = new ArrayList<Map<String, Simulation>>();


    private void initList() {
        simList.add(createSim("simulation", new COBA()));
        simList.add(createSim("simulation", new COBA()));
    }

    private HashMap<String, Simulation> createSim(String key, Simulation sim) {
        HashMap<String, Simulation> simulation = new HashMap<String, Simulation>();
        simulation.put(key, sim);
        return simulation;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_briandroid_main);

        initList();

        ListView lv = (ListView) findViewById(android.R.id.list);


        // This is a simple adapter that accepts as parameter
        // Context
        // Data list
        // The row layout that is used during the row creation
        // The keys used to retrieve the data
        // The View id used to show the data. The key number and the view id must match
        // TODO: Replace with ArrayAdapter http://developer.android.com/reference/android/widget/ArrayAdapter.html
        SimpleAdapter simpleAdpt = new SimpleAdapter(this, simList, android.R.layout.simple_list_item_1, new String[] {"simulation"},
                new int[] {android.R.id.text1});

        lv.setAdapter(simpleAdpt);


        // React to user clicks on item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                    long id) {
                startSimulationActivity(id);
            }
        });

    }

    public void startSimulationActivity(int loc) {
        // get simulation object from HashMap
        Simulation selectedSim = simList.get(loc).get("COBA");
        // Send simulation object to the detail activity
        Intent simulationIntent = new Intent(this, SimulationDetailActivity.class);
    }


}
