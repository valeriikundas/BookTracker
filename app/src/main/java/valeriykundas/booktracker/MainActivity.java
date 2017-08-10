package valeriykundas.booktracker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private int minutes = 0;
    private int seconds = 0;
    private StopwatchState stopwatchState = StopwatchState.STOPPED;
    private DatabaseHelper databaseHelper;

    public static String convertToTimeFormat(int minutes, int seconds) {
        return String.format(Locale.US, "%02d", minutes) + ":" + String.format(Locale.US, "%02d", seconds);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.stopwatch).setVisibility(View.GONE);
        findViewById(R.id.startButton).setVisibility(View.VISIBLE);
        findViewById(R.id.stopButton).setVisibility(View.GONE);
        findViewById(R.id.pauseButton).setVisibility(View.GONE);

        //set listener for hiding keyboard when user clicks outside keyboard
        EditText et = findViewById(R.id.bookTitle);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideSoftKeyboard(view);
                }
            }
        });

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        Toolbar new_toolbar = findViewById(R.id.new_toolbar);
        setSupportActionBar(new_toolbar);
    }

    public void onStartButtonClick() {
        String bookName = ((EditText)findViewById(R.id.bookTitle)).getText().toString();
        if (bookName.equals("")) {
            Toast toasty = Toast.makeText(getApplicationContext(), "Please enter book title", Toast.LENGTH_SHORT);
            toasty.show();
            return;
        }

        findViewById(R.id.stopwatch).setVisibility(View.VISIBLE);
        findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
        findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
        findViewById(R.id.startButton).setVisibility(View.GONE);

        stopwatchState = StopwatchState.RUNNING;
        startOrContinueStopwatch();
    }

    public void onPauseButtonClick() {
        findViewById(R.id.pauseButton).setVisibility(View.GONE);
        findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
        findViewById(R.id.startButton).setVisibility(View.VISIBLE);

        stopwatchState = StopwatchState.PAUSED;
    }

    public void onStopButtonClick() {
        findViewById(R.id.stopwatch).setVisibility(View.GONE);
        findViewById(R.id.pauseButton).setVisibility(View.GONE);
        findViewById(R.id.stopButton).setVisibility(View.GONE);
        findViewById(R.id.startButton).setVisibility(View.VISIBLE);

        stopwatchState = StopwatchState.STOPPED;

        //save book in database
        EditText et = findViewById(R.id.bookTitle);
        String bookTitle = et.getText().toString();
        String timeSpent = convertToTimeFormat(minutes, seconds);

        //check if such book is already in database
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] projection = {DatabaseHelper.COLUMN_NAME_ID, DatabaseHelper.COLUMN_NAME_TITLE, DatabaseHelper.COLUMN_NAME_MINUTES_SPENT, DatabaseHelper.COLUMN_NAME_SECONDS_SPENT};
        String whereClause = DatabaseHelper.COLUMN_NAME_TITLE + " = ?";
        String[] whereArgs = {bookTitle};
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, projection, whereClause, whereArgs, null, null, null, null);

        Vector<BookInfo> booksWithGivenName = new Vector<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME_TITLE));
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME_ID));
            int minutesSpentAlready = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME_MINUTES_SPENT));
            int secondsSpentAlready = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME_SECONDS_SPENT));
            BookInfo bookInfo = new BookInfo(title, minutesSpentAlready, secondsSpentAlready);

            booksWithGivenName.add(bookInfo);
        }
        cursor.close();

        if (booksWithGivenName.size() > 1) {
            Toast.makeText(getApplicationContext(), "more than one book with same title", Toast.LENGTH_SHORT).show();

            int minutesAll = 0, secondsAll = 0;
            for (int i = 0; i < booksWithGivenName.size(); ++i) {
                minutesAll += booksWithGivenName.get(i).minutes;
                secondsAll += booksWithGivenName.get(i).seconds;
            }
            minutesAll += secondsAll / 60;
            secondsAll %= 60;

            BookInfo bookInfo = new BookInfo(booksWithGivenName.firstElement().title, minutesAll, secondsAll);

            whereClause = DatabaseHelper.COLUMN_NAME_TITLE + " = ?";
            whereArgs = new String[]{bookInfo.title};
            db.delete(DatabaseHelper.TABLE_NAME, whereClause, whereArgs);

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME_TITLE, bookInfo.title);
            values.put(DatabaseHelper.COLUMN_NAME_MINUTES_SPENT, bookInfo.minutes);
            values.put(DatabaseHelper.COLUMN_NAME_SECONDS_SPENT, bookInfo.seconds);
            db.insert(DatabaseHelper.TABLE_NAME, null, values);
        } else if (booksWithGivenName.size() == 1) {
            BookInfo bookInfo = booksWithGivenName.firstElement();

            db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME_TITLE, bookTitle);
            values.put(DatabaseHelper.COLUMN_NAME_MINUTES_SPENT, minutes + bookInfo.minutes);
            values.put(DatabaseHelper.COLUMN_NAME_SECONDS_SPENT, seconds + bookInfo.seconds);

            whereClause = DatabaseHelper.COLUMN_NAME_TITLE + " = ?";
            whereArgs = new String[]{String.valueOf(bookInfo.title)};

            db.update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);
        } else {
            db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME_TITLE, bookTitle);
            values.put(DatabaseHelper.COLUMN_NAME_MINUTES_SPENT, minutes);
            values.put(DatabaseHelper.COLUMN_NAME_SECONDS_SPENT, seconds);
            db.insert(DatabaseHelper.TABLE_NAME, null, values);
        }

        String text = "You have read " + bookTitle + " for " + timeSpent + " minutes";
        Toast toasty = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toasty.show();

        et.setText("");

        TextView tv = findViewById(R.id.stopwatch);
        tv.setText(R.string.stopwatch_starting_time);
        minutes = 0;
        seconds = 0;
    }

    private void startOrContinueStopwatch() {
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stopwatchState == StopwatchState.RUNNING) {
                            TextView stopwatch = findViewById(R.id.stopwatch);
                            stopwatch.setText(convertToTimeFormat(minutes, seconds));
                            seconds = seconds + 1;

                            if (seconds == 60) {
                                seconds = 0;
                                minutes += 1;
                            }
                        }
                        else {
                            timer.cancel();
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        try {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("hiding keyboard", "InputMethodManager.hideSoftInputFromWindow");
            e.printStackTrace();
        }
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

    private enum StopwatchState {
        STOPPED, RUNNING, PAUSED
    }

    class BookInfo {
        public String title;
        public int minutes;
        public int seconds;

        BookInfo(String title, int minutes, int seconds) {
            this.title = title;
            this.minutes = minutes;
            this.seconds = seconds;
        }
    }
}
