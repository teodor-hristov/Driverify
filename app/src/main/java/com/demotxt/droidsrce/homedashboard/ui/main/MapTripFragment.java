package com.demotxt.droidsrce.homedashboard.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demotxt.droidsrce.homedashboard.Home;
import com.demotxt.droidsrce.homedashboard.R;
import com.demotxt.droidsrce.homedashboard.Utils.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class MapTripFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private Bundle arguments;
    private String filePath = null;
    private File dataFile = null;

    public MapTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arguments = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        File file;

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            filePath = bundle.getString("file_name", "");
        }
        file = new File(filePath);
        File[] dir = new File(Constants.DATA_LOG_PATH).listFiles();

        long min = Math.abs(parse(dir[0].getName()).getTime() - parse(file.getName()).getTime());
        long reckoning;
        File neededFile = null;

        for (File item : dir) {
            if (item.isFile()) {
                reckoning = Math.abs(parse(item.getName()).getTime() - parse(file.getName()).getTime());
                if (min >= reckoning && reckoning < 300000) {
                    min = reckoning;
                    neededFile = item;
                }
            }
        }

        if (neededFile == null) {
            startActivity(new Intent(getContext(), Home.class));
        }
        dataFile = neededFile;

        mapView = view.findViewById(R.id.map2);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    private Date parse(String fileName) {
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            date = dateFormat.parse(fileName.substring(0, 19));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private List<LatLng> LatLongReadCSV(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner fileReader = new Scanner(file);
        List<LatLng> customDataEntries = new ArrayList<>();
        String lat, lon;

        fileReader.nextLine();
        while (fileReader.hasNextLine()) {
            String[] array = fileReader.nextLine().split(",");
            lat = array[4];
            lon = array[5];
            if (!lat.equals("N/A") && !lon.equals("N/A"))
                customDataEntries.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
        }
        fileReader.close();
        return customDataEntries;
    }

    private List<PolylineOptions> setColoredPolylinesOnTheMap(List<LatLng> points) {
        List<PolylineOptions> polylines = new ArrayList<>();

        for (int i = 1; i < points.size(); i++) {
//            if (i % 2 == 0) {
//                polylines.add(new PolylineOptions().add(points.get(i), points.get(i - 1)).color(Color.RED));
//            } else {
            polylines.add(new PolylineOptions().add(points.get(i), points.get(i - 1)).color(Color.BLACK));
            //}
        }

        return polylines;
    }

    private String[] ReadCSV(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner fileReader = new Scanner(file);
        List<String> data = new ArrayList<>();

        fileReader.nextLine();
        while (fileReader.hasNextLine()) {
            data.add(fileReader.nextLine());
        }
        return Arrays.asList(data.toArray()).toArray(new String[data.toArray().length]);
    }

    private void addMarkersWithValues(GoogleMap googleMap, File dataFile) throws FileNotFoundException {
        String[] dataPoints = ReadCSV(dataFile.getAbsolutePath());
        String[] currentLine;
        String rpm, speed, load, lat, lon, alt, time;

        for (String dataPoint : dataPoints) {
            currentLine = dataPoint.split(",");
            lat = currentLine[4];
            lon = currentLine[5];
            alt = currentLine[6];
            rpm = currentLine[0];
            speed = currentLine[1];
            load = currentLine[3];
            time = new Date(new Timestamp(Long.parseLong(currentLine[9])).getTime()).toString();

            if (lat.equals("N/A") || lon.equals("N/A") || alt.equals("N/A")) {
            } else {
                googleMap.addMarker(new MarkerOptions().position(
                        new LatLng(Double.parseDouble(lat),
                                Double.parseDouble(lon)))
                        .alpha(0)
                        .title("Current info on " + time + ": ")
                        .snippet("speed: " + speed + " km/h rpm: " + rpm + " load: " + load + "%"));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        List<LatLng> latLngList = null;

        try {
            addMarkersWithValues(googleMap, dataFile);
            latLngList = LatLongReadCSV(dataFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (PolylineOptions option : setColoredPolylinesOnTheMap(latLngList)) {
            googleMap.addPolyline(option);
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(0), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}

