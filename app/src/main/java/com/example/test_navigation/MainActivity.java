package com.example.test_navigation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private MapView map;
    private Button btnMyLocation;
    private ImageButton btnRotateToggle;
    private boolean isMapRotationEnabled = false;
    private float heading = 90f;
    private GeoPoint currentLocation;
    private Marker locationMarker;

    // 조이스틱 버튼 추가
    private Button btnUp, btnDown, btnLeft, btnRight;

    // 2m 이동을 위한 상수 (위도 1도 = 약 111km, 경도 1도 = 약 88km)
    private static final double LATITUDE_DELTA = 2.0 / 111000.0;  // 위도 2m
    private static final double LONGITUDE_DELTA = 2.0 / 88000.0;  // 경도 2m

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        setContentView(R.layout.activity_main);

        initializeMap();
        initializeButtons();
        setInitialLocation();
    }

    private void initializeMap() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER);

        map.setScrollableAreaLimitLatitude(38.625, 33.112, 0);
        map.setScrollableAreaLimitLongitude(124.611, 132.000, 0);
        map.setMinZoomLevel(10.0);
        map.setMaxZoomLevel(22.0);

        IMapController mapController = map.getController();
        mapController.setZoom(18.5);

        locationMarker = new Marker(map);
        map.getOverlays().add(locationMarker);
    }

    private void initializeButtons() {
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnRotateToggle = findViewById(R.id.btnRotateToggle);

        btnMyLocation.setOnClickListener(v -> moveToMyLocation());
        btnRotateToggle.setOnClickListener(v -> toggleMapRotation());

        // 조이스틱 버튼 초기화
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        // 조이스틱 버튼에 리스너 추가
        btnUp.setOnClickListener(v -> moveLocation(LATITUDE_DELTA, 0));
        btnDown.setOnClickListener(v -> moveLocation(-LATITUDE_DELTA, 0));
        btnLeft.setOnClickListener(v -> moveLocation(0, -LONGITUDE_DELTA));
        btnRight.setOnClickListener(v -> moveLocation(0, LONGITUDE_DELTA));
    }

    private void setInitialLocation() {
        // 초기 위치 설정 (예: 서울시청)
        currentLocation = new GeoPoint(37.5665, 126.9780);
        updateMapLocation();
    }

    private void updateMapLocation() {
        map.getController().animateTo(currentLocation);
        locationMarker.setPosition(currentLocation);
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.invalidate();
    }

    private void moveToMyLocation() {
        updateMapLocation();
    }

    private void toggleMapRotation() {
        isMapRotationEnabled = !isMapRotationEnabled;
        if (isMapRotationEnabled) {
            enableMapRotation();
        } else {
            disableMapRotation();
        }
    }

    private void enableMapRotation() {
        updateMapRotation();
        Toast.makeText(this, "지도 회전이 활성화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void disableMapRotation() {
        map.setMapOrientation(0);
        btnRotateToggle.setRotation(0);
        Toast.makeText(this, "지도가 정북으로 고정되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void updateMapRotation() {
        if (isMapRotationEnabled) {
            map.setMapOrientation(heading);
            btnRotateToggle.setRotation(heading);
        }
    }

    public void setHeading(float newHeading) {
        heading = newHeading;
        if (isMapRotationEnabled) {
            updateMapRotation();
        }
    }

    // 새로운 메서드: 사용자 위치 변경
    public void setUserLocation(double latitude, double longitude) {
        currentLocation = new GeoPoint(latitude, longitude);
        updateMapLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    // 새로운 메서드: 위치 이동
    private void moveLocation(double latDelta, double lonDelta) {
        double newLat = currentLocation.getLatitude() + latDelta;
        double newLon = currentLocation.getLongitude() + lonDelta;
        setUserLocation(newLat, newLon);
    }
}