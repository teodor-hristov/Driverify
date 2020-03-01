package com.demotxt.droidsrce.homedashboard.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

public class MapTripFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private Bundle arguments;
    private String filePath = null;
    private File mapDataFile = null;

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
        File[] dir = new File(Constants.LOCATON_LIVE_DATA_PATH).listFiles();

        long min = Math.abs(parse(dir[0].getName()).getTime() - parse(file.getName()).getTime());
        long reckoning;
        File neededFile = null;

        for (File item : new File(Constants.LOCATON_LIVE_DATA_PATH).listFiles()) {
            if (item.isFile()) {
                reckoning = Math.abs(parse(item.getName()).getTime() - parse(file.getName()).getTime());
                if (min >= reckoning && reckoning < 300000) {
                    min = reckoning;
                    neededFile = item;
                }
            }
        }
        mapDataFile = neededFile;

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
        fileReader.nextLine();
        while (fileReader.hasNextLine()) {
            String[] array = fileReader.nextLine().split(",");
            customDataEntries.add(new LatLng(Double.parseDouble(array[0]), Double.parseDouble(array[1])));
        }
        fileReader.close();
        return customDataEntries;
    }

    private List<PolylineOptions> setColoredPolylinesOnTheMap(List<LatLng> points) {
        List<PolylineOptions> polylines = new ArrayList<>();

        for (int i = 1; i < points.size(); i++) {
            if (i % 2 == 0) {
                polylines.add(new PolylineOptions().add(points.get(i), points.get(i - 1)).color(Color.RED));
            } else {
                polylines.add(new PolylineOptions().add(points.get(i), points.get(i - 1)).color(Color.BLACK));
            }
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

    private void addMarkersWithValues(GoogleMap googleMap, File locationDataFile, File carDataFile) throws FileNotFoundException {
//        for (LatLng marker : points) {
//            googleMap.addMarker(new MarkerOptions().position(marker)
//                    .alpha(0)
//                    .title("Current point info: ")
//                    .snippet("speed: 150 rpm: 3840 load: 48%"));
//        }
        String[] carDataPoints = ReadCSV(carDataFile.getAbsolutePath());
        String[] locationDataPoints = ReadCSV(locationDataFile.getAbsolutePath());
        Dictionary indexes = new Hashtable<String, Integer>();
        indexes.put("rpm", 0);
        indexes.put("speed", 1);
        indexes.put("load", 3);
        indexes.put("lat", 0);
        indexes.put("long", 1);


        for (String locationPoint : locationDataPoints) {
            for (String carPoint : carDataPoints) {
//                if(locationPoint.split(",")[3] == carPoint.split(",")[4]){
                googleMap.addMarker(new MarkerOptions().position(
                        new LatLng(Double.parseDouble(locationPoint.split(",")[0]),
                                Double.parseDouble(locationPoint.split(",")[1])))
                        .alpha(0.1f)
                        .title("Current point info: ")
                        .snippet("speed: 150 rpm: 3840 load: 48%"));
                //}
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        List<LatLng> latLngList = null;

        try {
            latLngList = LatLongReadCSV(mapDataFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (LatLng marker : latLngList) {
            googleMap.addMarker(new MarkerOptions().position(marker)
                    .alpha(0)
                    .title("Current point info: ")
                    .snippet("speed: 150 rpm: 3840 load: 48%"));
        }

        for (PolylineOptions option : setColoredPolylinesOnTheMap((latLngList))) {
            googleMap.addPolyline(option);
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(0), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}

