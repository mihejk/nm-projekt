package hr.fer.nm_projekt.featureExtraction;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Crossings implements FeatureExtractor {

	private WritableRaster raster;
	private int w, h;
	
	private int countCrossings( Point start, Point dir ) {
		int last = 0, ret = 0;
		int arr[] = new int[1];
		for(;;) {
			if( start.x == w || start.y == h ) break;
			
			raster.getPixel( start.x, start.y, arr );
			
			if( last == 0 && arr[0] == 0 ) ++ret;
			
			last = (arr[0]==255)? 0: 1;
			start.x += dir.x;
			start.y += dir.y;
		}
		
		return ret;
	}
	
	public double[] extract(BufferedImage image) {
		raster = image.getRaster();
		w = raster.getWidth();
		h = raster.getHeight();
		
		double[] ret = new double[w+h];
		
		for( int i = 0; i < h; ++i ) ret[i] = (double) countCrossings( new Point(0,i), new Point( 1, 0 ) );
		for( int i = 0; i < w; ++i ) ret[h+i] = (double) countCrossings( new Point(i,0), new Point( 0, 1 ) );
		
		return ret;
	}
}
