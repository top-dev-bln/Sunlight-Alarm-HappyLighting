package com.example.sunlightalarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Protocol_create extends AppCompatActivity {
    private RGBAdapter adapter;
    private int hour;
    private int minute;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol_create);
        LinearLayout DurationSelect = findViewById(R.id.duration_select);
        RecyclerView RGBRecycler = findViewById(R.id.RGB_recycler);
        Button buttonSave = findViewById(R.id.buttonSave);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RGBRecycler.setLayoutManager(layoutManager);

        TextView DurationText = findViewById(R.id.duration_text);
        DurationSelect.setOnClickListener(v ->
                showWheelTimePicker("Select Protocol Duration", (hour, minute) -> {
                    DurationText.setText(
                            getString(R.string.time_format, hour, minute)
                    );
                    this.hour = hour;
                    this.minute = minute;

                    RGBRecycler.setLayoutManager(new LinearLayoutManager(this));
                    adapter = new RGBAdapter(this, hour*60+minute);
                    RGBRecycler.setAdapter(adapter);

                })
        );


        buttonSave.setOnClickListener(v -> {
            EditText protocolNameInput = findViewById(R.id.protocolNameInput);
            String protocolName = protocolNameInput.getText().toString().trim();

            if (protocolName.isEmpty()) {
                Toast.makeText(this, "Please enter a protocol name", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SharedPreferences prefs = getSharedPreferences("Protocols", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                JSONObject protocolJson = new JSONObject();
                protocolJson.put("name", protocolName);
                protocolJson.put("duration", hour * 60 + minute);

                JSONArray rgbArray = new JSONArray();
                for (RGBAdapter.RGBValue val : adapter.getRgbValues()) {
                    JSONObject rgb = new JSONObject();
                    rgb.put("r", val.r);
                    rgb.put("g", val.g);
                    rgb.put("b", val.b);
                    rgbArray.put(rgb);
                }

                protocolJson.put("rgbValues", rgbArray);
                editor.putString(protocolName, protocolJson.toString());
                editor.apply();

                Toast.makeText(this, "Protocol saved", Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save protocol", Toast.LENGTH_SHORT).show();
            }

            startActivity(new Intent(this, Protocols.class));
        });




    }

    private void applyBlurBehindDialog(View rootView, boolean enable) {
        if (enable) {
            RenderEffect blurEffect = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurEffect = RenderEffect.createBlurEffect(50f, 50f, Shader.TileMode.CLAMP);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rootView.setRenderEffect(blurEffect);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rootView.setRenderEffect(null);
            }
        }
    }

    private void updateHourScale(RecyclerView recyclerView, LinearSnapHelper snapHelper) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null || snapHelper == null) return;

        View snapView = snapHelper.findSnapView(layoutManager);
        int snapPos = snapView != null ? layoutManager.getPosition(snapView) : -1;

        WheelAdapter adapter = (WheelAdapter) recyclerView.getAdapter();
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            WheelAdapter.WheelViewHolder holder = (WheelAdapter.WheelViewHolder) recyclerView.getChildViewHolder(child);

            if (layoutManager.getPosition(child) == snapPos) {
                adapter.setItemScale(holder, 1.0f);
                holder.textView.setTextColor(Color.parseColor("#2196F3")); // blue for selected
            } else {
                adapter.setItemScale(holder, 0.8f);
                holder.textView.setTextColor(Color.WHITE);
            }
        }
    }


    private void showWheelTimePicker(String title, Controller.OnTimeSelectedListener listener) {
        View view = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);

        TextView pickerTitle = view.findViewById(R.id.picker_title);
        pickerTitle.setText(title);

        List<String> hours = new ArrayList<>();
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 24; i++) hours.add(String.format("%02d", i));
        for (int i = 0; i < 60; i++) minutes.add(String.format("%02d", i));


        List<String> circularHours = new ArrayList<>();
        List<String> circularMinutes = new ArrayList<>();
        int repeat = 100;
        for (int i = 0; i < repeat; i++) {
            circularHours.addAll(hours);
            circularMinutes.addAll(minutes);
        }

        RecyclerView hourRecycler = view.findViewById(R.id.hour_picker_recycler);
        RecyclerView minuteRecycler = view.findViewById(R.id.minute_picker_recycler);


        WheelAdapter hourAdapter = new WheelAdapter(this, circularHours);
        WheelAdapter minuteAdapter = new WheelAdapter(this, circularMinutes);

        LinearLayoutManager hourLayout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        LinearLayoutManager minuteLayout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);


        hourRecycler.setLayoutManager(hourLayout);
        hourRecycler.setAdapter(hourAdapter);
        hourRecycler.setHasFixedSize(true);
        hourRecycler.setNestedScrollingEnabled(false);

        minuteRecycler.setLayoutManager(minuteLayout);
        minuteRecycler.setAdapter(minuteAdapter);
        minuteRecycler.setHasFixedSize(true);
        minuteRecycler.setNestedScrollingEnabled(false);


        LinearSnapHelper hourSnap = new LinearSnapHelper();
        hourSnap.attachToRecyclerView(hourRecycler);




        LinearSnapHelper minuteSnap = new LinearSnapHelper();
        minuteSnap.attachToRecyclerView(minuteRecycler);

        int middleHour = circularHours.size() / 2;
        int middleMinute = circularMinutes.size() / 2;
        minuteRecycler.scrollToPosition(middleMinute);
        hourRecycler.scrollToPosition(middleHour);
        hourRecycler.post(() -> {
            updateHourScale(hourRecycler, hourSnap);
        });
        minuteRecycler.post(() -> {
            updateHourScale(minuteRecycler, minuteSnap);
        });







        RecyclerView.OnScrollListener hourScaleListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null ) return;
                int recyclerCenterY = recyclerView.getHeight() / 2;
                WheelAdapter adapter = (WheelAdapter) recyclerView.getAdapter();

                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    float childCenterY = (child.getTop() + child.getBottom()) / 2f;
                    float distance = Math.abs(recyclerCenterY - childCenterY);
                    float scale = 1f - Math.min(distance / recyclerCenterY, 1f);

                    if (recyclerView.getChildViewHolder(child) instanceof WheelAdapter.WheelViewHolder && adapter != null) {
                        WheelAdapter.WheelViewHolder holder = (WheelAdapter.WheelViewHolder) recyclerView.getChildViewHolder(child);
                        adapter.setItemScale(holder, scale);
                        if (scale >= 0.85f) {
                            holder.textView.setTextColor(Color.parseColor("#2196F3"));
                        } else {
                            holder.textView.setTextColor(Color.argb(255, 255, 255, 255));
                        }
                    }
                }
            }
        };


        RecyclerView.OnScrollListener minuteScaleListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                int recyclerCenterY = recyclerView.getHeight() / 2;
                WheelAdapter adapter = (WheelAdapter) recyclerView.getAdapter();
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    float childCenterY = (child.getTop() + child.getBottom()) / 2f;
                    float distance = Math.abs(recyclerCenterY - childCenterY);
                    float scale = 1f - Math.min(distance / recyclerCenterY, 1f);
                    if (recyclerView.getChildViewHolder(child) instanceof WheelAdapter.WheelViewHolder && adapter != null) {
                        WheelAdapter.WheelViewHolder holder = (WheelAdapter.WheelViewHolder) recyclerView.getChildViewHolder(child);
                        assert adapter != null;
                        adapter.setItemScale(holder, scale);
                        if (scale >= 0.85f) {
                            holder.textView.setTextColor(Color.parseColor("#2196F3"));
                        } else {
                            holder.textView.setTextColor(Color.argb(255, 255, 255, 255));
                        }
                    }
                }
            }
        };

        hourRecycler.addOnScrollListener(hourScaleListener);
        minuteRecycler.addOnScrollListener(minuteScaleListener);

        BottomSheetDialog dialog = getBottomSheetDialog(view);

        Button confirm = view.findViewById(R.id.btn_ok);
        Button cancel = view.findViewById(R.id.btn_cancel);

        cancel.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            int selectedHour = 0;
            int selectedMinute = 0;

            View centerHour = hourSnap.findSnapView(hourLayout);
            if (centerHour != null) {
                selectedHour = hourLayout.getPosition(centerHour) % 24;
            }

            View centerMinute = minuteSnap.findSnapView(minuteLayout);
            if (centerMinute != null) {
                selectedMinute = minuteLayout.getPosition(centerMinute) % 60;
            }

            listener.onTimeSelected(selectedHour, selectedMinute);
            dialog.dismiss();
        });


        dialog.show();
    }

    @NonNull
    private BottomSheetDialog getBottomSheetDialog(View view) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        dialog.setContentView(view);

        dialog.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight((int) (screenHeight * 0.66f), true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(true);
            } else {
                dialog.getWindow().setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.66f)
                );
            }

            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                applyBlurBehindDialog(rootView, true);
            }
        });

        dialog.setCancelable(false);



        dialog.setOnDismissListener(d -> {
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                applyBlurBehindDialog(rootView, false);
            }
        });
        return dialog;
    }


}


