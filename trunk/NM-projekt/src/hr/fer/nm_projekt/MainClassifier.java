package hr.fer.nm_projekt;

import java.awt.image.BufferedImage;

public interface MainClassifier {
	
	public double[] classify(BufferedImage image, int dimensions);
	
	public boolean isReliable(double[] classification);

}
