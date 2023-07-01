package machine.microspin.com.flyerConsole;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

@SuppressWarnings("ALL")
public class SplashScreen extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        int SPLASH_TIME_OUT = 2500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toNext();
            }
        }, SPLASH_TIME_OUT);
    }

    /**
     * Navigate to Next Screen
     */
    private void toNext(){
        SplashScreen.this.finish();
        Intent i = new Intent(SplashScreen.this, DeviceListActivity.class);
        startActivity(i);
    }
}

