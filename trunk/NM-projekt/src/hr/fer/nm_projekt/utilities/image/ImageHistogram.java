package hr.fer.nm_projekt.utilities.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Vertikalni i horizontalni histogram slike.
 * @author toni
 */
public class ImageHistogram {
	
	/**
	 * Velićina ruba koji se ignorira kod stvaranje histograma.
	 * Quickfix za problem rubova.
	 */
	private int ignoreBorders=0;
	
	private int[] horizontalHistorgram;
	private int[] verticalHistogram;
	
	public ImageHistogram(BufferedImage image) {
		this(image.getRaster());
	}
	
	public ImageHistogram(WritableRaster raster) {
		
		horizontalHistorgram = new int[raster.getWidth()];
		verticalHistogram = new int[raster.getHeight()];
		
		int[] arr = new int[1];
		for(int i = 0+ignoreBorders; i < raster.getWidth()-ignoreBorders; i++){
			for(int j = 0+ignoreBorders; j < raster.getHeight()-ignoreBorders; j++) {
				raster.getPixel( i, j, arr );
//				System.out.printf("(%d,%d)=%d\n",i,j,arr[0]);
				if(arr[0]==0){
					horizontalHistorgram[i]+=1;
					verticalHistogram[j]+=1;
				}
			}
		}
	}
	
	/**
	 * Pronalazi minimium-bounding-rectangle slike, [xmin,xmax,ymin,ymax]
	 * @return mbr polje
	 */
	public int[] getMBR(){
		
		int xmin=horizontalHistorgram.length-1,xmax=0,
			ymin=verticalHistogram.length-1,ymax=0;
		
		for(int i=0;i<horizontalHistorgram.length;i++){
			if(horizontalHistorgram[i]>0){
				xmin=i<xmin?i:xmin;
				xmax=i>xmax?i:xmax;
			}
		}
		
		for(int i=0;i<verticalHistogram.length;i++){
			if(verticalHistogram[i]>0){
				ymin=i<ymin?i:ymin;
				ymax=i>ymax?i:ymax;
			}
		}
		
		int[] boundaries={xmin,xmax,ymin,ymax};
		return boundaries;
	}

	/**
	 * @return horizontalni histogram
	 */
	public int[] getHorizontalHistorgram() {
		return horizontalHistorgram;
	}

	/**
	 * @return vertikalni histogram
	 */
	public int[] getVerticalHistogram() {
		return verticalHistogram;
	}
	
}
