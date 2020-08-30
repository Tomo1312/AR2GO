package com.example.ar2go;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import static java.lang.String.valueOf;
import static java.lang.Thread.sleep;

public class FreeLanceActivity extends AppCompatActivity implements OnMapReadyCallback {

    //private Double currentLongitude, currentLatitude;
    private TextView sculptureShown, arhitektureShown, spomeniciShown;
    private LatLng currentLocation;
    public static final int REQUEST_LOCATION = 99;
    private Location getLastLocation;
    @Nullable
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ImageView back, collection, showToolbar, checkboxSculptures, checkboxArhitekture, checkboxSpomenici;
    private Marker marker;
    @NonNull
    private Boolean flag = false;
    private boolean isFrist;
    protected FirebaseAuth firebaseAuth;
    protected FirebaseDatabase firebaseDatabase;
    protected DatabaseReference databaseReference;
    @Nullable
    protected UserProfile userProfile;
    protected int bodovi, lifes;
    protected String unlockedSculptures, userName, userEmail;
    private GoogleMap mMap;
    protected static ArrayList<Sculpture> sculptures, arhitekture, spomenici;
    protected ArrayList<CustomLocation> customLocations = new ArrayList<>();
    protected ArrayList<Marker> markersCustomLocations = new ArrayList<>();
    private LinearLayout toolbarLayout;
    private Animation showToolbarAnimation, unshowToolbarAnimation;
    private ScrollView leftScrollView;
    private boolean toolbarShown, isSculptureShown, isArhitektureShown, isSpomeniciShown;

    private Intent mServiceIntent;
    private Timer mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelance);

        //if you want to lock screen for always Portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        setuUiVeiws();

        //Save xml file to var Sculptures
        getAllThingsForMap();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            if (ActivityCompat.checkSelfPermission(FreeLanceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                currentLocation = new LatLng(getLastLocation.getLatitude(), getLastLocation.getLongitude());
            }
        } catch (Exception ex) {
            Toast.makeText(FreeLanceActivity.this, "Turn on location to continue", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(FreeLanceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        unlockedSculptures = "";
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userProfile = new UserProfile();
        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        databaseReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userProfile = dataSnapshot.getValue(UserProfile.class);
                        unlockedSculptures = userProfile.getOtkljucaneSkulputre();
                        bodovi = userProfile.getUserBodovi();
                        userName = userProfile.getUserName();
                        userEmail = userProfile.getUserEmail();
                        lifes = userProfile.getUserLifes();
                        if (lifes < 20) {
                            mService = new Timer();
                            mServiceIntent = new Intent(FreeLanceActivity.this, Timer.class);
                            if (!isMyServiceRunning(mService.getClass())) {
                                startService(mServiceIntent);
                            }
                            Log.i("Broadcast", "Started service");
                        }
                        if (mMap != null)
                            onMapReady(mMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(FreeLanceActivity.this, "Couldn't connect to database", Toast.LENGTH_LONG);
                    }
                });

        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } catch (Exception ex) {
            Toast.makeText(FreeLanceActivity.this, "NEZZ KOJI K:" + ex, Toast.LENGTH_LONG).show();
        }

        flag = displayGpsStatus();
        if (flag) {
            locationListener = new MyLocationListener();
            try {
                if (ActivityCompat.checkSelfPermission(FreeLanceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 30, locationListener);
            } catch (Exception ex) {
                Toast.makeText(FreeLanceActivity.this, "Location acces not granted ", Toast.LENGTH_LONG).show();
            }

        } else {
            finish();
            startActivity(new Intent(FreeLanceActivity.this, SecondActivity.class));
        }
        menuBar();
        //startCustomLocationTimer();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    /*----Method to Check GPS is enable or disable ----- */
    @NonNull
    protected Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        return gpsStatus;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        if (sculptures != null && unlockedSculptures != null && isSculptureShown) {
            for (Sculpture sculpture : sculptures) {
                if (unlockedSculptures.contains(sculpture.imagePath)) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(sculpture.latitude), Double.valueOf(sculpture.longitude))).title(sculpture.name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    marker.setTag(sculpture.imagePath);
                } else {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(sculpture.latitude), Double.valueOf(sculpture.longitude))).title("Unknown"));
                }
            }
        }

        if (spomenici != null && unlockedSculptures != null && isSpomeniciShown) {
            for (Sculpture sculpture : spomenici) {
                if (unlockedSculptures.contains(sculpture.imagePath)) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(sculpture.latitude), Double.valueOf(sculpture.longitude))).title(sculpture.name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    marker.setTag(sculpture.imagePath);
                } else {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(sculpture.latitude), Double.valueOf(sculpture.longitude))).title("Unknown").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                }
            }
        }

        if (arhitekture != null && unlockedSculptures != null && isArhitektureShown) {
            for (Sculpture sculpture : arhitekture) {
                if (unlockedSculptures.contains(sculpture.imagePath)) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(sculpture.latitude), Double.valueOf(sculpture.longitude))).title(sculpture.name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    marker.setTag(sculpture.imagePath);
                } else {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(sculpture.latitude), Double.valueOf(sculpture.longitude))).title("Unknown").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

                }
            }
        }
        final ArrayList<Sculpture> AllSculptures = new ArrayList<>();
        AllSculptures.addAll(sculptures);
        AllSculptures.addAll(spomenici);
        AllSculptures.addAll(arhitekture);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (!marker.getTitle().equals("Unknown") && marker.getTag() != null) {
                    for (Sculpture sculptureTmp : AllSculptures) {
                        if (sculptureTmp.imagePath.equals(marker.getTag())) {
                            Pop popSculpture = new Pop(sculptureTmp.name, sculptureTmp.imagePath, sculptureTmp.description, sculptureTmp.author);
                            popSculpture.setFirstTime(false);
                            Intent i = new Intent(FreeLanceActivity.this, Pop.class);
                            i.putExtra("currentSculpture", popSculpture);
                            startActivity(i);
                        }
                    }
                }
            }
        });
        if (currentLocation != null && isFrist) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.5f));
            isFrist = false;
        }
        googleMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (toolbarShown) {
                    toolbarShown = false;
                    toolbarLayout.startAnimation(unshowToolbarAnimation);
                    unshowToolbarAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            toolbarLayout.setVisibility(View.INVISIBLE);
                            leftScrollView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });
        getCustomLocation();
    }

    void getCustomLocation() {
        final DatabaseReference databaseReferenceOfLocations = firebaseDatabase.getReference("Locations");

        databaseReferenceOfLocations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CustomLocation customLocation = postSnapshot.getValue(CustomLocation.class);
                    String locationName = customLocation.getName();
                    String locationtDescription = customLocation.getDescription();
                    double locationLatitude = customLocation.getLatitude();
                    double locationLongitude = customLocation.getLongitude();
                    int locationRadius = customLocation.getRadius();

                    CustomLocation tmpLoc = new CustomLocation(locationName, locationtDescription, locationLatitude, locationLongitude, locationRadius);
                    customLocations.add(tmpLoc);

                    //mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(locationLatitude), Double.valueOf(locationLongitude))).title(locationName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                    marker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(locationLatitude), Double.valueOf(locationLongitude)))
                                    .title(locationName)
                                    .snippet(timeLeftFormatted)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.confetti)));
                    marker.setTag(customLocation.name + customLocation.radius);

                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(Double.valueOf(locationLatitude), Double.valueOf(locationLongitude)))
                            .radius(locationRadius)
                            .strokeWidth(0f)
                            .fillColor(0x550000FF));
                    markersCustomLocations.add(marker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FreeLanceActivity.this, "Couldn't connect to database", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setuUiVeiws() {
        //editLocation = (TextView)findViewById(R.id.editTextLocation);
        //textDistance = (TextView)findViewById(R.id.tvDistance);
        //textPopup = (TextView)findViewById(R.id.tvXML);
        back = findViewById(R.id.ivBack);
        collection = findViewById(R.id.ivColelction);
        toolbarLayout = findViewById(R.id.toolbarLayout);
        showToolbar = findViewById(R.id.ivShowToolbar);
        leftScrollView = findViewById(R.id.scrollViewZaNazad);
        showToolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.righttolefttoolbar);
        unshowToolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.lefttorighttoolbar);
        arhitektureShown = findViewById(R.id.showArhitekture);
        sculptureShown = findViewById(R.id.showSculptures);
        spomeniciShown = findViewById(R.id.showSpomenici);
        checkboxArhitekture = findViewById(R.id.checkboxArhitekture);
        checkboxSculptures = findViewById(R.id.checkboxSculpureShown);
        checkboxSpomenici = findViewById(R.id.checkboxSpomenici);
        toolbarShown = false;
        isSculptureShown = true;
        isArhitektureShown = false;
        isSpomeniciShown = false;
        isFrist = true;
    }

    private void menuBar() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.setBackgroundColor(Color.parseColor("#55000000"));
                finish();
            }
        });

        collection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FreeLanceActivity.this, CollectionActivity.class));
            }
        });

        showToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!toolbarShown) {
                    toolbarShown = true;
                    toolbarLayout.startAnimation(showToolbarAnimation);
                    showToolbarAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            toolbarLayout.setVisibility(View.VISIBLE);
                            leftScrollView.setVisibility(View.VISIBLE);

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    toolbarShown = false;
                    toolbarLayout.startAnimation(unshowToolbarAnimation);
                    unshowToolbarAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            toolbarLayout.setVisibility(View.INVISIBLE);
                            leftScrollView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        spomeniciShown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSpomeniciShown) {
                    isSpomeniciShown = false;
                    checkboxSpomenici.setVisibility(View.INVISIBLE);
                    onMapReady(mMap);
                } else {
                    isSpomeniciShown = true;
                    checkboxSpomenici.setVisibility(View.VISIBLE);
                    onMapReady(mMap);
                }
            }
        });

        sculptureShown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSculptureShown) {
                    isSculptureShown = false;
                    checkboxSculptures.setVisibility(View.INVISIBLE);
                    onMapReady(mMap);
                } else {
                    isSculptureShown = true;
                    checkboxSculptures.setVisibility(View.VISIBLE);
                    onMapReady(mMap);
                }
            }
        });

        arhitektureShown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isArhitektureShown) {
                    isArhitektureShown = false;
                    checkboxArhitekture.setVisibility(View.INVISIBLE);
                    onMapReady(mMap);
                } else {
                    isArhitektureShown = true;
                    checkboxArhitekture.setVisibility(View.VISIBLE);
                    onMapReady(mMap);
                }
            }
        });
    }

    private void getAllThingsForMap() {
        try {
            sculptures = getXmlFiles("sculptures.xml", "Sculpture");
            spomenici = getXmlFiles("spomenici.xml", "Spomenik");
            arhitekture = getXmlFiles("arhitekture.xml", "Arhitektura");
        } catch (XmlPullParserException ex) {
            Toast.makeText(FreeLanceActivity.this, "No data in XML", Toast.LENGTH_LONG).show();
        } catch (IOException ex) {
            Toast.makeText(FreeLanceActivity.this, "Couldn't open XML", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<Sculpture> getXmlFiles(String xmlFile, String firstTag) throws IOException, XmlPullParserException {
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try {
            InputStream is = getAssets().open(xmlFile);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        } catch (IOException e) {
        }
        ArrayList<Sculpture> namesInXml = new ArrayList<>();
        int eventType = parser.getEventType();
        Sculpture currentSculpture = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if (firstTag.equals(eltName)) {
                        currentSculpture = new Sculpture();
                        namesInXml.add(currentSculpture);
                    } else if (currentSculpture != null) {
                        if ("name".equals(eltName)) {
                            currentSculpture.name = parser.nextText();
                        } else if ("description".equals(eltName)) {
                            currentSculpture.description = parser.nextText();
                        } else if ("author".equals(eltName)) {
                            currentSculpture.author = parser.nextText();
                        } else if ("imagePath".equals(eltName)) {
                            currentSculpture.imagePath = parser.nextText();
                        } else if ("points".equals(eltName)) {
                            currentSculpture.points = parser.nextText();
                        } else if ("latitude".equals(eltName)) {
                            currentSculpture.latitude = parser.nextText();
                        } else if ("longitude".equals(eltName)) {
                            currentSculpture.longitude = parser.nextText();
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        return namesInXml;
    }

    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location loc) {

            /*----------to get City-Name from coordinates ------------- /
            editLocation.setText("");
             Toast.makeText(getBaseContext(),"Location changed : Lat: " +
                           loc.getLatitude()+ " Lng: " + loc.getLongitude(),
                   Toast.LENGTH_SHORT).show();
             String longitude = "Longitude: " +loc.getLongitude();
            String latitude = "Latitude: " +loc.getLatitude();


            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String s = "CurrentLocation:" + longitude+"\n"+latitude +
                    "\n\nMy Currrent City is: "+cityName;*/

            Location loc1 = new Location("");
            loc1.setLongitude(loc.getLongitude());
            loc1.setLatitude(loc.getLatitude());
            if (sculptures != null && spomenici != null && arhitekture != null && customLocations != null) {
                checkIfCurrentLocationIsNear(sculptures, loc1);
                checkIfCurrentLocationIsNear(spomenici, loc1);
                checkIfCurrentLocationIsNear(arhitekture, loc1);
                checkIfCustomLocationIsNear(loc1);
            }
        }

        private void checkIfCurrentLocationIsNear(ArrayList<Sculpture> important, Location loc1) {
            for (Sculpture sculpture : important) {
                Double sculptureLatitude = Double.valueOf(sculpture.latitude);
                Double sculptureLongtitude = Double.valueOf(sculpture.longitude);

                Location loc2 = new Location("");
                loc2.setLongitude(sculptureLongtitude);
                loc2.setLatitude(sculptureLatitude);

                float distanceInMeters = loc1.distanceTo(loc2);
                if (distanceInMeters < 50.0 &&
                        checkIfSculptureUnlocked(sculpture.imagePath) && lifes > 0) {
                    Pop popSculpture = new Pop(sculpture.name, sculpture.imagePath, sculpture.description, sculpture.author);
                    bodovi = bodovi + Integer.valueOf(sculpture.points);
                    unlockedSculptures = unlockedSculptures + ", " + sculpture.imagePath;
                    if (userName != null && userEmail != null) {
                        lifes--;
                        UserProfile addBodovi = new UserProfile(userName, userEmail, bodovi, unlockedSculptures, lifes);
                        databaseReference.setValue(addBodovi);
                        popSculpture.setFirstTime(true);
                        Intent i = new Intent(FreeLanceActivity.this, Pop.class);
                        i.putExtra("currentSculpture", popSculpture);
                        startActivity(i);
                    }
                }
            }
        }

        protected boolean checkIfSculptureUnlocked(@NonNull String name) {
            return !unlockedSculptures.contains(name);
        }

        private void checkIfCustomLocationIsNear(Location loc) {
            for (CustomLocation customLocation : customLocations) {

                Double customLocationeLatitude = customLocation.latitude;
                Double customLocationLongtitude = customLocation.longitude;

                Location loc2 = new Location("");
                loc2.setLongitude(customLocationLongtitude);
                loc2.setLatitude(customLocationeLatitude);

                float distanceInMeters = loc.distanceTo(loc2);
                if (distanceInMeters < customLocation.radius &&
                        checkIfSculptureUnlocked(customLocation.name + customLocation.radius)) {
                    customLocationPublic = loc2;
                    rangePublic = customLocation.radius;
                    AlertDialog.Builder builder = new AlertDialog.Builder(FreeLanceActivity.this);
                    builder.setMessage("Usli ste u krug zabave, ostanite u krugu dok odbrojavanje ne dode na 00:00 da bi ste dobili dodate bodove!\nStisnite na pin lokacije kako bi ste vidjeli koliko je još ostalo!\n")
                            .setCancelable(false)
                            .setTitle(customLocation.name)
                            .setNeutralButton("Okay!",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(@NonNull DialogInterface dialog, int id) {
                                            // cancel the dialog box
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    if (!mTimerRunning)
                        mTimeLeftInMillis = START_TIME_IN_MILLIS;

                    for (Marker markerTmp : markersCustomLocations) {
                        String tmpTag = customLocation.name + customLocation.radius;
                        customLocationNamePublic = customLocation.name;
                        customLocationRangePublic = customLocation.radius;
                        if (markerTmp.getTag().equals(tmpTag)) {
                            markerPublic = markerTmp;
                            startCustomLocationTimer(markerPublic);
                        }
                    }

//                    startStopwatchForCustomLocation();
//                    Pop popSculpture = new Pop(sculpture.name, sculpture.imagePath, sculpture.description, sculpture.author);
//                    bodovi = bodovi + Integer.valueOf(sculpture.points);
//                    unlockedSculptures = unlockedSculptures + ", " + sculpture.imagePath;
//                    if (userName != null && userEmail != null) {
//                        UserProfile addBodovi = new UserProfile(userName, userEmail, bodovi, unlockedSculptures, lifes - 1);
//                        databaseReference.setValue(addBodovi);
//                        popSculpture.setFirstTime(true);
//                        Intent i = new Intent(FreeLanceActivity.this, Pop.class);
//                        i.putExtra("currentSculpture", popSculpture);
//                        startActivity(i);
                } else {
                    if (mTimerRunning) {
                        mTimerRunning = false;
                        mTimeLeftInMillis = START_TIME_IN_MILLIS;
                        Toast.makeText(FreeLanceActivity.this, "Izasli ste iz kruga!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    }

    private static final int START_TIME_IN_MILLIS = 30 * 60 * 1000;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private String timeLeftFormatted;
    private String customLocationNamePublic;
    private int customLocationRangePublic;
    protected Location customLocationPublic;
    protected int rangePublic;
    protected Marker markerPublic;

    public void startCustomLocationTimer(final Marker markerTmp) {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
//                Marker marker = mMap.addMarker(
//                        new MarkerOptions()
//                                .position(new LatLng(45.866211494878, 15.792399123994))
//                                .title("Dinamo")
//                                .snippet(timeLeftFormatted)
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.confetti)));
                markerTmp.setSnippet(timeLeftFormatted);
                //Toast.makeText(FreeLanceActivity.this, "left: " + timeLeftFormatted, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                if (ActivityCompat.checkSelfPermission(FreeLanceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FreeLanceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                float distanceInMeters = loc.distanceTo(customLocationPublic);
                if (distanceInMeters < rangePublic) {
                    unlockedSculptures = unlockedSculptures + ", " + customLocationNamePublic + customLocationRangePublic;
                    if (userName != null && userEmail != null) {
                        UserProfile addBodovi = new UserProfile(userName, userEmail, bodovi, unlockedSculptures, lifes - 1);
                        databaseReference.setValue(addBodovi);
//                        popSculpture.setFirstTime(true);
//                        Intent i = new Intent(FreeLanceActivity.this, Pop.class);
//                        i.putExtra("currentSculpture", popSculpture);
//                        startActivity(i);
//                        Toast.makeText(FreeLanceActivity.this, "BRAVO IDIOTE" + timeLeftFormatted, Toast.LENGTH_LONG).show();
                    }
                }
                resetTimer();
            }
        }.start();
        mTimerRunning = true;
    }

    private void resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
    }


    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);
        editor.putString("customLocation", customLocationNamePublic + customLocationRangePublic);
        editor.apply();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        String tmpTag = prefs.getString("customLocation", "");

        updateCountDownText();
        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
            } else {
                for (Marker markerTmp : markersCustomLocations) {
                    if (markerTmp.getTag().equals(tmpTag)) {
                        markerPublic = markerTmp;
                        startCustomLocationTimer(markerPublic);
                    }
                }
            }
        }
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    /*    @NonNull
    protected void getAllSculptures() throws IOException, XmlPullParserException{
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try{
            InputStream is = getAssets().open("sculptures.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        }catch (IOException e){
        }
        ArrayList<Sculpture> sculpturesInXml = new ArrayList<>();
        int eventType = parser.getEventType();
        Sculpture currentSculpture = null;

        while(eventType != XmlPullParser.END_DOCUMENT){
            String eltName = null;
            switch (eventType){
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if("Sculpture".equals(eltName)){
                        currentSculpture = new Sculpture();
                        sculpturesInXml.add(currentSculpture);
                    }else if (currentSculpture != null){
                        if ("name".equals(eltName)){
                            currentSculpture.name = parser.nextText();
                        }else if ("description".equals(eltName)){
                            currentSculpture.description= parser.nextText();
                        }else if ("author".equals(eltName)){
                            currentSculpture.author= parser.nextText();
                        }else if ("imagePath".equals(eltName)){
                            currentSculpture.imagePath= parser.nextText();
                        }else if ("points".equals(eltName)){
                            currentSculpture.points = parser.nextText();
                        }else if ("latitude".equals(eltName)){
                            currentSculpture.latitude = parser.nextText();
                        }else if ("longitude".equals(eltName)){
                            currentSculpture.longitude = parser.nextText();
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        sculptures = sculpturesInXml;
    }

    protected void getAllSpomenici() throws IOException, XmlPullParserException{
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try{
            InputStream is = getAssets().open("spomenici.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        }catch (IOException e){
        }
        ArrayList<Sculpture> spomeniciInXml = new ArrayList<>();
        int eventType = parser.getEventType();
        Sculpture currentSculpture = null;

        while(eventType != XmlPullParser.END_DOCUMENT){
            String eltName = null;
            switch (eventType){
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if("Spomenik".equals(eltName)){
                        currentSculpture = new Sculpture();
                        spomeniciInXml.add(currentSculpture);
                    }else if (currentSculpture != null){
                        if ("name".equals(eltName)){
                            currentSculpture.name = parser.nextText();
                        }else if ("description".equals(eltName)){
                            currentSculpture.description= parser.nextText();
                        }else if ("author".equals(eltName)){
                            currentSculpture.author= parser.nextText();
                        }else if ("imagePath".equals(eltName)){
                            currentSculpture.imagePath= parser.nextText();
                        }else if ("points".equals(eltName)){
                            currentSculpture.points = parser.nextText();
                        }else if ("latitude".equals(eltName)){
                            currentSculpture.latitude = parser.nextText();
                        }else if ("longitude".equals(eltName)){
                            currentSculpture.longitude = parser.nextText();
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        spomenici = spomeniciInXml;
    }

    protected void getAllArhitektures() throws IOException, XmlPullParserException{
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try{
            InputStream is = getAssets().open("arhitekture.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        }catch (IOException e){
        }
        ArrayList<Sculpture> arhitecturessInXml = new ArrayList<>();
        int eventType = parser.getEventType();
        Sculpture currentSculpture = null;

        while(eventType != XmlPullParser.END_DOCUMENT){
            String eltName = null;
            switch (eventType){
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if("Arhitektura".equals(eltName)){
                        currentSculpture = new Sculpture();
                        arhitecturessInXml.add(currentSculpture);
                    }else if (currentSculpture != null){
                        if ("name".equals(eltName)){
                            currentSculpture.name = parser.nextText();
                        }else if ("description".equals(eltName)){
                            currentSculpture.description= parser.nextText();
                        }else if ("author".equals(eltName)){
                            currentSculpture.author= parser.nextText();
                        }else if ("imagePath".equals(eltName)){
                            currentSculpture.imagePath= parser.nextText();
                        }else if ("points".equals(eltName)){
                            currentSculpture.points = parser.nextText();
                        }else if ("latitude".equals(eltName)){
                            currentSculpture.latitude = parser.nextText();
                        }else if ("longitude".equals(eltName)){
                            currentSculpture.longitude = parser.nextText();
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        arhitekture = arhitecturessInXml;
    }

    protected void getAllFontanes() throws IOException, XmlPullParserException{
        XmlPullParserFactory parserFactory;
        XmlPullParser parser;
        parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        try{
            InputStream is = getAssets().open("fontane.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        }catch (IOException e){
        }
        ArrayList<Sculpture>fontaneInXml = new ArrayList<>();
        int eventType = parser.getEventType();
        Sculpture currentSculpture = null;

        while(eventType != XmlPullParser.END_DOCUMENT){
            String eltName = null;
            switch (eventType){
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if("Fontana".equals(eltName)){
                        currentSculpture = new Sculpture();
                        fontaneInXml.add(currentSculpture);
                    }else if (currentSculpture != null){
                        if ("name".equals(eltName)){
                            currentSculpture.name = parser.nextText();
                        }else if ("description".equals(eltName)){
                            currentSculpture.description= parser.nextText();
                        }else if ("author".equals(eltName)){
                            currentSculpture.author= parser.nextText();
                        }else if ("imagePath".equals(eltName)){
                            currentSculpture.imagePath= parser.nextText();
                        }else if ("points".equals(eltName)){
                            currentSculpture.points = parser.nextText();
                        }else if ("latitude".equals(eltName)){
                            currentSculpture.latitude = parser.nextText();
                        }else if ("longitude".equals(eltName)){
                            currentSculpture.longitude = parser.nextText();
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        fontane = fontaneInXml;
    }*/
}
