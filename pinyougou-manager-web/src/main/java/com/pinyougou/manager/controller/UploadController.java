package com.pinyougou.manager.controller;

import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

/**
 * 文件上传控制类
 * @author lenovo
 *
 */
@RestController
public class UploadController {
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL; //文件服务器地址，在配置文件中application.properties
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		//1.获取文件的扩展名
		String originalFilename = file.getOriginalFilename(); //上传时的文件名
		//文件的扩展名
		String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
		
		try {
			//2.创建一个FastDFS客户端
			FastDFSClient fastDFSClient = new FastDFSClient(UploadController.class.getResource("/config/fdfs_client.conf").getPath());
			//3.执行上传处理
			String path = fastDFSClient.uploadFile(file.getBytes(),extName); //服务器返回一个 图片存储的地址
			//4.拼接ip地址 ，成为一个完整访问的url
			String url = FILE_SERVER_URL+path;
			return new Result(true,url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"上传失败！");
		}
		
	}
}
