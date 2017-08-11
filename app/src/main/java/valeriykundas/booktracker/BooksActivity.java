package valeriykundas.booktracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Vector;

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
                DatabaseHelper.COLUMN_NAME_SECONDS_SPENT,
                DatabaseHelper.COLUMN_NAME_CURRENT_PAGE
        };

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, projection, null, null, null, null, null);
        Vector<Vector<String>> data = new Vector<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int indexOfBookTitleColumn = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME_TITLE);
                int indexOfMinutesSpentColumn = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME_MINUTES_SPENT);
                int indexOfSecondsSpendColumn = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME_SECONDS_SPENT);
                int indexOfCurPageColumn = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME_CURRENT_PAGE);

                int minutes = Integer.valueOf(cursor.getString(indexOfMinutesSpentColumn));
                int seconds = Integer.valueOf(cursor.getString(indexOfSecondsSpendColumn));
                String time = MainActivity.convertToTimeFormat(minutes, seconds);
                String title = cursor.getString(indexOfBookTitleColumn);
                String currentPage = String.valueOf(cursor.getInt(indexOfCurPageColumn));

                Vector<String> tableRow = new Vector<>();
                tableRow.add(title);
                tableRow.add(time);
                tableRow.add(currentPage);

                data.add(tableRow);

                cursor.moveToNext();
            }
        }
        cursor.close();

        fillTableWithData(R.id.table, data);

        Toolbar new_toolbar = findViewById(R.id.new_toolbar);
        setSupportActionBar(new_toolbar);
    }

    private void fillTableWithData(int tableId, Vector<Vector<String>> data) {
        TableLayout table = findViewById(tableId);
        for (int i = 0; i < data.size(); ++i) {
            TableRow tr = new TableRow(getApplicationContext());
            tr.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            TextView cell;
            for (int j = 0; j < data.get(0).size(); ++j) {
                cell = new TextView(getApplicationContext());
                cell.setText(data.get(i).get(j));
                cell.setPadding(10, 10, 10, 10);
                tr.addView(cell);
            }
            table.addView(tr);
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_main_page:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_show_books: {
                intent = new Intent(this, BooksActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
