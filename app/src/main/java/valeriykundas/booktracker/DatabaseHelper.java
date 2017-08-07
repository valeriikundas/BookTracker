package valeriykundas.booktracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance = null;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_FILENAME = "data.db";

    public final static String TABLE_NAME = "books";
    public final static String COLUMN_NAME_ID = "id";
    public final static String COLUMN_NAME_TITLE = "title";
    public final static String COLUMN_NAME_MINUTES_SPENT = "minutesSpent";
    public final static String COLUMN_NAME_SECONDS_SPENT = "secondsSpent";
    public final static String COLUMN_NAME_PAGES_READ_COUNT = "pagesReadCount";
    public final static String COLUMN_NAME_PAGES_ALL_COUNT = "pagesAllCount";
    public final static String COLUMN_NAME_BOOK_COVER_IMAGE_PATH = "bookCoverImagePath";

    private final static String SQL_CREATE_TABLES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INT PRIMARY KEY," +
                    COLUMN_NAME_TITLE + " TEXT, " +
                    COLUMN_NAME_MINUTES_SPENT + " INT, " +
                    COLUMN_NAME_SECONDS_SPENT + " INT, " +
                    COLUMN_NAME_PAGES_READ_COUNT + " INT, " +
                    COLUMN_NAME_PAGES_ALL_COUNT + " INT, " +
                    COLUMN_NAME_BOOK_COVER_IMAGE_PATH + " TEXT" +
                    ")";
    private final static String SQL_DELETE_TABLES = "DELETE TABLE IF EXISTS " + TABLE_NAME;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_FILENAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
