package com.syz.eurekaserver.utils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


public class QRCodeUtil {
    //定义int类型的常量用于存放生成二维码时的类型
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final String CHARSET = "utf-8";
    private static final String FORMAT_NAME = "JPG";
    // 二维码尺寸
    private static final int QRCODE_SIZE = 300;
    // LOGO宽度
    private static final int WIDTH = 60;
    // LOGO高度
    private static final int HEIGHT = 60;

    /**
     * 加密：文字-->图片  将文本变成二维数组，
     * @param imagePath
     * @param format:文件格式及后缀名
     * @param content
     * @throws WriterException
     * @throws IOException
     */
    public static void encodeImage(String imagePath , String format , String content , int width , int height , String logo) throws WriterException, IOException{

        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType , Object>();
        //容错率:L(7%)<M(15%)<Q(25%)<H(35%);H容错率最高
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //编码格式
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //外边距
        hints.put(EncodeHintType.MARGIN, 1);

        /***
         * BarcodeFormat.QR_CODE:解析什么类型的文件：要解析的二维码的类型
         * content：解析文件的内容
         * width：生成二维码的宽
         * height:生成二维码的高
         * hints:涉及到加密用到的参数: 即 编码 和容错率
         */
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height , hints);
        //使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
        int matrixWidth = bitMatrix.getWidth();

        /**
         * 内存中的一张图片:是RenderedImage的子类，而RenderedImage是一个接口
         * 此时需要的图片是一个二维码-->需要一个boolean[][]数组存放二维码 --->Boolean数组是由BitMatrix产生的
         * BufferedImage.TYPE_INT_RGB : 表示生成图片的类型： 此处是RGB模式
         */
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //生成二维数组
        for(int x=0;x<matrixWidth;x++){
            for(int y=0 ; y<matrixWidth;y++){
                //二维坐标整个区域：画什么颜色
                img.setRGB(x, y, bitMatrix.get(x, y) ? BLACK : WHITE);
            }
        }

        //画log
        img = logoImg(img, logo);

        //将二维码图片转换成文件
        File file = new File(imagePath);
        //生成图片
        ImageIO.write(img, format, file);

    }

    /**
     * 解密，将生成的二维码转换成文字
     * @param file:二维码文件
     * @throws Exception
     */
    public static String decodeImage(File file) throws Exception{

        //首先判断文件是否存在
        if(!file.exists()){
            return "";
        }
        //将file转换成内存中的一张图片
        BufferedImage image = ImageIO.read(file);
        MultiFormatReader formatter = new MultiFormatReader();
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        //将图片的文字信息解析到result中
        Result result = formatter.decode(binaryBitmap);
        System.out.println(result.getText());
        return result.getText();
    }

    /**
     * 传logo和二维码，生成-->带logo的二维码
     * @param matrixImage :二维码
     * @param logo ： 中间的logo
     * @return
     * @throws IOException
     */
    public static BufferedImage logoImg(BufferedImage matrixImage ,String logo) throws IOException{
        //在二维码上画logo:产生一个二维码画板
        Graphics2D g2 = matrixImage.createGraphics();
        //画logo,将String类型的logo图片存放入内存中;即 string-->BufferedImage
        BufferedImage logoImage = ImageIO.read(new File(logo));
        //获取二维码的高和宽
        int height = matrixImage.getHeight();
        int width = matrixImage.getWidth();
        /**
         * 纯log图片
         * logoImage：内存中的图片
         * 在二维码的高和宽的2/5,2/5开始画log,logo占用整个二维码的高和宽的1/5,1/5
         */
        g2.drawImage(logoImage,width*2/5, height*2/5, width*1/5, height*1/5, null);

        /**
         * 画白色的外边框
         * 产生一个画白色圆角正方形的画笔
         * BasicStroke.CAP_ROUND:画笔的圆滑程度，此处设置为圆滑
         * BasicStroke.JOIN_ROUND：在边与边的连接点的圆滑程度，此处设置为圆滑
         */
        BasicStroke stroke = new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        //将画板和画笔关联起来
        g2.setStroke(stroke);

        /**
         * 画一个正方形
         * RoundRectangle2D是一个画长方形的类，folat是他的内部类
         */
        RoundRectangle2D.Float round = new RoundRectangle2D.Float(width*2/5, height*2/5, width*1/5, height*1/5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        //设置为画白色
        g2.setColor(Color.WHITE);
        g2.draw(round);

        //画灰色的内边框，原理与画白色边框相同
        BasicStroke stroke2 = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke2);

        RoundRectangle2D.Float round2 = new RoundRectangle2D.Float(width*2/5+2, height*2/5+2, width*1/5-4, height*1/5-4,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        //另一种设置灰色的方法：Color color = new Color(128,128,128);其中三个参数是 R G  B
        g2.setColor(Color.GRAY);
        g2.draw(round2);

        //释放内存
        g2.dispose();
        //刷新二维码
        matrixImage.flush();
        return matrixImage;
    }

    /**
     * 生成二维码(内嵌LOGO)
     *
     * @param content
     *            内容
     * @param imgPath
     *            LOGO地址
     * @param output
     *            输出流
     * @throws Exception
     */
    public static void encode(String content, String imgPath,
                              OutputStream output) throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, imgPath);
        ImageIO.write(image, FORMAT_NAME, output);
    }

    private static BufferedImage createImage(String content, String imgPath) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? BLACK : WHITE);
            }
        }
        if (imgPath == null || "".equals(imgPath)) {
            return image;
        }
        // 插入图片
        QRCodeUtil.insertImage(image, imgPath);
        return image;
    }

    /**
     * 插入LOGO
     *
     * @param source
     *            二维码图片
     * @param imgPath
     *            LOGO图片地址
     * @throws Exception
     */
    private static void insertImage(BufferedImage source, String imgPath) throws Exception {

        HttpURLConnection httpUrl = null;
        URL url = null;
        InputStream inputStream = null;
        try {
            url = new URL(imgPath);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            inputStream = httpUrl.getInputStream();
            Image src = ImageIO.read(inputStream);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
//        if (needCompress) { // 压缩LOGO
            if (width > WIDTH) {
                width = WIDTH;
            }
            if (height > HEIGHT) {
                height = HEIGHT;
            }
            Image image = src.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            src = image;

//        }
            // 插入LOGO
            Graphics2D graph = source.createGraphics();
            int x = (QRCODE_SIZE - width) / 2;
            int y = (QRCODE_SIZE - height) / 2;
            graph.drawImage(src, x, y, width, height, null);
            Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
            graph.setStroke(new BasicStroke(3f));
            graph.draw(shape);
            graph.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } finally {
            try {
                httpUrl.disconnect();
                if(inputStream != null){
                    inputStream.close();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private static BufferedInputStream getInputStream(String imgPath) {
        HttpURLConnection httpUrl = null;
        try {
            URL url = new URL(imgPath);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            return new BufferedInputStream(httpUrl.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        } finally {
            try {
                httpUrl.disconnect();
            } catch (NullPointerException e) {
                e.printStackTrace();;
            }
        }
        return null;
    }

    /**
     * 生成二维码(内嵌LOGO)
     *
     * @param content
     *            内容
     * @param imgPath
     *            LOGO地址
     * @param destPath
     *            存放目录
     * @throws Exception
     */
    public static String encode(String content, String imgPath, String destPath) throws Exception {
        BufferedImage image = QRCodeUtil.createImage(content, imgPath);
        mkdirs(destPath);
        String file = getKeyWord(content)+DateUtil.getCurrentDateAndMilliSecond()+".jpg";
        ImageIO.write(image, FORMAT_NAME, new File(destPath+File.separator+file));
        return file;
    }

    private static String getKeyWord(String content) {
        String[] split = content.split("\\.");
        if(split.length > 1){
            return split[1];
        }
        return null;
    }

    public static void saveToFile(BufferedInputStream inputStream) {
        FileOutputStream fos = null;
        int BUFFER_SIZE = 1024;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;
        try {
            fos = new FileOutputStream("E:\\qrcodeTest\\kingdee.jpg");
            while ((size = inputStream.read(buf)) != -1) {
                fos.write(buf, 0, size);
            }
            fos.flush();
        } catch (IOException e) {
        } catch (ClassCastException e) {
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
            } catch (NullPointerException e) {
            }
        }
    }

    /**
     * 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
     * @author lanyuan
     * Email: mmm333zzz520@163.com
     * @date 2013-12-11 上午10:16:36
     * @param destPath 存放目录
     */
    public static void mkdirs(String destPath) {
        File file =new File(destPath);
        //当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static void main(String[] args) throws Exception {
//        String url = "http://www.baidu.com";  //这里设置自定义网站url
//        String logoPath = "E:\\qrcodeTest\\logo.jpg";
//        String destPath = "E:\\qrcodeTest";
//        QRCodeUtil.encode(url, logoPath, destPath);
        QRCodeUtil.saveToFile("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1596190690982&di=4b8edeec77fe699b086fec3505919e66&imgtype=0&src=http%3A%2F%2Fimg3.doubanio.com%2Fview%2Fnote%2Fl%2Fpublic%2Fp46003132.jpg");
    }

    public static void saveToFile(String destUrl) {
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        HttpURLConnection httpUrl = null;
        URL url = null;
        int BUFFER_SIZE = 1024;
        byte[] buf = new byte[BUFFER_SIZE];
        int size = 0;
        try {
            url = new URL(destUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());
            fos = new FileOutputStream("E:\\qrcodeTest\\kingdee.jpg");
            while ((size = bis.read(buf)) != -1) {
                fos.write(buf, 0, size);
            }
            fos.flush();
        } catch (IOException e) {
        } catch (ClassCastException e) {
        } finally {
            try {
                fos.close();
                bis.close();
                httpUrl.disconnect();
            } catch (IOException e) {
            } catch (NullPointerException e) {
            }
        }
    }
}
