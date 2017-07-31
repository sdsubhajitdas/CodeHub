package com.subhajitdas.c.post;


import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.subhajitdas.c.Constants;
import com.subhajitdas.c.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationFragment extends Fragment {

    private TextView mEmpty;
    private RecyclerView mNotiRecyclerView;
    private Button mReadAll;
    private List<Map<String, String>> mDataSet;
    private ArrayList<String> mNotiKey;
    private DatabaseReference mNotiRef;
    private NotiDataAdapter mAdapter;
    private ChildEventListener mNotiListener;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private Boolean notiShown = false;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataSet = new ArrayList<Map<String, String>>();
        mNotiKey = new ArrayList<>();
        mNotiRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.NOTI)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mNotiRef.keepSynced(true);


        mNotiListener = mNotiRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (mDataSet.isEmpty()) {
                    mNotiRecyclerView.setVisibility(View.VISIBLE);
                    mEmpty.setVisibility(View.INVISIBLE);
                }
                mNotiKey.add(dataSnapshot.getKey());
                Map<String, String> block = new HashMap<String, String>();
                block.put(Constants.NOTI_TEXT, dataSnapshot.child(Constants.NOTI_TEXT).getValue().toString());
                block.put(Constants.NOTI_TYPE, dataSnapshot.child(Constants.NOTI_TYPE).getValue().toString());
                block.put(Constants.NOTI_POST_KEY, dataSnapshot.child(Constants.NOTI_POST_KEY).getValue().toString());
                block.put(Constants.NOTI_READ, dataSnapshot.child(Constants.NOTI_READ).getValue().toString());

                mDataSet.add(block);
                mAdapter.notifyItemInserted(mDataSet.size() - 1);
                mNotiRecyclerView.smoothScrollToPosition(mDataSet.size() - 1);
                checkForNewNoti();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int index = -1;
                index = mNotiKey.indexOf(dataSnapshot.getKey());
                if (index != -1) {
                    Map<String, String> block = new HashMap<String, String>();
                    block.put(Constants.NOTI_TEXT, dataSnapshot.child(Constants.NOTI_TEXT).getValue().toString());
                    block.put(Constants.NOTI_TYPE, dataSnapshot.child(Constants.NOTI_TYPE).getValue().toString());
                    block.put(Constants.NOTI_POST_KEY, dataSnapshot.child(Constants.NOTI_POST_KEY).getValue().toString());
                    block.put(Constants.NOTI_READ, dataSnapshot.child(Constants.NOTI_READ).getValue().toString());
                    mDataSet.remove(index);
                    mDataSet.add(index, block);
                    mAdapter.notifyItemChanged(index);
                    checkIfShowNoti();
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mNotiRecyclerView = (RecyclerView) getActivity().findViewById(R.id.noti_recycler_view);
        mReadAll = (Button) getActivity().findViewById(R.id.noti_read_all);
        mEmpty = (TextView) getActivity().findViewById(R.id.no_noti);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        mNotiRecyclerView.setLayoutManager(manager);
        mAdapter = new NotiDataAdapter(mDataSet);
        mNotiRecyclerView.setAdapter(mAdapter);
        mAdapter.updateKeySet(mNotiKey);
        mNotiRecyclerView.setVisibility(View.INVISIBLE);

        //Notification items
        mBuilder = new NotificationCompat.Builder(getActivity());
        mNotifyMgr = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

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
        mReadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < mNotiKey.size(); i++) {
                    mNotiRef.child(mNotiKey.get(i))
                            .child(Constants.NOTI_READ)
                            .setValue("true");
                }
            }
        });
    }

    public void checkForNewNoti() {
        boolean unread = false;
        for (int i = mDataSet.size() - 1; i >= 0; i--) {
            if (mDataSet.get(i).get(Constants.NOTI_READ).equals("false")) {
                unread = true;
                break;
            }
        }

        if (unread) {
            if (!notiShown) {
                if (mBuilder != null && mNotifyMgr != null) {
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("CodeHub Notifications")
                            .setContentText("You have some pending notifications")
                            .setSound(uri);

                    int notiId = 001;

                    mNotifyMgr.notify(notiId, mBuilder.build());
                    notiShown = true;
                }
            }
        }
    }

    public void checkIfShowNoti() {
        boolean flag = false;
        for (int i = mDataSet.size() - 1; i >= 0; i--) {
            if (mDataSet.get(i).get(Constants.NOTI_READ).equals("false")) {
                flag = false;
            }
        }

        if (flag == false) {
            notiShown = false;
        }
    }


    private class NotiDataAdapter extends RecyclerView.Adapter<NotiDataAdapter.ViewHolder> {

        List<Map<String, String>> mDataset;
        ArrayList<String> mKeySet;

        public NotiDataAdapter(List<Map<String, String>> dataset) {
            mDataset = dataset;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //A new view is created.
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.noti_layout, parent, false);

            //Returning the new created view.
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.notiText.setText(mDataset.get(position).get(Constants.NOTI_TEXT));
            String state = mDataset.get(position).get(Constants.NOTI_READ);
            if (state.equals("false")) {
                //Toast.makeText(holder.context,"You might have some new notifications",Toast.LENGTH_LONG).show();
                holder.readBox.setChecked(false);
                ColorDrawable colorDrawable = new ColorDrawable(holder.context.getResources().getColor(R.color.notiBackGroundFalse));
                holder.background.setBackground(colorDrawable);
            } else {
                holder.readBox.setChecked(true);
                ColorDrawable colorDrawable = new ColorDrawable(holder.context.getResources().getColor(R.color.notiBackGroundTrue));
                holder.background.setBackground(colorDrawable);
            }

            holder.readBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == true) {

                        Map tempUpload = mDataset.get(position);
                        tempUpload.remove(Constants.NOTI_READ);
                        tempUpload.put(Constants.NOTI_READ, "true");
                        FirebaseDatabase.getInstance().getReference()
                                .child(Constants.NOTI)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(mNotiKey.get(position))
                                .setValue(tempUpload);
                    } else if (isChecked == false) {
                        Map tempUpload = mDataset.get(position);
                        tempUpload.remove(Constants.NOTI_READ);
                        tempUpload.put(Constants.NOTI_READ, "false");
                        FirebaseDatabase.getInstance().getReference()
                                .child(Constants.NOTI)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(mNotiKey.get(position))
                                .setValue(tempUpload);
                    }
                }
            });

            holder.notiText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map tempUpload = mDataset.get(position);
                    tempUpload.remove(Constants.NOTI_READ);
                    tempUpload.put(Constants.NOTI_READ, "true");
                    FirebaseDatabase.getInstance().getReference()
                            .child(Constants.NOTI)
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(mNotiKey.get(position))
                            .setValue(tempUpload);
                    NotiOpenInterface openInterface = (NotiOpenInterface) getActivity();
                    openInterface.sendPostKey(mDataset.get(position).get(Constants.NOTI_POST_KEY));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public void updateKeySet(ArrayList<String> dataset) {
            mKeySet = dataset;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView notiText;
            CheckBox readBox;
            ConstraintLayout background;
            Context context;

            ViewHolder(View v) {
                super(v);
                notiText = (TextView) v.findViewById(R.id.noti_text);
                readBox = (CheckBox) v.findViewById(R.id.read_box);
                background = (ConstraintLayout) v.findViewById(R.id.noti_back);
                context = v.getContext();
            }
        }

    }
}
