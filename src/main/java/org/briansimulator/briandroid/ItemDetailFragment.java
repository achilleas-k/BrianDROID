package org.briansimulator.briandroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.briansimulator.briandroid.Simulations.SimRunner;

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
    public static final String ARG_SIM_ID = "sim_id";

    /**
     * The Simulations name this fragment is presenting.
     */
    private ItemListActivity.SimItem sItem;

    /**
     * The selected simulation
     */
    private SimRunner selectedSim;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_SIM_ID)) {
            sItem = ItemListActivity.SIM_MAP.get(getArguments().getString(ARG_SIM_ID));
           // int duration = Toast.LENGTH_LONG;
           // Activity context = getActivity();
           // Toast toast = Toast.makeText(context, sItem.content, duration);
           // toast.show();
            // TODO: save selectedSim instance and reload it to avoid copying dex on orientation change
            if (selectedSim == null)
                selectedSim = new SimRunner(getActivity(), sItem.abspath);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        if (sItem != null) {
            ((TextView) rootView.findViewById(R.id.statusText)).setText(selectedSim.getDescription());
        } else {
         //   statusView.setText("ERROR while loading simulation.");
        }


        return rootView;
    }

}
