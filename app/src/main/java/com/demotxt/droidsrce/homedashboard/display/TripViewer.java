package com.demotxt.droidsrce.homedashboard.display;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TripViewer extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TripViewer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_view);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String filePath = getIntent().getStringExtra(Constants.CHECKOUT_TRIP);
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Data: " + new File(filePath).getName());

        cartesian.yAxis(0).title("RPM");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        Set set = Set.instantiate();
        try {
            set.data(ReadCSV(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("RPM");
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
        series2.name("Speed");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        anyChartView.setChart(cartesian);
    }

    private List<DataEntry> ReadCSV(String path) throws FileNotFoundException {
        File file = new File(path);

        Scanner fileReader = new Scanner(file);
        List<DataEntry> customDataEntries = new ArrayList<>();
        fileReader.nextLine();
        while (fileReader.hasNextLine()) {
            String[] array = fileReader.nextLine().split(",");
            customDataEntries.add(new CustomDataEntry(array[4], Integer.parseInt(array[0]), Integer.parseInt(array[1]), 1));
        }
        return customDataEntries;
    }

    private List<LatLng> LatLongReadCSV(String path) throws FileNotFoundException {
        File file = new File(path);

        Scanner fileReader = new Scanner(file);
        List<LatLng> customDataEntries = new ArrayList<>();
        fileReader.nextLine();
        while (fileReader.hasNextLine()) {
            String[] array = fileReader.nextLine().split(",");
            customDataEntries.add(new LatLng(Double.parseDouble(array[0]), Double.parseDouble(array[1])));
        }
        return customDataEntries;
    }

    private List<PolylineOptions> setColoredPolylinesOnTheMap() {
        List<PolylineOptions> polylines = null;

        return polylines;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String fileName = "2020-02-25T14:02:11.csv";
        List<LatLng> latLngList = null;
        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        try {
            latLngList = LatLongReadCSV(Constants.LOCATON_LIVE_DATA_PATH + "/" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (LatLng marker : latLngList) {
            googleMap.addMarker(new MarkerOptions().position(marker)
                    .alpha(0)
                    .title("Current point info: ")
                    .snippet("speed: 150 rpm: 3840 load: 48%"));
        }
        googleMap.addPolyline(polylineOptions.addAll(latLngList));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(0), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value, Number value2, Number value3) {
            super(x, value);
            setValue("value2", value2);
        }

    }

}
