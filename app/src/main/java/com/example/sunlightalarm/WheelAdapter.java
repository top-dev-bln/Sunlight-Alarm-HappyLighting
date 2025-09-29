package com.example.sunlightalarm;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


import androidx.annotation.NonNull;

public class WheelAdapter extends RecyclerView.Adapter<WheelAdapter.WheelViewHolder> {

    private List<String> items;
    private Context context;

    public WheelAdapter(Context context, List<String> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public WheelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setGravity(Gravity.CENTER);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(lp);
        return new WheelViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull WheelViewHolder holder, int position) {
        holder.textView.setText(items.get(position));
        holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // default size
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Dynamically set text size based on scale (0.0 to 1.0)
    public void setItemScale(WheelViewHolder holder, float scale) {
        float minSize = 18f;
        float maxSize = 48f;
        float textSize = minSize + (maxSize - minSize) * scale;
        holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    public static class WheelViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public WheelViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
