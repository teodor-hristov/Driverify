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
    private List<PolylineOptions> coloredPolylineOptions;
    private LatLng zoomPoint;

    public MapTripFragment() {
        // Required empty public constructor
        coloredPolylineOptions = new ArrayList<>();
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

    private void mapEntities(GoogleMap googleMap, File dataFile) throws FileNotFoundException {
        String[] dataPoints = ReadCSV(dataFile.getAbsolutePath());
        String[] currentLine;
        String lat, lon, sleeping, happiness;
        LatLng prevPoint = null;
        LatLng currentPoint;

        for (String dataPoint : dataPoints) {
            currentLine = dataPoint.split(",");
            lat = currentLine[4];
            lon = currentLine[5];
            sleeping = currentLine[7];
            happiness = currentLine[8];

            addMarker(googleMap, currentLine);

            if (!lat.equals("N/A") && !lon.equals("N/A")) {
                currentPoint = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                zoomPoint = currentPoint;

                if (prevPoint != null) {

                    if (!sleeping.equals("N/A") && !happiness.equals("N/A")) {
                        if (sleeping.equals("1") || Double.parseDouble(happiness) < 0.05) {
                            addPolyline(prevPoint, currentPoint, Color.RED);
                        } else {
                            addPolyline(prevPoint, currentPoint, Color.BLACK);
                        }

                    } else {
                        addPolyline(prevPoint, currentPoint, Color.BLACK);
                    }
                }

                prevPoint = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        List<LatLng> latLngList = null;

        try {
            mapEntities(googleMap, dataFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (PolylineOptions option : getColoredPolylineOptions()) {
            googleMap.addPolyline(option);
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomPoint, 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void addPolyline(LatLng firstPoint, LatLng secondPoint, int color) {
        this.coloredPolylineOptions.add(new PolylineOptions().add(secondPoint, firstPoint).color(color));
    }

    public List<PolylineOptions> getColoredPolylineOptions() {
        return this.coloredPolylineOptions;
    }

    public void addMarker(GoogleMap googleMap, String[] currentLine) {
        String rpm, speed, load, lat, lon, alt, time;

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

    public double calculateDistanceBetweenTwoPoints(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return Radius * c;
    }
}

