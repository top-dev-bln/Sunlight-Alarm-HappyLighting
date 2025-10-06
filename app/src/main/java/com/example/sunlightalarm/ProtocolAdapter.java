package com.example.sunlightalarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProtocolAdapter extends RecyclerView.Adapter<ProtocolAdapter.ViewHolder> {
    private final Context context;
    private final List<Protocol> protocols;
    private final View.OnClickListener listener;

    public ProtocolAdapter(Context context, List<Protocol> protocols, View.OnClickListener listener) {
        this.context = context;
        this.protocols = protocols;
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_protocol, parent, false);
        view.setOnClickListener(listener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        Protocol p = protocols.get(position);
        holder.name.setText(p.name);


        int h = p.durationMinutes / 60;
        int m = p.durationMinutes % 60;
        holder.duration.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        holder.itemView.setTag(p);




        holder.editIcon.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, Protocol_create.class);
            intent.putExtra("PROTOCOL_NAME", p.name);
            context.startActivity(intent);
        });


        holder.deleteIcon.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("Protocols", Context.MODE_PRIVATE);
            prefs.edit().remove(p.name).apply();

            Intent intent = new Intent(context, Controller.class);
            context.startActivity(intent);


            protocols.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, protocols.size());
        });


    }

    @Override
    public int getItemCount() {
        return protocols.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, duration;
        ImageView editIcon, deleteIcon;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.protocol_name);
            duration = itemView.findViewById(R.id.protocol_duration);
            editIcon = itemView.findViewById(R.id.edit_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);

        }
    }
}
