package com.example.test_navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import static android.content.ContentValues.TAG;

/**
 * MainActivity 클래스는 지도 기반 네비게이션 앱의 주요 활동을 관리합니다.
 * 이 클래스는 지도 표시, 사용자 위치 추적, 마커 추가, 경로 그리기 등의 기능을 제공합니다.
 */
public class MainActivity extends AppCompatActivity {

    private MapView map;
    private Button btnHvLocation;
    private ImageButton btnRotateToggle;
    private boolean isMapRotationEnabled = false;
    private float heading = 90f;
    private GeoPoint currentLocation;
    private Marker locationMarker;
    private static final Double ZOOM_DEFAULT = 19.5;
    private Polyline pathPolyline;
    private List<GeoPoint> pathPoints;
    private Button btnClearPath;

    private Marker frontRvMarker;

    // 삭제 예정
    // 조이스틱 버튼 추가
    private Button btnUp, btnDown, btnLeft, btnRight;

    // 2m 이동을 위한 상수 (위도 1도 = 약 111km, 경도 1도 = 약 88km)
    private static final double LATITUDE_DELTA = 10.0 / 111000.0;  // 위도 2m
    private static final double LONGITUDE_DELTA = 10.0 / 88000.0;  // 경도 2m

    private Button btnAddMarkers, btnClearMarkers;
    private List<Marker> randomMarkers = new ArrayList<>();
    private static final double MARKER_RADIUS = 100; // 마커 생성 반경 (미터)
    private Random random = new Random();
    private Button btnAddFrontRv;

    /**
     * 액티비티가 생성될 때 호출되는 메서드입니다.
     * 지도 초기화, 버튼 설정, 초기 위치 설정 등을 수행합니다.
     *
     * @param savedInstanceState 이전에 저장된 인스턴스 상태
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        setContentView(R.layout.activity_main);

        initializeMap();
        initializeButtons();
        setInitialLocation();
        initializePathTracking();
    }

    /**
     * 지도를 초기화하고 설정하는 메서드입니다.
     * 지도 타일 소스, 줌 레벨, 스크롤 제한 등을 설정합니다.
     */
    private void initializeMap() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER);

        map.setScrollableAreaLimitLatitude(38.625, 33.112, 0);
        map.setScrollableAreaLimitLongitude(124.611, 132.000, 0);
        map.setMinZoomLevel(18.0);
        map.setMaxZoomLevel(22.0);
        map.isTilesScaledToDpi();

        IMapController mapController = map.getController();
        mapController.setZoom(ZOOM_DEFAULT);

        // 경로를 먼저 추가
        pathPolyline = new Polyline();
        pathPolyline.setColor(Color.BLUE);
        pathPolyline.setWidth(5f);
        pathPolyline.setGeodesic(true);
        map.getOverlays().add(pathPolyline);

        locationMarker = new Marker(map);
        Drawable userIcon = ContextCompat.getDrawable(this, R.drawable.icon_hv_marker);
        locationMarker.setIcon(userIcon);
        locationMarker.setAnchor(0.35f, 0.65f); // 앵커 포인트를 하단 중앙으로 변경
        map.getOverlays().add(locationMarker);
    }

    /**
     * 사용자 인터페이스 버튼들을 초기화하고 이벤트 리스너를 설정하는 메서드입니다.
     */
    private void initializeButtons() {
        btnHvLocation = findViewById(R.id.btnMyLocation);
        btnRotateToggle = findViewById(R.id.btnRotateToggle);

        btnHvLocation.setOnClickListener(v -> moveHvLocation());
        btnRotateToggle.setOnClickListener(v -> toggleMapRotation());

        // 조이스틱 버튼 초기화 -> 삭제 예정
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        // 조이스틱 버튼에 리스너 추가 -> 삭제 예정
        btnUp.setOnClickListener(v -> moveLocation(LATITUDE_DELTA, 0));
        btnDown.setOnClickListener(v -> moveLocation(-LATITUDE_DELTA, 0));
        btnLeft.setOnClickListener(v -> moveLocation(0, -LONGITUDE_DELTA));
        btnRight.setOnClickListener(v -> moveLocation(0, LONGITUDE_DELTA));


        btnAddMarkers = findViewById(R.id.btnAddMarkers);
        btnClearMarkers = findViewById(R.id.btnClearMarkers);

        btnAddMarkers.setOnClickListener(v -> addRandomMarkers());
        btnClearMarkers.setOnClickListener(v -> clearRandomMarkers());

        btnClearPath = findViewById(R.id.btnClearPath);
        btnClearPath.setOnClickListener(v -> clearPath());

        btnAddFrontRv = findViewById(R.id.btnAddFrontRv);
        btnAddFrontRv.setOnClickListener(v -> addFrontRv());
    }

    /**
     * 경로 추적을 위한 데이터 구조를 초기화하는 메서드입니다.
     */
    private void initializePathTracking() {
        pathPoints = new ArrayList<>();
    }

    /**
     * 초기 사용자 위치를 설정하는 메서드입니다.
     */
    private void setInitialLocation() {
        // 초기 위치 설정
        currentLocation = new GeoPoint(37.392231, 126.958882);
        updateMapLocation();
    }

    /**
     * 지도 위치를 업데이트하고 사용자 위치 마커를 이동시키는 메서드입니다.
     */
    private void updateMapLocation() {
        IMapController mapController = map.getController();

        locationMarker.setPosition(currentLocation);
        mapController.animateTo(currentLocation);
    }

    /**
     * 사용자의 현재 위치로 지도를 이동시키는 메서드입니다.
     */
    private void moveHvLocation(){
        IMapController mapController = map.getController();
        mapController.setZoom(ZOOM_DEFAULT);
        updateMapLocation();
    }

    /**
     * 지도 회전 기능을 토글하는 메서드입니다.
     */
    private void toggleMapRotation() {
        isMapRotationEnabled = !isMapRotationEnabled;
        if (isMapRotationEnabled) {
            enableMapRotation();
        } else {
            disableMapRotation();
        }
    }

    /**
     * 지도 회전 기능을 활성화하는 메서드입니다.
     */
    private void enableMapRotation() {
        updateMapRotation();
        Toast.makeText(this, "지도 회전이 활성화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    /**
     * 지도 회전 기능을 비활성화하는 메서드입니다.
     */
    private void disableMapRotation() {
        map.setMapOrientation(0);
        btnRotateToggle.setRotation(0);
        Toast.makeText(this, "지도가 정북으로 고정되었습니다.", Toast.LENGTH_SHORT).show();
    }

    /**
     * 지도의 회전을 업데이트하는 메서드입니다.
     */
    private void updateMapRotation() {
        if (isMapRotationEnabled) {
            map.setMapOrientation(heading);
            btnRotateToggle.setRotation(heading);
        }
    }

    /**
     * 새로운 방향(헤딩)을 설정하는 메서드입니다.
     *
     * @param newHeading 새로운 방향 (도 단위)
     */
    public void setHeading(float newHeading) {
        heading = newHeading;
        if (isMapRotationEnabled) {
            updateMapRotation();
        }
    }

    /**
     * 사용자의 위치를 설정하고 지도를 업데이트하는 메서드입니다.
     *
     * @param latitude 새로운 위도
     * @param longitude 새로운 경도
     */
    public void setUserLocation(double latitude, double longitude) {
        currentLocation = new GeoPoint(latitude, longitude);
        updateMapLocation();
        addPointToPath(currentLocation);
    }

    /**
     * 액티비티가 재개될 때 호출되는 메서드입니다.
     */
    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    /**
     * 액티비티가 일시 중지될 때 호출되는 메서드입니다.
     */
    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    /**
     * 사용자 위치를 지정된 델타만큼 이동시키는 메서드입니다.
     *
     * @param latDelta 위도 변화량
     * @param lonDelta 경도 변화량
     */
    private void moveLocation(double latDelta, double lonDelta) {
        double newLat = currentLocation.getLatitude() + latDelta;
        double newLon = currentLocation.getLongitude() + lonDelta;
        setUserLocation(newLat, newLon);
    }

    /**
     * 현재 위치 주변에 랜덤한 마커를 추가하는 메서드입니다.
     */
    private void addRandomMarkers() {
        clearRandomMarkers(); // 기존 마커 제거
        int markerCount = random.nextInt(21); // 0~20개의 마커 생성
        Drawable randomIcon = ContextCompat.getDrawable(this, R.drawable.icon_rv_marker);
        for (int i = 0; i < markerCount; i++) {
            GeoPoint randomPoint = getRandomPointInRadius(currentLocation, MARKER_RADIUS);
            Marker marker = new Marker(map);
            marker.setPosition(randomPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            marker.setIcon(randomIcon);
            map.getOverlays().add(marker);
            randomMarkers.add(marker);
        }
        map.invalidate();
        Toast.makeText(this, markerCount + "개의 마커가 추가되었습니다.", Toast.LENGTH_SHORT).show();
    }

    /**
     * 모든 랜덤 마커를 제거하는 메서드입니다.
     */
    private void clearRandomMarkers() {
        for (Marker marker : randomMarkers) {
            map.getOverlays().remove(marker);
        }
        randomMarkers.clear();
        map.invalidate();
        Log.d(TAG, "clearRandomMarkers: 마커 재거 완료");
    }

    /**
     * 주어진 중심점과 반경 내에서 랜덤한 위치를 생성하는 메서드입니다.
     *
     * @param center 중심점
     * @param radius 반경 (미터)
     * @return 랜덤하게 생성된 GeoPoint
     */
    private GeoPoint getRandomPointInRadius(GeoPoint center, double radius) {
        double x0 = center.getLongitude();
        double y0 = center.getLatitude();

        // 랜덤한 각도 (라디안)
        double randomAngle = random.nextDouble() * 2 * Math.PI;
        // 랜덤한 반지름 (미터를 도(degree)로 변환)
        double randomRadius = Math.sqrt(random.nextDouble()) * radius / 111320.0;

        // 극좌표를 직교좌표로 변환
        double dx = randomRadius * Math.cos(randomAngle);
        double dy = randomRadius * Math.sin(randomAngle);

        // 새로운 위도와 경도 계산
        double newLongitude = x0 + dx / Math.cos(Math.toRadians(y0));
        double newLatitude = y0 + dy;

        return new GeoPoint(newLatitude, newLongitude);
    }

    /**
     * 경로에 새로운 점을 추가하는 메서드입니다.
     *
     * @param point 추가할 GeoPoint
     */
    private void addPointToPath(GeoPoint point) {
        pathPoints.add(point);
        pathPolyline.setPoints(pathPoints);
        map.invalidate();
    }

    /**
     * 현재까지 그려진 경로를 초기화하는 메서드입니다.
     */
    private void clearPath() {
        pathPoints.clear();
        pathPolyline.setPoints(pathPoints);
        map.invalidate();
        Toast.makeText(this, "경로가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    /**
     * 사용자 위치 주변 5m 이내에 무작위로 front_rv를 추가하는 메서드입니다.
     */
    private void addFrontRv() {
        if (frontRvMarker != null) {
            map.getOverlays().remove(frontRvMarker);
        }

        GeoPoint frontRvPoint = getRandomPointInRadius(currentLocation, 5);
        frontRvMarker = new Marker(map);
        frontRvMarker.setPosition(frontRvPoint);
        frontRvMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        
        Drawable frontRvIcon = ContextCompat.getDrawable(this, R.drawable.icon_front_rv_marker);
        frontRvMarker.setIcon(frontRvIcon);
        
        map.getOverlays().add(frontRvMarker);
        map.invalidate();
        
        Toast.makeText(this, "Front RV가 추가되었습니다.", Toast.LENGTH_SHORT).show();
    }
}