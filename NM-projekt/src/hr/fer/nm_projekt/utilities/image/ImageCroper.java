package hr.fer.nm_projekt.utilities.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageCroper {

	public static BufferedImage crop(BufferedImage image,int xMin,int xMax,int yMin,int yMax){
		int width = xMax-xMin;
		int height = yMax-yMin;
		if (width < 2 || height < 2) {
			return new BufferedImage(2, 2, image.getType());
			// TODO: mora klasificirati kao prazno?
		}
		
	    BufferedImage dest = new BufferedImage(width, height, image.getType());
	    Graphics imageG = dest.createGraphics();
	    imageG.drawImage(image, 0, 0, xMax-xMin, yMax-yMin, xMin, yMin, xMax, yMax, null);

	    if (xMin < 0) {
	    	imageG.fillRect(0, 0, -xMin, dest.getHeight());
	    }
	    if (xMax > image.getWidth()) {
	    	int widthToCorrect = xMax - image.getWidth();
	    	imageG.fillRect(dest.getWidth() - widthToCorrect, 0, widthToCorrect, dest.getHeight());
	    }
	    if (yMin < 0) {
	    	imageG.fillRect(0, 0, dest.getWidth(), -yMin);
	    }
	    if (yMax > image.getHeight()) {
	    	int heightToCorrect = yMax - image.getHeight();
	    	imageG.fillRect(0, dest.getHeight() - heightToCorrect, dest.getWidth(), heightToCorrect);
	    }
	    
	    imageG.dispose();
	    return dest;
	}
	
}
