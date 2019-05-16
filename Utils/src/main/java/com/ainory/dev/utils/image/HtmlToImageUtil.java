package com.ainory.dev.utils.image;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by ainory on 2018. 4. 5..
 */
public class HtmlToImageUtil {

    private static final Logger logger = LoggerFactory.getLogger(HtmlToImageUtil.class);

    private static final int DEFULAT_WIDTH = 1024;
    private static final int DEFULAT_HEIGHT = 1024;
    private static final float DEFAULT_QUALITY = 1f;

    public static final String FILE_POSTFIX_JPG = ".jpg";
    public static final String FILE_POSTFIX_PNG = ".png";

    /**
     * html url convert jpg
     *
     * @param url
     * @param jpgFilePath
     * @return
     */
    public static boolean htmlUrlConvertJpg(String url, String jpgFilePath){
        try{
            return htmlConvertJpg(createRanderer(url, DEFULAT_WIDTH, DEFULAT_HEIGHT), jpgFilePath, DEFAULT_QUALITY);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html url convert jpg
     *
     * @param url
     * @param jpgFilePath
     * @param widht
     * @return
     */
    public static boolean htmlUrlConvertJpg(String url, String jpgFilePath, int widht){
        try{
            return htmlConvertJpg(createRanderer(url, widht, DEFULAT_HEIGHT), jpgFilePath, DEFAULT_QUALITY);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html url convert jpg
     *
     * @param url
     * @param jpgFilePath
     * @param widht
     * @param height
     * @return
     */
    public static boolean htmlUrlConvertJpg(String url, String jpgFilePath, int widht, int height){
        try{
            return htmlConvertJpg(createRanderer(url, widht, height), jpgFilePath, DEFAULT_QUALITY);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html url convert jpg
     *
     * @param url
     * @param jpgFilePath
     * @param widht
     * @param height
     * @param quality   quality rete(0.1 ~ 1)
     * @return
     */
    public static boolean htmlUrlConvertJpg(String url, String jpgFilePath, int widht, int height, float quality){
        try{
            return htmlConvertJpg(createRanderer(url, widht, height), jpgFilePath, quality);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html file convert jpg
     *
     * @param htmlFilePath
     * @param jpgFilePath
     * @return
     */
    public static boolean htmlFileConvertJpg(String htmlFilePath, String jpgFilePath) throws Exception{
            return htmlConvertJpg(createRanderer(new File(htmlFilePath), DEFULAT_WIDTH, DEFULAT_HEIGHT), jpgFilePath, DEFAULT_QUALITY);

    }

    /**
     * html file convert jpg
     *
     * @param htmlFilePath
     * @param jpgFilePath
     * @param widht
     * @return
     */
    public static boolean htmlFileConvertJpg(String htmlFilePath, String jpgFilePath, int widht){
        try{
            return htmlConvertJpg(createRanderer(new File(htmlFilePath), widht, DEFULAT_HEIGHT), jpgFilePath, DEFAULT_QUALITY);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html file convert jpg
     *
     * @param htmlFilePath
     * @param jpgFilePath
     * @param widht
     * @param height
     * @return
     */
    public static boolean htmlFileConvertJpg(String htmlFilePath, String jpgFilePath, int widht, int height){
        try{
            return htmlConvertJpg(createRanderer(new File(htmlFilePath), widht, height), jpgFilePath, DEFAULT_QUALITY);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html file convert jpg
     *
     * @param htmlFilePath
     * @param jpgFilePath
     * @param widht
     * @param height
     * @param quality  quality rete(0.1 ~ 1)
     * @return
     */
    public static boolean htmlFileConvertJpg(String htmlFilePath, String jpgFilePath, int widht, int height, float quality){
        try{
            return htmlConvertJpg(createRanderer(new File(htmlFilePath), widht, height), jpgFilePath, quality);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html url convert png
     *
     * @param url
     * @param pngFilePath
     * @return
     */
    public static boolean htmlUrlConvertPng(String url, String pngFilePath){
        try{
            return htmlConvertJpg(createRanderer(url, DEFULAT_WIDTH, DEFULAT_HEIGHT), pngFilePath, DEFAULT_QUALITY);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html url convert png
     *
     * @param url
     * @param pngFilePath
     * @param widht
     * @return
     */
    public static boolean htmlUrlConvertPng(String url, String pngFilePath, int widht){
        try{
            return htmlConvertPng(createRanderer(url, widht, DEFULAT_HEIGHT), pngFilePath);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html url convert png
     *
     * @param url
     * @param pngFilePath
     * @param widht
     * @param height
     * @return
     */
    public static boolean htmlUrlConvertPng(String url, String pngFilePath, int widht, int height){
        try{
            return htmlConvertPng(createRanderer(url, widht, height), pngFilePath);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html file convert png
     *
     * @param htmlFilePath
     * @param pngFilePath
     * @return
     */
    public static boolean htmlFileConvertPng(String htmlFilePath, String pngFilePath){
        try{
            return htmlConvertPng(createRanderer(new File(htmlFilePath), DEFULAT_WIDTH, DEFULAT_HEIGHT), pngFilePath);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html file convert png
     *
     * @param htmlFilePath
     * @param pngFilePath
     * @param widht
     * @return
     */
    public static boolean htmlFileConvertPng(String htmlFilePath, String pngFilePath, int widht){
        try{
            return htmlConvertPng(createRanderer(new File(htmlFilePath), widht, DEFULAT_HEIGHT), pngFilePath);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html file convert png
     *
     * @param htmlFilePath
     * @param pngFilePath
     * @param widht
     * @param height
     * @return
     */
    public static boolean htmlFileConvertPng(String htmlFilePath, String pngFilePath, int widht, int height){
        try{
            return htmlConvertPng(createRanderer(new File(htmlFilePath), widht, height), pngFilePath);
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * create renderer (file base)
     *
     * @param file
     * @param widht
     * @param height
     * @return Java2DRenderer
     * @throws Exception
     */
    private static Java2DRenderer createRanderer(File file, int widht, int height) throws Exception{
        return new Java2DRenderer(file, widht, height);
    }

    /**
     * create renderer (url base)
     *
     * @param url
     * @param widht
     * @param height
     * @return Java2DRenderer
     */
    private static Java2DRenderer createRanderer(String url, int widht, int height) {
        return new Java2DRenderer(url, widht, height);
    }

    /**
     * html convert jpg
     *
     * @param java2DRenderer
     * @param jpgFilePath
     * @param quality   quality rete(0.1 ~ 1)
     * @return
     */
    private static boolean htmlConvertJpg(Java2DRenderer java2DRenderer, String jpgFilePath, float quality){

        try{

            BufferedImage image = java2DRenderer.getImage();

            FSImageWriter fsImageWriter = FSImageWriter.newJpegWriter(quality);
            fsImageWriter.write(image, jpgFilePath);

            return true;
        }catch (Exception e){
            logger.error(jpgFilePath +"\n"+ ExceptionUtils.getMessage(e));
            return false;
        }
    }

    /**
     * html convert png
     *
     * @param java2DRenderer
     * @param pngFilePath
     * @return
     */
    private static boolean htmlConvertPng(Java2DRenderer java2DRenderer, String pngFilePath){

        try{

            BufferedImage image = java2DRenderer.getImage();

            FSImageWriter fsImageWriter = new FSImageWriter();
            fsImageWriter.write(image, pngFilePath);

            return true;
        }catch (Exception e){
            logger.error(pngFilePath +"\n"+ ExceptionUtils.getMessage(e));
            return false;
        }
    }

    public static void main(String[] args) throws Exception {


        try{
            String source = "/Users/ainory/Desktop/NammtAhuModel1_201804250200_mms.html";
            htmlFileConvertJpg(source, "/Users/ainory/Desktop/test.jpg");
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
        }
    }
}