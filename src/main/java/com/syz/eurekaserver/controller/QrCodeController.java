package com.syz.eurekaserver.controller;

import com.syz.eurekaserver.utils.QRCodeUtil;
import com.syz.eurekaserver.vo.RequestQrCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping(value = "qrcode")
@Slf4j
public class QrCodeController {

    @Value("${dest.path}")
    private String destPath;

    @Value("${dest.url}")
    private String destUrl;

    //http://localhost:8001/qrcode/getQrcode?url=http://baidu.com&logo=E:\qrcodeTest\logo.jpg
    @PostMapping(value = "getQrCode")
    public String postQrCode(@RequestParam("url") String url,
                             @RequestParam(value = "logo",required = false) String logo){
        try{
            return destUrl + File.separator+ QRCodeUtil.encode(url,logo,destPath);
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @PostMapping(value = "getQrCode1")
    public String postQrCode1(@RequestBody RequestQrCodeVO vo){
        try{
            return destUrl + File.separator+ QRCodeUtil.encode(vo.getUrl(),vo.getLogo(),destPath);
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping(value = "getQrCode")
    public String getQrCode(){
        try{
            return "图片目录：" + destPath + ",目标url:"+destUrl ;
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }
}
