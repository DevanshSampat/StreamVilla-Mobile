package com.devansh.entertainment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class UserDataAdapter extends RecyclerView.Adapter<UserDataAdapter.ViewHolder> {

    private ArrayList<DataSnapshot> dataSnapshotArrayList;

    public UserDataAdapter(ArrayList<DataSnapshot> dataSnapshotArrayList) {
        this.dataSnapshotArrayList = dataSnapshotArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_data_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserDataAdapter.ViewHolder holder, int position) {
        DataSnapshot dataSnapshot = dataSnapshotArrayList.get(position);
        ((TextView)holder.itemView.findViewById(R.id.title)).setText(dataSnapshot.getKey());
        ((TextView)holder.itemView.findViewById(R.id.data)).setText(dataSnapshot.getValue().toString());
    }

    @Override
    public int getItemCount() {
        return dataSnapshotArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
