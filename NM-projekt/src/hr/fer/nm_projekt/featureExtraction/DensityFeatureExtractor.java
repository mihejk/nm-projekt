package hr.fer.nm_projekt.featureExtraction;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class DensityFeatureExtractor implements FeatureExtractor {
	
	private final BufferedImage scaledImage;
	private final Graphics2D scaledGraphics;
	private final int width;
	private final int height;
	
	private final int[] buffer;
	                  
	public DensityFeatureExtractor(int width, int height) {
		this.width = width;
		this.height = height;
		buffer = new int[width * height];
		scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		scaledGraphics = scaledImage.createGraphics();
		scaledGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	@Override
	public double[] extract(BufferedImage image) {
		scaledGraphics.drawImage(image, 0, 0, width, height, null);
		double[] features = new double[width * height];
		scaledImage.getRaster().getPixels(0, 0, width, height, buffer);
		for (int i = 0; i < buffer.length; i++) {
			features[i] = 1.0 - buffer[i] / 255.0;
		}
		return features;
	}

}
