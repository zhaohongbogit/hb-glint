package tk.hongbo.camera.stream;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSubThread {

    private Socket connection = null;
    private ServerSocket serverSocket = null;
    private InputStream inputStream = null;
    public static String rec_data;
    private OutputStream out;

    private RecListener recListener;

    public ServerSubThread(RecListener recListener) {
        this.recListener = recListener;
    }

    public void sendMessage(final Bitmap bitmap) {
        new Thread(() -> {
            try {
                if (connection == null) {
                    return;
                }
                byte[] outdata = transImage(bitmap, 640, 480);
                int datalen = outdata.length;
                out = connection.getOutputStream();
                out.write((byte) 0xA0);
                out.write(intTOBytes(datalen));
                out.write(outdata, 0, datalen);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void creatServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(8086);
                connection = serverSocket.accept();
                while (true) {
                    try {
                        inputStream = connection.getInputStream();
                        Reader reader = new InputStreamReader(inputStream);
                        BufferedReader br = new BufferedReader(reader);
                        rec_data = null;
                        rec_data = br.readLine();//不输入数据时一直停留等待,而不是返回null,是一个阻塞函数.在数据流异常或断开时才会返回null
                        if (rec_data != null) {
                            recListener.getMessage();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 数据转换，将bitmap转换为byte
     */
    private byte[] transImage(Bitmap bitmap, int width, int height) {
        try {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            //缩放图片的尺寸
            float scaleWidth = (float) width / bitmapWidth;
            float scaleHeight = (float) height / bitmapHeight;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            //产生缩放后的Bitmap对象
            Bitmap resizeBitemp = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizeBitemp.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            outputStream.close();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (!resizeBitemp.isRecycled()) {
                resizeBitemp.recycle();
            }
            return byteArray;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 将int 数值转换为4个字节
     */
    private byte[] intTOBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

}
