package com.skeeterSoftworks.WorkOrderCentral.util;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EImageFormat;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
public class ImageUtils {

	 public static String imageToBase64String(BufferedImage image, EImageFormat type) {

	        String imageString = null;
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();

	        String imageFormat = type != null ? type.name() : "JPG";

	        try {//from   ww  w .java 2 s.c om
	            ImageIO.write(image, imageFormat, bos);
	            byte[] imageBytes = bos.toByteArray();

	            imageString = Base64.getEncoder().encodeToString(imageBytes);

	            bos.close();
	        } catch (IOException e) {
	        	log.error(e.getMessage(),e);
	        }
	        return imageString;
	    }
}
