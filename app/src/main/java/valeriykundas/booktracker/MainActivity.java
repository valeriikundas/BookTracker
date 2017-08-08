package valeriykundas.booktracker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        startOrContinueStopwatch();
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
        values.put(DatabaseHelper.COLUMN_NAME_TITLE, bookTitle);
        values.put(DatabaseHelper.COLUMN_NAME_MINUTES_SPENT, minutes);
        values.put(DatabaseHelper.COLUMN_NAME_SECONDS_SPENT, seconds);
        db.insert(DatabaseHelper.TABLE_NAME, null, values);

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
            case R.id.action_show_books: {
                intent = new Intent(this, BooksActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_settings: {
                intent = new Intent(this, SettingsActivity.class);
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
}
