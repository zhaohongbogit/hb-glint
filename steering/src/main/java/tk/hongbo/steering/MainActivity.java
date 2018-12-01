package tk.hongbo.steering;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmservo.Servo;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String PWM_BUS = "PWM0";
    private Servo mServo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupServo();
        new Thread(() -> {
            while (true) {
                try {
//                    Log.d(TAG, "MaximumAngle:" + mServo.getMaximumAngle()
//                            + ",MaximumPulseDuration:" + mServo.getMaximumPulseDuration()
//                            + ",MinimumAngle:" + mServo.getMinimumAngle()
//                            + ",MinimumPulseDuration:" + mServo.getMinimumPulseDuration());
                    Log.d(TAG, "Now angle:" + mServo.getAngle());
                    if (mServo.getAngle() == 0) {
                        mServo.setAngle(0);
                        sleep(300);
                        mServo.setAngle(360);
                    } else {
                        mServo.setAngle(360);
                        sleep(300);
                        mServo.setAngle(0);
                    }
                    sleep(2000);
                } catch (IOException e) {
                    Log.e(TAG, "Error setting the angle", e);
                }
            }
        }).start();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error sleep", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyServo();
    }

    private void setupServo() {
        try {
            mServo = new Servo(PWM_BUS);
            mServo.setAngleRange(0f, 360f);
            mServo.setEnabled(true);
        } catch (IOException e) {
            Log.e(TAG, "Error creating Servo", e);
        }
    }

    private void destroyServo() {
        if (mServo != null) {
            try {
                mServo.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo");
            } finally {
                mServo = null;
            }
        }
    }
}
