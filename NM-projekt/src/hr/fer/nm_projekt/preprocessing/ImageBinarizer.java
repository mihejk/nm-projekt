package hr.fer.nm_projekt.preprocessing;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class ImageBinarizer implements ImageTransformer {	
	private final Point[] delta4 = new Point[] { new Point( 0, 1 ), 		// desno
														new Point( 1, 0 ),		// dolje
														new Point( 0, -1 ),		// lijevo
														new Point( -1, 0 ) };	// gore
	private final int invisible = 10;
	private final int black = 80;
	private final int grey = 220;
	private final int ignore = 5;
	private final int maxWidth = 10;
	private final int maxEmpty = 3;
	private final int colWidth = 50;
	
	private int[][] arr; // pikseli slike
	private int rows, cols;
	private int white;

	
	private void init( WritableRaster r ) {
		rows = r.getHeight();
		cols = r.getWidth();
		arr = new int[ rows ][ cols ];
		
		for( int i = 0; i < rows; ++i )
			for( int j = 0; j < cols; ++j ) {
				int[] col = new int[1];
				r.getPixel( j, i, col );
				arr[i][j] = col[0];
			}
	}
	
	private void write( WritableRaster r ) {
		for( int i = 0; i < rows; ++i )
			for( int j = 0; j < cols; ++j ) {
				int[] col = new int[] { arr[i][j] };
				r.setPixel( j, i, col );
			}
	}
	
	private int arrGet( Point p ) { // vrati index-u tocku od tocke p u smjeru dir
		return arr[ p.x ][ p.y ];
	}
	
	private void arrSet( Point p, int val ) { // isto sto i gore
		arr[ p.x ][ p.y ] = val;
	}
	
	private Point move( Point p, int dir, int i ) {
		return new Point( p.x + delta4[dir].x*i, p.y + delta4[dir].y*i );
	}
	
	private int guessBorderWidth( Point start, int d ) {
		int l = 0, r;
		
		for( l = 0; l < maxEmpty; ++l ) // preskoci nekoliko bijelih polja, mozda rub ne pocinje odmah
			if( arrGet( move( start, d, l ) ) < white ) break;
		
		if( l == maxEmpty ) return 0; // nema ruba
		
		for( r = l+1; r < maxWidth; ++r ) // staje na prvom svijetlom polju ili kad smo predaleko
			if( arrGet( move( start, d, r ) ) >= white-invisible ) break;
		
		return r;
	}
	
	private int findThreshold() {
		int threshold = 255, sum = 0;
		
		int[] col = new int[256];
		for( int i = 0; i < rows; ++i )
			for( int j = 0; j < cols; ++j ) ++col[ arr[i][j] ];
		
		for( sum = 0; sum < rows*cols*0.5; sum += col[threshold--] );
		
		return threshold + 1;
	}
	
	private int[] calcBorder( Point start, int d ) {
		int n = (d%2 == 0)? cols: rows;
		int q = (d+3)%4;
		int[] border = new int[n];
		int size = maxWidth;
				
		for( int i = 0; i < n; ++i ) border[i] = guessBorderWidth( move( start, d, i ), q );
		
		for( int i = ignore; i < n-ignore; ++i ) size = Math.min( border[i], size );
		for( int i = 0; i < ignore; ++i ) border[i] = border[n-i-1] = size + 3;
		
		for( int i = ignore; i < n-ignore; ++i ) {
			Point p = move( start, d, i );
			
			if( border[i]-size > 2 ) {
				
				border[i] = size;
				if( border[i] > 0 && Math.abs( arrGet( move( p, q, border[i]-1 ) ) - arrGet( move( p, q, border[i] ) ) ) < 30 ) --border[i];
				
			} else {
				int l = -1, r = 0;
				for( int j = 0; j < border[i]; ++j ) {
					int col = arrGet( move( p, q, j ) );
					if( col < black ) l = j;
					else if( col < grey ) r = j;
				}
				
				boolean ok = false;
				
				int A = (n<70)? 2: 4;
				int B = (n<70)? 1: 2;
				int C = (n<70)? 60: 35;
				
				if( r-l > A ) ok = true;
				else if( r-l > B ) {
					for( int j = l+1; j < r; ++j )
						if( Math.abs( arrGet( move( p, q, j+1 ) ) - arrGet( move( p, q, j ) ) ) < C ) ok = true;
				}
				
				if( ok ) border[i] = l+1;
			}
			
			for(; border[i] < maxWidth && arrGet( move( p, q, border[i] ) ) >= white-invisible; ++border[i] );
		}
		
		return border;
	}
	
	private void removeBorder( int[] b, Point p, int d ) {
		for( int i = 0; i < b.length; ++i )
			for( int j = 0; j < b[i]; ++j )
				arrSet( move( move( p, d, i ), (d+3)%4, j ), 255 );
	}
	
	private void binarize() {
		int avg = 0, cnt = 0;
		
		for( int i = 0; i < rows; ++i )
			for( int j = 0; j < cols; ++j )
				if( arr[i][j] < white ) { avg += arr[i][j]; ++cnt; }
		
		if( cnt == 0 ) return;
		avg /= cnt;
		
		for( int i = 0; i < rows; ++i )
			for( int j = 0; j < cols; ++j )
				if( arr[i][j] < white )
					arr[i][j] = ( arr[i][j] > avg+colWidth )? 255: 0;
	}
	
	private void run( WritableRaster r ) {
		init( r );
		
		white = findThreshold();
		
		int bl[] = calcBorder( new Point( 0, 0 ),  1 );
		int br[] = calcBorder( new Point( rows-1, cols-1 ),  3 );
		int bu[] = calcBorder( new Point( 0, cols-1 ),  2 );
		int bd[] = calcBorder( new Point( rows-1, 0 ),  0 );
		
		removeBorder( bl, new Point( 0, 0 ),  1 );
		removeBorder( br, new Point( rows-1, cols-1 ),  3 );
		removeBorder( bu, new Point( 0, cols-1 ),  2 );
		removeBorder( bd, new Point( rows-1, 0 ),  0 );
		
		binarize();
		
		write( r );
	}

	public BufferedImage transform(BufferedImage img) {
		BufferedImage result = new BufferedImage( img.getWidth(), img.getHeight(), img.getType() );
		Graphics g = result.getGraphics();
		g.drawImage( img, 0, 0, null );
		WritableRaster r = result.getRaster();
		
		run(r);
		
		return result;
	}
}
