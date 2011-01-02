package hr.fer.nm_projekt.featureExtraction;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class ProfileFeatureExtractor implements FeatureExtractor{

	double getMinValue(double[] array)
	{
		double min = array[0];
		
		for(int i = 1; i < array.length; i++)
		{
			if(array[i] < min)
				min = array[i];
		}
		
		return min;
	}
	
	//za bounding box
	void setNewValues(double[] array, double min)
	{
		for(int i = 0; i < array.length; i++)
		{ 
			array[i] -= min;
		}
	}
	
	
	@Override
	public double[] extract(BufferedImage image) {
		
		WritableRaster raster = image.getRaster();
		double[] features = new double[2 * raster.getHeight() + 2 * raster.getWidth()];
		double num = 0;
		
		double[] arrayTop = new double[raster.getWidth()];
		double[] arrayRight = new double[raster.getHeight()];
		double[] arrayBottom = new double[raster.getWidth()];
		double[] arrayLeft = new double[raster.getHeight()];
		
		Arrays.fill(arrayTop, raster.getHeight());
		Arrays.fill(arrayRight, raster.getWidth());
		Arrays.fill(arrayBottom, raster.getHeight());
		Arrays.fill(arrayLeft, raster.getWidth());
		
		int right = raster.getWidth() - 1, bottom = raster.getHeight() - 1;
		
		double[] pixel = new double[1];
		
		for(int i = 0; i < raster.getHeight(); i++)
		{	
			for(int j = 0; j < raster.getWidth(); j++)
			{
				raster.getPixel(j, i, pixel);
				
				if(pixel[0] != 255)
				{
					if(i < arrayTop[j])
						arrayTop[j] = i;
					
					if((right - j) < arrayRight[i])
						arrayRight[i] = right - j;
					
					if(j < arrayLeft[i])
						arrayLeft[i] = j;
					
					if((bottom - i) < arrayBottom[j])
						arrayBottom[j] = bottom - i;
				}
			}
		}
		
		setNewValues(arrayTop, getMinValue(arrayTop));
		setNewValues(arrayRight, getMinValue(arrayRight));
		setNewValues(arrayBottom, getMinValue(arrayBottom));
		setNewValues(arrayLeft, getMinValue(arrayLeft));
		
		System.arraycopy(arrayTop,0, features, 0, arrayTop.length);
		System.arraycopy(arrayRight, 0, features, arrayTop.length, arrayRight.length);
		System.arraycopy(arrayBottom, 0, features, arrayTop.length + arrayRight.length, arrayBottom.length);
		System.arraycopy(arrayLeft, 0, features, arrayTop.length + arrayRight.length + arrayBottom.length, arrayLeft.length);
				
//		System.out.println("features: " + Arrays.toString(features));
		
		return features;
	}

}
