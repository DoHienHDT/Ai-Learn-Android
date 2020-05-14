package jp.co.miosys.aiworldview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jp.co.miosys.aiworldview.Utils.NetworkUtil;
import jp.co.miosys.aiworldview.Utils.OkHttpService;
import jp.co.miosys.aiworldview.adapter.AdapterMemos;
import jp.co.miosys.aiworldview.connect_api.ApiService;
import jp.co.miosys.aiworldview.connect_api.RetrofitClient;
import jp.co.miosys.aiworldview.data_post_response.Category;
import jp.co.miosys.aiworldview.data_post_response.Memo;
import jp.co.miosys.aiworldview.fragment.VideoFragment;
import jp.co.miosys.aiworldview.static_value.ValueStatic;
import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore and Sceneform APIs.
 */
public class Camera3DActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener, LocationProvide.OnUpdateLocation,
        AdapterMemos.IOperationCategory {
    public int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    LocationManager manager = null;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    private boolean installRequested;
    private boolean hasFinishedLoading = false;
    private Snackbar loadingMessageSnackbar = null;
    private ArSceneView arSceneView; // arSceneView là 1 view camera được định nghĩa bởi google.
    private ArrayList<ViewRenderable> viewRenderables; // Tạo 1 list view chứa các view được kiết xuất
    private LocationScene locationScene; // Địa điểm của marker
    private ApiService apiService; // 1 lớp chứa các hàm kết nối tới server
    private List<Memo> memos;  // list memo chứa các thông tin về các marker trả về từ server
    private List<Category> listSelectCategory = new ArrayList<>(); // danh sách category trả về từ server
    private List<Category> categories = new ArrayList<>();
    private RecyclerView recyclerCategory;  // listview hiển thị category
    private AdapterMemos adapterMemos;  // adapter cho mỗi 1 category
    private ImageButton imbBack, imbNext, imbSetting;
    private TextView txtCategory, txtNumber, txtKilometer;

    private SeekBar sebRange;
    private LocationProvide locationProvide; // dối tượng quản lý update location
    private Location current;
    private boolean firstTimeGetGPS = false;
    private ArrayList<CompletableFuture<ViewRenderable>> completableFutures; // đối tượng list quản lý phần chạy không đồng bộ khi tạo view Renderable
    private int distance = ValueStatic.DISTANCE_MAX;
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

        }
    };

    public Camera3DActivity() {
        super();
    }

    // Đọc nội dung text của một file json local.
    private static String readText(Context context) throws IOException {
        InputStream is = context.getAssets().open("pin.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = br.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    // CompletableFuture requires api level 24
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);

//        this.setFinishOnTouchOutside(false);
        arSceneView = findViewById(R.id.ar_scene_view);
        initComponent();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }

    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    private void initComponent() {
        recyclerCategory = findViewById(R.id.recycler_list_category);
        imbBack = findViewById(R.id.imbBack);
        imbSetting = findViewById(R.id.imbSetting);
        imbNext = findViewById(R.id.imbNext);
        txtCategory = findViewById(R.id.textCategory);
        txtNumber = findViewById(R.id.textNumber);
        txtKilometer = findViewById(R.id.textKilometer);
        sebRange = findViewById(R.id.sebDistance);

        apiService = RetrofitClient.getClient(ValueStatic.URL).create(ApiService.class);
        memos = new ArrayList<>();
        viewRenderables = new ArrayList<>();
        completableFutures = new ArrayList<>();
        locationProvide = new LocationProvide(this, this);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        imbNext.setOnClickListener(this);
        imbBack.setOnClickListener(this);
        imbSetting.setOnClickListener(this);
        sebRange.setOnSeekBarChangeListener(this);
    }

    private void onRefreshAnchors() {
        if (locationScene != null) {
            Iterator<LocationMarker> itr = locationScene.mLocationMarkers.iterator();
            while (itr.hasNext()) {
                LocationMarker number = itr.next();
                if (number.anchorNode != null) {
                    number.anchorNode.getAnchor().detach();
                    number.anchorNode.setAnchor(null);
                    number.anchorNode.setEnabled(false);
                    number.anchorNode = null;
                }
                itr.remove();
            }
            locationScene.refreshAnchors();
        }
    }

    private void connectApi() {
        boolean checkConnect = NetworkUtil.isNetworkConnected(this);
        if (checkConnect) {
//            getListCategory(); // Lấy danh sách category từ server
//            loadMarkerFromServer();
            loadMarkerFromJsonLocal(); // lấy danh sách marker từ server
        } else {
            Toast.makeText(this, "Check Connect Internet", Toast.LENGTH_LONG).show();
        }
    }

    private void initComponentProcessMarker() {
        if (memos != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            // Create views by adding to completableFutures
            Log.d("length", String.valueOf(memos.size()));
            for (int i = 0; i < memos.size(); i++) {
                View eView = layoutInflater.inflate(R.layout.example_layout, null);//exampleLayoutRenderable.getView();
                ImageView imgPin = eView.findViewById(R.id.imgPin);
                Memo memo = memos.get(i);
                switch (memo.getImagePin()) {
                    case "pin01": {
                        imgPin.setImageResource(R.drawable.black_pin);
                        break;
                    }
                    case "pin02": {
                        imgPin.setImageResource(R.drawable.blue_pin);
                        break;
                    }
                    case "pin03": {
                        imgPin.setImageResource(R.drawable.green_pin);
                        break;
                    }
                    case "pin04": {
                        imgPin.setImageResource(R.drawable.orange_pin);
                        break;
                    }
                }



                        completableFutures.add(ViewRenderable.builder()
                                .setView(Camera3DActivity.this, eView)//R.layout.example_layout)
                                .build());
            }


            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).handle(
                    (notUsed, throwable) -> {

                        if (throwable != null) {
                            DemoUtils.displayError(Camera3DActivity.this, "Unable to load renderables", throwable);
                            return null;
                        }
                        try {
                            for (int i = 0; i < memos.size(); i++) {
                                viewRenderables.add(completableFutures.get(i).get());
                            }
                            hasFinishedLoading = true;
                        } catch (InterruptedException | ExecutionException ex) {
                            DemoUtils.displayError(Camera3DActivity.this, "Unable to load renderables", ex);
                        }
                        return null;
                    });
            // Set an update listener on the Scene that will hide the loading message once a Plane is
            // detected.
            arSceneView
                    .getScene()
                    .addOnUpdateListener(
                            frameTime -> {
                                if (!hasFinishedLoading) {
                                    return;
                                }

                                // Once
                                if (locationScene == null) {
                                    // If our locationScene object hasn't been setup yet, this is a good time to do it
                                    // We know that here, the AR components have been initiated.
                                    locationScene = new LocationScene(this, arSceneView);
                                }

                                setDisplayPoint(ValueStatic.DISTANCE_MAX);
                                // Once
//                                if (firstTimeGetGPS) {
//                                    if (current != null) {
//                                        setDisplayPoint(ValueStatic.DISTANCE_MAX);
////                                        firstTimeGetGPS = false;
//                                    }
//                                }

                                Frame frame = arSceneView.getArFrame();
                                if (frame == null) {
                                    return;
                                }

                                if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                    return;
                                }

                                if (locationScene != null) {
                                    locationScene.processFrame(frame);
                                }

                                if (loadingMessageSnackbar != null) {
                                    for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                        if (plane.getTrackingState() == TrackingState.TRACKING) {
                                            hideLoadingMessage();
                                        }
                                    }
                                }
                            });
        }


        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        ARLocationPermissionHelper.requestPermission(this);
    }

    private void addMarker(ViewRenderable exampleLayoutRenderable, double lat, double lng, Memo memo, int distance) {
        LocationMarker layoutLocationMarker = new LocationMarker(lng, lat, getExampleView(exampleLayoutRenderable, memo));
//        layoutLocationMarker.setOnlyRenderWhenWithin(distance);
        // An example "onRender" event, called every frame
        // Updates the layout with the markers distance
        layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
            @Override
            public void render(LocationNode node) {
            }
        });
        locationScene.mLocationMarkers.add(layoutLocationMarker);
    }

    private void setDisplayPoint(int distance) {
        for (int i = 0; i < memos.size(); i++) {
            if (listSelectCategory.size() == categories.size()) {

                addMarker(viewRenderables.get(i), Double.valueOf(memos.get(i).getLat()), Double.valueOf(memos.get(i).getLng()), memos.get(i), distance);
            } else {
                for (int j = 1; j < listSelectCategory.size(); j++)
                    if (listSelectCategory.get(j).getId() == memos.get(i).getCategoryId()) {
                        addMarker(viewRenderables.get(i), Double.valueOf(memos.get(i).getLat()), Double.valueOf(memos.get(i).getLng()), memos.get(i), distance);
                    }
            }
        }
    }

    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    current = location;
                                    connectApi();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    /**
     * Make sure we call locationScene.resume();
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (checkPermissions()) {
            getLastLocation();
        }

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
    }

    /**
     * Make sure we call locationScene.pause();
     */
    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onUpdate(Location mCurrentLocation) {
        this.current = mCurrentLocation;
        if (!firstTimeGetGPS & locationScene != null) {
//            setDisplayPoint(ValueStatic.DISTANCE_MAX);
            firstTimeGetGPS = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(this);
            } else {
                Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideNavigation(flags);
    }

    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar = Snackbar.make(Camera3DActivity.this.findViewById(android.R.id.content), R.string.plane_finding, Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }

    private void showComponent() {
        imbSetting.setVisibility(View.GONE);
        imbNext.setVisibility(View.VISIBLE);
        txtCategory.setVisibility(View.VISIBLE);
        sebRange.setVisibility(View.VISIBLE);
        txtNumber.setVisibility(View.VISIBLE);
        txtKilometer.setVisibility(View.VISIBLE);
        recyclerCategory.setVisibility(View.VISIBLE);
    }

    private void hideComponent() {
        imbNext.setVisibility(View.GONE);
        txtCategory.setVisibility(View.GONE);
        sebRange.setVisibility(View.GONE);
        txtNumber.setVisibility(View.GONE);
        txtKilometer.setVisibility(View.GONE);
        imbSetting.setVisibility(View.VISIBLE);
        recyclerCategory.setVisibility(View.GONE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (i < 1)
            sebRange.setProgress(1);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int range = sebRange.getProgress();
        distance = range * 1000;
        if (range > 0 && range <= 1) {
            sebRange.setProgress(1);
            txtNumber.setText(String.valueOf(1));
        } else if (range > 1 && range <= 2) {
            sebRange.setProgress(2);
            txtNumber.setText(String.valueOf(2));
        } else if (range > 1 && range <= 3) {
            sebRange.setProgress(3);
            txtNumber.setText(String.valueOf(3));
        } else if (range > 1 && range <= 4) {
            sebRange.setProgress(4);
            txtNumber.setText(String.valueOf(5));
        } else if (range > 1 && range <= 5) {
            sebRange.setProgress(5);
            txtNumber.setText(String.valueOf(10));
        } else if (range > 1 && range <= 6) {
            sebRange.setProgress(6);
            txtNumber.setText(String.valueOf(15));
        } else if (range > 1 && range <= 7) {
            sebRange.setProgress(7);
            txtNumber.setText(String.valueOf(20));
        }
        setDisplayPoint(distance);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imbSetting: {
                showComponent();
                break;
            }
            case R.id.imbNext: {
                hideComponent();
                break;
            }
            case R.id.imbBack: {
                onBackPressed();
                break;
            }
        }
    }

    @Override
    public void onStopUpdate() {

    }

    @Override
    public void sendIdCategory(Category category) {
        if (category.getId() == 123456) {
            if (category.isSelect())
                listSelectCategory.removeAll(listSelectCategory);
            else
                listSelectCategory.addAll(categories);
        } else {
            if (!category.isSelect())
                listSelectCategory.add(category);
            else
                listSelectCategory.remove(category);
        }
        setDisplayPoint(distance);
    }

    private void addFragment(Memo memo) {
        VideoFragment videoFragment = VideoFragment.newInstance(memo);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            transaction.remove(prev);
        }
        transaction.addToBackStack(null);
        videoFragment.show(transaction, "dialog");
    }

    public void hideNavigation(final int uiOption) {
        //  This work only for android 4.4+
        final View decorView = getWindow().getDecorView();
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            getWindow().getDecorView().setSystemUiVisibility(uiOption);
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(uiOption);
                }
            });
        }
    }

    private void initRecyclerCategory(List<Category> categoryList) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerCategory.setLayoutManager(layoutManager);

        adapterMemos = new AdapterMemos(this, categoryList);
        recyclerCategory.setAdapter(adapterMemos);
    }

    private void getListCategory() {
//        Call<BaseDataResponse<List<Category>>> call;
//        call = apiService.apiGetListCategory("Bearer ");
//        call.enqueue(new Callback<BaseDataResponse<List<Category>>>() {
//            @Override
//            public void onResponse(Call<BaseDataResponse<List<Category>>> call, Response<BaseDataResponse<List<Category>>> response) {
//                if (response.isSuccessful()) {
//                    if (response.body() != null) {
//                        if (response.body().getData() != null) {
//                            Category category = new Category();
//                            category.setId(123456);
//                            category.setName("UnSelect All");
//                            response.body().getData().add(0, category);
//                            for (int i = 0; i < response.body().getData().size(); i++) {
//                                response.body().getData().get(i).setSelect(true);
//                            }
//                            listSelectCategory.addAll(response.body().getData());
//                            categories.addAll(response.body().getData());
//                            initRecyclerCategory(response.body().getData());
//                        }
//                    } else {
//                        Toast.makeText(getBaseContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(getBaseContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<BaseDataResponse<List<Category>>> call, Throwable t) {
//                Toast.makeText(getBaseContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
//            }
//        });
        new OkHttpService(OkHttpService.Method.GET, false, this, ValueStatic.URL_CATEGORY, null, false) {

            @Override
            public void onFailureApi(okhttp3.Call call, Exception e) {

            }

            @Override
            public void onResponseApi(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String result = response.body().string();
                Log.d("resultRespon", result);

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray data = jsonObject.getJSONArray("data");
                    Log.d("category", result);
//                    Category category = new Category();
//                    category.setId(123456);
//                    category.setName("UnSelect All");
////                    category.get
                    Log.d("dataRespon", data.toString());
//                    listSelectCategory.add;
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Camera3DActivity.this, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }

            }
        };

    }

    /**
     * Example node of a layout
     */
    @SuppressLint("ClickableViewAccessibility")
    private Node getExampleView(ViewRenderable exampleLayoutRenderable, Memo memo) {
        Node base = new Node();
        base.setRenderable(exampleLayoutRenderable);
        // Add  listeners etc here
        View eView = exampleLayoutRenderable.getView();
        eView.setOnTouchListener((v, event) -> {
            addFragment(memo);
            return false;
        });

        return base;
    }


    /***
     * Example Node of a 3D model
     *
     * @return
     */
    private Node getAndy() {
        Node base = new Node();

//        base.setRenderable(andyRenderable);
        Context c = this;
        base.setOnTapListener((v, event) -> {
            Toast.makeText(
                    c, "Andy touched.", Toast.LENGTH_LONG)
                    .show();
        });
        return base;
    }

    private void loadMarkerFromServer() {

        Map<String, Object> params = new HashMap<>();
        params.put("username", ValueStatic.userName);
        params.put("lat", String.valueOf(current.getLatitude()));
        params.put("lng", String.valueOf(current.getLongitude()));
        params.put("square", "100");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View eView = layoutInflater.inflate(R.layout.example_layout, null);
        ImageView imgPin = eView.findViewById(R.id.imgPin);

        new OkHttpService(OkHttpService.Method.GET, this, ValueStatic.URL_LIST_MEMO, params, false) {
            @Override
            public void onFailureApi(okhttp3.Call call, Exception e) {
                Toast.makeText(Camera3DActivity.this, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponseApi(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    Log.d("Demo",jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String pin_type =  jsonArray.getJSONObject(i).getString("pin_type");
                        if (isNullOrEmpty(pin_type)) {
                        } else {
                            Memo memo = new Memo();
                            memo.setCategoryName(jsonArray.getJSONObject(i).getString("title"));
                            memo.setLat(jsonArray.getJSONObject(i).getString("lat"));
                            memo.setLng(jsonArray.getJSONObject(i).getString("lng"));
                            memo.setLinkVideo(jsonArray.getJSONObject(i).getString("link_pdf"));
                            memo.setImagePin(jsonArray.getJSONObject(i).getString("pin_type"));
                            memos.add(memo);
                        }
                    }

                    initComponentProcessMarker();
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Camera3DActivity.this, "会社コードが不正です。", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        };
    }

    public static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }

    private void loadMarkerFromJsonLocal() {
//        proLoadMarker.setVisibility(View.GONE);
        try {
            String jsonText = readText(this);
            JSONObject jsonRoot = new JSONObject(jsonText);
            JSONArray jsonArray = jsonRoot.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                Memo memo = new Memo();
                memo.setCategoryName(jsonArray.getJSONObject(i).getString("name"));
                memo.setLat(jsonArray.getJSONObject(i).getString("lat"));
                memo.setLng(jsonArray.getJSONObject(i).getString("long"));
                memo.setLinkVideo(jsonArray.getJSONObject(i).getString("url"));
                memo.setImagePin(jsonArray.getJSONObject(i).getString("image"));
                memos.add(memo);
            }
//            locationProvide.startUpdatesButtonHandler();
            initComponentProcessMarker();
        } catch (Exception e) {
            Log.e("msg", e.getMessage());
//            proLoadMarker.setVisibility(View.GONE);
        }
    }

}

