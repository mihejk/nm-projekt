package hr.fer.nm_projekt.preprocessing;

import hr.fer.nm_projekt.utilities.image.ImageCroper;
import hr.fer.nm_projekt.utilities.image.ImageHistogram;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Skaliranje slike.
 * @author toni
 */
public class ImageScaler implements ImageTransformer {

	public static final int SCALE_MBR=0;
	public static final int SCALE_MBS=1;
	private final int width;
	private final int height;
	
	private int scaleStrategy;
	
	public ImageScaler(int width, int height) {
		this(SCALE_MBS, width, height);
	}
	
	public ImageScaler(int scaleStrategy, int width, int height) {
		setScaleStrategy(scaleStrategy);
		this.width = width;
		this.height = height;
	}

	private void setScaleStrategy(int scaleStrategy) {
		if(scaleStrategy!=SCALE_MBR && scaleStrategy!=SCALE_MBS){
			throw new IllegalArgumentException("unknown scale strategy");
		}
		this.scaleStrategy=scaleStrategy;
	}

	public BufferedImage transform( BufferedImage img ) {	
//		BufferedImage result = new BufferedImage( img.getWidth(), img.getHeight(), img.getType() );
//		Graphics g = result.getGraphics();
//		g.drawImage( img, 0, 0, null );
//		WritableRaster raster = result.getRaster();
		
		/*crop image*/
		ImageHistogram histogram = new ImageHistogram(img.getRaster());
		int[] mbrArray = histogram.getMBR();
		
		int xMin=mbrArray[0];
		int xMax=mbrArray[1];
		int yMin=mbrArray[2];
		int yMax=mbrArray[3];
		
		if(SCALE_MBS==this.scaleStrategy){
			int width=xMax-xMin;
			int height=yMax-yMin;
			int diff=width-height;
			
			if(diff>0){
				yMin-=Math.abs(diff)/2;
				yMax+=Math.abs(diff)/2;
			}else if(diff<0){
				xMin-=Math.abs(diff)/2;
				xMax+=Math.abs(diff)/2;
			}
		}
		
		BufferedImage croppedImage = ImageCroper.crop(img, xMin, xMax, yMin, yMax);

		
		
		/*scale image*/
		BufferedImage scaledImage = new BufferedImage(width, height, img.getType());
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphics2D.drawImage(croppedImage, 0, 0, width,height, null);
		graphics2D.dispose();
		
		
		return scaledImage;
	}
}
