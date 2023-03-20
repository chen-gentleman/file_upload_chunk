package com.chen.fileserver.controller;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author @Chenxc
 * @date 2022年10月28日 11:12
 */
@Controller
public class UploadController {
    private static final List<String> MD5 = new ArrayList<>();

    @GetMapping("/upload")
    public String upload(){
        return "upload";
    }

    @PostMapping("/fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("upload") MultipartFile file, HttpServletRequest request){
        //当前块
        Integer schunk = null;
        //总的块数
        Integer schunks = null;
        //文件名
        String fileName = null;
        //临时文件保存路径
        String uploadTempPath = "C:\\Users\\Administrator\\Desktop\\upload\\Test\\temp";
        //结果文件保存路径
        String uploadPath = "C:\\Users\\Administrator\\Desktop\\upload\\Test";
        //输出流
        BufferedOutputStream bos = null;
        try{
            if(file != null && request.getParameter("chunks") == null){
                fileName = file.getOriginalFilename();
                File resultFile = new File(uploadPath,fileName);
                file.transferTo(resultFile);
                return "上传成功："+ fileName;
            }

            schunk = Integer.parseInt(request.getParameter("chunk"));
            schunks = Integer.parseInt(request.getParameter("chunks"));
            fileName = request.getParameter("name");

            if(file != null){
                if(fileName != null){
                    if(schunk != null){
                        String tempFileName = schunk+"_"+fileName;
                        //写入临时文件
                        File chunkFile = new File(uploadTempPath,tempFileName);
                        if(!chunkFile.exists()){
                            file.transferTo(chunkFile);
                        }

                    }
                }
            }
//            DiskFileItemFactory factory = new DiskFileItemFactory();
//            factory.setSizeThreshold(1024);//缓冲区大小
//            factory.setRepository(new File(uploadTempPath));
//            //使用工具解析request
//            ServletFileUpload upload = new ServletFileUpload(factory);
//            //设置参数：
//            //单个文件大小 5G
//            upload.setFileSizeMax(5L * 1024L * 1024L * 1024L );
//            //最大上传 10G
//            upload.setSizeMax(5L * 1024L * 1024L * 1024L);
//            //总的文件大小
//            List<FileItem> fileItems = upload.parseRequest(request);
//            //遍历
//            for (FileItem item : fileItems) {
//                if(item.isFormField()){//判断是form表单数据
//                    if("chunk".equals(item.getFieldName())){
//                        schunk = Integer.parseInt(item.getString());
//                    }
//                    if("chunks".equals(item.getFieldName())){
//                        schunks = Integer.parseInt(item.getString());
//                    }
//                    if("name".equals(item.getFieldName())){
//                        fileName =item.getString();
//                    }
//                }
//            }
//
//            for (FileItem item : fileItems) {
//                if(!item.isFormField()){//判断不是form表单数据 就是文件流
//                   if(fileName != null){
//                       if(schunk != null){
//                           String tempFileName = schunk+"_"+fileName;
//                           //写入临时文件
//                           File chunkFile = new File(uploadTempPath,tempFileName);
//                           if(!chunkFile.exists()){
//                               item.write(chunkFile);
//                           }
//                       }
//                   }
//                }
//            }
        //文件合并
         if(schunk != null && schunk.intValue() == schunks.intValue() - 1){
             File resultFile = new File(uploadPath,fileName);
             bos = new BufferedOutputStream(new FileOutputStream(resultFile));
             for (int i = 0; i < schunks; i++) {
                 File tempFile = new File(uploadTempPath,i+"_"+fileName);
                 while (!tempFile.exists()){
                     TimeUnit.MILLISECONDS.sleep(100);//因为是并发，不确定哪一块先上传玩
                 }
                 byte[] bytes = FileUtils.readFileToByteArray(tempFile);
                 bos.write(bytes);
                 bos.flush();
                 tempFile.delete();//删除临时文件
             }
             bos.flush();
         }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != bos){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "上传成功:" + fileName;
    }


    @PostMapping("/checkFileExist")
    @ResponseBody
    public String checkFileExist(@RequestParam("md5") String md5){
        if(MD5.contains(md5)){
            return "1";
        }
        MD5.add(md5);
        return "0";
    }

}
