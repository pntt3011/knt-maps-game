package com.example.maplogin.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.maplogin.R;
import com.example.maplogin.databinding.FragmentHistoryBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;

public class HistoryFragment extends Fragment {
    public HistoryViewModel viewModel;

    private TextView year;
    private TextView totalCheckedIn;
    private TextView totalBadges;
    private TextView totalPoints;
    private BarChart barChart;
    ArrayList<BarEntry> barEntryArrayList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        com.example.maplogin.databinding.FragmentHistoryBinding binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View historyView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(historyView, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        ImageButton back = historyView.findViewById(R.id.button_back);
        ImageButton next = historyView.findViewById(R.id.button_next);
        year = historyView.findViewById(R.id.text_date);
        barChart = historyView.findViewById(R.id.chart);
        totalCheckedIn = historyView.findViewById(R.id.total_checked_text);
        totalPoints = historyView.findViewById(R.id.total_points_text);
        totalBadges = historyView.findViewById(R.id.total_badges_text);

        init();
        showCurrentInfo();

        back.setOnClickListener(view -> {
            viewModel.changeBack();
            viewModel.initHistory();
        });

        next.setOnClickListener(view -> {
            viewModel.changeForward();
            viewModel.initHistory();
        });

        viewModel.getTotalCheckedIn().observe(getViewLifecycleOwner(), n ->
                totalCheckedIn.setText(String.valueOf(n)));

        viewModel.getTotalPoints().observe(getViewLifecycleOwner(), n ->
                totalPoints.setText(String.valueOf(n)));

        viewModel.getTotalBadges().observe(getViewLifecycleOwner(), n ->
                totalBadges.setText(String.valueOf(n)));

        viewModel.getCurrentYear().observe(getViewLifecycleOwner(), s ->
                year.setText(String.valueOf(s)));

        viewModel.getCheckedInHistory().observe(getViewLifecycleOwner(), this::drawChart);
    }

    private void drawChart(ArrayList<Long> data) {
        barEntryArrayList = new ArrayList<>();
        barEntryArrayList.clear();
        ArrayList<String> labelName;
        labelName = new ArrayList<>();

        for (int i = 0; i < data.size(); ++i) {
            barEntryArrayList.add(new BarEntry(i, data.get(i)));
            labelName.add(String.valueOf(i + 1));
        }
        BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Total Checked in");
        barDataSet.setColors(getContext().getColor(R.color.green_bold));
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
        viewModel.countTotalPoint();
        viewModel.countTotalBadges();
    }
}
