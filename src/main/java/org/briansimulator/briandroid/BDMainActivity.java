package org.briansimulator.briandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import org.briansimulator.briandroid.Simulations.COBA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SimulationActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SimulationListFragment} and the item details
 * (if present) is a {@link SimulationFragment}.
 * <p>
 * This activity also implements the required
 * {@link SimulationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class BDMainActivity extends FragmentActivity
        implements SimulationListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;


    public static List<SimItem> SIMS = new ArrayList<SimItem>();
    public static Map<String, SimItem> SIMS_MAP = new HashMap<String, SimItem>();


    public static class SimItem {
        public String id;
        public String description;
        COBA simulation;

        public SimItem(String id, String description) {
            this.id = id;
            this.description = description;
            this.simulation = new COBA();
            this.simulation.setup();
        }

        @Override
        public String toString() {
            return this.description;
        }

        public void run(TextView tv) {
            this.simulation.setProgressView(tv);
            if (this.simulation.getState() == 0) {
                this.simulation.execute();
            } else if (this.simulation.getState() == 2) {

            }
        }
    }

    private static void addItem(SimItem item) {
        SIMS.add(item);
        SIMS_MAP.put(item.id, item);
    }

    private static void buildClassList(String directory) {
        // TODO: Build class list based on directory contents
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((SimulationListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        String classDirectory = "";
        buildClassList(classDirectory);
        SIMS.clear();

        addItem(new SimItem("COBA", "COBA simulation"));
    }

    /**
     * Callback method from {@link SimulationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SimulationFragment.ARG_ITEM_ID, id);
            SimulationFragment fragment = new SimulationFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, SimulationActivity.class);
            detailIntent.putExtra(SimulationFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
