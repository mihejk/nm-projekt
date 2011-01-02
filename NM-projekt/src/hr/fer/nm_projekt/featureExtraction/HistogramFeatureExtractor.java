package hr.fer.nm_projekt.featureExtraction;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Polje značajki sadrži redom histograme:
 * xTop
 * xBottom
 * yLeft
 * yRight
 * @author toni
 */
public class HistogramFeatureExtractor implements FeatureExtractor {

	public double[] extract(BufferedImage image) {

		WritableRaster raster = image.getRaster();
	
		double[] features = new double[raster.getWidth()*2 + raster.getHeight()*2];
		
		int xTopIndex=0;
		int xBottomIndex=xTopIndex+raster.getWidth();
		int yLeftIndex=xBottomIndex+raster.getWidth();
		int yRightIndex=yLeftIndex+raster.getHeight();
		
		int midX=raster.getWidth()/2;
		int midY=raster.getHeight()/2;
		
		double xQuant=2.0/raster.getHeight();
		double yQuant=2.0/raster.getWidth();
		
		int[] arr = new int[1];
		for(int i = 0; i < raster.getWidth(); i++){
			for(int j = 0; j < raster.getHeight(); j++) {
				raster.getPixel( i, j, arr );

				if(arr[0]==0){
					if(i<=midX){
						features[yLeftIndex+j]+=yQuant;
					}else{
						features[yRightIndex+j]+=yQuant;
					}
					if(j<=midY){
						features[xTopIndex+i]+=xQuant;
					}else{
						features[xBottomIndex+i]+=xQuant;
					}
				}
			}
		}
		
		return features;
	}

}
