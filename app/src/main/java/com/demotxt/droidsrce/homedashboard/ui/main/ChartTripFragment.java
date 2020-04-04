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
        String filePath = null;

        if (bundle != null) {
            filePath = bundle.getString("file_name", "");
        }

        tvX = view.findViewById(R.id.tvXMax);
        tvY = view.findViewById(R.id.tvYMax);

        seekBarX = view.findViewById(R.id.seekBar1);
        seekBarY = view.findViewById(R.id.seekBar2);

        seekBarY.setOnSeekBarChangeListener(this);
        seekBarX.setOnSeekBarChangeListener(this);

        chart = view.findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);
        chart.setDrawGridBackground(false);

        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        // chart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        CustomMarkerView mv = new CustomMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart

        XAxis xl = chart.getXAxis();
        xl.setAvoidFirstLastClipping(true);
        xl.setAxisMinimum(0f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setInverted(true);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        // add data
        seekBarX.setProgress(25);
        seekBarY.setProgress(50);

        // // restrain the maximum scale-out factor
        // chart.setScaleMinima(3f, 3f);
        //
        // // center the view to a specific position inside the chart
        // chart.centerViewPort(10, 50);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        // don't forget to refresh the drawing
        chart.invalidate();

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

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float xVal = (float) (Math.random() * range);
            float yVal = (float) (Math.random() * range);
            entries.add(new Entry(xVal, yVal));
        }

        // sort by x-value
        Collections.sort(entries, new EntryXComparator());

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(entries, "DataSet 1");

        set1.setLineWidth(1.5f);
        set1.setCircleRadius(4f);

        // create a data object with the data sets
        LineData data = new LineData(set1);

        // set data
        chart.setData(data);
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
