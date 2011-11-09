/**
 * Copyright (c) www.bugull.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mongo.fs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ImageUploader extends Uploader{
    
    private final static Logger logger = Logger.getLogger(ImageUploader.class);
    
    public final static String DIMENSION = "dimension";
    
    public ImageUploader(File file, String originalName){
        super(file, originalName);
    }
    
    public ImageUploader(File file, String originalName, boolean rename){
        super(file, originalName, rename);
    }
    
    public ImageUploader(InputStream input, String originalName){
        super(input, originalName);
    }
    
    public ImageUploader(InputStream input, String originalName, boolean rename){
        super(input, originalName, rename);
    }
    
    public ImageUploader(byte[] data, String originalName){
        super(data, originalName);
    }
    
    public ImageUploader(byte[] data, String originalName, boolean rename){
        super(data, originalName, rename);
    }
    
    public void save(Watermark watermark){
        processFilename();
        if(watermark.getText() != null){
            pressText(watermark);
        }
        else if(watermark.getImagePath() != null){
            pressImage(watermark);
        }
        else{
            saveInputStream();
        }
    }
    
    private void pressText(Watermark watermark){
        BufferedImage originalImage = openImage(input);
        int originalWidth = originalImage.getWidth(null);
        int originalHeight = originalImage.getHeight(null);
        BufferedImage newImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(originalImage, 0, 0, originalWidth, originalHeight, null);
        g.setColor(watermark.getColor());
        g.setFont(new Font(watermark.getFontName(), watermark.getFontStyle(), watermark.getFontSize())); 
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, watermark.getAlpha())); 
        String text = watermark.getText();
        int len = text.length();
        int fontSize = watermark.getFontSize();
        switch(watermark.getAlign()){
            case Watermark.BOTTOM_RIGHT:
                g.drawString(text, originalWidth - (len * fontSize) - watermark.getRight(), originalHeight - fontSize - watermark.getBottom());
                break;
            case Watermark.CENTER:
                g.drawString(text, (originalWidth - (len * fontSize)) / 2, (originalHeight - fontSize) / 2);
                break;
            default:
                break;
        }
        g.dispose();
        saveImage(newImage);
    }
    
    private void pressImage(Watermark watermark){
        BufferedImage originalImage = openImage(input);
        BufferedImage watermarkImage = openImage(new File(watermark.getImagePath()));
        int originalWidth = originalImage.getWidth(null);
        int originalHeight = originalImage.getHeight(null);
        int watermarkWidth = watermarkImage.getWidth(null);
        int watermarkHeight = watermarkImage.getHeight(null);
        if (originalWidth < watermarkWidth || originalHeight < watermarkHeight) {
            saveInputStream();
            return;
        }
        BufferedImage newImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(originalImage, 0, 0, originalWidth, originalHeight, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, watermark.getAlpha())); 
        //position of the watermark
        switch(watermark.getAlign()){
            case Watermark.BOTTOM_RIGHT:
                g.drawImage(watermarkImage, originalWidth - watermarkWidth - watermark.getRight(), originalHeight - watermarkHeight - watermark.getBottom(), watermarkWidth, watermarkHeight, null);
                break;
            case Watermark.CENTER:
                g.drawImage(watermarkImage, (originalWidth - watermarkWidth) / 2, (originalHeight - watermarkHeight) / 2, watermarkWidth, watermarkHeight, null);
                break;
            default:
                break;
        }
        g.dispose();
        saveImage(newImage);
    }
    
    public void compress(String dimension, int maxWidth, int maxHeight) {
        setAttribute(DIMENSION, dimension);
        BufferedImage srcImage = openImage(getOriginalInputStream());
        int srcWidth = srcImage.getWidth(null);
        int srcHeight = srcImage.getHeight(null);
        if(srcWidth <= maxWidth && srcHeight <= maxHeight){
            saveImage(srcImage);
            return;
        }
        Image scaledImage = srcImage.getScaledInstance(srcWidth, srcHeight, Image.SCALE_SMOOTH); 
        double ratio = Math.min((double) maxWidth / srcWidth, (double) maxHeight / srcHeight);
        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null); 
        scaledImage = op.filter(srcImage, null);
        saveImage((BufferedImage)scaledImage);
    }
    
    private InputStream getOriginalInputStream(){
        DBObject query = new BasicDBObject(BuguFS.FILENAME, filename);
        query.put(DIMENSION, null);
        GridFSDBFile f = fs.findOne(query);
        return f.getInputStream();
    }
    
    private BufferedImage openImage(File f){
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(f);
        } catch (Exception ex) {
            logger.error(ex);
        }
        return bi;
    }
    
    private BufferedImage openImage(InputStream is){
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(is);
            is.close();
        } catch (Exception ex) {
            logger.error(ex);
        }
        return bi;
    }
    
    private void saveImage(BufferedImage bi){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, getExtention(), baos);
        } catch (Exception ex) {
            logger.error(ex);
        }
        fs.save(baos.toByteArray(), filename, folder, params);
        try{
            baos.close();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    /**
     * Get the image's width and height.
     * @return element 0 for width, element 1 for height
     */
    public int[] getSize(){
        int[] size = new int[2];
        BufferedImage image = openImage(getOriginalInputStream());
        size[0] = image.getWidth(null);
        size[1] = image.getHeight(null);
        return size;
    }
    
}
