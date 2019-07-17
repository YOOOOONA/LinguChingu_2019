package org.tensorflow.demo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;



public class DiaryWriteActivity extends BaseActivity implements View.OnClickListener {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;


    private Uri mImageCaptureUri;
    private ImageView ivPic;
    private int id_view;
    private String id, date, no, dimgpath, content, updateFlag = "NON";
    public byte[] byteArray2;
    public Bitmap photo;
    private String absoultePath;

    private TextView tvDate, tvRead, tvNo;

    private Button btSave, btCancel;
    private EditText etWrite;


    dbHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        /* 타이틀바 설정 title.setText만 변경해주면됨*/
        View textView = (View) findViewById(R.id.icHeader);
        TextView title = (TextView) textView.findViewById(R.id.tvHeader);
        ImageButton ibMenu = (ImageButton) textView.findViewById(R.id.ibMenu);
        ImageButton ibLogout = (ImageButton) textView.findViewById(R.id.ibLogout);
        title.setText("LINGU CHINGU");

        tvDate = (TextView) findViewById(R.id.tvDate);
        ivPic = (ImageView) findViewById(R.id.ivPic);
        btSave = (Button) findViewById(R.id.btSave);
        btCancel = (Button) findViewById(R.id.btCancel);
        etWrite = (EditText) findViewById(R.id.etWrite);
        tvNo = (TextView) findViewById(R.id.tvNo);


        if (MainpageActivity.UPDATEIS == "O") {
            Intent updatIntent = getIntent();
            updateFlag = updatIntent.getExtras().getString("FLAG");
            no = updatIntent.getExtras().getString("no");
            date = updatIntent.getExtras().getString("date");
            dimgpath = updatIntent.getExtras().getString("dimgpath");
            content = updatIntent.getExtras().getString("content");
            id = updatIntent.getExtras().getString("id");
            byteArray2 =updatIntent.getExtras().getByteArray("byteArray2");

            absoultePath = dimgpath;
            tvNo.setText(no);
            tvDate.setText(date);
            // 이미지나 내용이 없을경우 발생하면 null 오류 발생하여
            // 한번 더 체크하게 만듬
            if (byteArray2 != null) {
                photo = BitmapFactory.decodeByteArray(byteArray2,0,byteArray2.length);
                ivPic.setImageBitmap(photo);
            }
            if (content != null) {
                etWrite.setText(content);
            }
            MainpageActivity.UPDATEIS = "X";
        } else {

            Intent intent = getIntent();
            id = intent.getExtras().getString("Id");
            date = intent.getExtras().getString("date");

            tvDate.setText(date);

        }
        ivPic.setOnClickListener(this);
        btSave.setOnClickListener(this);
        btCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.ivPic: {

                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTakeAlbumAction();
                    }
                };
                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle("Select a photo")
                        .setNeutralButton("Select an album", albumListener)
                        .setNegativeButton("Cancel", cancelListener)
                        .show();
                break;
            }

            case R.id.btSave: {
                Log.d("저장할때엔", absoultePath + etWrite.getText().toString());
                // db준비
                try {
                    helper = new dbHelper(DiaryWriteActivity.this);
                    db = helper.getWritableDatabase();
                } catch (SQLiteException e) {
                    db = helper.getReadableDatabase();
                }
                // 로그인 정보
                MApp appDel = (MApp) getApplication();

                DiaryVO diary = new DiaryVO();
                diary.setId(id);
                diary.setDdate(date);
                diary.setDcontent(etWrite.getText().toString());

                diary.setDimgpath(absoultePath);
                diary.setByteArray2(byteArray2);
                // 쿼리준비
                if (updateFlag.equals("UPDATESIGN")) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("UPDATE diary ");
                    sb.append("SET dimgpath = ?, dcontent = ?, byteArray2=? ");
                    sb.append("WHERE id = ? AND dno = ?");

                    String query = sb.toString();
                    db.execSQL(query, new Object[]{diary.getDimgpath(),diary.getDcontent(),diary.getByteArray2(),diary.getId(),tvNo.getText().toString()});

/*
                    query = query.replace("#imgpath#", "'" + diary.getDimgpath() + "'");
                    query = query.replace("#content#", "'" + diary.getDcontent() + "'");
                    query = query.replace("#no#", tvNo.getText().toString());
                    query = query.replace("#id#", "'" + diary.getId() + "'");
                    db.execSQL(query);
                    db.close();
                    Intent intent = new Intent(DiaryWriteActivity.this, DiaryReadActivity.class);
                    //startActivityForResult();
                    finish();
*/
                    db.close();
                    Intent intent = new Intent(DiaryWriteActivity.this, DiaryReadActivity.class);
                    intent.putExtra("byteArray2",byteArray2);/////////////////
                    startActivity(intent);////////////////////////
                    photo = BitmapFactory.decodeByteArray(byteArray2,0  ,byteArray2.length);
                    ivPic.setImageBitmap(photo);
                    finish();

                } else {
                    StringBuffer sb = new StringBuffer();
                    sb.append("INSERT INTO diary (");
                    sb.append("dno, ddate, dimgpath, dcontent, id , byteArray2)");
                    sb.append("VALUES (?, ?, ?, ?, ?,?)");
                    db.execSQL(sb.toString(), new Object[]{null, diary.getDdate(), diary.getDimgpath(),
                                            diary.getDcontent(), diary.getId(), diary.getByteArray2()});
                    db.close();
                    finish();
                }
                MainpageActivity.UPDATEIS = "X";

                Data d =new Data();////////////??????????????///

                d.setTitle("HIHI");
                /*MainpageActivity.adapter.addItem(d);
                MainpageActivity.adapter.notifyDataSetChanged();*/
            }
            case R.id.btCancel: {
                finish();
            }
        }
    }

    public void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {

            case PICK_FROM_ALBUM: {
                mImageCaptureUri = data.getData();
            }
            case PICK_FROM_CAMERA: {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                intent.putExtra("outputX", 200); // 크롭한 이미지의 X크기
                intent.putExtra("outputY", 200); // 크롭한 이미지의 Y크기
                intent.putExtra("aspectX", 1); // 크롭박스 X비율
                intent.putExtra("aspectY", 1); // 크롭박스 Y비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_IMAGE);
                break;
            }
            case CROP_FROM_IMAGE: {
                if (resultCode != RESULT_OK) {
                    return;
                }
                final Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo1 = extras.getParcelable("data"); // crop된 bitmap
                    ivPic.setImageBitmap(photo1);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo1.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byteArray2 = stream.toByteArray();
                    photo = BitmapFactory.decodeByteArray(byteArray2,0  ,byteArray2.length);
                    break;
                }
            }
        }

    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DiaryWriteActivity.this);

        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("Exit the edit page?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

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
}
