package tk.hongbo.microphone

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager

private val TAG = MainActivity::class.java.simpleName
private val GPIO_OUT = "GPIO6_IO13";

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()
    }

    fun setup() {
        val manage = PeripheralManager.getInstance()
        if (manage != null) {
            val gpio = manage.openGpio(GPIO_OUT)
            gpio.setDirection(Gpio.DIRECTION_IN)
            gpio.setActiveType(Gpio.ACTIVE_LOW)
            gpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
            gpio.registerGpioCallback(callback)
        }
    }

    val callback = object : GpioCallback {
        override fun onGpioEdge(p0: Gpio?): Boolean {
            Log.d(TAG, "Gpio：" + p0.toString())
            return true
        }
    }
}
