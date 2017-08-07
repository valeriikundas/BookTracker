package valeriykundas.booktracker;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class BooksActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {
            DatabaseHelper.COLUMN_NAME_ID,
                DatabaseHelper.COLUMN_NAME_TITLE,
                DatabaseHelper.COLUMN_NAME_MINUTES_SPENT,
                DatabaseHelper.COLUMN_NAME_SECONDS_SPENT
        };

        Cursor cursor = db.query(databaseHelper.TABLE_NAME, projection, null, null, null, null, null);
        String data = "";

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int indexOfBookTitleColumn = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME_TITLE);
                int indexOfMinutesSpentColumn = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME_MINUTES_SPENT);
                int indexOfSecondsSpendColumn = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME_SECONDS_SPENT);

                int minutes = Integer.valueOf(cursor.getString(indexOfMinutesSpentColumn));
                int seconds = Integer.valueOf(cursor.getString(indexOfSecondsSpendColumn));
                String name = cursor.getString(indexOfBookTitleColumn) + " - " + MainActivity.convertToTimeFormat(minutes, seconds);

                data = data + "\n" + name;
                cursor.moveToNext();
            }
        }
        cursor.close();

        TextView tv = findViewById(R.id.booksTableDemo);
        tv.setText(data);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
