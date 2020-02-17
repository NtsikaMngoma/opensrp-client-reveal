package org.smartregister.reveal.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowProgressDialog;
import org.robolectric.shadows.ShadowToast;
import org.smartregister.Context;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Task;
import org.smartregister.family.util.Constants.INTENT_KEY;
import org.smartregister.reveal.BaseUnitTest;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseDrawerContract;
import org.smartregister.reveal.model.FamilyCardDetails;
import org.smartregister.reveal.model.IRSVerificationCardDetails;
import org.smartregister.reveal.model.MosquitoHarvestCardDetails;
import org.smartregister.reveal.model.SprayCardDetails;
import org.smartregister.reveal.model.TaskFilterParams;
import org.smartregister.reveal.presenter.ListTaskPresenter;
import org.smartregister.reveal.presenter.ValidateUserLocationPresenter;
import org.smartregister.reveal.util.CardDetailsUtil;
import org.smartregister.reveal.util.Constants.Intervention;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.RevealMapHelper;
import org.smartregister.reveal.util.TestingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.layers.BoundaryLayer;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static io.ona.kujaku.utils.Constants.RequestCode.LOCATION_SETTINGS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.smartregister.receiver.SyncStatusBroadcastReceiver.SyncStatusListener;
import static org.smartregister.receiver.SyncStatusBroadcastReceiver.getInstance;
import static org.smartregister.receiver.SyncStatusBroadcastReceiver.init;
import static org.smartregister.reveal.util.Constants.ANIMATE_TO_LOCATION_DURATION;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.FIRST_NAME;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.TASK_ID;
import static org.smartregister.reveal.util.Constants.Filter.FILTER_SORT_PARAMS;
import static org.smartregister.reveal.util.Constants.Intervention.BEDNET_DISTRIBUTION;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.Properties.TASK_IDENTIFIER;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_FAMILY_PROFILE;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_FILTER_TASKS;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_TASK_LISTS;

/**
 * Created by samuelgithengi on 1/23/20.
 */
public class ListTasksActivityTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ListTasksActivity listTasksActivity;


    private android.content.Context context = RuntimeEnvironment.application;

    private ImageButton myLocationButton = new ImageButton(context);

    private ImageButton layerSwitcherFab = new ImageButton(context);

    @Mock
    private ListTaskPresenter listTaskPresenter;

    @Mock
    private RevealMapHelper revealMapHelper;

    @Mock
    private BaseDrawerContract.View drawerView;

    @Mock
    private Feature feature;

    @Mock
    private RevealMapView kujakuMapView;

    @Mock
    private GeoJsonSource geoJsonSource;


    @Mock
    private CardDetailsUtil cardDetailsUtil;

    @Mock
    private MapboxMap mMapboxMap;

    @Mock
    private ValidateUserLocationPresenter locationPresenter;

    @Mock
    private ILocationClient locationClient;

    @Captor
    private ArgumentCaptor<BoundaryLayer> boundaryLayerArgumentCaptor;

    @Captor
    private ArgumentCaptor<CameraPosition> cameraPositionArgumentCaptor;

    @Before
    public void setUp() {
        Context.bindtypes = new ArrayList<>();
        listTasksActivity = Robolectric.buildActivity(ListTasksActivity.class).create().get();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 30;
        myLocationButton.setLayoutParams(params);
        Whitebox.setInternalState(listTasksActivity, "myLocationButton", myLocationButton);
        Whitebox.setInternalState(listTasksActivity, "cardDetailsUtil", cardDetailsUtil);
    }


    @Test
    public void testOnCreate() {
        assertNotNull(Robolectric.buildActivity(ListTasksActivity.class).create().get());
    }


    @Test
    public void testCloseSprayCardView() {
        View sprayCardView = listTasksActivity.findViewById(R.id.spray_card_view);
        sprayCardView.setVisibility(VISIBLE);
        listTasksActivity.closeCardView(R.id.btn_collapse_spray_card_view);
        assertEquals(GONE, sprayCardView.getVisibility());
    }

    @Test
    public void testCloseMosquitoCardView() {
        View cardView = listTasksActivity.findViewById(R.id.mosquito_collection_card_view);
        cardView.setVisibility(VISIBLE);
        listTasksActivity.closeCardView(R.id.btn_collapse_mosquito_collection_card_view);
        assertEquals(GONE, cardView.getVisibility());
    }


    @Test
    public void testCloseCardLarvalCardView() {
        View cardView = listTasksActivity.findViewById(R.id.larval_breeding_card_view);
        cardView.setVisibility(VISIBLE);
        listTasksActivity.closeCardView(R.id.btn_collapse_larval_breeding_card_view);
        assertEquals(GONE, cardView.getVisibility());
    }

    @Test
    public void testClosePAOTCardView() {
        View cardView = listTasksActivity.findViewById(R.id.potential_area_of_transmission_card_view);
        cardView.setVisibility(VISIBLE);
        listTasksActivity.closeCardView(R.id.btn_collapse_paot_card_view);
        assertEquals(GONE, cardView.getVisibility());
    }

    @Test
    public void testCloseIndicatorsCardView() {
        View cardView = listTasksActivity.findViewById(R.id.indicators_card_view);
        cardView.setVisibility(VISIBLE);
        listTasksActivity.closeCardView(R.id.btn_collapse_indicators_card_view);
        assertEquals(GONE, cardView.getVisibility());
    }

    @Test
    public void testCloseVerificationCardView() {
        View cardView = listTasksActivity.findViewById(R.id.irs_verification_card_view);
        cardView.setVisibility(VISIBLE);
        listTasksActivity.closeCardView(R.id.btn_collapse_irs_verification_card_view);
        assertEquals(GONE, cardView.getVisibility());
    }


    @Test
    public void testCloseAllCardViews() {
        listTasksActivity.findViewById(R.id.spray_card_view).setVisibility(VISIBLE);
        listTasksActivity.findViewById(R.id.mosquito_collection_card_view).setVisibility(VISIBLE);
        listTasksActivity.findViewById(R.id.larval_breeding_card_view).setVisibility(VISIBLE);
        listTasksActivity.findViewById(R.id.potential_area_of_transmission_card_view).setVisibility(VISIBLE);
        listTasksActivity.findViewById(R.id.indicators_card_view).setVisibility(VISIBLE);
        listTasksActivity.findViewById(R.id.irs_verification_card_view).setVisibility(VISIBLE);
        listTasksActivity.closeAllCardViews();
        assertEquals(GONE, listTasksActivity.findViewById(R.id.spray_card_view).getVisibility());
        assertEquals(GONE, listTasksActivity.findViewById(R.id.mosquito_collection_card_view).getVisibility());
        assertEquals(GONE, listTasksActivity.findViewById(R.id.larval_breeding_card_view).getVisibility());
        assertEquals(GONE, listTasksActivity.findViewById(R.id.potential_area_of_transmission_card_view).getVisibility());
        assertEquals(GONE, listTasksActivity.findViewById(R.id.indicators_card_view).getVisibility());
        assertEquals(GONE, listTasksActivity.findViewById(R.id.irs_verification_card_view).getVisibility());
    }

    @Test
    public void testPositionMyLocation() {
        listTasksActivity.positionMyLocationAndLayerSwitcher();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
        assertEquals(0, layoutParams.topMargin);
        assertEquals(30, layoutParams.bottomMargin);
        assertEquals(Gravity.BOTTOM | Gravity.END, layoutParams.gravity);
    }

    @Test
    public void testPositionMyLocationZambia() {
        listTasksActivity = spy(listTasksActivity);
        when(listTasksActivity.getBuildCountry()).thenReturn(Country.ZAMBIA);
        Whitebox.setInternalState(listTasksActivity, "myLocationButton", myLocationButton);
        listTasksActivity.positionMyLocationAndLayerSwitcher();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) myLocationButton.getLayoutParams();
        assertEquals(0, layoutParams.topMargin);
        int progressHeight = context.getResources().getDimensionPixelSize(R.dimen.progress_height);
        assertEquals(progressHeight + 40, layoutParams.bottomMargin);
        assertEquals(Gravity.BOTTOM | Gravity.END, layoutParams.gravity);
    }


    @Test
    public void testPositionLayerSwitcher() {
        listTasksActivity = spy(listTasksActivity);
        when(listTasksActivity.getBuildCountry()).thenReturn(Country.ZAMBIA);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layerSwitcherFab.setLayoutParams(params);
        Whitebox.setInternalState(listTasksActivity, "layerSwitcherFab", layerSwitcherFab);
        listTasksActivity.positionMyLocationAndLayerSwitcher();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layerSwitcherFab.getLayoutParams();
        assertEquals(0, layoutParams.topMargin);
        int progressHeight = context.getResources().getDimensionPixelSize(R.dimen.progress_height);
        assertEquals(progressHeight + 80, layoutParams.bottomMargin);
        assertEquals(myLocationButton.getMeasuredHeight(), layoutParams.height);
        assertEquals(myLocationButton.getMeasuredWidth(), layoutParams.width);
    }

    @Test
    public void testOpenFilterTaskActivity() {
        listTasksActivity.findViewById(R.id.filter_tasks_fab).performClick();
        Intent startedIntent = shadowOf(listTasksActivity).getNextStartedActivity();
        assertEquals(FilterTasksActivity.class, shadowOf(startedIntent).getIntentClass());
    }


    @Test
    public void testOpenTaskRegister() {
        listTasksActivity.findViewById(R.id.task_register).performClick();
        Intent startedIntent = shadowOf(listTasksActivity).getNextStartedActivity();
        assertEquals(TaskRegisterActivity.class, shadowOf(startedIntent).getIntentClass());
    }


    @Test
    public void testOnAddStructure() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Whitebox.setInternalState(listTasksActivity, "revealMapHelper", revealMapHelper);
        listTasksActivity.findViewById(R.id.btn_add_structure).performClick();
        verify(listTaskPresenter).onAddStructureClicked(false);
        verify(revealMapHelper).isMyLocationComponentActive(any(), any());
    }

    @Test
    public void testOnChangeSprayStatus() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.findViewById(R.id.change_spray_status).performClick();
        verify(listTaskPresenter).onChangeInterventionStatus(Intervention.IRS);
    }


    @Test
    public void testRecordMosquitoCollection() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.findViewById(R.id.btn_record_mosquito_collection).performClick();
        verify(listTaskPresenter).onChangeInterventionStatus(Intervention.MOSQUITO_COLLECTION);
    }


    @Test
    public void testRecordLarvalDipping() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.findViewById(R.id.btn_record_larval_dipping).performClick();
        verify(listTaskPresenter).onChangeInterventionStatus(Intervention.LARVAL_DIPPING);
    }

    @Test
    public void testEditPAOTDetails() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.findViewById(R.id.btn_edit_paot_details).performClick();
        verify(listTaskPresenter).onChangeInterventionStatus(Intervention.PAOT);
    }

    @Test
    public void testCloseCardView() {

        listTasksActivity.findViewById(R.id.btn_collapse_spray_card_view).performClick();
        assertEquals(GONE, listTasksActivity.findViewById(R.id.spray_card_view).getVisibility());
    }

    @Test
    public void testOtherCardView() {
        listTasksActivity.findViewById(R.id.btn_collapse_mosquito_collection_card_view).performClick();
        assertEquals(GONE, listTasksActivity.findViewById(R.id.mosquito_collection_card_view).getVisibility());
    }

    @Test
    public void testOpenDrawerMenu() {
        Whitebox.setInternalState(listTasksActivity, "drawerView", drawerView);
        listTasksActivity.findViewById(R.id.drawerMenu).performClick();
        verify(drawerView).openDrawerLayout();
    }

    @Test
    public void testOpenIndicators() {
        assertEquals(GONE, listTasksActivity.findViewById(R.id.indicators_card_view).getVisibility());
        listTasksActivity.findViewById(R.id.progressIndicatorsGroupView).performClick();
        assertEquals(VISIBLE, listTasksActivity.findViewById(R.id.indicators_card_view).getVisibility());
    }

    @Test
    public void testRegisterFamily() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        when(listTaskPresenter.getSelectedFeature()).thenReturn(feature);
        listTasksActivity.findViewById(R.id.register_family).performClick();
        Intent startedIntent = shadowOf(listTasksActivity).getNextStartedActivity();
        assertEquals(FamilyRegisterActivity.class, shadowOf(startedIntent).getIntentClass());
        assertEquals(GONE, listTasksActivity.findViewById(R.id.spray_card_view).getVisibility());
        assertTrue(startedIntent.hasExtra(TASK_IDENTIFIER));
    }


    @Test
    public void testOpenStructureProfile() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        when(listTaskPresenter.getSelectedFeature()).thenReturn(feature);
        CommonPersonObjectClient client = TestingUtils.getCommonPersonObjectClient();
        listTasksActivity.openStructureProfile(client);
        Intent startedIntent = shadowOf(listTasksActivity).getNextStartedActivity();
        assertEquals(FamilyProfileActivity.class, shadowOf(startedIntent).getIntentClass());
        assertEquals(client.getColumnmaps().get(FIRST_NAME), startedIntent.getStringExtra(INTENT_KEY.FAMILY_NAME));
        assertEquals(client.getCaseId(), startedIntent.getStringExtra(INTENT_KEY.FAMILY_BASE_ENTITY_ID));
        assertTrue(startedIntent.hasExtra(TASK_IDENTIFIER));

    }


    @Test
    public void testSetGeoJsonSourceWithNullOperationalArea() {
        Whitebox.setInternalState(listTasksActivity, "geoJsonSource", geoJsonSource);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        listTasksActivity.setGeoJsonSource(featureCollection, null, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verifyZeroInteractions(mMapboxMap);
    }


    @Test
    public void testSetGeoJsonSource() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Whitebox.setInternalState(listTasksActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(listTasksActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(listTasksActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(listTasksActivity, "revealMapHelper", revealMapHelper);
        Feature operationalArea = mock(Feature.class);
        CameraPosition cameraPosition = new CameraPosition.Builder().build();
        when(mMapboxMap.getCameraForGeometry(operationalArea.geometry())).thenReturn(cameraPosition);
        when(listTaskPresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        listTasksActivity.setGeoJsonSource(featureCollection, operationalArea, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(mMapboxMap).setCameraPosition(cameraPosition);
        verify(kujakuMapView).addLayer(boundaryLayerArgumentCaptor.capture());
        verify(revealMapHelper).addIndexCaseLayers(mMapboxMap, listTasksActivity, featureCollection);
        assertEquals(FeatureCollection.fromFeature(operationalArea), boundaryLayerArgumentCaptor.getValue().getFeatureCollection());
        assertEquals(2, boundaryLayerArgumentCaptor.getValue().getLayerIds().length);
    }


    @Test
    public void testSetGeoJsonSourceSetsCameraOnIndexCase() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Whitebox.setInternalState(listTasksActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(listTasksActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(listTasksActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(listTasksActivity, "revealMapHelper", revealMapHelper);
        Feature indexCase = Feature.fromJson("{\"type\":\"Feature\",\"id\":\"000c99d7\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[28.821878,-10.203831]}}");
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(18.5).build();
        when(mMapboxMap.getCameraPosition()).thenReturn(cameraPosition);
        when(revealMapHelper.getIndexCase(featureCollection)).thenReturn(indexCase);
        when(listTaskPresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        listTasksActivity.setGeoJsonSource(featureCollection, mock(Feature.class), true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(mMapboxMap).setCameraPosition(cameraPositionArgumentCaptor.capture());
        verify(kujakuMapView).addLayer(boundaryLayerArgumentCaptor.capture());
        verify(revealMapHelper).addIndexCaseLayers(mMapboxMap, listTasksActivity, featureCollection);
        assertEquals(28.821878, cameraPositionArgumentCaptor.getValue().target.getLongitude(), 0);
        assertEquals(-10.203831, cameraPositionArgumentCaptor.getValue().target.getLatitude(), 0);

    }

    @Test
    public void testSetGeoJsonSourceUpdatesBoundaryLayerAndIndexCaseLayers() throws JSONException {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Whitebox.setInternalState(listTasksActivity, "mMapboxMap", mMapboxMap);
        Whitebox.setInternalState(listTasksActivity, "geoJsonSource", geoJsonSource);
        Whitebox.setInternalState(listTasksActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(listTasksActivity, "revealMapHelper", revealMapHelper);
        BoundaryLayer boundaryLayer = mock(BoundaryLayer.class);
        Whitebox.setInternalState(listTasksActivity, "boundaryLayer", boundaryLayer);
        Feature operationalArea = mock(Feature.class);
        when(listTaskPresenter.getInterventionLabel()).thenReturn(R.string.focus_investigation);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        when(revealMapHelper.getIndexCaseLineLayer()).thenReturn(mock(LineLayer.class));
        listTasksActivity.setGeoJsonSource(featureCollection, operationalArea, true);
        verify(geoJsonSource).setGeoJson(featureCollection);
        verify(boundaryLayer).updateFeatures(FeatureCollection.fromFeature(operationalArea));
        verify(revealMapHelper).updateIndexCaseLayers(mMapboxMap, featureCollection, listTasksActivity);

    }


    @Test
    public void testDisplayNotificationWithArgs() {
        listTasksActivity.displayNotification(R.string.archive_family, R.string.archive_family_failed, "Test");
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals("Archiving family Test failed", tv.getText());

    }

    @Test
    public void testDisplayNotification() {
        listTasksActivity.displayNotification(R.string.archive_family, R.string.confirm_archive_family);
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals("Confirm Household Archival", tv.getText());

    }

    @Test
    public void testOpenSprayCardView() {
        assertEquals(GONE, listTasksActivity.findViewById(R.id.spray_card_view).getVisibility());
        Whitebox.setInternalState(listTasksActivity, "cardDetailsUtil", cardDetailsUtil);
        SprayCardDetails cardDetails = mock(SprayCardDetails.class);
        listTasksActivity.openCardView(cardDetails);
        verify(cardDetailsUtil).populateSprayCardTextViews(cardDetails, listTasksActivity);
        assertEquals(VISIBLE, listTasksActivity.findViewById(R.id.spray_card_view).getVisibility());
    }

    @Test
    public void testOpenMosquitoCardView() {
        MosquitoHarvestCardDetails mosquitoHarvestCardDetails = mock(MosquitoHarvestCardDetails.class);
        listTasksActivity.openCardView(mosquitoHarvestCardDetails);
        verify(cardDetailsUtil).populateAndOpenMosquitoHarvestCard(mosquitoHarvestCardDetails, listTasksActivity);
    }

    @Test
    public void testOpenIRSVerificationCardView() {
        IRSVerificationCardDetails irsVerificationCardDetails = mock(IRSVerificationCardDetails.class);
        listTasksActivity.openCardView(irsVerificationCardDetails);
        verify(cardDetailsUtil).populateAndOpenIRSVerificationCard(irsVerificationCardDetails, listTasksActivity);
    }

    @Test
    public void testOpenFamilyCardView() {
        assertEquals(GONE, listTasksActivity.findViewById(R.id.spray_card_view).getVisibility());
        FamilyCardDetails familyCardDetails = mock(FamilyCardDetails.class);
        listTasksActivity.openCardView(familyCardDetails);
        verify(cardDetailsUtil).populateFamilyCard(familyCardDetails, listTasksActivity);
        assertEquals(VISIBLE, listTasksActivity.findViewById(R.id.spray_card_view).getVisibility());
    }

    @Test
    public void testStartJsonForm() {
        RevealJsonFormUtils jsonFormUtils = mock(RevealJsonFormUtils.class);
        Whitebox.setInternalState(listTasksActivity, "jsonFormUtils", jsonFormUtils);
        JSONObject form = new JSONObject();
        listTasksActivity.startJsonForm(form);
        verify(jsonFormUtils).startJsonForm(form, listTasksActivity);
    }

    @Test
    public void testDisplaySelectedFeature() {
        Whitebox.setInternalState(listTasksActivity, "kujakuMapView", kujakuMapView);
        Whitebox.setInternalState(listTasksActivity, "selectedGeoJsonSource", geoJsonSource);
        Whitebox.setInternalState(listTasksActivity, "mMapboxMap", mMapboxMap);
        LatLng latLng = new LatLng();
        when(mMapboxMap.getCameraPosition()).thenReturn(new CameraPosition.Builder().zoom(18).build());
        listTasksActivity.displaySelectedFeature(feature, latLng);
        verify(kujakuMapView).centerMap(latLng, ANIMATE_TO_LOCATION_DURATION, 18);
        verify(geoJsonSource).setGeoJson(FeatureCollection.fromFeature(feature));
    }

    @Test
    public void testClearSelectedFeature() {
        Whitebox.setInternalState(listTasksActivity, "selectedGeoJsonSource", geoJsonSource);
        listTasksActivity.clearSelectedFeature();
        verify(geoJsonSource).setGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}");
    }


    @Test
    public void testOnActivityResultSavesForm() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Intent intent = new Intent();
        intent.putExtra(JSON_FORM_PARAM_JSON, "{form_data}");
        listTasksActivity.onActivityResult(REQUEST_CODE_GET_JSON, Activity.RESULT_OK, intent);
        verify(listTaskPresenter).saveJsonForm("{form_data}");
    }

    @Test
    public void testOnActivityResultWaitForUserLocation() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Whitebox.setInternalState(listTasksActivity, "hasRequestedLocation", true);
        when(listTaskPresenter.getLocationPresenter()).thenReturn(locationPresenter);
        listTasksActivity.onActivityResult(LOCATION_SETTINGS, Activity.RESULT_OK, null);
        verify(locationPresenter).waitForUserLocation();
        assertFalse(Whitebox.getInternalState(listTasksActivity, "hasRequestedLocation"));
    }

    @Test
    public void testOnActivityResultWaitForUserLocationCancelled() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Whitebox.setInternalState(listTasksActivity, "hasRequestedLocation", true);
        when(listTaskPresenter.getLocationPresenter()).thenReturn(locationPresenter);
        listTasksActivity.onActivityResult(LOCATION_SETTINGS, Activity.RESULT_CANCELED, null);
        verify(locationPresenter).onGetUserLocationFailed();
        assertFalse(Whitebox.getInternalState(listTasksActivity, "hasRequestedLocation"));
    }


    @Test
    public void testOnActivityResultResetFeatures() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        Intent intent = new Intent();
        String id = UUID.randomUUID().toString();
        Task task = TestingUtils.getTask(id);
        intent.putExtra(STRUCTURE_ID, id);
        intent.putExtra(TASK_ID, task);
        listTasksActivity.onActivityResult(REQUEST_CODE_FAMILY_PROFILE, Activity.RESULT_OK, intent);
        verify(listTaskPresenter).resetFeatureTasks(id, task);
    }

    @Test
    public void testOnActivityResultFilterFeatures() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        TaskFilterParams params = new TaskFilterParams("Doe");
        Intent intent = new Intent();
        intent.putExtra(FILTER_SORT_PARAMS, params);
        listTasksActivity.onActivityResult(REQUEST_CODE_FILTER_TASKS, Activity.RESULT_OK, intent);
        verify(listTaskPresenter).filterTasks(params);
    }


    @Test
    public void testOnActivityResultInializeFilterParams() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        TaskFilterParams params = new TaskFilterParams("Doe");
        Intent intent = new Intent();
        intent.putExtra(FILTER_SORT_PARAMS, params);
        listTasksActivity.onActivityResult(REQUEST_CODE_TASK_LISTS, Activity.RESULT_OK, intent);
        verify(listTaskPresenter).setTaskFilterParams(params);
    }

    @Test
    public void testShowProgressDialog() {
        listTasksActivity.showProgressDialog(R.string.saving_title, R.string.saving_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        assertNotNull(progressDialog);
        assertTrue(progressDialog.isShowing());
        assertEquals(context.getString(R.string.saving_title), ShadowApplication.getInstance().getLatestDialog().getTitle());
    }

    @Test
    public void testHideProgressDialog() {
        listTasksActivity.showProgressDialog(R.string.saving_title, R.string.saving_message);
        ProgressDialog progressDialog = (ProgressDialog) ShadowProgressDialog.getLatestDialog();
        listTasksActivity.hideProgressDialog();
        assertNotNull(progressDialog);
        assertFalse(progressDialog.isShowing());
    }


    @Test
    public void testGetUserCurrentLocation() {
        Whitebox.setInternalState(listTasksActivity, "kujakuMapView", kujakuMapView);
        Location location = listTasksActivity.getUserCurrentLocation();
        assertNull(location);

        Location expected = new Location("test");
        when(kujakuMapView.getLocationClient()).thenReturn(locationClient);
        when(locationClient.getLastLocation()).thenReturn(expected);
        assertEquals(expected, listTasksActivity.getUserCurrentLocation());

    }

    @Test
    public void testRequestUserLocation() {
        Whitebox.setInternalState(listTasksActivity, "kujakuMapView", kujakuMapView);
        listTasksActivity.requestUserLocation();
        verify(kujakuMapView).setWarmGps(true, getString(R.string.location_service_disabled), getString(R.string.location_services_disabled_spray));
        assertTrue(Whitebox.getInternalState(listTasksActivity, "hasRequestedLocation"));
    }


    @Test
    public void testOnDestroy() {
        listTasksActivity.onDestroy();
        assertNull(Whitebox.getInternalState(listTasksActivity, "listTaskPresenter"));
    }

    @Test
    public void testDisplayToast() {
        listTasksActivity.displayToast(R.string.sync_complete);
        assertEquals(getString(R.string.sync_complete), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testOnSyncStart() {
        init(context);
        Whitebox.setInternalState(getInstance(), "isSyncing", true);
        listTasksActivity.onSyncStart();
        Snackbar snackbar = Whitebox.getInternalState(listTasksActivity, "syncProgressSnackbar");
        assertTrue(snackbar.isShown());
    }


    @Test
    public void testOnSyncInProgressFetchedDataSnackBarIsStillShown() {
        init(context);
        listTasksActivity.onSyncInProgress(FetchStatus.fetched);
        Snackbar snackbar = Whitebox.getInternalState(listTasksActivity, "syncProgressSnackbar");
        assertTrue(snackbar.isShown());
    }


    @Test
    public void testOnSyncInProgressFetchFailedSnackBarIsDismissed() {
        init(context);
        listTasksActivity.onSyncInProgress(FetchStatus.fetchedFailed);
        Snackbar snackbar = Whitebox.getInternalState(listTasksActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());


    }

    @Test
    public void testOnSyncInProgressNothingFetchedSnackBarIsDismissed() {
        init(context);
        listTasksActivity.onSyncInProgress(FetchStatus.nothingFetched);
        Snackbar snackbar = Whitebox.getInternalState(listTasksActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnSyncInProgressNoConnectionSnackBarIsDismissed() {
        init(context);
        listTasksActivity.onSyncInProgress(FetchStatus.noConnection);
        Snackbar snackbar = Whitebox.getInternalState(listTasksActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnSyncCompleteSnackBarIsDismissed() {
        init(context);
        listTasksActivity.onSyncComplete(FetchStatus.nothingFetched);
        Snackbar snackbar = Whitebox.getInternalState(listTasksActivity, "syncProgressSnackbar");
        assertFalse(snackbar.isShown());
    }

    @Test
    public void testOnResume() {
        init(context);
        Whitebox.setInternalState(listTasksActivity, "drawerView", drawerView);
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.onResume();
        verify(drawerView).onResume();
        verify(listTaskPresenter).onResume();
    }


    @Test
    public void testOnPause() {
        init(context);
        Whitebox.setInternalState(listTasksActivity, "revealMapHelper", revealMapHelper);
        getInstance().addSyncStatusListener(listTasksActivity);
        listTasksActivity.onPause();
        List<SyncStatusListener> syncStatusListeners = Whitebox.getInternalState(getInstance(), "syncStatusListeners");
        assertTrue(syncStatusListeners.isEmpty());
        assertFalse(RevealApplication.getInstance().isMyLocationComponentEnabled());
    }

    @Test
    public void testOnDrawerClosed() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.onDrawerClosed();
        verify(listTaskPresenter).onDrawerClosed();
    }

    @Test
    public void testFocusOnUserLocation() {
        Whitebox.setInternalState(listTasksActivity, "kujakuMapView", kujakuMapView);
        listTasksActivity.focusOnUserLocation(true);
        verify(kujakuMapView).focusOnUserLocation(true);
    }

    @Test
    public void testIsMyLocationComponentActive() {
        Whitebox.setInternalState(listTasksActivity, "revealMapHelper", revealMapHelper);
        when(revealMapHelper.isMyLocationComponentActive(any(), any())).thenReturn(true);
        assertTrue(listTasksActivity.isMyLocationComponentActive());
    }

    @Test
    public void testDisplayMarkStructureInactiveDialog() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.displayMarkStructureInactiveDialog();
        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());
        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.confirm_mark_location_inactive), tv.getText());

        alertDialog.getButton(BUTTON_POSITIVE).performClick();
        verify(listTaskPresenter).onMarkStructureInactiveConfirmed();
        assertFalse(alertDialog.isShowing());

    }

    @Test
    public void testSetNumberOfFiltersToZero() {
        listTasksActivity.setNumberOfFilters(0);
        assertEquals(VISIBLE, listTasksActivity.findViewById(R.id.filter_tasks_fab).getVisibility());
        assertEquals(GONE, listTasksActivity.findViewById(R.id.filter_tasks_count_layout).getVisibility());
    }

    @Test
    public void testSetNumberOfFiltersToNoneZero() {
        listTasksActivity.setNumberOfFilters(3);
        assertEquals(GONE, listTasksActivity.findViewById(R.id.filter_tasks_fab).getVisibility());
        assertEquals(VISIBLE, listTasksActivity.findViewById(R.id.filter_tasks_count_layout).getVisibility());
        assertEquals("3", ((TextView) listTasksActivity.findViewById(R.id.filter_tasks_count)).getText());
    }

    @Test
    public void testSetSearchPhrase() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        String searchBy = "Jane Doe";
        listTasksActivity.setSearchPhrase(searchBy);
        assertEquals(searchBy, ((TextView) listTasksActivity.findViewById(R.id.edt_search)).getText().toString());
        verify(listTaskPresenter).searchTasks(searchBy);
    }


    @Test
    public void testDisplayResetTaskInfoDialog() {
        Whitebox.setInternalState(listTasksActivity, "listTaskPresenter", listTaskPresenter);
        listTasksActivity.displayResetInterventionTaskDialog(BEDNET_DISTRIBUTION);

        AlertDialog alertDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        assertTrue(alertDialog.isShowing());

        TextView tv = alertDialog.findViewById(android.R.id.message);
        assertEquals(getString(R.string.undo_task_msg), tv.getText());

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        verify(listTaskPresenter).onUndoInterventionStatus(eq(BEDNET_DISTRIBUTION));
        assertFalse(alertDialog.isShowing());
    }


}