package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

/**
 * date: 2020/3/16 21:51
 * author: zengfansheng
 */
public class FileInfo {    
    private String formDataName = "file";
    private String filename;   
    private InputStream inputStream;
    
    public FileInfo(@NonNull String filename, @NonNull InputStream inputStream, @NonNull String formDataName) {
        this.filename = filename;
        this.inputStream = inputStream;
        this.formDataName = formDataName;
    }
    
    public FileInfo(@NonNull File file) throws FileNotFoundException {
        this(file.getName(), new FileInputStream(file));
    }
    
    public FileInfo(@NonNull String filename, @NonNull InputStream inputStream) {
        this.filename = filename;
        this.inputStream = inputStream;
    }
    
    /**
     * 表单数据的名称，例如：file
     */
    @NonNull
    public String getFormDataName() {
        return formDataName;
    }

    /**
     * 表单数据的名称，例如：file
     */
    public void setFormDataName(@NonNull String formDataName) {
        Objects.requireNonNull(formDataName);
        this.formDataName = formDataName;
    }

    /**
     * 文件名
     */
    @NonNull
    public String getFilename() {
        return filename;
    }

    /**
     * 文件名
     */
    public void setFilename(@NonNull String filename) {
        Objects.requireNonNull(filename);
        this.filename = filename;
    }

    /**
     * 文件输入流
     */
    @NonNull
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 文件输入流
     */
    public void setInputStream(@NonNull InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        this.inputStream = inputStream;
    }
}
