package cn.wandersnail.http.upload;

import java.io.InputStream;

/**
 * date: 2020/3/16 21:51
 * author: zengfansheng
 */
public class FileInfo {    
    private String name;   
    private InputStream inputStream;

    /**
     * 文件名
     */
    public String getName() {
        return name;
    }

    /**
     * 文件名
     */
    public void setName(String name) {
        this.name = name;
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
