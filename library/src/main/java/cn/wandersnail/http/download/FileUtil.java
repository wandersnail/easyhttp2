package cn.wandersnail.http.download;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import cn.wandersnail.http.util.HttpUtils;

/**
 * Created by 曾繁盛 on 2024/12/29 10:53
 */
class FileUtil {
    static void copyFile(@NonNull File src, @NonNull File target) {
        BufferedInputStream fis = null;
        BufferedOutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(src));
            fos = new BufferedOutputStream(new FileOutputStream(target));
            byte[] buffer = new byte[40960];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            target.delete();
        } finally {
            HttpUtils.closeQuietly(fis, fos);
        }
    }
}
