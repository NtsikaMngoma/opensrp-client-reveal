package org.smartregister.reveal.interactor;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.TaskRegisterFragmentContract;
import org.smartregister.reveal.model.TaskDetails;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Constants.DatabaseKeys;

import java.util.ArrayList;
import java.util.List;

import static org.smartregister.reveal.util.Constants.DatabaseKeys.BUSINESS_STATUS;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.CODE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.FAMILY_NAME;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.FOR;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.LATITUDE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.LONGITUDE;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.NAME;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.SPRAYED_STRUCTURES;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURES_TABLE;

/**
 * Created by samuelgithengi on 3/18/19.
 */
public class TaskRegisterFragmentInteractor {

    private SQLiteDatabase database;
    private AppExecutors appExecutors;

    private TaskRegisterFragmentContract.Presenter presenter;

    public TaskRegisterFragmentInteractor(AppExecutors appExecutors, TaskRegisterFragmentContract.Presenter presenter) {
        this.appExecutors = appExecutors;
        this.presenter = presenter;
        database = RevealApplication.getInstance().getRepository().getReadableDatabase();
    }

    private String mainSelect(String mainCondition) {
        String tableName = DatabaseKeys.TASK_TABLE;
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.SelectInitiateMainTable(tableName, mainColumns(tableName), ID);
        queryBuilder.customJoin(String.format(" JOIN %s ON %s.%s = %s.%s  COLLATE NOCASE",
                STRUCTURES_TABLE, tableName, FOR, STRUCTURES_TABLE, ID));
        queryBuilder.customJoin(String.format(" LEFT JOIN %s ON %s.%s = %s.%s  COLLATE NOCASE",
                SPRAYED_STRUCTURES, tableName, FOR, SPRAYED_STRUCTURES, DBConstants.KEY.BASE_ENTITY_ID));
        return queryBuilder.mainCondition(mainCondition);
    }

    private String[] mainColumns(String tableName) {
        return new String[]{
                tableName + "." + ID,
                tableName + "." + CODE,
                tableName + "." + FOR,
                tableName + "." + BUSINESS_STATUS,
                STRUCTURES_TABLE + "." + LATITUDE,
                STRUCTURES_TABLE + "." + LONGITUDE,
                STRUCTURES_TABLE + "." + NAME,
                SPRAYED_STRUCTURES + "." + FAMILY_NAME,
        };
    }


    public void findTasks(String mainCondition) {
        appExecutors.diskIO().execute(() -> {
            List<TaskDetails> tasks = new ArrayList<>();
            String query = mainSelect(mainCondition);
            Cursor cursor = null;
            try {
                cursor = database.rawQuery(query, null);
                while (cursor.moveToNext()) {
                    tasks.add(readTaskDetails(cursor));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            appExecutors.mainThread().execute(() -> {
                presenter.onTasksFound(tasks);
            });

        });

    }


    private TaskDetails readTaskDetails(Cursor cursor) {
        TaskDetails task = new TaskDetails();
        task.setTaskId(cursor.getString(cursor.getColumnIndex(ID)));
        task.setTaskCode(cursor.getString(cursor.getColumnIndex(CODE)));
        task.setTaskEntity(cursor.getString(cursor.getColumnIndex(FOR)));
        task.setBusinessStatus(cursor.getString(cursor.getColumnIndex(BUSINESS_STATUS)));
        task.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
        task.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
        task.setStructureName(cursor.getString(cursor.getColumnIndex(NAME)));
        task.setFamilyName(cursor.getString(cursor.getColumnIndex(FAMILY_NAME)));
        return task;
    }


}
