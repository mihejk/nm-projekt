package hr.fer.nm_projekt.preprocessing;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class ImageNoiseRemover implements ImageTransformer {
	
	private static final int k = 5;
	private static final int ON = 0;
	private static final int OFF = 255;
	
	private final int[] corePixels = new int[(k - 2) * (k - 2)];
	private final int[] top = new int[k];
	private final int[] bottom = new int[k];
	private final int[] left = new int[k - 2];
	private final int[] right = new int[k - 2];
	private final int[] coreONPixels = new int[(k - 2) * (k - 2)];
	private final int[] coreOFFPixels = new int[(k - 2) * (k - 2)];
	private final int corePixelCount = (k - 2) * (k - 2);
	
	

	
	public ImageNoiseRemover() {
		Arrays.fill(coreONPixels, ON);
		Arrays.fill(coreOFFPixels, OFF);
	}
	

	@Override
	public BufferedImage transform(BufferedImage img) {
		final int width = img.getWidth();
		final int height = img.getHeight();
		final BufferedImage transformedImage = new BufferedImage(width, height, img.getType());
		final WritableRaster inRaster = img.getRaster();
		final WritableRaster outRaster = transformedImage.getRaster();
		transformedImage.getGraphics().drawImage(img, 0, 0, null);
		
		for (int currentY = 0; currentY < height - k; currentY++) {
			for (int currentX = 0; currentX < width - k; currentX++) {
				inRaster.getPixels(currentX + 1, currentY + 1, k - 2, k - 2, corePixels);
				inRaster.getPixels(currentX, currentY, k, 1, top);
				inRaster.getPixels(currentX, currentY + k - 1, k, 1, bottom);
				inRaster.getPixels(currentX, currentY + 1, 1, k - 2, left);
				inRaster.getPixels(currentX + k - 1, currentY + 1, 1, k - 2, right);
				
				int coreONCount = countPixels(corePixels, ON);
				boolean uniqueConnectedGroup = isUniqueConnectedGroup(top, bottom, left, right);
				
				int[] fillWith;
				if (coreONCount >= corePixelCount / 2) {
					int n = countPixels(top, OFF) + countPixels(bottom, OFF) + countPixels(left, OFF) + countPixels(right, OFF);
					int r = (top[0] == OFF ? 1 : 0) + (top[k - 1] == OFF ? 1 : 0) + (bottom[0] == OFF ? 1 : 0) + (bottom[k - 1] == OFF ? 1 : 0);
					if (uniqueConnectedGroup && ((n > 3 * k - 4) || ((n == 3 * k - 4) && (r == 2)))) {
						fillWith = coreOFFPixels;
					} else {
						fillWith = coreONPixels;
					}
				} else {
//					int n = countPixels(top, ON) + countPixels(bottom, ON) + countPixels(left, ON) + countPixels(right, ON);
//					int r = (top[0] == ON ? 1 : 0) + (top[k - 1] == ON ? 1 : 0) + (bottom[0] == ON ? 1 : 0) + (bottom[k - 1] == ON ? 1 : 0);
//					if (uniqueConnectedGroup && ((n > 3 * k - 4) || ((n == 3 * k - 4) && (r == 2)))) {
//						fillWith = coreONPixels;
//					} else {
						fillWith = coreOFFPixels;
//					}
				}
				outRaster.setPixels(currentX + 1, currentY + 1, k - 2, k - 2, fillWith);
			}
		}

		return transformedImage;
	}
	
	private boolean isUniqueConnectedGroup(int[] top, int[] bottom, int[] left, int[] right) {
		int count = 0;
		int last = left[0];
		for (int i = 0; i < top.length; i++) {
			int current = top[i];
			if (last != current) {
				count++;
				if (count > 2) {
					return false;
				}
			}
			last = current;
		}
		for (int i = 0; i < right.length; i++) {
			int current = right[i];
			if (last != current) {
				count++;
				if (count > 2) {
					return false;
				}
			}
			last = current;
		}
		for (int i = bottom.length - 1; i >= 0; i--) {
			int current = bottom[i];
			if (last != current) {
				count++;
				if (count > 2) {
					return false;
				}
			}
			last = current;
		}
		for (int i = left.length - 1; i >= 0; i--) {
			int current = left[i];
			if (last != current) {
				count++;
				if (count > 2) {
					return false;
				}
			}
			last = current;
		}
		return true;
	}
	
	private int countPixels(int[] corePixels, int type) {
		int counter = 0;
		for (int i = 0; i < corePixels.length; i++) {
			if (corePixels[i] == type) counter++;
		}
		return counter;
	}

}
