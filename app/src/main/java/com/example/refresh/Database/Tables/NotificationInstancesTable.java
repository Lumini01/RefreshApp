package com.example.refresh.Database.Tables;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.refresh.Model.NotificationInstance;

public class NotificationInstancesTable {

    public static final String TABLE_NAME = "notification_instances";

    public enum Columns {
        INSTANCE_ID("instanceID"),
        TEMPLATE_ID("templateID"),
        TIME("time");

        private final String columnName;

        Columns(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }
    }

    public static final String CREATE_TABLE =

            "CREATE TABLE " + TABLE_NAME + " (" +
            Columns.INSTANCE_ID.getColumnName() + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Columns.TEMPLATE_ID.getColumnName() + " INTEGER NOT NULL, " +
            Columns.TIME.getColumnName() + " TEXT NOT NULL, " +
            "FOREIGN KEY(" + Columns.TEMPLATE_ID.getColumnName() + ") " +
            "REFERENCES " + NotificationTemplatesTable.TABLE_NAME + " (" +
            NotificationTemplatesTable.Columns.TEMPLATE_ID.getColumnName() + "));";

    public static ContentValues toContentValues(NotificationInstance instance) {

        ContentValues values = new ContentValues();
        values.put(Columns.TEMPLATE_ID.getColumnName(), instance.getTemplateID());
        values.put(Columns.TIME.getColumnName(), instance.getTime());

        return values;
    }

    public static NotificationInstance fromCursor(Cursor cursor) {

        int instanceID = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.INSTANCE_ID.getColumnName()));
        int templateID = cursor.getInt(cursor.getColumnIndexOrThrow(Columns.TEMPLATE_ID.getColumnName()));
        String time = cursor.getString(cursor.getColumnIndexOrThrow(Columns.TIME.getColumnName()));

        return new NotificationInstance(instanceID, templateID, time);
    }
}