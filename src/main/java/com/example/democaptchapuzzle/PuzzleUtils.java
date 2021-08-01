package com.example.democaptchapuzzle;


import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Random;

public class PuzzleUtils {
 
    private static int BOLD = 5;
    private static final String IMG_FILE_TYPE = "jpg";
    private static final String TEMP_IMG_FILE_TYPE = "png";
 
    /**
     * Generate base and puzzle pieces
     * @param templateFile
     * @param targetFile
     * @return
     * @throws Exception
     */
    public static Slide pictureTemplatesCut(File templateFile, File targetFile) throws Exception {    	
        
        // A template for puzzle pieces
        // get buffer of template image
        BufferedImage imageTemplate = ImageIO.read(templateFile);
        int templateWidth = imageTemplate.getWidth();
        int templateHeight = imageTemplate.getHeight();
 
        // The original puzzle
        // get buffer of target image
        BufferedImage oriImage = ImageIO.read(targetFile);
        int oriImageWidth = oriImage.getWidth();
        int oriImageHeight = oriImage.getHeight();
 
        //get random position of template in target image
        Random random = new Random();
        int widthRandom = random.nextInt(oriImageWidth - 2*templateWidth) + templateWidth;
        int heightRandom = random.nextInt(oriImageHeight - templateHeight);
 
        // create buffer same template. buffer to use transparent in target image
        BufferedImage newImage = new BufferedImage(templateWidth, templateHeight, imageTemplate.getType());

        // create graphics to draw in target image
        Graphics2D graphics = newImage.createGraphics();

        // Transparency
        newImage = graphics.getDeviceConfiguration().createCompatibleImage(templateWidth, templateHeight, Transparency.TRANSLUCENT);

        // Transparency in target image
        cutByTemplate(oriImage,imageTemplate,newImage,widthRandom,heightRandom);
 
        // draw small image same template
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(BOLD, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        graphics.drawImage(newImage, 0, 0, null);
        graphics.dispose();
 
        ByteArrayOutputStream newImageOs = new ByteArrayOutputStream();
        ImageIO.write(newImage, TEMP_IMG_FILE_TYPE, newImageOs);
        byte[] newImagebyte = newImageOs.toByteArray();
 
        ByteArrayOutputStream oriImagesOs = new ByteArrayOutputStream();
        ImageIO.write(oriImage, IMG_FILE_TYPE, oriImagesOs);
        byte[] oriImageByte = oriImagesOs.toByteArray();
 
        Slide resultSlide = new Slide();
        resultSlide.setSmallImage(Base64Utils.encodeToString(newImagebyte));
        resultSlide.setBigImage(Base64Utils.encodeToString(oriImageByte));
        resultSlide.setXWidth(widthRandom);
        resultSlide.setYHeight(heightRandom);        
        return resultSlide;
    }

 
    /**
     * @param oriImage  
     * @param templateImage 
     * @param newImage  
     * @param x         
     * @param y         
     * @throws Exception
     */
    private static void cutByTemplate(BufferedImage oriImage, BufferedImage templateImage,BufferedImage newImage, int x, int y){
    	
        int[][] martrix = new int[3][3];
        int[] values = new int[9];
 
        int xLength = templateImage.getWidth();
        int yLength = templateImage.getHeight();
        for (int i = 0; i < xLength; i++) {
            for (int j = 0; j < yLength; j++) {
                int rgb = templateImage.getRGB(i, j);
                if (rgb < 0) {
                    newImage.setRGB(i, j,oriImage.getRGB(x + i, y + j));
                    // Matting area Gaussian blur
                    readPixel(oriImage, x + i, y + j, values);
                    fillMatrix(martrix, values);
                    oriImage.setRGB(x + i, y + j, avgMatrix(martrix));
                }
 
                if(i == (xLength-1) || j == (yLength-1)){
                    continue;
                }
                int rightRgb = templateImage.getRGB(i + 1, j);
                int downRgb = templateImage.getRGB(i, j + 1);
                //, take the boundary point with and without pixels, and judge whether the point is a critical contour point. If so, the coordinate pixel is white
                if((rgb >= 0 && rightRgb < 0) || (rgb < 0 && rightRgb >= 0) || (rgb >= 0 && downRgb < 0) || (rgb < 0 && downRgb >= 0)){
                     newImage.setRGB(i, j, Color.yellow.getRGB());
                     oriImage.setRGB(x + i, y + j, Color.white.getRGB());
                }
            }
        }
    }
 
    private static void readPixel(BufferedImage img, int x, int y, int[] pixels) {
        int xStart = x - 1;
        int yStart = y - 1;
        int current = 0;
        for (int i = xStart; i < 3 + xStart; i++)
            for (int j = yStart; j < 3 + yStart; j++) {
                int tx = i;
                if (tx < 0) {
                    tx = -tx;
 
                } else if (tx >= img.getWidth()) {
                    tx = x;
                }
                int ty = j;
                if (ty < 0) {
                    ty = -ty;
                } else if (ty >= img.getHeight()) {
                    ty = y;
                }
                pixels[current++] = img.getRGB(tx, ty);
 
            }
    }
 
    private static void fillMatrix(int[][] matrix, int[] values) {
        int filled = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                x[j] = values[filled++];
            }
        }
    }
 
    private static int avgMatrix(int[][] matrix) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                if (j == 1) {
                    continue;
                }
                Color c = new Color(x[j]);
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        r  = r/8;
        g  = g/8;
        b  = b/8;
        int test = 20;
        System.out.println(r + " " + g + " " + b);
        if (r > test) {
            r = r - test;
        }

        if (g > test) {
            g = g - test;
        }

        if (b > test) {
            b = b - test;
        }
        return new Color( r, g, b).getRGB();
    }

}