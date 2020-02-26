package com.demotxt.droidsrce.homedashboard.display;

import android.content.Context;
import android.content.Intent;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.demotxt.droidsrce.homedashboard.R;
import com.demotxt.droidsrce.homedashboard.Utils.Constants;

import java.io.File;
import java.util.List;

public class ListViewTripDisplayAdapter extends RecyclerView.Adapter<ListViewTripDisplayAdapter.ItemViewHolder> {
    private List<File> files;
    private Context _context;

    public ListViewTripDisplayAdapter(Context ctx, List<File> files) {
        this._context = ctx;
        this.files = files;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_row_container, parent, false);

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final File file = files.get(position);
        holder.name.setText(file.getName());
        holder.dataSize.setText(convertToKB(file.length()));
        holder.click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(holder.click);
                Intent intent = new Intent(_context, TripViewer.class);
                intent.putExtra(Constants.CHECKOUT_TRIP, files.get(holder.getAdapterPosition()).getAbsolutePath());
                _context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    private String convertToKB(long size) {
        return size / 1024 + "Kb";
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView name, dataSize;
        private RelativeLayout click;

        public ItemViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            dataSize = view.findViewById(R.id.dataSize);
            click = view.findViewById(R.id.click);
        }
    }
}