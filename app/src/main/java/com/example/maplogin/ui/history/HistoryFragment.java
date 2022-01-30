package com.example.maplogin.ui.history;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentHistoryBinding;
import com.example.maplogin.models.User;
import com.example.maplogin.struct.LocationInfo;
import com.example.maplogin.ui.follow.FollowViewModel;
import com.example.maplogin.utils.CheckinRecyclerAdapter;
import com.example.maplogin.utils.DatabaseAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    public HistoryViewModel viewModel;
    private DatabaseAdapter database;
    private Activity activity;

    private ImageButton back;
    private ImageButton next;
    private TextView monthYear;
    private TextView totalCheckedIn;
    private TextView totalBadges;
    private TextView totalPoints;
    private BarChart barChart;
    ArrayList<BarEntry> barEntryArrayList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View historyView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(historyView, savedInstanceState);
        database = DatabaseAdapter.getInstance();
        activity = getActivity();
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        back = historyView.findViewById(R.id.button_back);
        next = historyView.findViewById(R.id.button_next);
        monthYear = historyView.findViewById(R.id.text_date);
        barChart = historyView.findViewById(R.id.chart);
        totalCheckedIn = historyView.findViewById(R.id.total_checked_text);
        totalPoints = historyView.findViewById(R.id.total_points_text);
        totalBadges = historyView.findViewById(R.id.total_badges_text);

        init();
        showCurrentInfo();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.changeBack();
                viewModel.initHistory();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.changeForward();
                viewModel.initHistory();
            }
        });

        viewModel.getTotalCheckedIn().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                totalCheckedIn.setText(String.valueOf(integer));
            }
        });

        viewModel.getMonthYear().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                monthYear.setText(s);
            }
        });

        viewModel.getCheckedInHistory().observe(getViewLifecycleOwner(), new Observer<ArrayList<Integer>>() {
            @Override
            public void onChanged(ArrayList<Integer> integers) {
                drawChart(integers);
            }
        });
    }

    private void drawChart(ArrayList<Integer> integers) {
        barEntryArrayList = new ArrayList<>();
        barEntryArrayList.clear();
        ArrayList<String> labelName;
        labelName = new ArrayList<>();

        for (int i = 0; i < integers.size(); ++i) {
            barEntryArrayList.add(new BarEntry(i, integers.get(i)));
            labelName.add(String.valueOf(i+1));
        }
        BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Total Checked in");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(barDataSet);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelName));

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextSize(9f);
        xAxis.setLabelCount(labelName.size());


        barChart.setBackgroundColor(Color.WHITE);
        barChart.setData(barData);
        barChart.invalidate();
    }


    private void init() {
        viewModel.initHistory();
    }

    private void showCurrentInfo() {
        viewModel.countTotalCheckedIn();
    }
}
