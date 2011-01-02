package hr.fer.nm_projekt.featureExtraction;

import java.awt.image.BufferedImage;

public interface FeatureExtractor {
	
	public double[] extract(BufferedImage image);

}
