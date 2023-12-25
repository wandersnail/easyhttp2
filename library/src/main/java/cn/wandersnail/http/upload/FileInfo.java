package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

import cn.wandersnail.http.exception.MissingArgsException;
import cn.wandersnail.http.util.HttpUtils;
import okhttp3.MediaType;

/**
 * date: 2020/3/16 21:51
 * author: zengfansheng
 */
public class FileInfo {    
    private String formDataName;
    private String filename;   
    private InputStream inputStream;
    private File file;
    private MediaType mediaType;

    private FileInfo(String formDataName, String filename, InputStream inputStream, File file, MediaType mediaType) {
        this.formDataName = formDataName;
        this.filename = filename;
        this.inputStream = inputStream;
        this.file = file;
        this.mediaType = mediaType;
    }

    /**
     * 表单数据的名称，例如：file
     */
    @NonNull
    public String getFormDataName() {
        return formDataName;
    }

    @NonNull
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * 文件名
     */
    @Nullable
    public String getFilename() {
        return filename;
    }

    /**
     * 文件输入流
     */
    @Nullable
    public InputStream getInputStream() {
        return inputStream;
    }

    @Nullable
    public File getFile() {
        return file;
    }

    public static class Builder {
        private File file;
        private InputStream inputStream;
        private String formDataName;
        private String filename;
        private MediaType mediaType;

        /**
         * 此方法和{@link #setInputStream(InputStream)}二选一，优先使用此方法
         *
         * @param file 文件
         */
        public Builder setFile(@NonNull File file) {
            this.file = file;
            return this;
        }

        /**
         * 此方法和{@link #setFile(File)}二选一，优先使用{@link #setFile(File)}
         *
         * @param inputStream 文件输入流
         */
        public Builder setInputStream(@NonNull InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        /**
         * 如不设置会使用默认值“file”
         *
         * @param formDataName 表单数据的名称，例如：file
         */
        public Builder setFormDataName(@Nullable String formDataName) {
            this.formDataName = formDataName;
            return this;
        }

        /**
         * 如果不设置文件名，但设置了{@link #setFile(File)}，则自动获取文件名
         *
         * @param filename 文件名
         */
        public Builder setFilename(@Nullable String filename) {
            this.filename = filename;
            return this;
        }

        /**
         * @param mediaType 文件类型
         */
        public Builder setMediaType(@Nullable MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public FileInfo build() throws MissingArgsException {
            if (file == null && inputStream == null) {
                throw new MissingArgsException("file和inputStream不能同时为空");
            }
            String fn = filename;
            if (filename == null && file != null) {
                fn = file.getName();
            }
            MediaType mt = mediaType;
            if (mt == null && fn != null) {
                String type = HttpUtils.getMimeType(fn);
                if (type != null) {
                    mt = MediaType.parse(type);
                }
            }
            return new FileInfo(
                    formDataName == null ? "file" : formDataName,
                    fn,
                    inputStream,
                    file,
                    mt == null ? MediaType.parse("application/octet-stream") : mt
            );
        }
    }
}
