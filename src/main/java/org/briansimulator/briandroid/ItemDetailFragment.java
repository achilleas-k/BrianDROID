package org.briansimulator.briandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.briansimulator.briandroid.Simulations.SimulationContent;
import org.briansimulator.briandroid.Simulations.COBA;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String LOGID = "org.briansimulator.briandroid.ItemDetailActivity";

    /**
     * The Simulations content this fragment is presenting.
     */
    private SimulationContent.SimItem sItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the Simulations content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            sItem = SimulationContent.SIMS_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        // Show the Simulations content as text in a TextView.
        if (sItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(sItem.description);
            Log.d(LOGID, "STARTING SIMULATION HERE!   "+sItem.description+"\n\n\n");
            if (sItem.id.equals("COBA")) {
                COBA thesim = new COBA();
                thesim.run();
            }

        }


        return rootView;
    }


}
