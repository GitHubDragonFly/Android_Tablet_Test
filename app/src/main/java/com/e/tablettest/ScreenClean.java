package com.e.tablettest;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.TextView;

public class ScreenClean extends AppCompatActivity {
    TextView tvSC;
    String display;
    CountDownTimer cdTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.screen_clean);

        tvSC = findViewById(R.id.tvScreenClean);

        cdTimer = new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                display = "Screen Clean: " + millisUntilFinished / 1000;
                tvSC.setText(display);
            }

            public void onFinish() {
                display = "Done!";
                tvSC.setText(display);

                CountDownFinished();
            }
        }.start();
    }

    private void CountDownFinished() {
        if (display.equals("Done!"))
            this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        cdTimer.cancel();
    }
}
