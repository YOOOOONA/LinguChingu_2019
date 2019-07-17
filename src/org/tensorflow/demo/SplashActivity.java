package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity{
        @Override
        protected void onCreate(Bundle savedInstancesState){
            super.onCreate(savedInstancesState);

            try{
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            startActivity(new Intent(this, DetectorActivity.class));
            finish();
        }
}
