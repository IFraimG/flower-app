package com.example.ecoapp.fragments;

import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.ecoapp.R;
import com.example.ecoapp.databinding.FragmentMapBinding;
import com.example.ecoapp.domain.helpers.PermissionHandler;
import com.example.ecoapp.domain.helpers.StorageHandler;
import com.example.ecoapp.models.EventCustom;
import com.example.ecoapp.presentation.viewmodels.EventViewModel;
import com.example.ecoapp.presentation.viewmodels.MapViewModel;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements UserLocationObjectListener, DrivingSession.DrivingRouteListener {

    private FragmentMapBinding binding;
    private WeakReference<FragmentMapBinding> mBinding;
    private MapView mapView;
    private LocationListener locationListener;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private boolean isAvailableLocation = false;
    private LocationManager locationManager;
    private final Animation pingAnimation = new Animation(Animation.Type.SMOOTH, 0);
    private Point myPoint;
    private EventViewModel viewModel;
    private PermissionHandler permissionHandler;
    private Bundle bundle;
    private Geocoder geoCoder;
    ArrayList<EventCustom> eventCustoms;
    private final InputListener listener = new InputListener() {
        @Override
        public void onMapTap(@NonNull Map map, @NonNull Point point) {
            double latitude = point.getLatitude();
            double longitude = point.getLongitude();

            if (bundle != null) bundle.clear();
            bundle = new Bundle();
            bundle.putDouble("lat", latitude);
            bundle.putDouble("long", longitude);

            try {
                List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String fullAddress = address.getAddressLine(0);
                    bundle.putString("address", fullAddress);

                    boolean isTrue = false;

                    for (EventCustom eventCustom: eventCustoms) {
                        if (eventCustom.getPlace().equals(fullAddress)) {
                            isTrue = true;
                            bundle.putString("eventID", eventCustom.getEventID());
                            break;
                        }
                    }

                    binding.mapCardView.setVisibility(View.VISIBLE);
                    binding.mapCoords.setText(fullAddress);

                    if (isTrue) {
                        binding.mapButtonOneEvent.setOnClickListener(v -> {
                            Navigation.findNavController(v).navigate(R.id.eventFragment, bundle);
                        });
                        binding.mapButton.setVisibility(View.GONE);
                        binding.mapButtonOneEvent.setVisibility(View.VISIBLE);
                    } else {
                        binding.mapButton.setVisibility(View.VISIBLE);
                        binding.mapButtonOneEvent.setVisibility(View.GONE);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMapLongTap(@NonNull Map map, @NonNull Point point) {}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionHandler = new PermissionHandler();
        if (permissionHandler != null) permissionHandler.requestMapPermissions((AppCompatActivity) requireActivity());

        SearchFactory.initialize(requireContext());
        DirectionsFactory.initialize(requireContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        if (locationListener != null) locationManager.unsubscribe(locationListener);
        super.onStop();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater);
        mBinding = new WeakReference<>(binding);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        this.mapView = binding.mapview;

        if (permissionHandler != null && permissionHandler.checkPermissions((AppCompatActivity) requireActivity())) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
                    if (locationStatus == LocationStatus.AVAILABLE) isAvailableLocation = true;
                }

                @Override
                public void onLocationUpdated(@NonNull com.yandex.mapkit.location.Location location) {
                    myPoint = location.getPosition();
                }
            };

            locationManager = MapKitFactory.getInstance().createLocationManager();
            if (locationListener != null) locationManager.subscribeForLocationUpdates(0, 0, 0, false, FilteringMode.OFF, locationListener);
        }

        initMap();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBinding.clear();

        if (mapView != null) mapView.getMap().getMapObjects().clear();
    }

    /**
     * Инициализация параметров отображения яндекс карты
     * Отображение меток магазинов на карте
     * Отображение метки пользователя по умолчанию
     * Запрос на включение геолокации
     */
    private void initMap() {
        if (permissionHandler != null) permissionHandler.requestMapPermissions((AppCompatActivity) requireActivity());

        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        mapView.getMap().move(new CameraPosition(new Point(55.71989101308894, 37.5689757769603), 14, 0, 0), pingAnimation, null);

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();

        binding.mapButton.setOnClickListener(v -> {
            if (bundle != null) {
                Navigation.findNavController(v).navigate(R.id.createEventFragment, bundle);
            }
        });

        geoCoder = new Geocoder(requireContext(), Locale.getDefault());


        viewModel.getEventsList().observe(requireActivity(), eventsList -> {
            ImageProvider imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.place_mark);
            eventCustoms = eventsList;
            for (EventCustom event: eventsList) {
                if (event.getLongt() != 0 && event.getLat() != 0) {
                    mapObjects.addPlacemark(new Point(event.getLat(), event.getLongt()), imageProvider);
                }
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView.getMap().addInputListener(listener);
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        for (DrivingRoute route: list) {
            mapObjects.addPolyline(route.getGeometry());
        }
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {

    }

    @Override
    public void onObjectAdded(UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float)(mapView.getWidth() * 0.5), (float)
                        (mapView.getHeight() * 0.5)),
                new PointF((float)(mapView.getWidth() * 0.5), (float)
                        (mapView.getHeight() * 0.83)));

        userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                requireContext(), R.drawable.add_guide_icon));

        CompositeIcon pinIcon = userLocationView.getPin().useCompositeIcon();

        pinIcon.setIcon("icon", ImageProvider.fromResource(requireContext(), R.drawable.add_guide_icon),
                new IconStyle().setAnchor(new PointF(0f, 0f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(0f)
                        .setScale(1f)
        );
    }

    @Override
    public void onObjectRemoved(@NotNull UserLocationView view) {}

    @Override
    public void onObjectUpdated(@NotNull UserLocationView view, @NotNull ObjectEvent event) {}
}

