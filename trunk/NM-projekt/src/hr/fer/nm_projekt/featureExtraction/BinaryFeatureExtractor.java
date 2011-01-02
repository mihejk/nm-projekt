package hr.fer.nm_projekt.featureExtraction;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * @author toni
 */
public class BinaryFeatureExtractor implements FeatureExtractor {

	public double[] extract(BufferedImage image) {

		WritableRaster raster = image.getRaster();
	
		double[] features = new double[raster.getWidth()*raster.getHeight()];
		
		int[] arr = new int[1];
		for(int i = 0; i < raster.getWidth(); i++){
			for(int j = 0; j < raster.getHeight(); j++) {
				raster.getPixel( i, j, arr );
				features[i*raster.getWidth()+j]=(arr[0]==0)?1.0:0.0;
			}
		}
		
//		System.out.println("features: "+Arrays.toString(features));
		return features;
	}

}
