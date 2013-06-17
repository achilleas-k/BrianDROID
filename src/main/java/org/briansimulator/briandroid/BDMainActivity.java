package org.briansimulator.briandroid;

import android.app.ListActivity;
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

    SimpleCursorAdapter mAdapter;

    public static List<Map<String, Simulation>> simList = new ArrayList<Map<String, Simulation>>();
    public static Map<String, Simulation> SIMS_MAP = new HashMap<String, Simulation>();


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
        SimpleAdapter simpleAdpt = new SimpleAdapter(this, simList, android.R.layout.simple_list_item_1, new String[] {"simulation"},
                new int[] {android.R.id.text1});

        lv.setAdapter(simpleAdpt);


        // React to user clicks on item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
                                    long id) {

                // We know the View is a TextView so we can cast it
                TextView clickedView = (TextView) view;
                // Send simulation object to the detail activity
                Toast.makeText(BDMainActivity.this, "Item with id [" + id + "] - Position [" + position + "] - Planet [" + clickedView.getText() + "]",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

       // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

}
