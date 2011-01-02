package hr.fer.nm_projekt.featureExtraction;

import hr.fer.nm_projekt.utilities.image.Vector2D;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class RadialHistogramFeatureExtractor implements FeatureExtractor {

	private final int black = 0;

	private int resolution;
	private WritableRaster raster = null;

	public RadialHistogramFeatureExtractor(int resolution) {
		super();
		this.resolution = resolution;
	}

	private Point calcCenter() {
		Vector2D center = new Vector2D(0, 0);

		int[] color = new int[4];
		int count = 0;
		for (int y = 0; y < raster.getHeight(); y++)
			for (int x = 0; x < raster.getWidth(); x++)
				if (raster.getPixel(x, y, color)[0] == black) {
					center.add(x, y);
					count++;
				}
		if (count == 0)
			return new Point(raster.getWidth() / 2, raster.getHeight() / 2);
		else
			return center.multiply(1.0 / count);
	}

	@Override
	public double[] extract(BufferedImage image) {
		raster = image.getRaster();
		double[] res = new double[resolution];

		final double Pi_2 = 2 * Math.PI;
		Point center = calcCenter();
		int[] color = new int[4];
		for (int y = 0; y < raster.getHeight(); y++)
			for (int x = 0; x < raster.getWidth(); x++)
				if (raster.getPixel(x, y, color)[0] == black) {
					Vector2D point = new Vector2D(x, y).substract(center);
					if (point.isNull())
						continue;
					double angle = point.getAngle() + Pi_2;
					res[(int)Math.round(angle * resolution / Pi_2) % resolution]++;
				}
		
		return res;
	}

}
