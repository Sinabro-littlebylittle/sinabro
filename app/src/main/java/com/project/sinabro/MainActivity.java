package com.project.sinabro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener,Runnable {

    /**
     * ?????? ?????? ?????? ????????? ?????????
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1981;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 2981;

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Callback for Location events.
     */

    private MapView mapView;
    private ViewGroup mapViewContainer;
    private MapPoint currPoint, prevPoint;
    private MapPOIItem marker;

    private ArrayList<MapPOIItem> markers = new ArrayList<>();

    private LocationCallback mLocationCallback;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mLastLocation;
    private Button currentLocation_btn, mapZoomIn_btn, mapZoomOut_btn, peopleCount_btn, editLocation_btn, bookmarkEmpty_btn;

    private ImageButton hamburger_ibtn;

    private DrawerLayout drawerlayout;

    private BottomSheetBehavior bottomSheetBehavior;

    /**
     * ?????? MainActivity??? context, activity??????
     * ?????? ???????????? ???????????? ?????? ???????????? ?????????
     */
    private Context context = this;
    private Activity activity = this;

    /**
     * ?????? ????????? ?????? ?????? ??????
     */
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    //???????????? ?????? ??????
    private ResultView mResultView;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;

    //?????? ?????? ?????? ?????? ??????
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** ?????? ???????????? ????????? ????????? ?????? */
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        /** ?????? ?????? ????????? ?????? ???????????? ????????? */
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);

        /** ????????? ???????????? ?????? ???????????? ?????? ????????? ??? */
//        for (int k = 0; k < markers.size(); k++) {
//            marker = new MapPOIItem();
//            marker.setItemName("?????? ??????(" + (k + 1) + ")");
//            marker.setTag(k);
//            /** DB?????? ????????? ?????? ????????? ?????? ?????? ?????? ???????????? ???????????? ??? */
//            // prevPoint = MapPoint.mapPointWithGeoCoord(36.628986, 127.456355);
//            // marker.setMapPoint(prevPoint);
//            marker.setMarkerType(MapPOIItem.MarkerType.RedPin);
//            // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
//            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
//
//            mapView.addPOIItem(marker);
//            markers.add(marker);
//        }

        /** ??? ?????? ?????? ??? ?????? ?????? ?????? ????????? ?????????
         * (?????? ?????? ??????) ??? (?????? ?????? ??????)??? ?????? */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocation();
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            /** ???????????? ?????? ?????? */
            GetMyLocation getMyLocation = new GetMyLocation(this, this);
            Location userLocation = getMyLocation.getMyLocation();
            if (userLocation != null) {
                double latitude = userLocation.getLatitude();
                double longitude = userLocation.getLongitude();
                System.out.println("////////////?????? ??? ????????? : " + latitude + "," + longitude);
                currPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);

                /** ????????? ?????? */
                mapView.setMapCenterPoint(currPoint, true);
            }
        }

        /** ?????? ?????? ?????? ?????? */
        init();

        /** ?????? ?????? ?????? ?????? ??? ????????? ????????? ?????? */
        currentLocation_btn = (Button) findViewById(R.id.currentLocation_btn);
        currentLocation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocation();
            }
        });

        /** ?????? ?????? ?????? ?????? ??? ?????? ?????? */
        mapZoomIn_btn = (Button) findViewById(R.id.mapZoomIn_btn);
        mapZoomIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomIn(true);
            }
        });

        /** ?????? ?????? ?????? ?????? ??? ?????? ?????? */
        mapZoomOut_btn = (Button) findViewById(R.id.mapZoomOut_btn);
        mapZoomOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomOut(true);
            }
        });

        /** ========================= Sidebar Navigation Drawer(????????? ?????????) ========================= */
        drawerlayout = findViewById(R.id.drawer_layout);
        /** ????????? ?????? ?????? ??? sidebar navigation??? ??????????????? ?????? ?????? */
        hamburger_ibtn = (ImageButton) findViewById(R.id.hamburger_ibtn);
        hamburger_ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sidebar navigation??? ?????????
                drawerlayout.openDrawer(GravityCompat.START);
                // bottom sheet layout??? ???????????? ???
                bottomSheetBehavior.setPeekHeight(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        /** sidebar navigation ?????? menu item(??????) ?????? ??? ?????? ???????????????
         *  ????????? ??? ????????? ?????? ?????? */
        NavigationView navigationView = findViewById(R.id.navigationView);
        NavController navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        /** ========================= bottom sheet ???????????? ========================= */
//        bottomSheet_layout = (FrameLayout) findViewById(R.id.bottomSheet_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet_layout));
//        bottomSheetBehavior.setPeekHeight(200);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//            }
//        });

        mResultView = findViewById(R.id.resultView);
//        mResultView.setVisibility(View.INVISIBLE);
        /** ????????? ?????? ?????? */
        peopleCount_btn = findViewById(R.id.peopleCount_btn);
        peopleCount_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ????????? ????????? ???????????? ???????????? ????????? ???????????? ?????????.
                Log.d("?????????", "/////////?????????//////////");
                finish();
                final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });

        /** ?????? ??????/?????? ?????? */
        editLocation_btn = findViewById(R.id.editLocation_btn);
        editLocation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ????????? ?????? ?????? ??????????????? ???????????? ????????? ???????????? ?????????.
            }
        });

        /** ????????? ??????/?????? ?????? */
        bookmarkEmpty_btn = findViewById(R.id.bookmarkEmpty_btn);
        bookmarkEmpty_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ????????? ????????? ?????? ??????????????? ???????????? ????????? ???????????? ?????????.
            }
        });



        //?????? ??????..
        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "yolov5s.torchscript.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("\n\nre"+requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        mBitmap = (Bitmap) data.getExtras().get("data");
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90.0f);
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        //mImageView.setImageBitmap(mBitmap);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                mBitmap = BitmapFactory.decodeFile(picturePath);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90.0f);
                                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                //mImageView.setImageBitmap(mBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);

        runOnUiThread(() -> {
            //mButtonDetect.setEnabled(true);
            //mButtonDetect.setText(getString(R.string.detect));
            //mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.setResults(results);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);
        });
    }

    private void init() {
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20 * 1000);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocation() {
        if (isPermissionGranted()) startLocationUpdates();
        else requestPermissions();
    }

    private boolean isPermissionGranted() {
        for (String permission : PERMISSIONS) {
            if (permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                continue;
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (PackageManager.PERMISSION_GRANTED != result)
                return false;
        }
        return true;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                /** ???????????? ?????? ?????? */
                GetMyLocation getMyLocation = new GetMyLocation(context, activity);
                Location userLocation = getMyLocation.getMyLocation();
                if (userLocation != null) {
                    double latitude = userLocation.getLatitude();
                    double longitude = userLocation.getLongitude();
                    System.out.println("////////////?????? ??? ????????? : " + latitude + "," + longitude);
                    currPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);

                    /** ????????? ?????? */
                    mapView.setMapCenterPoint(currPoint, true);
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    resolveLocationSettings(e);
                }
            }
        });
    }

    public void resolveLocationSettings(Exception exception) {
        ResolvableApiException resolvable = (ResolvableApiException) exception;
        try {
            resolvable.startResolutionForResult(this, REQUEST_CODE_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                startLocationUpdates();
            } else {
                // Permission denied.
                for (String permission : permissions) {
                    if ("android.permission.ACCESS_FINE_LOCATION".equals(permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                        builder.setTitle("??????");
                        builder.setMessage("?????? ????????? ?????? ?????? ????????? ????????? ?????????. (????????????)");
                        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                /** ?????? ?????? ??????????????? '???????????? ??????' ?????? ??? */
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                /** ?????? ?????? ??????????????? '??????' ?????? ??? */
//                                Toast.makeText(MainActivity.this, "Cancel Click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(0, 133, 254));
                                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(123, 123, 123));
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        }
    }

    /**
     * ???????????? ????????? ?????????
     */
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
    }

    /**
     * ?????? ??? ??? ?????? ???
     */
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        /** ????????? DB??? ???????????? ?????? ?????? ???????????? ?????????
         *  ?????? ????????? ???????????? ????????? ?????? ???????????? ?????????
         *  ?????????????????????. ????????? ?????? ?????? ????????? ????????????
         *  ?????? ????????? ????????? ???????????????. */
        marker = new MapPOIItem();
        if (markers.size() == 1) {
            mapView.removePOIItem(markers.get(markers.size() - 1));
            markers.remove(markers.size() - 1);
        }
        marker.setItemName("????????? ??????(" + (markers.size() + 1) + ")");
        marker.setTag(markers.size());
        marker.setMapPoint(mapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.YellowPin); // ???????????? ???????????? BluePin ?????? ??????.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.

        mapView.addPOIItem(marker);
        markers.add(marker);

        bottomSheetBehavior.setPeekHeight(85);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * ?????? ??? ??? ?????? ???
     */
    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        mapView.zoomIn(true);
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }

}