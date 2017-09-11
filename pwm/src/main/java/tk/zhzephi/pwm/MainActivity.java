package tk.zhzephi.pwm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String PWM_BUS = "PWM0";
    private Pwm pwm;
    private double sum = 1;
    private boolean mIsPulseIncreasing = true;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            pwm = service.openPwm(PWM_BUS);
            pwm.setPwmFrequencyHz(50);
            pwm.setPwmDutyCycle(1);
            pwm.setEnabled(true);
        } catch (IOException e) {
            Log.e(TAG, "Error setting the angle", e);
        }
        handler.postDelayed(runnable, 300);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setupServo();
            handler.postDelayed(runnable, 300);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyServo();
    }

    private void setupServo() {
        try {
            Log.d(TAG, "===GO====sum:" + sum);
            if (mIsPulseIncreasing) {
                sum += 0.2;
            } else {
                sum -= 0.2;
            }
            if (sum > 2) {
                sum = 2;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            } else if (sum < 1) {
                sum = 1;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            }
            if (pwm != null) {
                pwm.setPwmDutyCycle(100 * sum / 20);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating Servo", e);
        }
    }

    private void destroyServo() {
        if (pwm != null) {
            try {
                pwm.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Servo");
            } finally {
                pwm = null;
            }
        }
    }
}
