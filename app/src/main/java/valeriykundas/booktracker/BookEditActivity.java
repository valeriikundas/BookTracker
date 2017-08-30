package valeriykundas.booktracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class BookEditActivity extends AppCompatActivity {

    private final String[] columns = {
            DBHelper.COLUMN_NAME_TITLE,
            DBHelper.COLUMN_NAME_AUTHOR,
            DBHelper.COLUMN_NAME_BOOK_COVER_IMAGE_PATH,
            DBHelper.COLUMN_NAME_CURRENT_PAGE,
            DBHelper.COLUMN_NAME_PAGE_COUNT,
    };
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);

        EditText et = findViewById(R.id.edit_et_title);
        final String title = getIntent().getStringExtra("title");
        et.setText(title);

        dbHelper = DBHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBHelper.TABLE_NAME, columns, DBHelper.COLUMN_NAME_TITLE + " = ?", new String[]{title}, null, null, null);
        cursor.moveToFirst();

        final String author = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_AUTHOR));
        final String cover_image_path = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_BOOK_COVER_IMAGE_PATH));
        final int current_page = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_CURRENT_PAGE));
        final int page_count = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME_PAGE_COUNT));

        et = findViewById(R.id.edit_et_author);
        et.setText(author);

        et = findViewById(R.id.edit_et_curpage);
        et.setText(String.valueOf(current_page));

        et = findViewById(R.id.edit_et_pagecount);
        et.setText(String.valueOf(page_count));

        File image_file;
        try {
            image_file = new File(cover_image_path);
            if (image_file != null && image_file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(image_file.getAbsolutePath());
                ImageView imv = findViewById(R.id.edit_imgv_cover);
                imv.setImageBitmap(bm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button save_button = findViewById(R.id.edit_btn_save);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                Cursor cursor = db.query(DBHelper.TABLE_NAME, columns, DBHelper.COLUMN_NAME_TITLE + " = ?", new String[]{title}, null, null, null);
                cursor.moveToFirst();

                EditText et = findViewById(R.id.edit_et_title);
                String new_title = et.getText().toString();

                final String author = ((EditText) findViewById(R.id.edit_et_author)).getText().toString();
                // final String cover_image_path = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_NAME_BOOK_COVER_IMAGE_PATH));
                final int current_page = Integer.parseInt(((EditText) findViewById(R.id.edit_et_curpage)).getText().toString());
                final int page_count = Integer.parseInt(((EditText) findViewById(R.id.edit_et_pagecount)).getText().toString());

                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_NAME_TITLE, new_title);
                values.put(DBHelper.COLUMN_NAME_AUTHOR, author);
                values.put(DBHelper.COLUMN_NAME_CURRENT_PAGE, current_page);
                values.put(DBHelper.COLUMN_NAME_PAGE_COUNT, page_count);
                values.put(DBHelper.COLUMN_NAME_BOOK_COVER_IMAGE_PATH, cover_image_path);

                db.update(DBHelper.TABLE_NAME, values, DBHelper.COLUMN_NAME_TITLE + "= ?", new String[]{title});
                db.close();

                finish();
            }
        });
    }
}
