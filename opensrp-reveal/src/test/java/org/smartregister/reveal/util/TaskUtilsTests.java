package org.smartregister.reveal.util;

import android.content.Context;

import net.sqlcipher.MatrixCursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.Location;
import org.smartregister.domain.Task;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.BaseUnitTest;
import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.model.TaskDetails;
import org.smartregister.util.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.smartregister.domain.Task.TaskStatus.READY;
import static org.smartregister.repository.BaseRepository.TYPE_Unsynced;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.Intervention.BEDNET_DISTRIBUTION;
import static org.smartregister.reveal.util.Constants.Intervention.BLOOD_SCREENING;
import static org.smartregister.reveal.util.Constants.Intervention.CASE_CONFIRMATION;
import static org.smartregister.reveal.util.Constants.Intervention.MDA_ADHERENCE;
import static org.smartregister.reveal.util.Constants.Intervention.MDA_DISPENSE;
import static org.smartregister.reveal.util.Constants.Intervention.REGISTER_FAMILY;

/**
 * Created by Richard Kareko on 2/17/20.
 */

public class TaskUtilsTests extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PreferencesUtil prefsUtil;

    @Mock
    private SQLiteDatabase sqLiteDatabase;

    @Captor
    private ArgumentCaptor<Task> taskArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Integer> integerArgumentCaptor;

    private Context context;

    private TaskUtils taskUtils;

    private String jurisdictionId;

    private String expectedEntityId;

    private String expectedStructureId;


    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
        jurisdictionId = UUID.randomUUID().toString();
        expectedEntityId = UUID.randomUUID().toString();
        expectedStructureId = UUID.randomUUID().toString();
        taskUtils = TaskUtils.getInstance();
        Whitebox.setInternalState(taskUtils, "taskRepository", taskRepository);

        String planId = UUID.randomUUID().toString();

        when(prefsUtil.getCurrentPlanId()).thenReturn(planId);
        when(prefsUtil.getCurrentOperationalArea()).thenReturn(jurisdictionId);
        Location jurisdiction = new Location();
        jurisdiction.setId(jurisdictionId);
        Cache<Location> cache = mock(Cache.class);
        when(cache.get(anyString(), any())).thenReturn(jurisdiction);
        Whitebox.setInternalState(Utils.class, cache);

    }

    @Test
    public void testResetTask() {

        String taskIdentifier = UUID.randomUUID().toString();
        TaskDetails taskDetails = TestingUtils.getTaskDetails();
        taskDetails.setTaskCode(CASE_CONFIRMATION);

        Task task = TestingUtils.getTask("task_1");
        task.setCode(CASE_CONFIRMATION);
        task.setIdentifier(taskIdentifier);

        when(taskRepository.getTaskByIdentifier(anyString())).thenReturn(task);

        taskUtils.resetTask(taskDetails);

        verify(taskRepository).addOrUpdate(taskArgumentCaptor.capture());
        assertEquals(task.getIdentifier(), taskArgumentCaptor.getValue().getIdentifier());
        assertEquals(jurisdictionId, taskArgumentCaptor.getValue().getForEntity());
        assertEquals(NOT_VISITED, taskArgumentCaptor.getValue().getBusinessStatus());
        assertEquals(READY, taskArgumentCaptor.getValue().getStatus());
        assertEquals(TYPE_Unsynced, taskArgumentCaptor.getValue().getSyncStatus());


    }

    @Test
    public void testGenerateTask() {

        String expectedBusinessStatus = NOT_VISITED;
        String expectedIntervention = BEDNET_DISTRIBUTION;
        int expectedDescription = R.string.bednet_distribution_description;

        Task actualTask = taskUtils.generateTask(context, expectedEntityId, expectedStructureId, expectedBusinessStatus, expectedIntervention, expectedDescription);

        assertEquals(expectedEntityId, actualTask.getForEntity());
        assertEquals(expectedStructureId, actualTask.getStructureId());
        assertEquals(expectedIntervention, actualTask.getCode());
        assertEquals(expectedBusinessStatus, actualTask.getBusinessStatus());
        assertEquals(context.getString(expectedDescription), actualTask.getDescription());
        assertNotNull(actualTask.getIdentifier());
        assertEquals(Task.TaskPriority.ROUTINE, actualTask.getPriority());
        assertNotNull(actualTask.getExecutionPeriod());
        assertNotNull(actualTask.getExecutionPeriod().getStart());
        assertNotNull(actualTask.getAuthoredOn());
        assertNotNull(actualTask.getLastModified());
        assertEquals(BaseRepository.TYPE_Created, actualTask.getSyncStatus());

    }

    @Test
    public void testGenerateRegisterFamilyTask() {

        taskUtils = spy(taskUtils);

        taskUtils.generateRegisterFamilyTask(context, expectedEntityId);

        verify(taskUtils).generateTask(eq(context), stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
                stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), integerArgumentCaptor.capture());

        Iterator<String> iterator = stringArgumentCaptor.getAllValues().iterator();
        assertEquals(expectedEntityId, iterator.next());
        assertEquals(expectedEntityId, iterator.next());
        assertEquals(NOT_VISITED, iterator.next());
        assertEquals(REGISTER_FAMILY, iterator.next());
        assertEquals(R.string.register_family_description, integerArgumentCaptor.getValue().intValue());
    }

    @Test
    public void testGenerateBloodScreeningTask() {

        String plan = UUID.randomUUID().toString();
        PreferencesUtil.getInstance().setCurrentPlanId(plan);
        PreferencesUtil.getInstance().setActionCodesForPlan(plan, Collections.singletonList(BLOOD_SCREENING));
        taskUtils = spy(taskUtils);

        taskUtils.generateBloodScreeningTask(context, expectedEntityId, expectedStructureId);

        verify(taskUtils).generateTask(eq(context), stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
                stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), integerArgumentCaptor.capture());

        Iterator<String> iterator = stringArgumentCaptor.getAllValues().iterator();
        assertEquals(expectedEntityId, iterator.next());
        assertEquals(expectedStructureId, iterator.next());
        assertEquals(NOT_VISITED, iterator.next());
        assertEquals(BLOOD_SCREENING, iterator.next());
        assertEquals(R.string.blood_screening_description, integerArgumentCaptor.getValue().intValue());
    }

    @Test
    public void testGenerateBednetDistributionTask() {

        taskUtils = spy(taskUtils);

        String plan = UUID.randomUUID().toString();
        PreferencesUtil.getInstance().setCurrentPlanId(plan);
        PreferencesUtil.getInstance().setActionCodesForPlan(plan, Collections.singletonList(BEDNET_DISTRIBUTION));
        taskUtils.generateBedNetDistributionTask(context, expectedEntityId);

        verify(taskUtils).generateTask(eq(context), stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
                stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), integerArgumentCaptor.capture());

        Iterator<String> iterator = stringArgumentCaptor.getAllValues().iterator();
        assertEquals(expectedEntityId, iterator.next());
        assertEquals(expectedEntityId, iterator.next());
        assertEquals(NOT_VISITED, iterator.next());
        assertEquals(BEDNET_DISTRIBUTION, iterator.next());
        assertEquals(R.string.bednet_distribution_description, integerArgumentCaptor.getValue().intValue());
    }

    @Test
    public void testGenerateMDADispenceTask() {

        taskUtils = spy(taskUtils);

        taskUtils.generateMDADispenseTask(context, expectedEntityId, expectedStructureId);

        verify(taskUtils).generateTask(eq(context), stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
                stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), integerArgumentCaptor.capture());

        Iterator<String> iterator = stringArgumentCaptor.getAllValues().iterator();
        assertEquals(expectedEntityId, iterator.next());
        assertEquals(expectedStructureId, iterator.next());
        assertEquals(NOT_VISITED, iterator.next());
        assertEquals(MDA_DISPENSE, iterator.next());
        assertEquals(R.string.mda_dispense_desciption, integerArgumentCaptor.getValue().intValue());
    }

    @Test
    public void testGenerateMDAAdherenceTask() {

        taskUtils = spy(taskUtils);

        taskUtils.generateMDAAdherenceTask(context, expectedEntityId, expectedStructureId);

        verify(taskUtils).generateTask(eq(context), stringArgumentCaptor.capture(), stringArgumentCaptor.capture(),
                stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), integerArgumentCaptor.capture());

        Iterator<String> iterator = stringArgumentCaptor.getAllValues().iterator();
        assertEquals(expectedEntityId, iterator.next());
        assertEquals(expectedStructureId, iterator.next());
        assertEquals(NOT_VISITED, iterator.next());
        assertEquals(MDA_ADHERENCE, iterator.next());
        assertEquals(R.string.mda_adherence_desciption, integerArgumentCaptor.getValue().intValue());
    }

    @Test
    public void testTagEventTaskdetails() {

        Event event = new Event();
        event.setEventId("event-it");

        List<Event> eventList = new ArrayList<>();
        event.setDetails(new HashMap<>());
        eventList.add(event);

        assertNull(event.getDetails().get(Constants.Properties.TASK_IDENTIFIER));
        assertNull(event.getDetails().get(Constants.Properties.TASK_BUSINESS_STATUS));
        assertNull(event.getDetails().get(Constants.Properties.TASK_STATUS));
        assertNull(event.getDetails().get(Constants.Properties.LOCATION_ID));
        assertNull(event.getDetails().get(Constants.Properties.APP_VERSION_NAME));
        assertNull(event.getLocationId());


        Task expectedTask = TestingUtils.getTask("task-id");
        MatrixCursor cursor = TestingUtils.getTaskCursor(expectedTask);

        when(sqLiteDatabase.rawQuery("select * from task where for =? and status =? and code =? limit 1",
                new String[]{event.getBaseEntityId(), Task.TaskStatus.COMPLETED.name(),
                        Constants.Intervention.IRS})).thenReturn(cursor);
        when(taskRepository.readCursor(cursor)).thenReturn(expectedTask);

        taskUtils.tagEventTaskDetails(eventList, sqLiteDatabase);

        assertEquals(expectedTask.getIdentifier(), event.getDetails().get(Constants.Properties.TASK_IDENTIFIER));
        assertEquals(expectedTask.getBusinessStatus(), event.getDetails().get(Constants.Properties.TASK_BUSINESS_STATUS));
        assertEquals(expectedTask.getStatus().name(), event.getDetails().get(Constants.Properties.TASK_STATUS));
        assertEquals(expectedTask.getForEntity(), event.getDetails().get(Constants.Properties.LOCATION_ID));
        assertEquals(BuildConfig.VERSION_NAME, event.getDetails().get(Constants.Properties.APP_VERSION_NAME));
        assertEquals(expectedTask.getGroupIdentifier(), event.getLocationId());


    }

}