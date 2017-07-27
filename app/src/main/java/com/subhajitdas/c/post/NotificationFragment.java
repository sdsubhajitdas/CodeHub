package com.subhajitdas.c.post;


import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.subhajitdas.c.R;

public class NotificationFragment extends Fragment {

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_notifications);
    }
}
