package tk.hongbo.cameraapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import tk.hongbo.cameraapp.stream.Connection;
import tk.hongbo.cameraapp.stream.MListener;

import static android.os.Environment.DIRECTORY_DCIM;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;

    public static String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM) + "/123.jpg";
    public static Handler handler;

    private static final String IP = "192.168.128.233";
    private static final int PORT = 8086;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bitmap bitmap = (Bitmap) msg.obj;
                imageView.setImageBitmap(bitmap);
            }
        };

        final Connection connection = new Connection(new MListener() {
            @Override
            public void ChangeDirection() {
            }

            @Override
            public void setImageView() {
            }
        });
        connection.getConnect(IP, PORT, MainActivity.this);
    }
}
