package com.example.sunlightalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RGBAdapter extends RecyclerView.Adapter<RGBAdapter.ViewHolder> {
    private final Context context;
    private final int itemCount;
    private final List<RGBValue> rgbValues;

    public RGBAdapter(Context context, int itemCount) {
        this.context = context;
        this.itemCount = itemCount;
        this.rgbValues = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            rgbValues.add(new RGBValue(0, 0, 0));
        }
    }

    static class RGBValue {
        int r, g, b;
        RGBValue(int r, int g, int b) { this.r = r; this.g = g; this.b = b; }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.card_time_rgb, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.minuteText.setText("Minute " + (position + 1));

        holder.red.addTextChangedListener(new SimpleWatcher(s -> {
            rgbValues.get(position).r = parseIntSafe(s);
        }));
        holder.green.addTextChangedListener(new SimpleWatcher(s -> {
            rgbValues.get(position).g = parseIntSafe(s);
        }));
        holder.blue.addTextChangedListener(new SimpleWatcher(s -> {
            rgbValues.get(position).b = parseIntSafe(s);
        }));

    }

    public List<RGBValue> getRgbValues() {
        return rgbValues;
    }

    private int parseIntSafe(CharSequence s) {
        try { return Integer.parseInt(s.toString()); }
        catch (NumberFormatException e) { return 0; }
    }


    @Override
    public int getItemCount() {
        return itemCount;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView minuteText;
        EditText red, green, blue;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            minuteText = itemView.findViewById(R.id.minute);
            red = itemView.findViewById(R.id.red);
            green = itemView.findViewById(R.id.green);
            blue = itemView.findViewById(R.id.blue);
        }
    }

    private static class SimpleWatcher implements android.text.TextWatcher {
        private final java.util.function.Consumer<CharSequence> consumer;
        SimpleWatcher(java.util.function.Consumer<CharSequence> consumer) {
            this.consumer = consumer;
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            consumer.accept(s);
        }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

}
