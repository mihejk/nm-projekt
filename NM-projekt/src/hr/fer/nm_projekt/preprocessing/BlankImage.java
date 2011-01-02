package hr.fer.nm_projekt.preprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BlankImage {

	private double treshold;
	
	public BlankImage(double treshold)
	{
		this.treshold = treshold;
	}
	
	/**
	 * 
	 * @param image BufferedImage
	 * @return true if blank
	 */
	public boolean isBlank(BufferedImage image)
	{
		boolean isBlank = false;
		
		int sirina = image.getWidth();
		int visina = image.getHeight();
		
		int[] pixels = new int[sirina * visina];
		
		image.getRGB(0, 0, sirina, visina, pixels, 0, sirina);
		
		int count = 0;
		for(int i = 0; i < pixels.length; i++)
		{
			Color color = new Color(pixels[i]);
			int red = color.getRed();
			int green = color.getGreen();
			int blue = color.getBlue();
			
			if(red >= 200 && green >= 200 && blue >= 200)
			{
				count++;
			}
		}
		
		double percentage = ((double) count / (double) pixels.length);
		
		if(percentage >= this.treshold)
			isBlank = true;
		else 
			isBlank = false;
		
		return isBlank;
	}
}
