package com.demotxt.droidsrce.homedashboard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demotxt.droidsrce.homedashboard.R;
import com.demotxt.droidsrce.homedashboard.io.CustomMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;

public class ChartTripFragment extends Fragment implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {
    private LineChart chart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;
    private ArrayList<Entry> acceleration = new ArrayList<>();
    private ArrayList<Entry> timestamp = new ArrayList<>();
    private ArrayList<Entry> load = new ArrayList<>();
    private ArrayList<Entry> fuelEconomyCoef = new ArrayList<>();
    private String filePath = null;

    public ChartTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    private List<DataEntry> ReadCSV(String path) throws FileNotFoundException {
//        File file = new File(path);
//        Scanner fileReader = new Scanner(file);
//        List<DataEntry> customDataEntries = new ArrayList<>();
//
//        fileReader.nextLine();
//
//        String prevLine = null;
//        String currentLine;
//        String[] currLineArray;
//        String[] prevLineArray;
//        while (fileReader.hasNextLine()) {
//            currentLine = fileReader.nextLine();
//            if (prevLine != null) {
//                currLineArray = currentLine.split(",");
//                prevLineArray = prevLine.split(",");
//                if (!currLineArray[0].equals("N/A") && !prevLineArray.equals("N/A")) {
//                    customDataEntries.add(new CustomDataEntry(currLineArray[9],
//                            Double.parseDouble(currLineArray[1]) - Double.parseDouble(prevLineArray[1]),
//                            Double.parseDouble(currLineArray[3]) / 10,
//                            (Double.parseDouble(currLineArray[1]) - Double.parseDouble(prevLineArray[1])) *
//                                    Double.parseDouble(currLineArray[3])
//                    ));
//                }
//            }
//
//            prevLine = currentLine;
//        }
//        return customDataEntries;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_chart_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            filePath = bundle.getString("file_name", "");
        }

        tvX = view.findViewById(R.id.tvXMax);
        tvY = view.findViewById(R.id.tvYMax);

        seekBarX = view.findViewById(R.id.seekBar1);
        seekBarX.setOnSeekBarChangeListener(this);

        seekBarY = view.findViewById(R.id.seekBar2);
        seekBarY.setOnSeekBarChangeListener(this);

        chart = view.findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);

        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE);

        try {
            ReadCSV(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // add data
        seekBarX.setMax(timestamp.size() - 1);
        seekBarX.setProgress(20);
        seekBarY.setMax(1000);
        seekBarY.setProgress(500);

        chart.animateX(1500);

        CustomMarkerView customMarkerView = new CustomMarkerView(getActivity(), R.layout.custom_marker_view);
        customMarkerView.setChartView(chart); // For bounds control
        chart.setMarker(customMarkerView); // Set the marker to the chart

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.parseColor("#758184"));
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
//        l.setYOffset(11f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(-500f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(Color.RED);
        rightAxis.setAxisMaximum(200);
        rightAxis.setAxisMinimum(-200);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setGranularityEnabled(false);
    }

    private void ReadCSV(String path) throws FileNotFoundException {
        int count = 0;
        File file = new File(path);
        Scanner fileReader = new Scanner(file);

        fileReader.nextLine();

        String prevLine = null;
        String currentLine;
        String[] currLineArray;
        String[] prevLineArray;
        while (fileReader.hasNextLine()) {
            currentLine = fileReader.nextLine();
            if (prevLine != null) {
                currLineArray = currentLine.split(",");
                prevLineArray = prevLine.split(",");
                if (!currLineArray[0].equals("N/A") && !prevLineArray.equals("N/A")) {

                    timestamp.add(new Entry(count, Long.parseLong(currLineArray[9])));
                    acceleration.add(new Entry(count, Float.parseFloat(currLineArray[1]) - Float.parseFloat(prevLineArray[1])));
                    load.add(new Entry(count, Float.parseFloat(currLineArray[3])));
                    fuelEconomyCoef.add(new Entry(count, Float.parseFloat(currLineArray[1]) - Float.parseFloat(prevLineArray[1]) *
                            Float.parseFloat(currLineArray[3])));
                }
            }
            count++;
            prevLine = currentLine;
        }

    }

    private void setData(int count, float range) {
        List<Entry> accelerationData = new ArrayList<>(),
                loadData = new ArrayList<>(),
                fuelEconomy = new ArrayList<>();
        Entry tempAcceleration, tempLoad, tempFuelEconomy;

        for (int i = 0; i < count; i++) {
            tempAcceleration = acceleration.get(i);
            tempLoad = load.get(i);
            tempFuelEconomy = fuelEconomyCoef.get(i);

            if (tempAcceleration.getY() < range && tempLoad.getY() < range && tempFuelEconomy.getY() < range) {
                accelerationData.add(tempAcceleration);
                loadData.add(tempLoad);
                fuelEconomy.add(tempFuelEconomy);
            }
        }

        LineDataSet set1, set2, set3, set4;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) chart.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) chart.getData().getDataSetByIndex(2);

            set1.setValues(accelerationData);
            set2.setValues(loadData);
            set3.setValues(fuelEconomy);

            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(acceleration, "Acceleration");

            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.parseColor("#4cd3c2"));
            set1.setLineWidth(2f);
            set1.setCircleRadius(1f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);

            // create a dataset and give it a type
            set2 = new LineDataSet(load, "Engine load");
            set2.setAxisDependency(YAxis.AxisDependency.LEFT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.parseColor("#d7385e"));
            set2.setLineWidth(2f);
            set2.setCircleRadius(1f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));

            set3 = new LineDataSet(timestamp, "Fuel economy coefficient");
            set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set3.setColor(Color.YELLOW);
            set3.setCircleColor(Color.parseColor("#f2ed6f"));
            set3.setLineWidth(2f);
            set3.setCircleRadius(1f);
            set3.setFillAlpha(65);
            set3.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            set3.setDrawCircleHole(false);
            set3.setHighLightColor(Color.rgb(244, 117, 117));

            // create a data object with the data sets
            LineData data = new LineData(set1, set2, set3);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            chart.setData(data);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText(String.valueOf(seekBarX.getProgress()));
        tvY.setText(String.valueOf(seekBarY.getProgress()));

        setData(seekBarX.getProgress(), seekBarY.getProgress());

        // redraw
        chart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", xIndex: " + e.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
