package com.example.twelker.reminderdemo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private static final String PARAMETER_INDEX = "Reminder index";

    public DetailFragment() {
        // Required empty public constructor
    }

    public static DetailFragment newInstance (long index) {
        final Bundle args = new Bundle();
        args.putLong(PARAMETER_INDEX, index);
        final DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and return the view
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {

            //Obtain the parameters
            long index = bundle.getLong(PARAMETER_INDEX);

            //Fill textview with the input parameter
            //Note that you need to add "getView()" here
            final TextView reminderIDView = (TextView) getView().findViewById(R.id.reminderID);
            reminderIDView.setText("_id = " + Long.toString(index));
        }
    }
}
