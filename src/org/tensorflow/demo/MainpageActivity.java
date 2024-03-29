package org.tensorflow.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.w3c.dom.Text;

import java.sql.Array;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class MainpageActivity extends BaseActivity implements View.OnClickListener {

    /*    private RecyclerView recyclerView ;
        private RecyclerView.LayoutManager layoutManager;
        static public RecyclerViewAdapter adapter;
    */
    Calendar calendar = Calendar.getInstance();
    MaterialCalendarView materialCalendarView;
    Button btDate, btWrite, btRead, btAll, camera;
    String id, name;

    dbHelper helper;
    SQLiteDatabase db;
    ListView lv;

    //  static Bitmap bitmap;

    public static String UPDATEIS = "X";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        /* 타이틀바 설정 title.setText만 변경해주면됨*/
        View textView = (View) findViewById(R.id.icHeader);
        TextView title = (TextView) textView.findViewById(R.id.tvHeader);
        ImageButton ibMenu = (ImageButton) textView.findViewById(R.id.ibMenu);
        ImageButton ibLogout = (ImageButton) textView.findViewById(R.id.ibLogout);
        title.setText("메인페이지");


/*
        recyclerView = (RecyclerView)findViewById(R.id.recyclerVIew);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(this,3);
        layoutManager.setAutoMeasureEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        Context context = getApplicationContext();
        Drawable drawable = getResources().getDrawable(R.drawable.lc_icon);

// drawable 타입을 bitmap으로 변경
        bitmap = ((BitmapDrawable)drawable).getBitmap();

*/

        btDate = (Button) findViewById(R.id.btDate);
        btWrite = (Button) findViewById(R.id.btWrite);
        btRead = (Button) findViewById(R.id.btRead);
        btAll = (Button) findViewById(R.id.btAll);
        camera = (Button) findViewById(R.id.camera);
        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);


        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1))
                .setMaximumDate(CalendarDay.from(2030, 11, 31))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();


        Long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(date);
        btDate.setText(today);
        ibMenu.setOnClickListener(this);
        ibLogout.setOnClickListener(this);
        btDate.setOnClickListener(this);
        btWrite.setOnClickListener(this);
        btRead.setOnClickListener(this);
        camera.setOnClickListener(this);


        Intent intent = getIntent();
        name = intent.getExtras().getString("name");
        id = intent.getExtras().getString("id");

        Toast.makeText(this, name + " has signed in.", Toast.LENGTH_SHORT).show();

        ArrayList<DiaryVO> diaryVOS = new ArrayList<DiaryVO>();
        diaryVOS.add(new DiaryVO());


    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        //디비연동
        try {
            helper = new dbHelper(MainpageActivity.this);
            db = helper.getReadableDatabase();
        } catch (SQLiteException e) {
        }

        switch (v.getId()) {
            case R.id.camera:
                finish();
                Intent intent1 = new Intent(MainpageActivity.this, DetectorActivity.class); //인텐드생성
                startActivity(intent1);
                break;
            case R.id.btDate:
                new DatePickerDialog(MainpageActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK, dataSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH) + 1).show();
                break;
            case R.id.btWrite:

                final String ckDate = btDate.getText().toString();
                final String ckId = id;

                StringBuffer sbCk = new StringBuffer();
                sbCk.append("SELECT * FROM diary WHERE id = #id# AND ddate = #date#");

                String queryCk = sbCk.toString();
                queryCk = queryCk.replace("#id#", "'" + ckId + "'");
                queryCk = queryCk.replace("#date#", "'" + ckDate + "'");

                Cursor cursorCk;

                cursorCk = db.rawQuery(queryCk, null);
                if (cursorCk.moveToNext()) {
                    String no = cursorCk.getString(0);
                    Toast.makeText(getApplicationContext(), "EDIT ONLY", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainpageActivity.this);
                    // 제목셋팅
                    alertDialogBuilder.setTitle("EDIT !");

                    // AlertDialog 셋팅
                    alertDialogBuilder
                            .setMessage("Edit " + ckDate + "'s Diary?")
                            .setCancelable(false)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    modifyDiaryData(ckDate, ckId);
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    // 다이얼로그 생성
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show
                    alertDialog.show();

                    break;
                } else {
                    Intent intent = new Intent(MainpageActivity.this, DiaryWriteActivity.class);
                    intent.putExtra("date", btDate.getText().toString());
                    intent.putExtra("Id", id);
                    startActivity(intent);


                    break;
                }
            case R.id.btRead:
                String selectDate = btDate.getText().toString();

                DiaryVO diary = new DiaryVO();
                diary.setId(id);
                diary.setDdate(selectDate);

                StringBuffer sb = new StringBuffer();
                sb.append("SELECT * FROM diary WHERE id = #id# AND ddate = #date#");

                String query = sb.toString();
                query = query.replace("#id#", "'" + diary.getId() + "'");
                query = query.replace("#date#", "'" + diary.getDdate() + "'");

                Cursor cursor;

                // select문 실행
                cursor = db.rawQuery(query, null);


                if (cursor.moveToNext()) {
                    String no = cursor.getString(0);
                    String date = cursor.getString(1);
                    String dimgpath = cursor.getString(3);
                    String content = cursor.getString(4);
                    String id = cursor.getString(5);
                    byte[] byteArray2 = cursor.getBlob(6);

                    if (no != null) {
                        Toast.makeText(getApplicationContext(), no + date + dimgpath + content + id, Toast.LENGTH_SHORT).show();
                    }


                    cursor.close();
                    db.close();

                    Intent readIntent = new Intent(MainpageActivity.this, DiaryReadActivity.class);
                    readIntent.putExtra("no", no);
                    readIntent.putExtra("date", date);
                    readIntent.putExtra("dimgpath", dimgpath);
                    readIntent.putExtra("content", content);
                    readIntent.putExtra("id", id);
                    readIntent.putExtra("byteArray2",byteArray2);
                    startActivity(readIntent);

                } else {
                    Toast.makeText(getApplicationContext(), "No Diary Yet.", Toast.LENGTH_SHORT).show();
                    return;
                }
        }
    }

    // 날짜 setting
    private DatePickerDialog.OnDateSetListener dataSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String newYear, newMonth, newDayOfMonth;
            newYear = String.valueOf(year);
            month = month + 1;
            if (month <= 9) {
                newMonth = "0" + String.valueOf(month);
            } else {
                newMonth = String.valueOf(month);
            }
            if (dayOfMonth <= 9) {
                newDayOfMonth = "0" + String.valueOf(dayOfMonth);
            } else {
                newDayOfMonth = String.valueOf(dayOfMonth);
            }
            // yyyy-MM-dd
            String msg = newYear + "-" + newMonth + "-" + newDayOfMonth;
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            btDate.setText(msg);
        }
    };

    private void modifyDiaryData(String ckDate, String ckId) {
        try {
            helper = new dbHelper(MainpageActivity.this);
            db = helper.getReadableDatabase();
        } catch (SQLiteException e) {
        }


        StringBuffer sb = new StringBuffer();

        sb.append("SELECT * FROM DIARY WHERE id = #id# AND ddate = #ddate#");

        String query = sb.toString();
        query = query.replace("#id#", "'" + ckId + "'");
        query = query.replace("#ddate#", "'" + ckDate + "'");


        Cursor cursor;
        cursor = db.rawQuery(query, null);

        if (cursor.moveToNext()) {
            String no = cursor.getString(0);
            String date = cursor.getString(1);
            String dimgpath = cursor.getString(3);
            String content = cursor.getString(4);
            String id = cursor.getString(5);
            byte[] byteArray2 = cursor.getBlob(6);

            cursor.close();
            db.close();

            Intent updatIntent = new Intent(MainpageActivity.this, DiaryWriteActivity.class);
            updatIntent.putExtra("no", no);
            updatIntent.putExtra("date", date);
            updatIntent.putExtra("dimgpath", dimgpath);
            updatIntent.putExtra("content", content);
            updatIntent.putExtra("id", id);
            updatIntent.putExtra("byteArray2",byteArray2);
            updatIntent.putExtra("FLAG", "UPDATESIGN");
            MainpageActivity.UPDATEIS = "O";
            startActivity(updatIntent);

        }


    }
}