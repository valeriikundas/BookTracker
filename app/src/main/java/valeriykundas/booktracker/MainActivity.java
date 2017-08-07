package valeriykundas.booktracker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private int minutes = 0;
    private int seconds = 0;

    private enum StopwatchState {
        STOPPED, RUNNING, PAUSED
    }
    private StopwatchState stopwatchState = StopwatchState.STOPPED;

    private DatabaseHelper databaseHelper;

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
    }

    public void onStartButtonClick(View view) {
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
        startOrContinueStopwatch(view);
    }

    public void onPauseButtonClick(View view) {
        findViewById(R.id.pauseButton).setVisibility(View.GONE);
        findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
        findViewById(R.id.startButton).setVisibility(View.VISIBLE);

        stopwatchState = StopwatchState.PAUSED;
    }

    public void onStopButtonClick(View view) {
        findViewById(R.id.stopwatch).setVisibility(View.GONE);
        findViewById(R.id.pauseButton).setVisibility(View.GONE);
        findViewById(R.id.stopButton).setVisibility(View.GONE);
        findViewById(R.id.startButton).setVisibility(View.VISIBLE);

        stopwatchState = StopwatchState.STOPPED;

        //save book in database
        EditText et = findViewById(R.id.bookTitle);
        String bookTitle = et.getText().toString();
        String timeSpent = convertToTimeFormat(minutes, seconds);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(databaseHelper.COLUMN_NAME_TITLE, bookTitle);
        values.put(databaseHelper.COLUMN_NAME_MINUTES_SPENT, minutes);
        values.put(databaseHelper.COLUMN_NAME_SECONDS_SPENT, seconds);
        long newRowId = db.insert(databaseHelper.TABLE_NAME, null, values);

        String text = "You have read " + bookTitle + " for " + timeSpent + " minutes";
        Toast toasty = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toasty.show();

        et.setText("");

        TextView tv = findViewById(R.id.stopwatch);
        tv.setText("00:00");
        minutes = 0;
        seconds = 0;
    }

    private void startOrContinueStopwatch(View view) {
        final Timer timer = new Timer();
        //TODO refactor it so that it always runs on one thread without creating a new one
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

    public static String convertToTimeFormat(int minutes, int seconds) {
        return String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void onShowBooksButtonClick() {
        Intent intent = new Intent(this, BooksActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.books:
                onShowBooksButtonClick();
                return true;
            case R.id.settings:
                Toast.makeText(getApplicationContext(), "help", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
