package hr.fer.nm_projekt.featureExtraction;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Vector;

public class HuInvariantMoments implements FeatureExtractor {

	private Vector< Point > points;
	private BufferedImage img;
	private Point center = new Point();
	
	double[][] u = new double[4][4];
	double[][] n = new double[4][4];
	
	private void initPoints() {
		points = new Vector< Point >();
		
		WritableRaster raster = img.getRaster();
		int arr[] = new int[1];
		for( int i = 0; i < raster.getWidth(); ++i )
			for( int j = 0; j < raster.getHeight(); ++j ) {
				raster.getPixel(i, j, arr);
				
				if( arr[0] == 0 ) points.add( new Point( i, j ) );
			}
	}
	
	private void calcCenter() {
		center.x = center.y = 0;
		for( Point p : points ) {
			center.x += p.x;
			center.y += p.y;
		}
		center.x /= points.size();
		center.y /= points.size();
	}
	
	private void calcMoments() {
		int i, j, x, y;
		
		for( i = 0; i < 4; ++i )
			for( j = 0; j < 4; ++j ) u[i][j] = 0;
		
		for( Point p : points ) {
			x = 1;
			for( i = 0; i < 4; ++i ) {
				y = 1;
				for( j = 0; j < 4; ++j ) {
					u[i][j] += ((double)x)*y;
					y *= (p.y-center.y);
				}
				x *= (p.x-center.x);
			}
		}
	}
	
	private void calcScaleMoments() {
		for( int i = 0; i < 4; ++i )
			for( int j = 0; j < 4; ++j )
				if( i+j >= 2 ) n[i][j] = u[i][j] / Math.pow( u[0][0], 1+(i+j)/2. );
	}
	
	private double sqr( double x ) { return x*x; }
	
	public double[] extract(BufferedImage image) {
		img = image;
		
		initPoints();
		calcCenter();
		calcMoments();
		calcScaleMoments();
		
		double ret[] = new double[6];
		ret[0] = n[2][0] + n[0][2];
		ret[1] = sqr( n[2][0] - n[0][2] ) + sqr( 2*n[1][1] );
		ret[2] = sqr( n[3][0] - 3*n[1][2] ) + sqr( 3*n[2][1] - n[0][3] );
		ret[3] = sqr( n[3][0] + n[1][2] ) + sqr( n[2][1] + n[0][3] );
		ret[4] = (n[3][0] - 3*n[1][2])*(n[3][0] + n[1][2])*(sqr(n[3][0]+n[1][2]) - 3*sqr(n[2][1]+n[0][3]))+
				 (3*n[2][1] - n[0][3])*(n[2][1] + n[0][3])*(3*sqr(n[3][0]+n[1][2]) - sqr(n[2][1]+n[0][3]));
		ret[5] = (n[2][0] - n[0][2])*( sqr(n[3][0] + n[1][2]) - sqr( n[2][1]+n[0][3]) ) + 4*n[1][1]*(n[3][0]+n[1][2])*(n[2][1]+n[0][3]);
		
		return ret;
	}
}
