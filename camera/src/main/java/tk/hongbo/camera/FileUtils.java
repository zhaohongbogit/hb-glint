package tk.hongbo.camera;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    /**
     * 保存字节码到本地文件
     *
     * @param imageBytes
     */
    public static void saveFile(byte[] imageBytes, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e("FileUtils", "Failure, Create new file error", e);
                return;
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(imageBytes);
            fos.flush();
        } catch (FileNotFoundException e) {
            Log.e("FileUtils", "Failure, Not file", e);
        } catch (IOException e) {
            Log.e("FileUtils", "Failure, Write file error", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 保存bitmap到本地sdcard
     *
     * @param bitmap
     * @param filePath
     */
    public static void saveFile(Bitmap bitmap, String filePath) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        saveFile(baos.toByteArray(), filePath);
    }

    /**
     * 保存数据流为本地图片
     *
     * @param imageBytes
     */
    public void saveFile(final byte[] imageBytes) {
        if (imageBytes != null) {
//            FileUtils.saveFile(imageBytes, getExternalFilesDir("camera").getAbsolutePath());
        }
    }
}
