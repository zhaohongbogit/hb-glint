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
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            pwm = service.openPwm(PWM_BUS);
            pwm.setPwmFrequencyHz(120); //周期120Hz(有效期8.33ms),责任(时间25%,每周期2.08ms)
            pwm.setPwmDutyCycle(50);
            pwm.setEnabled(true);
        } catch (IOException e) {
            Log.e(TAG, "Error setting the angle", e);
        }
        handler.postDelayed(runnable, 2000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setupServo();
            handler.postDelayed(runnable, 2000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyServo();
    }

    private void setupServo() {
        try {
            sum++;
            if (sum > 100) {
                sum = 0;
            }
            if (pwm != null) {
                Log.d(TAG, "Value sum:" + sum);
//                double cycle = 100 * sum / hz;
//                Log.d(TAG, "This is pwmDutyCycle value :" + cycle);
                pwm.setPwmDutyCycle(sum);
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
