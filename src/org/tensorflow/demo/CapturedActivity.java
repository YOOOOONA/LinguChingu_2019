package org.tensorflow.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;

public class CapturedActivity extends Activity{
    //로컬 데이터베이스에 필요한 변수 선언
    SQLiteDatabase db;
    final String DATABASE_NAME = "DB39.db";
    final String TABLENAME = "LabelTable";
    final String TABLENAME2 = "SendImage";

    public ArrayList<String> title = new ArrayList<String>();
    public ArrayList<Float> left = new ArrayList<Float>();
    public ArrayList<Float> top = new ArrayList<Float>();
    public ArrayList<Float> right = new ArrayList<Float>();
    public ArrayList<Float> bottom = new ArrayList<Float>();
    public ArrayList<String> translated_korean = new ArrayList<String>();
    public ArrayList<String> translated_roman = new ArrayList<String>();

    public Integer length1;


    private TextToSpeech textToSpeech;
    ConstraintLayout constraintLayout;


    TextView korean_rome;

    TextView textview;

    TextView view1;

    Button soundbtn;
    Button gotodiary;

    View container;
    byte[] byteArray;
    byte[] bytecaptured;
    byte[] image;

    boolean possible;
    String rome;
    String en;

    public Float height;
    public Float width;
    public String recttitle;
    public Float rectleft;
    public Float recttop;
    public Float rectright;
    public Float rectbottom;
    public Integer len;
    public Integer getId;
    public Integer romanId;
    public float x=0;
    public float y=0;
    public String text;

    public ArrayList<String>result_korean = new ArrayList<String>();

    public final String CAPTURE_PATH = "/CAPTURE_TEST";

    public View testmain;
    ImageView imagetest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture);
        constraintLayout = (ConstraintLayout) findViewById(R.id.con);
        constraintLayout.addView(new MyView2(this));

        Button button = (Button)findViewById(R.id.button2);
        gotodiary = (Button)findViewById(R.id.diarybtn);
        view1 = (TextView) findViewById(R.id.textView2);

        textview = (TextView) findViewById(R.id.textView3);
        //soundbtn = (Button) findViewById(R.id.button3);


        korean_rome = (TextView)findViewById(R.id.textView4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), DetectorActivity.class); //인텐드생성
                startActivity(intent);
            }
        });

        //label Intent 받기
        Intent intent = getIntent();
        length1 = intent.getExtras().getInt("length");

        //원소들 확인

        for (int i = 0; i < length1; i++) {
            select(i);
        }
        //데이터베이스 전체 삭제
        delete();

        System.out.println(" 사물 " + title + " ( " + left + ", " + top + ", " +
                right + ", " + bottom + ")");

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.KOREA);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textToSpeech.setSpeechRate(0.8f);

        /*imagetest = (ImageView) findViewById(R.id.image);*/
        //다이어리 페이지 버튼
        gotodiary.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                /*View v = new View(getApplicationContext());
                v.getRootView();
                v.setDrawingCacheEnabled(true);
                v.buildDrawingCache(true);
                Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
                v.setDrawingCacheEnabled(false);*/

                //bytecaptured = captureView();
                //System.out.println("캡쳐 바이트 = " + byteArray);
                /*container = getWindow().getDecorView();
                container.buildDrawingCache(); //여기서 문제
                Bitmap captureView = container.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                captureView.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byteArray = stream.toByteArray();*/

                //byte넘기면...? 이건 가능한가?
                Intent intent2 = new Intent(getApplicationContext(),DiaryMain.class);
                startActivity(intent2);
            }
        });
    }


    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }


    public byte[] captureView(){
        //전체 화면 캡쳐
        container = getWindow().getDecorView();
        container.buildDrawingCache();
        Bitmap captureView = container.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        captureView.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byteArray = stream.toByteArray();

        return byteArray;
    }

    public void select(Integer id) {
        db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        String sql = "select title, box_left, box_top, box_right, box_bottom from " + TABLENAME + " WHERE _id= " + id + ";";
        Cursor cursor = db.rawQuery(sql, null);
        System.out.println("조회된 데이터 개수 : " + cursor.getCount());

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();

            System.out.println("#" + i + " -> 사물 " + cursor.getString(0) + " ( " + (float) cursor.getFloat(1)
                    + ", " + cursor.getFloat(2) + ", " +
                    cursor.getFloat(3) + ", " + cursor.getFloat(4) + ")");

            System.out.println(cursor.getString(0));
            title.add(cursor.getString(0));
            left.add(cursor.getFloat(1));
            top.add(cursor.getFloat(2));
            right.add(cursor.getFloat(3));
            bottom.add(cursor.getFloat(4));


        }
        cursor.close();
    }

    public void delete() {
        db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        System.out.println("레이블 데이터베이스 삭제");
    }
    public void delete_image() {
        db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME2);
        System.out.println("이미지 데이터베이스 삭제");
    }

    /*public static void captureActivity(Activity context) {
        final String CAPTURE_PATH = "/CAPTURE_TEST";

        if(context == null) return;
        View root = context.getWindow().getDecorView().getRootView();
        root.setDrawingCacheEnabled(true);
        root.buildDrawingCache();
        // 루트뷰의 캐시를 가져옴
        Bitmap screenshot = root.getDrawingCache();

        // get view coordinates
        int[] location = new int[2];
        root.getLocationInWindow(location);

        // 이미지를 자를 수 있으나 전체 화면을 캡쳐 하도록 함
        Bitmap bmp = Bitmap.createBitmap(screenshot, location[0], location[1], root.getWidth(), root.getHeight(), null, false);
        String strFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + CAPTURE_PATH;
        File folder = new File(strFolderPath);
        if(!folder.exists()) {
            folder.mkdirs();
        }

        String strFilePath = strFolderPath + "/" + System.currentTimeMillis() + ".png";
        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;
        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    class MyView2 extends View{
        public MyView2(Context context){super(context);}

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            len = title.size();
            System.out.println("확인 = " + title + len + left);
            getId = 0;
            romanId = 0;

//-------------------------------------------------여기서부터-------------------------------------------
            canvas.save();
            canvas.scale(0.4f, 0.4f);
            canvas.rotate(90,1100,1400);
            //이미지 불러오기 위한 데이터베이스
            db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE,null);
            //String sql = "select image from SendImage where id="+id+";";
            Cursor c = db.rawQuery("select image from SendImage", null);

            if(c.moveToNext()) {
                image = c.getBlob(0);
                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                canvas.drawBitmap(bmp, 0, 0, null);
            }
            //수정
            delete_image();
            c.close();
            db.close();
            canvas.restore();
//-------------------------------------------------여기 까지 --------------------------------------------
            canvas.save();
            canvas.rotate(90, 420, 580);
            canvas.scale(1.9f, 1.9f);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setTextSize(30);

            //사각형 그리기
            Paint paint2 = new Paint();
            paint2.setStyle(Paint.Style.STROKE); // 선 그리기
            paint2.setStrokeWidth(5.0f);

            for (int i = 0; i < len; i++) {

                if(i==0) { paint2.setARGB(204, 229,106, 21); }
                else if(i==1){ paint2.setARGB(204, 77, 229, 6); }
                else if(i==2){ paint2.setARGB(204, 247 , 230, 78); }
                else if(i==3){ paint2.setARGB(204, 236, 66, 48); }
                else { paint2.setARGB(204, 61, 104,243); }

                recttitle = title.get(i);
                rectleft = left.get(i);
                recttop = top.get(i);
                rectright = right.get(i);
                rectbottom = bottom.get(i);

                System.out.println(recttitle + ", " + rectleft + ", " + recttop + ", " + rectright + ", " + rectbottom);
                RectF rectF = new RectF(rectleft, recttop, rectright, rectbottom);
                canvas.drawRect(rectleft, recttop, rectright, rectbottom, paint2);

                //canvas.drawText(recttitle, rectright, rectbottom, paint);
                //canvas.drawText(recttitle, rectleft, rectbottom, paint);

                width = rectF.width();
                height = rectF.height();
                System.out.println(" 높이 = " + rectF.width() + " 너비 = " + rectF.height());
                System.out.println("캔버스 높이 = " + canvas.getHeight() + " 캔버스 너비 = " + canvas.getWidth());

                System.out.println(" rectitle = " + recttitle);
                System.out.println(" en = " + en);
                System.out.println("------------"+en+"-------------");
                NaverTranslateTask asyncTask = new NaverTranslateTask();
                // String sText = english.getText().toString();
                en = recttitle;
                System.out.println("en = " + en);

                String speak1 = "잠시만기다려줘";
                textToSpeech.speak(speak1, TextToSpeech.QUEUE_FLUSH, null, null);

                Toast.makeText(getApplicationContext(), "잠시만 기다려줘!", Toast.LENGTH_SHORT).show();
                asyncTask.execute(en);


                //------------------------------------영어 레이블 출력 테스트--------------------------------
                ConstraintLayout.LayoutParams lap = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);

                textview = new TextView(getContext());
                textview.setText(recttitle);
                textview.setX(100);
                textview.setY(1400 + (i*100));
                textview.setTextSize(18);
                if(i==0) { textview.setTextColor(Color.argb(204, 229,106, 21)); }
                else if(i==1){ textview.setTextColor(Color.argb(204, 77, 229, 6)); }
                else if(i==2){ textview.setTextColor(Color.argb(204, 247 , 230, 78)); }
                else if(i==3){ textview.setTextColor(Color.argb(204, 236, 66, 48)); }
                else {textview.setTextColor(Color.argb(204, 61, 104,243)); }
                textview.setLayoutParams(lap);
                constraintLayout.addView(textview);

                final Integer j = i;
                textview.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        String a = translated_korean.get(j);
                        Log.i("TTS", "button clicked: " + a);
                        int speechStatus = textToSpeech.speak(a, TextToSpeech.QUEUE_FLUSH, null, null);

                        if (speechStatus == TextToSpeech.ERROR) {
                            Log.e("TTS", "Error in converting Text to Speech!");
                        }
                    }

                });

            }

            canvas.restore();

        }

    }





    public class NaverTranslateTask extends AsyncTask<String, Void, String> {

        public String resultText;
        //Naver
        String clientId = "1dfX5Mw23sgDh5sQzTEB";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "ppX6hEqvRR";//애플리케이션 클라이언트 시크릿값";
        //언어선택도 나중에 사용자가 선택할 수 있게 옵션 처리해 주면 된다.
        String sourceLang = "en";
        String targetLang = "ko";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //AsyncTask 메인처리
        @Override
        protected String doInBackground(String... strings) {
            String sourceText = strings[0];

            try {
                //String text = URLEncoder.encode("만나서 반갑습니다.", "UTF-8");
                String text = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source="+sourceLang+"&target="+targetLang+"&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println(response.toString());
                return response.toString();

            } catch (Exception e) {
                //System.out.println(e);
                Log.d("error", e.getMessage());
                return null;
            }
        }

        //번역된 결과를 받아서 처리
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //최종 결과 처리부
            //Log.d("background result", s.toString()); //네이버에 보내주는 응답결과가 JSON 데이터이다.

            //JSON데이터를 자바객체로 변환해야 한다.
            //Gson을 사용할 것이다.

            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(s.toString())
                    //원하는 데이터 까지 찾아 들어간다.
                    .getAsJsonObject().get("message")
                    .getAsJsonObject().get("result");
            //안드로이드 객체에 담기
            TranslatedItem items = gson.fromJson(rootObj.toString(), TranslatedItem.class);

            String kor = items.getTranslatedText();

            System.out.println(" 한국말 = " + kor);

            //여러 레이블을 동적으로 출력하는 코드
            ConstraintLayout.LayoutParams lap = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);

            view1 = new TextView(getApplicationContext());
            view1.setText(kor);
            System.out.println(" 증가 값 = " + getId);
            view1.setX(450);
            view1.setY(1400 + (getId*100));
            view1.setTextSize(18);
            if(getId==0) { view1.setTextColor(Color.argb(204, 229,106, 21)); }
            else if(getId==1){ view1.setTextColor(Color.argb(204, 77, 229, 6)); }
            else if(getId==2){ view1.setTextColor(Color.argb(204, 247 , 230, 78)); }
            else if(getId==3){ view1.setTextColor(Color.argb(204, 236, 66, 48)); }
            else {view1.setTextColor(Color.argb(204, 61, 104,243)); }
            view1.setLayoutParams(lap);
            constraintLayout.addView(view1);

            translated_korean.add(kor);
            //-------------------------------------에러 나서 없앰-------------------------------------
            APIExamRoman apiExamRoman = new APIExamRoman();
            apiExamRoman.execute(kor);

            getId += 1;

        }

        //자바용 그릇
        private class TranslatedItem {
            String translatedText;

            public String getTranslatedText() {
                return translatedText;
            }
        }
    }
    // 네이버 한글인명-로마자변환 API 예제
    class APIExamRoman extends AsyncTask<String, Void, String> {


        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            String clientId = "1dfX5Mw23sgDh5sQzTEB";//애플리케이션 클라이언트 아이디값";
            String clientSecret = "ppX6hEqvRR";//애플리케이션 클라이언트 시크릿값";
            String sourceText = strings[0];

            String fullStr = sourceText;
            int len = fullStr.length();
            char lastChar = fullStr.charAt(fullStr.length()-1);
            if(lastChar=='.') {
                sourceText = sourceText.substring(0, len - 1);
                //          korean = sourceText;
                System.out.println(".을 떼었음 : "+ sourceText);
            }
            System.out.println("-----"+sourceText+"-------");
            System.out.println("로마자로 변환합니다.");

            possible= false;

            if(sourceText.equals("곰"))
            {
                rome = "Gom";
                possible = true;
            }
            else if(sourceText.equals("키보드")){
                rome = "kibodeu";
                possible = true;
            }
            else if(sourceText.equals("자전거.")){
                rome = "jajeongeo";
                possible = true;
            }
            else if(sourceText.equals("비행기의")){
                rome = "bihaenggiui";
                possible = true;
            }
            else if(sourceText.equals("버스")){
                rome = "beoseu";
                possible = true;
            }
            else if(sourceText.equals("무개화차")){
                rome = "mugaehwacha";
                possible = true;
            }
            else if(sourceText.equals("보트")){
                rome = "boteu";
                possible = true;
            }
            else if(sourceText.equals("교통 신호등")){
                rome = "gyotong sinhodeung";
                possible = true;
            }
            else if(sourceText.equals("정지 표지판")){
                rome = "jeongji pyojipan";
                possible = true;
            }
            else if(sourceText.equals("주차 미터")){
                rome = "jucha miteo";
                possible = true;
            }
            else if(sourceText.equals("벤치")){
                rome = "benchi";
                possible = true;
            }
            else if(sourceText.equals("새")){
                rome = "sae";
                possible = true;
            }
            else if(sourceText.equals("개.")){
                rome = "gae";
                possible = true;
            }
            else if(sourceText.equals("말.")){
                rome = "mal";
                possible = true;
            }
            else if(sourceText.equals("코끼리")){
                rome = "kokkiri";
                possible = true;
            }
            else if(sourceText.equals("새")){
                rome = "sae";
                possible = true;
            }
            else if(sourceText.equals("견디다")){
                rome = "gyeondida";
                possible = true;
            }
            else if(sourceText.equals("얼룩말")){
                rome = "eollungmal";
                possible = true;
            }
            else if(sourceText.equals("배낭을 메다")){
                rome = "baenangeul meda";
                possible = true;
            }
            else if(sourceText.equals("핸드백")){
                rome = "haendeubaek";
                possible = true;
            }
            else if(sourceText.equals("넥타이")){
                rome = "nektai";
                possible = true;
            }
            else if(sourceText.equals("여행 가방")){
                rome = "yeohaeng gabang";
                possible = true;
            }
            else if(sourceText.equals("프리즈비")){
                rome = "peurijeubi";
                possible = true;
            }
            else if(sourceText.equals("스키를 타다")){
                rome = "seukireul tada";
                possible = true;
            }
            else if(sourceText.equals("스노보드")){
                rome = "seunobodeu";
                possible = true;
            }
            else if(sourceText.equals("스포츠볼")){
                rome = "seupocheubol";
                possible = true;
            }
            else if(sourceText.equals("야구 글러브")){
                rome = "yagu geulleobeu";
                possible = true;
            }
            else if(sourceText.equals("스케이트보드")){
                rome = "seukeiteubodeu";
                possible = true;
            }
            else if(sourceText.equals("테니스 라켓")){
                rome = "teniseu raket";
                possible = true;
            }
            else if(sourceText.equals("칫솔")){
                rome = "chitsol";
                possible = true;
            }

            else if(sourceText.equals("헤어드라이어")){
                rome = "heeodeuraieo";
                possible = true;
            }

            else if(sourceText.equals("테디베어")){
                rome = "tedibeeo";
                possible = true;
            }

            else if(sourceText.equals("가위")){
                rome = "gawi";
                possible = true;
            }

            else if(sourceText.equals("꽃병")){
                rome = " kkotbyeong";
                possible = true;
            }

            else if(sourceText.equals("시계")){
                rome = "sigye";
                possible = true;
            }

            else if(sourceText.equals("책")){
                rome = "chaek";
                possible = true;
            }

            else if(sourceText.equals("냉장고")){
                rome = "naengjanggo";
                possible = true;
            }

            else if(sourceText.equals("가라앉다")){
                rome = "garaanda";
                possible = true;
            }

            else if(sourceText.equals("토스터")){
                rome = "toseuteo";
                possible = true;
            }

            else if(sourceText.equals("오븐")){
                rome = "obeun";
                possible = true;
            }

            else if(sourceText.equals("전자레인지")){
                rome = "jeonjareinji";
                possible = true;
            }

            else if(sourceText.equals("휴대 전화.")){
                rome = "hyudae jeonhwa";
                possible = true;
            }

            else if(sourceText.equals("원격의")){
                rome = "wongyeogui";
                possible = true;
            }

            else if(sourceText.equals("쥐")){
                rome = "jwi";
                possible = true;
            }

            else if(sourceText.equals("노트북")){
                rome = "noteubuk";
                possible = true;
            }

            else if(sourceText.equals("텔레비전")){
                rome = "tellebijeon";
                possible = true;
            }

            else if(sourceText.equals("화장실.")){
                rome = "hwajangsil";
                possible = true;
            }

            else if(sourceText.equals("식탁")){
                rome = "siktak";
                possible = true;
            }

            else if(sourceText.equals("침대")){
                rome = "chimdae";
                possible = true;
            }

            else if(sourceText.equals("화분.")){
                rome = "hwabun";
                possible = true;
            }

            else if(sourceText.equals("코치")){
                rome = "kochi";
                possible = true;
            }

            else if(sourceText.equals("의자.")){
                rome = "uija";
                possible = true;
            }

            else if(sourceText.equals("케익")){
                rome = "keik";
                possible = true;
            }

            else if(sourceText.equals("도넛")){
                rome = "doneot";
                possible = true;
            }

            else if(sourceText.equals("피자")){
                rome = "pija";
                possible = true;
            }

            else if(sourceText.equals("핫도그")){
                rome = "hatdogeu";
                possible = true;
            }

            else if(sourceText.equals("당근")){
                rome = "danggeun";
                possible = true;
            }

            else if(sourceText.equals("브로콜리")){
                rome = "beurokolli";
                possible = true;
            }

            else if(sourceText.equals("오렌지색의")){
                rome = "orenjisaeg";
                possible = true;
            }

            else if(sourceText.equals("샌드위치")){
                rome = "saendeuwichi";
                possible = true;
            }

            else if(sourceText.equals("사과")){
                rome = "sagwa";
                possible = true;
            }

            else if(sourceText.equals("바나나")){
                rome = " banana";
                possible = true;
            }

            else if(sourceText.equals("그릇")){
                rome = "geureut";
                possible = true;
            }

            else if(sourceText.equals("숟가락")){
                rome = "sutgarak";
                possible = true;
            }

            else if(sourceText.equals("칼")){
                rome = "kal";
                possible = true;
            }

            else if(sourceText.equals("포크")){
                rome = "pokeu";
                possible = true;
            }

            else if(sourceText.equals("컵")){
                rome = "keop";
                possible = true;
            }

            else if(sourceText.equals("와인잔")){
                rome = "wainjan";
                possible = true;
            }

            else if(sourceText.equals("병")){
                rome = "byeong";
                possible = true;
            }

            try {
                String text = URLEncoder.encode(sourceText, "UTF-8");
                System.out.println(text);
                String apiURL = "https://openapi.naver.com/v1/krdict/romanization?query="+ text;
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    System.out.println("정상호출");
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    System.out.println("에러발생");
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                    System.out.println(inputLine);
                }

                br.close();

                String s  = response.toString();
                System.out.println(s);

                if(!possible) {

                    Gson gson = new GsonBuilder().create();
                    JsonParser parser = new JsonParser();
                    JsonElement rootObj = parser.parse(s.toString())
                            //원하는 데이터 까지 찾아 들어간다.
                            .getAsJsonObject().get("aResult");
                    //안드로이드 객체에 담기
                    JsonArray jsonArray = (JsonArray) parser.parse(rootObj.toString());

                    JsonObject object = (JsonObject) jsonArray.get(0);
                    JsonElement aItems = parser.parse(object.toString())
                            //원하는 데이터 까지 찾아 들어간다.
                            .getAsJsonObject().get("aItems");
                    JsonArray jsonArray2 = (JsonArray) parser.parse(aItems.toString());
                    JsonObject object2 = (JsonObject) jsonArray2.get(0);
                    System.out.println(object2.toString());
                    JsonElement name = parser.parse(object2.toString())
                            //원하는 데이터 까지 찾아 들어간다.
                            .getAsJsonObject().get("name");
                    System.out.println(name.toString());

                    rome = name.toString();
                }

                return rome;
            } catch (Exception e) {
                System.out.println(rome);
                return null;
            }

        }

        protected void onPostExecute(String rome) {
            super.onPostExecute(rome);

            //여러 레이블을 동적으로 출력하는 코드
            try {
                ConstraintLayout.LayoutParams lap = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);


                System.out.println("현재 possible 값5 = ");
                System.out.println(possible);

                korean_rome = new TextView(getApplicationContext());
                korean_rome.append(rome);
                System.out.println("설정합니다." + rome);
                System.out.println("로마 증가 값 = " + romanId);

                korean_rome.setX(700);
                korean_rome.setY(1400 + (romanId * 100));
                korean_rome.setTextSize(18);
                if (romanId == 0) {
                    korean_rome.setTextColor(Color.argb(204, 229, 106, 21));
                } else if (romanId == 1) {
                    korean_rome.setTextColor(Color.argb(204, 77, 229, 6));
                } else if (romanId == 2) {
                    korean_rome.setTextColor(Color.argb(204, 247, 230, 78));
                } else if (romanId == 3) {
                    korean_rome.setTextColor(Color.argb(204, 236, 66, 48));
                } else {
                    korean_rome.setTextColor(Color.argb(204, 61, 104, 243));
                }
                korean_rome.setLayoutParams(lap);
                constraintLayout.addView(korean_rome);
                romanId += 1;
            }catch(Exception e){
                System.out.println("로마자 에러 발생");
            }

        }

    }


}
