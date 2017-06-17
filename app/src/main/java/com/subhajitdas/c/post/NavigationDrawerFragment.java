package com.subhajitdas.c.post;


import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.subhajitdas.c.R;


public class NavigationDrawerFragment extends Fragment {

    private Button mBookmarkButton;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBookmarkButton= (Button) getActivity().findViewById(R.id.bookmark_button);
    }

    @Override
    public void onResume() {
        super.onResume();

        mBookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().findViewById(R.id.main_container) != null) {
                    BookmarkFragment2 bookmarkFragment = new BookmarkFragment2();
                    getActivity()
                            .getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.main_container, bookmarkFragment)
                            .addToBackStack(null)
                            .commit();

                    DrawerLayout mDrawerLayout;
                    mDrawerLayout = (DrawerLayout)getActivity().findViewById(R.id.drawer);
                    mDrawerLayout.closeDrawers();
                }
            }
        });
    }
}
