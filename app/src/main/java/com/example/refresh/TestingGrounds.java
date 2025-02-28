package com.example.refresh;

import android.app.AlarmManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.refresh.Helper.DatabaseHelper;
import com.example.refresh.Database.NotificationInstancesTable;
import com.example.refresh.Model.NotificationInstance;
import com.example.refresh.Model.NotificationTemplate;
import com.example.refresh.Notification.NotificationScheduler;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TestingGrounds {
    public static void test(Context context) {
        ArrayList<Integer> templateIDs = new ArrayList<>();
        ArrayList<String> times = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // Add a random notification template for testing
        NotificationTemplate template = dbHelper.getRandomRecord(DatabaseHelper.Tables.NOTIFICATION_TEMPLATES);
        templateIDs.add(template.getTemplateID());

        // Add the current time for testing
        LocalTime timeNow = LocalTime.now().plusMinutes(2);

        // Format it to 24-hour format (HH:mm)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = timeNow.format(formatter);

        times.add(formattedTime);

        NotificationScheduler.addNotificationInstances(context, templateIDs, times);
    }

    // Test cleanup method
    public static void testCleanup(Context context, NotificationInstance instance) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        NotificationScheduler.cancelExistingAlarm(context, instance, alarmManager);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.deleteRecords(DatabaseHelper.Tables.NOTIFICATION_INSTANCES, NotificationInstancesTable.Columns.INSTANCE_ID, new String[]{String.valueOf(instance.getInstanceID())});
    }

    public static void draft() {
        String str = NotificationCompat.CATEGORY_ALARM;
    }
}
