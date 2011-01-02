package hr.fer.nm_projekt.preprocessing;

import hr.fer.nm_projekt.utilities.image.ImageCroper;
import hr.fer.nm_projekt.utilities.image.ImageHistogram;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Centriranje slike (za sada).
 * @author toni
 * @deprecated Integrirano unutar {@link ImageCroper}
 */
 @Deprecated
public class ImageCentralizer implements ImageTransformer {

	private boolean drawMBR=false;
	
	public BufferedImage transform( BufferedImage img ) {	
		BufferedImage result = new BufferedImage( img.getWidth(), img.getHeight(), img.getType() );
		Graphics g = result.getGraphics();
		g.drawImage( img, 0, 0, null );
		WritableRaster raster = result.getRaster();
		
		ImageHistogram histogram = new ImageHistogram(raster);
		int[] horizontalHistorgram = histogram.getHorizontalHistorgram();
		int[] verticalHistogram = histogram.getVerticalHistogram();
		int[] mbrArray = histogram.getMBR();
		
		int xMin=mbrArray[0];
		int xMax=mbrArray[1];
		int yMin=mbrArray[2];
		int yMax=mbrArray[3];
		
		int[] arr = new int[1];
		
		if(drawMBR){
			arr[0]=0;
			for(int i=xMin; i <=xMax; i++){
				raster.setPixel(i,yMin,arr);
				raster.setPixel(i,yMax,arr);
			}
			for(int j=yMin; j <=yMax; j++){
				raster.setPixel(xMin,j,arr);
				raster.setPixel(xMax,j,arr);
			}
		}
	
//		System.out.println(Arrays.toString(horizontalHistorgram));
//		System.out.println(Arrays.toString(verticalHistogram));
//		System.out.println(Arrays.toString(mbrArray));
		
		int xCenter=(xMax+xMin)/2;
		int actualXCenter=horizontalHistorgram.length/2;
		int xDiff=actualXCenter-xCenter;
		
		int yCenter=(yMax+yMin)/2;
		int actualYCenter=verticalHistogram.length/2;
		int yDiff=actualYCenter-yCenter;
		
//		System.out.println("xCenter:"+xCenter);
//		System.out.println("actualXCenter:"+actualXCenter);
//		System.out.println("xDiff:"+xDiff);
//		System.out.println("yCenter:"+yCenter);
//		System.out.println("actualYCenter:"+actualYCenter);
//		System.out.println("yDiff:"+yDiff);
		
		if(xDiff>0){
			//x->right
			for(int i=raster.getWidth()-1; i>=0; i--){
				if(yDiff<0){
					//y->up
					for(int j = 0; j < raster.getHeight(); j++) {
						if(i-xDiff>=0 && j-yDiff < raster.getHeight()){
							raster.getPixel(i-xDiff,j-yDiff,arr);
						}else{
							arr[0]=255;
						}
						raster.setPixel(i,j,arr);
					}
				}else{
					//y->down
					for(int j = raster.getHeight()-1; j>=0; j--) {
						if(i-xDiff>=0 && j-yDiff>=0){
							raster.getPixel(i-xDiff,j-yDiff,arr);
						}else{
							arr[0]=255;
						}
						raster.setPixel(i,j,arr);
					}
				}
			}
		}else{
			//x->left
			for(int i=0; i<raster.getWidth(); i++){
				if(yDiff<0){
					//y->up
					for(int j = 0; j < raster.getHeight(); j++) {
						if(i-xDiff<raster.getWidth() && j-yDiff < raster.getHeight()){
							raster.getPixel(i-xDiff,j-yDiff,arr);
						}else{
							arr[0]=255;
						}
						raster.setPixel(i,j,arr);
					}
				}else{
					//y->down
					for(int j = raster.getHeight()-1; j>=0; j--) {
						if(i-xDiff<raster.getWidth() && j-yDiff>=0){
							raster.getPixel(i-xDiff,j-yDiff,arr);
						}else{
							arr[0]=255;
						}
						raster.setPixel(i,j,arr);
					}
				}
			}
		}
		
		return result;
	}
}
