package org.briansimulator.briandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity
        implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;


    public static List<SimItem> SIMS = new ArrayList<SimItem>();
    public static Map<String, SimItem> SIM_MAP = new HashMap<String, SimItem>();

    public static class SimItem {
        public String id;
        public String content;

        public SimItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }

    private static void buildClassList(String directory) {
        // TODO: Filesystem checks (directory exists, is readable, etc)
        SIMS.clear();
        File classDir = new File(directory);
        try {
            URL[] fileURL = new URL[] {classDir.toURI().toURL()};
            ClassLoader loader = new URLClassLoader(fileURL);
            File[] dirListing = classDir.listFiles();
            int fidx = 0;
            for (File file : dirListing) {
                String filename = file.getName();
                if (filename.endsWith(".class")) { // TODO: Use a smarter check for class
                    SIMS.add(new SimItem(""+fidx, filename.replace(".class", "")));
                    fidx++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            Log.d("==============================", "TWO PANE IS TRUE!!!!\n\n\n\n\n");

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        } else {
            Log.d("==============================", "\n\n\n\n\nTWO PANE IS FALSE!!!!\n\n\n\n\n");
        }

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File storageRoot = Environment.getExternalStorageDirectory();
            String classDirectory = storageRoot.getAbsolutePath()+"/BrianDROIDsims/";
            buildClassList(classDirectory);
        } else {
            // TODO: popup error - storage not present or readable
        }

    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_SIM_ID, id);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_SIM_ID, id);
            startActivity(detailIntent);
        }
    }
}
