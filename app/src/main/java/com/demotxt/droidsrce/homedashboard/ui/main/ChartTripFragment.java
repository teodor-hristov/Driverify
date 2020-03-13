package com.demotxt.droidsrce.homedashboard.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.demotxt.droidsrce.homedashboard.R;
import com.demotxt.droidsrce.homedashboard.Utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ChartTripFragment extends Fragment {
    public ChartTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private List<DataEntry> ReadCSV(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner fileReader = new Scanner(file);
        List<DataEntry> customDataEntries = new ArrayList<>();

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
                    customDataEntries.add(new CustomDataEntry(currLineArray[9],
                            Double.parseDouble(currLineArray[1]) - Double.parseDouble(prevLineArray[1]),
                            Double.parseDouble(currLineArray[3]) / 10,
                            (Double.parseDouble(currLineArray[1]) - Double.parseDouble(prevLineArray[1])) *
                                    Double.parseDouble(currLineArray[3])
                    ));
                }
            }

            prevLine = currentLine;
        }
        return customDataEntries;
    }

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

        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view2);
        anyChartView.setProgressBar(view.findViewById(R.id.progress_bar2));

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Data: " + new File(filePath).getName());

        cartesian.yAxis(0).title("Load & Acceleration");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        Set set = Set.instantiate();
        try {
            set.data(ReadCSV(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
        Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");
        Mapping series4Mapping = set.mapAs("{ x: 'x', value: 'value4' }");


        Line series1 = cartesian.line(series1Mapping);
        series1.name("Acceleration");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series2 = cartesian.line(series2Mapping);
        series2.name("Engine load / 10 ");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series3 = cartesian.line(series3Mapping);
        series3.name("Fuel economy border");
        series3.hovered().markers().enabled(true);
        series3.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series3.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series4 = cartesian.line(series4Mapping);
        series4.name("Current fuel economy");
        series4.hovered().markers().enabled(true);
        series4.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series4.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.xScroller(true);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        anyChartView.setZoomEnabled(true);
        anyChartView.setChart(cartesian);
    }

    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value, Number value2, Number value4) {
            super(new Date(new Timestamp(Long.parseLong(x)).getTime()).toString(), value);
            setValue("value2", value2);
            setValue("value3", Constants.FUEL_ECONOMY_CONSTANT);
            setValue("value4", value4);
        }

    }
}
