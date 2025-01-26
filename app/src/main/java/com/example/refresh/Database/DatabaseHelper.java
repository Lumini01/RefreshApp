package com.example.refresh.Database;

import static com.example.refresh.Database.Tables.UsersTable.Columns.*;
import static com.example.refresh.Database.Tables.NotificationTemplatesTable.Columns.*;
import static com.example.refresh.Database.Tables.NotificationInstancesTable.Columns.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.refresh.Database.Tables.*;
import com.example.refresh.Model.*;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Enum for database table names
    public enum Tables {
        USERS(UsersTable.TABLE_NAME),
        NOTIFICATION_TEMPLATES(NotificationTemplatesTable.TABLE_NAME),
        NOTIFICATION_INSTANCES(NotificationInstancesTable.TABLE_NAME),
        FOODS(FoodsTable.TABLE_NAME),
        MEALS(MealsTable.TABLE_NAME);

        private final String tableName;

        Tables(String tableName) {
            this.tableName = tableName;
        }

        public String getTableName() {
            return tableName;
        }
    }

    private static final String DATABASE_FILE = "app_database.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_FILE, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(UsersTable.CREATE_TABLE);
        db.execSQL(NotificationTemplatesTable.CREATE_TABLE);
        db.execSQL(NotificationInstancesTable.CREATE_TABLE);
        db.execSQL(FoodsTable.CREATE_TABLE);
        db.execSQL(MealsTable.CREATE_TABLE);
        // Add more table creation logic here
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Tables.USERS.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + Tables.NOTIFICATION_TEMPLATES.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + Tables.NOTIFICATION_INSTANCES.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + Tables.FOODS.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + Tables.MEALS.getTableName());
        // Add more table drop logic here
        onCreate(db);
    }

    public Context getContext() {
        return context;
    }

    // CRUD Operations

    // Insert a record into the database
    public <T> Integer insert(Tables table, T model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = toContentValues(model, table);

        return (int) db.insert(table.getTableName(), null, values);
    }

    // Retrieve all records from the database
    public <T> List<T> getAllRecords(Tables table) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<T> records = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(table.getTableName(), null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    T record = createRecord(cursor, table);
                    if (record != null) {
                        records.add(record);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return records;
    }

    // Retrieve a single record from the database
    public <T> T getRecord(Tables table, Enum<?> columnEnum, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = getEnumColumnName(columnEnum) + " = ?";

        Cursor cursor = null;

        try {
            cursor = db.query(table.getTableName(), null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return createRecord(cursor, table);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null; // Return null if no record is found
    }

    // Retrieve a single column value from a record in the database
    public <T> T getFromRecord(Tables table, Enum<?> columnEnum, int index) {
        SQLiteDatabase db = this.getReadableDatabase();
        String columnName = getEnumColumnName(columnEnum);  // Convert the enum to the column name (e.g., "name")

        Cursor cursor = db.query(table.getTableName(), toStringArray(columnName), null, null, null, null, null);

        T result = null;

        if (cursor != null && cursor.moveToPosition(index)) {
            // Retrieve the value from the specified column
            int columnIndex = cursor.getColumnIndex(columnName);

            if (columnIndex != -1) {
                int columnType = cursor.getType(columnIndex);  // Get the type of the column value

                switch (columnType) {
                    case Cursor.FIELD_TYPE_STRING:
                        result = (T) cursor.getString(columnIndex);  // If it's a String
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        result = (T) Integer.valueOf(cursor.getInt(columnIndex));  // If it's an Integer
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported column type");
                }
            }
            cursor.close();
        }

        return result;  // Return the value or null if not found
    }

    // Edit a record in the database
    public <T> Integer editRecords(Tables table, T model,  Enum<?> columnEnum, String[] selectionArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        String columnName = getEnumColumnName(columnEnum);
        String selection = columnName + " = ?";
        ContentValues values = toContentValues(model, table);

        return db.update(table.getTableName(), values, selection, selectionArgs);
    }

    // Delete a record from the database
    public int deleteRecords(Tables table,  Enum<?> columnEnum, String[] selectionArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        String columnName = getEnumColumnName(columnEnum);

        return db.delete(table.getTableName(), columnName + " = ?", selectionArgs);
    }

    // Check if a value in the given column exists in a record of the database, if it does - return the record's index
    public int existsInDB(Tables table, Enum<?> columnEnum, String value) {
        SQLiteDatabase db = this.getReadableDatabase();
        String columnName = getEnumColumnName(columnEnum);  // Convert the enum to the column name

        Cursor cursor = db.query(table.getTableName(), toStringArray(columnName), null, null, null, null, null);

        int index = -1;  // Default to -1 (not found)

        if (cursor != null) {
            while  (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow(columnName)).equals(value)) {
                    index = cursor.getPosition();  // Get the index of the first matching record
                    break;  // Found a matching record
                }
            }

            cursor.close();
        }

        return index;
    }

    // Retrieve a random record from the database - for testing purposes
    public <T> T getRandomRecord(Tables table) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Tables.NOTIFICATION_TEMPLATES.getTableName() + " ORDER BY RANDOM() LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            T randomRecord = createRecord(cursor, table);
            // Populate your NotificationInstance object from the cursor
            // Example: randomInstance.setId(cursor.getInt(cursor.getColumnIndex("id")));
            cursor.close();
            return randomRecord;
        }

        return null;
    }

    // Create a record from a cursor
    @SuppressWarnings("unchecked")
    private <T> T createRecord(Cursor cursor, Tables table) {
        switch (table) {
            case USERS:
                // Example: User table mapping
                return (T) new UserInfo(
                        cursor.getString(cursor.getColumnIndexOrThrow(NAME.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(EMAIL.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(PHONE.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(PWD.getColumnName()))
                );
            case NOTIFICATION_TEMPLATES:
                // Example: NotificationTemplates table mapping
                return (T) new NotificationTemplate(
                        context,
                        cursor.getInt(cursor.getColumnIndexOrThrow(NotificationTemplatesTable.Columns.TEMPLATE_ID.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(TITLE.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(ICON_ID.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(ACTIVITY_CLASS.getColumnName()))
                );
            case NOTIFICATION_INSTANCES:
                // Example: NotificationInstances table mapping
                return (T) new NotificationInstance(
                        cursor.getInt(cursor.getColumnIndexOrThrow(INSTANCE_ID.getColumnName())),
                        cursor.getInt(cursor.getColumnIndexOrThrow(NotificationInstancesTable.Columns.TEMPLATE_ID.getColumnName())),
                        cursor.getString(cursor.getColumnIndexOrThrow(TIME.getColumnName()))
                );

            case FOODS:
                // Example: Foods table mapping
                return (T) FoodsTable.fromCursor(cursor);

            case MEALS:
                // Example: Meals table mapping
                return (T) MealsTable.fromCursor(cursor);

            default:
                throw new IllegalArgumentException("Unsupported table: " + table.getTableName());
        }
    }

    // Convert a model to ContentValues
    public <T> ContentValues toContentValues(T model, Tables table) {
        ContentValues values = new ContentValues();

        switch (table) {
            case USERS:
                UserInfo user = (UserInfo) model;
                values.put(NAME.getColumnName(), user.getName()); // Assuming UserInfo has a name
                values.put(EMAIL.getColumnName(), user.getEmail()); // Assuming UserInfo has an email
                values.put(PHONE.getColumnName(), user.getPhone()); // Assuming UserInfo has a phone
                values.put(PWD.getColumnName(), user.getPwd()); // Assuming UserInfo has a password
                break;

            case NOTIFICATION_TEMPLATES:
                NotificationTemplate template = (NotificationTemplate) model;
                values.put(NotificationInstancesTable.Columns.TEMPLATE_ID.getColumnName(), template.getTemplateID()); // Assuming NotificationTemplate has an ID
                values.put(CATEGORY.getColumnName(), template.getCategory()); // Assuming NotificationTemplate has a category
                values.put(TITLE.getColumnName(), template.getTitle()); // Assuming NotificationTemplate has a title
                values.put(MESSAGE.getColumnName(), template.getMessage()); // Assuming NotificationTemplate has a message
                values.put(ICON_ID.getColumnName(), template.getIconID()); // Assuming NotificationTemplate has an icon
                values.put(ACTIVITY_CLASS.getColumnName(), template.getActivityClassName());
                break;

            case NOTIFICATION_INSTANCES:
                NotificationInstance instance = (NotificationInstance) model;
                if (instance.getInstanceID() != -1)
                    values.put(INSTANCE_ID.getColumnName(), instance.getInstanceID()); // Assuming NotificationInstance has an ID
                values.put(NotificationInstancesTable.Columns.TEMPLATE_ID.getColumnName(), instance.getTemplateID()); // Assuming NotificationInstance has a template_id
                values.put(TIME.getColumnName(), instance.getTime()); // Assuming NotificationInstance has a timestamp
                break;

            case FOODS:
                values = FoodsTable.toContentValues((Food) model);
                break;

            case MEALS:
                values = MealsTable.toContentValues((Meal) model);
                break;

            default:
                throw new IllegalArgumentException("Unsupported table: " + table.getTableName());
        }

        return values;
    }

    // Helper method to convert a value to a String array
    public static <T> String[] toStringArray(T value) {
        return new String[] {String.valueOf(value)};
    }

    // Checks if the database is open
    public boolean isDatabaseOpen() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.isOpen();
    }

    // Helper method to get the column name from an enum
    public static String getEnumColumnName(Enum<?> columnEnum) {
        try {
            // Use reflection to call the `getColumnName` method
            return (String) columnEnum.getClass()
                    .getMethod("getColumnName") // Look for the method
                    .invoke(columnEnum);       // Invoke it on the enum constant
        } catch (Exception e) {
            throw new IllegalArgumentException("Enum does not have a getColumnName method", e);
        }
    }

}