package cn.wandersnail.http.upload;

import java.io.InputStream;

/**
 * date: 2020/3/16 21:51
 * author: zengfansheng
 */
public class FileInfo {    
    private String fromDataName;
    private String filename;   
    private InputStream inputStream;

    /**
     * 表单数据的名称，例如：file
     */
    public String getFromDataName() {
        return fromDataName;
    }

    /**
     * 表单数据的名称，例如：file
     */
    public void setFromDataName(String fromDataName) {
        this.fromDataName = fromDataName;
    }

    /**
     * 文件名
     */
    public String getFilename() {
        return filename;
    }

    /**
     * 文件名
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * 文件输入流
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 文件输入流
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
