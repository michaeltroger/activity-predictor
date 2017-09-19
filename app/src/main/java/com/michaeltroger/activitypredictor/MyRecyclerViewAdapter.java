package com.michaeltroger.activitypredictor;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.michaeltroger.activitypredictor.databinding.RecyclerviewRowBinding;

import java.util.Collections;
import java.util.List;


public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private List<String> mData = Collections.emptyList();

    // data is passed into the constructor
    public MyRecyclerViewAdapter(final List<String> data) {
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerviewRowBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.recyclerview_row, parent, false);
        return new MyViewHolder(viewDataBinding);
    }

    // binds the data to the textview in each row
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final String text = mData.get(position);
        holder.binding.tvActivityName.setText(text);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerviewRowBinding binding;

        public MyViewHolder(RecyclerviewRowBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

    }

}
