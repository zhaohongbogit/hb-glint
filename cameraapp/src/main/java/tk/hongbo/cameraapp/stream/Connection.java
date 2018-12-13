package tk.hongbo.cameraapp.stream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import tk.hongbo.cameraapp.MainActivity;

public class Connection {

    private static final String TAG = Connection.class.getSimpleName();

    public static boolean CONNECTION_STATE = false;
    private Socket socket = null;
    public InputStream ins = null;
    private OutputStream outputStream;

    private MListener listener;

    public Connection(MListener listener) {
        this.listener = listener;
    }

    public void getConnect(final String IP, final int PORT, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket == null) {
                        socket = new Socket(IP, PORT);
                        if (socket.isConnected()) {
                            CONNECTION_STATE = true;
                            Log.i(TAG, "CONNECTION  SOCKET" + "  " + true);
                            ins = socket.getInputStream();
                            getimage();
                        } else {
                            CONNECTION_STATE = false;
                            Log.i(TAG, "CONNECTION  SOCKET" + "  " + false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getimage() {
        int len = 0;
        Log.v(TAG, "ins");
        try {
            Log.v(TAG, "LL");
            if (socket.isConnected()) {
                while (true) {
                    if (ins.read() == 0xA0) {
                        Log.v(TAG, "LL22");
                        byte[] src = new byte[4];
                        len = ins.read(src);
                        Log.v(TAG, src + "d");
                        len = bytesToInt(src, 0);
                        Log.v(TAG, len + "");
                    }
                    byte[] srcData = new byte[len];
                    int cclen = 0;
                    while (cclen < len) {
                        int readlen = ins.read(srcData, cclen, len - cclen);
                        cclen += readlen;
                        Log.v(TAG, readlen + ":LEN");
                    }
                    Bitmap src = BitmapFactory.decodeByteArray(srcData, 0, len);
                    Message msg = new Message();
                    msg.obj = src;
                    MainActivity.handler.sendMessage(msg);
                    Log.v(TAG, "LL33");
                }
            } else {
                Log.e(TAG, "连接中断");
            }

        } catch (Exception ex) {
            Log.v(TAG, "LL444");
            ex.printStackTrace();
            Log.v(TAG, "LL55" + ex.getMessage());
        }
    }

    public int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    private void getMessage() throws IOException {
        //本方法本身已经在由上一方法开启的子线程中运行
        Log.i(TAG, "get message……");
        DataInputStream dataInput = new DataInputStream(socket.getInputStream());
        String file = MainActivity.path;
        int size = dataInput.readInt();
        byte[] data = new byte[size];
        int len = 0;
        while (len < size) {
            len += dataInput.read(data, len, size - len);
        }
        FileOutputStream outPut = new FileOutputStream(file);
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outPut);
        Log.i("123", "接收完毕");
        MainActivity.handler.sendEmptyMessage(1);

    }

    public void sendMessage(final String sendData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream = socket.getOutputStream();
                    //数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                    outputStream.write(sendData.getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void disconnect() {
        try {
            socket.close();
            if (socket.isConnected()) {
                Log.i(TAG, "断开失败！");
            } else {
                CONNECTION_STATE = false;
                Log.i(TAG, "断开成功！");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
