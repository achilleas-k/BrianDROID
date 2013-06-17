package org.briansimulator.briandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link BDMainActivity}
 * in two-pane mode (on tablets) or a {@link SimulationDetailActivity}
 * on handsets.
 */
public class SimulationDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The Simulations content this fragment is presenting.
     */
    private BDMainActivity.SimItem simItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SimulationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the Simulations content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            simItem = BDMainActivity.SIMS_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simulation_detail, container, false);
        if (simItem != null) {
            TextView statusView = (TextView) rootView.findViewById(R.id.item_detail);
            if (simItem.id.equals("COBA")) {
                simItem.run(statusView);
            }

        }


        return rootView;
    }


}
