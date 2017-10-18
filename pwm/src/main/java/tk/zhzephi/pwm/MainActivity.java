package tk.zhzephi.pwm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * 本次实验使用的舵机为SG90（辉盛），需要注意的有一下几点
 *  1. SG90脉冲周期为20ms，也就是Hz=50
 *  2. SG90支持的旋转角度为180度，脉宽0.5ms-2.5ms对应的角度-90度到+90度，对应的占空比为2.5%-12.5%，所以
 *  7.5%对应了舵机在中立点（0度）
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 1;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 2;
    private static final double PULSE_PERIOD_MS = 20;  // Frequency of 50Hz (1000/20)

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 0.2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 1000;

    private static final String PWM_BUS = "PWM0";
    private Pwm pwm;
    private double mActivePulseDuration;
    private boolean mIsPulseIncreasing = true;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            pwm = service.openPwm(PWM_BUS);
            pwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS); //周期50Hz(有效期20ms),责任(时间25%,每周期2.08ms)
            pwm.setPwmDutyCycle(mActivePulseDuration);
            pwm.setEnabled(true);
        } catch (IOException e) {
            Log.e(TAG, "Error setting the angle", e);
        }
        handler.post(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setupServo();
            // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
            handler.postDelayed(runnable, INTERVAL_BETWEEN_STEPS_MS);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyServo();
    }

    private void setupServo() {
        if (pwm == null) {
            return;
        }

        // Change the duration of the active PWM pulse, but keep it between the minimum and
        // maximum limits.
        // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
        // will bounce from MIN to MAX.
        if (mIsPulseIncreasing) {
            mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
        } else {
            mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
        }

        // Bounce mActivePulseDuration back from the limits
        if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
            mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
            mIsPulseIncreasing = !mIsPulseIncreasing;
        } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            mIsPulseIncreasing = !mIsPulseIncreasing;
        }
        Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

        try {
            // Duty cycle is the percentage of active (on) pulse over the total duration of the
            // PWM pulse
            pwm.setPwmDutyCycle(100 * mActivePulseDuration / PULSE_PERIOD_MS);
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
