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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Vector;

public class BooksActivity extends AppCompatActivity {

    public static final int REQUEST_FOR_EDITING_BOOK = 1010;
    private DBHelper dbHelper;
    private String[] fullProjection = {
            DBHelper.COLUMN_NAME_ID,
            DBHelper.COLUMN_NAME_TITLE,
            DBHelper.COLUMN_NAME_MINUTES_SPENT,
            DBHelper.COLUMN_NAME_SECONDS_SPENT,
            DBHelper.COLUMN_NAME_CURRENT_PAGE,
            DBHelper.COLUMN_NAME_PAGE_COUNT,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        setMainActionBar(R.id.main_toolbar);
        fillTableWithDataFromDatabase(R.id.table);
    }

    private void setMainActionBar(int id) {
        Toolbar toolbar = findViewById(id);
        setSupportActionBar(toolbar);
    }

    private void fillTableWithDataFromDatabase(int id) {
        dbHelper = DBHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_NAME, fullProjection, null, null, null, null, null);
        Vector<Vector<String>> data = getDataFromDatabase(cursor);
        cursor.close();
        fillTableWithData(id, data);
    }

    private Vector<Vector<String>> getDataFromDatabase(Cursor cursor) {
        Vector<Vector<String>> data = new Vector<>();

        while (cursor.moveToNext()) {
            int indexOfBookTitleColumn = cursor.getColumnIndex(DBHelper.COLUMN_NAME_TITLE);
            int indexOfMinutesSpentColumn = cursor.getColumnIndex(DBHelper.COLUMN_NAME_MINUTES_SPENT);
            int indexOfSecondsSpendColumn = cursor.getColumnIndex(DBHelper.COLUMN_NAME_SECONDS_SPENT);
            int indexOfCurPageColumn = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_CURRENT_PAGE);

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
        }
        return data;

    }

    private void fillTableWithData(final int id, Vector<Vector<String>> data) {
        TableLayout table = findViewById(id);
        table.removeAllViews();

        TableRow header = new TableRow(getApplicationContext());
        header.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView cell;
        String[] columnNames = {getResources().getString(R.string.title), getResources().getString(R.string.time), getResources().getString(R.string.current_page), ""};
        for (int i = 0; i < columnNames.length; ++i) {
            cell = new TextView(getApplicationContext());
            cell.setText(columnNames[i]);
            cell.setPadding(10, 10, 10, 10);
            header.addView(cell);
        }
        table.addView(header);

        for (int rowId = 0; rowId < data.size(); ++rowId) {
            TableRow tr = new TableRow(getApplicationContext());
            tr.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            for (int colId = 0; colId < data.get(0).size(); ++colId) {
                cell = new TextView(getApplicationContext());
                cell.setText(data.get(rowId).get(colId));
                cell.setPadding(10, 10, 10, 10);
                tr.addView(cell);
            }
            final String bookTitle = data.get(rowId).get(0);

            ImageButton imb = new ImageButton(getApplicationContext());
            imb.setPadding(10, 10, 10, 10);
            imb.setImageResource(R.drawable.remove);
            imb.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            imb.setMaxHeight(25);
            imb.setMaxWidth(25);
            imb.setScaleType(ImageView.ScaleType.CENTER);
            imb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_NAME_TITLE + " = ?", new String[]{bookTitle});
                    fillTableWithDataFromDatabase(id);
                }
            });

            tr.addView(imb);
            //final BooksActivity this_ref = this;
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), BookEditActivity.class);
                    intent.putExtra("title", bookTitle);
                    startActivityForResult(intent, REQUEST_FOR_EDITING_BOOK);
                }
            });
            table.addView(tr);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FOR_EDITING_BOOK) {
            if (resultCode == RESULT_OK) {
//                String new_title = data.getStringExtra("title");
//                String author = data.getStringExtra("author");
                //              int current_page = data.getIntExtra("current_page");
                //            int page_count = data.getIntExtra("page_count");
                //          String cover_path = data.getStringExtra("image_path");
                //TODO update database with this result
            }
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
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
