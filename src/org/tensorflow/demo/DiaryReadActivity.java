package org.tensorflow.demo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



public class DiaryReadActivity extends BaseActivity implements View.OnClickListener {

    private Uri mImageCaptureUri;
    private ImageView ivPic;
    private int id_view;
    private String no, date, dimgpath, content, id;
    byte[] byteArray2;
    private String absoultePath;
    private TextView tvDate, tvRead, tvNo;
    private Button btPre, btHome, btModify, btNext, btDelete;
    public static final int UPDATESIGN = 3;


    dbHelper helper;
    SQLiteDatabase db;
    Bitmap photo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        /* 타이틀바 설정 title.setText만 변경해주면됨*/
        View textView = (View) findViewById(R.id.icHeader);
        TextView title = (TextView) textView.findViewById(R.id.tvHeader);
        ImageButton ibMenu = (ImageButton) textView.findViewById(R.id.ibMenu);
        ImageButton ibLogout = (ImageButton) textView.findViewById(R.id.ibLogout);
        title.setText("LINGU CHINGU");


        tvDate = (TextView) findViewById(R.id.tvDate);
        ivPic = (ImageView) findViewById(R.id.ivPic);
        tvNo = (TextView) findViewById(R.id.tvNo);

        btHome = (Button) findViewById(R.id.btHome);
        tvRead = (TextView) findViewById(R.id.tvRead);
        btPre = (Button) findViewById(R.id.btPre);
        btNext = (Button) findViewById(R.id.btNext);
        btModify = (Button) findViewById(R.id.btModify);
        btDelete = (Button) findViewById(R.id.btDelete);

        Intent intent = getIntent();
        no = intent.getExtras().getString("no");
        date = intent.getExtras().getString("date");
        dimgpath = intent.getExtras().getString("dimgpath");
        content = intent.getExtras().getString("content");
        id = intent.getExtras().getString("id");
        byteArray2 = intent.getExtras().getByteArray("byteArray2");


        // setImage, setText
        // Intent로 받아온 데이터 뿌려주는 작업
        // 이미지 uri는 string으로 받아오기 때문에 parse작업이 꼭 필요함


        // 이미지나 내용이 없을경우 발생하면 null 오류 발생하여
        // 한번 더 체크하게 만듬
        if (byteArray2 != null) {
            photo = BitmapFactory.decodeByteArray(byteArray2,0,byteArray2.length);//byte -> bitmap
            ivPic.setImageBitmap(photo);//imageView에 bitmap 출력
        }
        if (content != null) {
            tvRead.setText(content);
        }
        tvDate.setText(date);
        tvNo.setText(no);

        btHome.setOnClickListener(this);
        btNext.setOnClickListener(this);
        btPre.setOnClickListener(this);
        btModify.setOnClickListener(this);
        btDelete.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btHome: {
                finish();
                break;
            }
            case R.id.btPre: {
                DiaryVO diary = new DiaryVO();
                diary.setId(id);
                diary.setDdate(tvDate.getText().toString());
                //diary.setNo(tvNo.getText().toString());
                /* 이전버튼 = P, 다음버튼 = N */
                String flag = "P";
                changeDiaryData(diary, flag);
                break;
            }
            case R.id.btNext: {
                DiaryVO diary = new DiaryVO();
                diary.setId(id);
                diary.setDdate(tvDate.getText().toString());
                String flag = "N";
                changeDiaryData(diary, flag);
                break;
            }
            case R.id.btModify: {
                DiaryVO diary = new DiaryVO();
                diary.setId(id);
                diary.setNo(tvNo.getText().toString());
                modifyDiaryData(diary);

                break;
                //Intent intent = new Intent(DiaryReadActivity.this, DiaryWriteActivity.class);
                //intent.putExtra("diary")

            }
            case R.id.btDelete: {
                DiaryVO diary = new DiaryVO();
                diary.setNo(no);
                deleteDiaryData(diary);
                break;
            }
        }
    }

    private void deleteDiaryData(final DiaryVO diary) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DiaryReadActivity.this);

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("ARE YOU SURE?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            helper = new dbHelper(DiaryReadActivity.this);
                            db = helper.getWritableDatabase();
                        } catch (SQLiteException e) {
                        }

                        StringBuffer sb = new StringBuffer();
                        sb.append("DELETE FROM DIARY WHERE dno = #no#");
                        String query = sb.toString();
                        query = query.replace("#no#", diary.getNo());
                        db.execSQL(query);
                        db.close();
                        finish();
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


    }

    private void changeDiaryData(DiaryVO diary, String flag) {

        try {
            helper = new dbHelper(DiaryReadActivity.this);
            db = helper.getReadableDatabase();
        } catch (SQLiteException e) {
        }

        StringBuffer sb = new StringBuffer();
        if (flag.equals("N")) {
            sb.append("SELECT * FROM DIARY WHERE id = #id# AND ddate > #date# ORDER BY ddate ASC LIMIT 1");
        } else if (flag.equals("P")) {
            sb.append("SELECT * FROM DIARY WHERE id = #id# AND ddate < #date# ORDER BY ddate DESC LIMIT 1");
        }
        String query = sb.toString();
        query = query.replace("#id#", "'" + diary.getId() + "'");
        query = query.replace("#date#", "'" + diary.getDdate() + "'");

        Cursor cursor;
        cursor = db.rawQuery(query, null);

        if (cursor.moveToNext()) {
            String no = cursor.getString(0);
            String date = cursor.getString(1);
            String dimgpath = cursor.getString(3);
            String content = cursor.getString(4);
            String id = cursor.getString(5);
            byte[] byteArray2 = cursor.getBlob(6);
            photo = BitmapFactory.decodeByteArray(byteArray2,0,byteArray2.length);

            if (no != null) {
                photo = BitmapFactory.decodeByteArray(byteArray2,0,byteArray2.length);
                Toast.makeText(getApplicationContext(), no + date + dimgpath + content + id, Toast.LENGTH_SHORT).show();
            }

            cursor.close();
            db.close();

            diary.setNo(no);
            diary.setDdate(date);
            diary.setDimgpath(dimgpath);
            diary.setDcontent(content);
            diary.setId(id);
            diary.setByteArray2(byteArray2);

            if (byteArray2 != null) {
                photo=BitmapFactory.decodeByteArray(byteArray2,0,byteArray2.length);
                ivPic.setImageBitmap(photo);
            }
            if (content != null) {
                tvRead.setText(content);
            }
            tvDate.setText(date);
            tvNo.setText(no);

        } else {
            Toast.makeText(getApplicationContext(), "NO DIARY!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void modifyDiaryData(DiaryVO diary) {
        try {
            helper = new dbHelper(DiaryReadActivity.this);
            db = helper.getReadableDatabase();
        } catch (SQLiteException e) {
        }

        StringBuffer sb = new StringBuffer();

        sb.append("SELECT * FROM DIARY WHERE id = #id# AND dno = #no#");

        String query = sb.toString();
        query = query.replace("#id#", "'" + diary.getId() + "'");
        query = query.replace("#no#", diary.getNo());


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

            Intent updatIntent = new Intent(DiaryReadActivity.this, DiaryWriteActivity.class);
            updatIntent.putExtra("no", no);
            updatIntent.putExtra("date", date);
            updatIntent.putExtra("dimgpath", dimgpath);
            updatIntent.putExtra("content", content);
            updatIntent.putExtra("id", id);
            updatIntent.putExtra("byteArray2",byteArray2);
            updatIntent.putExtra("FLAG", "UPDATESIGN");
            MainpageActivity.UPDATEIS = "O";
            startActivityForResult(updatIntent, UPDATESIGN);

        }


    }


}
