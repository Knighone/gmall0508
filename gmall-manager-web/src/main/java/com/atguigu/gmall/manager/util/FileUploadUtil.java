package com.atguigu.gmall.manager.util;

import com.atguigu.gmall.pojo.Constants;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FileUploadUtil {
    public static String uploadImage(MultipartFile file) {
        String path = FileUploadUtil.class.getClassLoader().getResource("tracker.conf").getPath();
        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null ;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StorageClient storageClient = new StorageClient(connection, null);

        String[] gifs = new String[0];

        String originalFilename = file.getOriginalFilename();
        String[] split = originalFilename.split("\\.");
        String extName = split[split.length - 1];
        System.out.println(extName);
        try {
            gifs = storageClient.upload_appender_file(file.getBytes(), extName, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        String imgUrl =  Constants.APP_PATH;

        for (String gif : gifs) {
            imgUrl = imgUrl + "/" + gif;
        }
        return imgUrl;
    }
}
