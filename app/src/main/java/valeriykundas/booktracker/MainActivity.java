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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private static final int dropDownWidth = 600;
    private int minutes = 0;
    private int seconds = 0;
    private StopwatchState stopwatchState;
    private DBHelper dbHelper;
    private ArrayAdapter<String> adapter;

    public static String convertToTimeFormat(int minutes, int seconds) {
        return String.format(Locale.US, "%02d", minutes) + ":" + String.format(Locale.US, "%02d", seconds);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMainActionBar(R.id.main_toolbar);
        setState(StopwatchState.STOPPED);

        hideKeyboardOnLoseFocus(R.id.main_actv_booktitle);
        hideKeyboardOnLoseFocus(R.id.main_et_curpage);

        setSuggestionsForBookTitle(R.id.main_actv_booktitle);
    }

    private ArrayList<String> getListOfBookSuggestions() {
        dbHelper = DBHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_NAME, new String[]{DBHelper.COLUMN_NAME_TITLE}, null, null, null, null, null);
        ArrayList<String> listOfBookTitles = new ArrayList<>();
        while (cursor.moveToNext()) {
            listOfBookTitles.add(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_TITLE)));
        }
        cursor.close();
        return listOfBookTitles;
    }

    private void setMainActionBar(int id) {
        Toolbar toolbar = findViewById(id);
        setSupportActionBar(toolbar);
    }

    private void setSuggestionsForBookTitle(int id) {
        ArrayList<String> listOfBookTitles = getListOfBookSuggestions();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listOfBookTitles);

        AutoCompleteTextView et_booktitle = findViewById(id);
        et_booktitle.setThreshold(1);
        et_booktitle.setDropDownWidth(dropDownWidth);
        et_booktitle.setAdapter(adapter);
    }

    private void hideKeyboardOnLoseFocus(int id) {
        View view = findViewById(id);
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(view);
                }
            }
        });
    }

    private void setState(StopwatchState ss) {
        stopwatchState = ss;
        switch (ss) {
            case PAUSED:
                findViewById(R.id.main_btn_stop).setVisibility(View.VISIBLE);
                findViewById(R.id.main_btn_start).setVisibility(View.VISIBLE);
                findViewById(R.id.main_btn_pause).setVisibility(View.GONE);
                break;
            case RUNNING:
                findViewById(R.id.main_tv_stopwatch).setVisibility(View.VISIBLE);
                findViewById(R.id.main_btn_pause).setVisibility(View.VISIBLE);
                findViewById(R.id.main_btn_stop).setVisibility(View.VISIBLE);
                findViewById(R.id.main_btn_start).setVisibility(View.GONE);
                break;
            case STOPPED:
                findViewById(R.id.main_btn_start).setVisibility(View.VISIBLE);
                findViewById(R.id.main_tv_stopwatch).setVisibility(View.GONE);
                findViewById(R.id.main_btn_stop).setVisibility(View.GONE);
                findViewById(R.id.main_btn_pause).setVisibility(View.GONE);
                break;
        }
    }

    public void onStartButtonClick(View view) {
        String bookName = ((EditText) findViewById(R.id.main_actv_booktitle)).getText().toString();
        if (bookName.equals("")) {
            Toast toasty = Toast.makeText(getApplicationContext(), "Please enter book title", Toast.LENGTH_SHORT);
            toasty.show();
            return;
        }

        setState(StopwatchState.RUNNING);
        startOrContinueStopwatch();
    }

    public void onPauseButtonClick(View view) {
        setState(StopwatchState.PAUSED);
    }

    public void onStopButtonClick(View view) {
        setState(StopwatchState.STOPPED);
        //saving book to database
        EditText et_booktitle = findViewById(R.id.main_actv_booktitle);

        EditText et_curpage = findViewById(R.id.main_et_curpage);
        String tmp = et_curpage.getText().toString();
        int currentPage;
        if (tmp.isEmpty())
            currentPage = 0;
        else
            currentPage = Integer.parseInt(tmp);


        BookInfo bookInfo = new BookInfo(et_booktitle.getText().toString(), minutes, seconds, currentPage);

        //checking if book is already in database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {DBHelper.COLUMN_NAME_ID, DBHelper.COLUMN_NAME_TITLE, DBHelper.COLUMN_NAME_MINUTES_SPENT, DBHelper.COLUMN_NAME_SECONDS_SPENT, DBHelper.COLUMN_NAME_CURRENT_PAGE};
        String whereClause = DBHelper.COLUMN_NAME_TITLE + " = ?";
        String[] whereArgs = {bookInfo.title};
        Cursor cursor = db.query(DBHelper.TABLE_NAME, projection, whereClause, whereArgs, null, null, null, null);

        Vector<BookInfo> booksWithGivenName = new Vector<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_TITLE));
            int minutesSpentAlready = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_MINUTES_SPENT));
            int secondsSpentAlready = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_SECONDS_SPENT));
            int currentPageAlready = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_CURRENT_PAGE));
            BookInfo newBookInfo = new BookInfo(title, minutesSpentAlready, secondsSpentAlready, currentPageAlready);

            booksWithGivenName.add(newBookInfo);
        }
        cursor.close();
        db = dbHelper.getWritableDatabase();

        if (booksWithGivenName.size() > 1) {
            Toast.makeText(getApplicationContext(), "more than one book with same title", Toast.LENGTH_SHORT).show();

            int minutesAll = 0, secondsAll = 0;
            int curPage = 0;
            for (int i = 0; i < booksWithGivenName.size(); ++i) {
                minutesAll += booksWithGivenName.get(i).minutes;
                secondsAll += booksWithGivenName.get(i).seconds;
                curPage = Math.max(curPage, booksWithGivenName.get(i).curPage);
            }
            minutesAll += secondsAll / 60;
            secondsAll %= 60;

            BookInfo mergedBookInfo = new BookInfo(booksWithGivenName.firstElement().title, minutesAll, secondsAll, curPage);

            whereClause = DBHelper.COLUMN_NAME_TITLE + " = ?";
            whereArgs = new String[]{mergedBookInfo.title};
            db.delete(DBHelper.TABLE_NAME, whereClause, whereArgs);

            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_NAME_TITLE, mergedBookInfo.title);
            values.put(DBHelper.COLUMN_NAME_MINUTES_SPENT, mergedBookInfo.minutes);
            values.put(DBHelper.COLUMN_NAME_SECONDS_SPENT, mergedBookInfo.seconds);
            db.insert(DBHelper.TABLE_NAME, null, values);
        } else if (booksWithGivenName.size() == 1) {
            BookInfo newBookInfo = booksWithGivenName.firstElement();

            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_NAME_TITLE, newBookInfo.title);
            values.put(DBHelper.COLUMN_NAME_MINUTES_SPENT, minutes + newBookInfo.minutes);
            values.put(DBHelper.COLUMN_NAME_SECONDS_SPENT, seconds + newBookInfo.seconds);
            values.put(DBHelper.COLUMN_NAME_CURRENT_PAGE, currentPage);

            whereClause = DBHelper.COLUMN_NAME_TITLE + " = ?";
            whereArgs = new String[]{String.valueOf(newBookInfo.title)};

            db.update(DBHelper.TABLE_NAME, values, whereClause, whereArgs);
        } else {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_NAME_TITLE, bookInfo.title);
            values.put(DBHelper.COLUMN_NAME_MINUTES_SPENT, minutes);
            values.put(DBHelper.COLUMN_NAME_SECONDS_SPENT, seconds);
            values.put(DBHelper.COLUMN_NAME_CURRENT_PAGE, currentPage);
            db.insert(DBHelper.TABLE_NAME, null, values);
            adapter.add(bookInfo.title);
        }

        String timeSpent = convertToTimeFormat(minutes, seconds);
        String text = "You have read " + bookInfo.title + " for " + timeSpent + " minutes";
        Toast toasty = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toasty.show();

        et_booktitle = findViewById(R.id.main_actv_booktitle);
        et_booktitle.setText("");

        TextView tv = findViewById(R.id.main_tv_stopwatch);
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
                            TextView stopwatch = findViewById(R.id.main_tv_stopwatch);
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

    private void hideKeyboard(View view) {
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


    public void onImageButtonClick(View view) {
        //TODO https://developer.android.com/training/camera/photobasics.html

        //search google for such picture
        //String url = "http://www.julesverne.ca/images/book/full/20k_1963_biggoldenbook_x.jpg";
        // save it in context.getCacheDir()
        //load here

        findViewById(R.id.main_imbtn_addimage).setVisibility(View.GONE);
        findViewById(R.id.main_iv_cover).setVisibility(View.VISIBLE);
    }

    private enum StopwatchState {
        STOPPED, RUNNING, PAUSED
    }

    class BookInfo {
        public String title;
        public int minutes;
        public int seconds;
        public int curPage;

        BookInfo(String title, int minutes, int seconds, int curPage) {
            this.title = title;
            this.minutes = minutes;
            this.seconds = seconds;
            this.curPage = curPage;
        }
    }


}
/*
- [ ] TODO create list of times when you read specific book
- [ ] TODO and option show those times
- [ ] TODO set an option to set a book color
- [ ] TODO add rating to books
- [ ] TODO add genre to books
- [ ] TODO add review to books
- [ ] TODO create and draw icon
- [ ] TODO add adding picture from camera
- [ ] TODO add adding picture from google
- [ ] TODO add searching for picture from barcode
- [ ] TODO add searching for picture through book photo
- [ ] TODO add status to books (todo, reading, done, paused)
- [ ] TODO search by books, reviews, authors
- [ ] TODO filter by books
- [ ] TODO clean main activity appbar. show it on click in the upper part of a screen
- [ ] TODO save current activity data and load it when returning to app



* */