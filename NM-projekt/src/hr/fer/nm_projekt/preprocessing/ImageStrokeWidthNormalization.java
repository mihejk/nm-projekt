package hr.fer.nm_projekt.preprocessing;

import hr.fer.nm_projekt.utilities.image.Vector2D;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ImageTransformer koji normalizira debljinu linija.
 * 
 * @author EmP
 * 
 */
public class ImageStrokeWidthNormalization implements ImageTransformer {

	private final int black = 0;
	private final int white = 255;

	private double desiredWidth;

	private int[][] imgData;
	private int imgHeigth;
	private int imgWidth;

	public ImageStrokeWidthNormalization(double desiredWidth) {
		super();
		this.desiredWidth = desiredWidth;
	}

	private void arrayToRaster(WritableRaster r) {
		for (int y = 0; y < this.imgHeigth; ++y)
			for (int x = 0; x < this.imgWidth; ++x) {
				int[] col = new int[] { this.imgData[y][x] };
				r.setPixel(x, y, col);
			}
	}

	private void dilatation(double intensity) {
		erosion(intensity, white, black);
	}

	private void erosion(double intensity) {
		erosion(intensity, black, white);
	}

	private void erosion(double intensity, int foreground, int background) {
		int maskRadius = (int) Math.ceil(intensity);
		int[][] mask = new int[2 * maskRadius + 1][2 * maskRadius + 1];
		for (int yn = -maskRadius; yn <= maskRadius; yn++)
			for (int xn = -maskRadius; xn <= maskRadius; xn++)
				if (Point.distance(xn, yn, 0, 0) <= intensity)
					mask[yn + maskRadius][xn + maskRadius] = 1;
				else
					mask[yn + maskRadius][xn + maskRadius] = 0;

		if (mask[maskRadius][maskRadius] == 0)
			return;

		int[][] newData = new int[this.imgHeigth][this.imgWidth];
		for (int y = 0; y < this.imgHeigth; y++)
			for (int x = 0; x < this.imgWidth; x++) {
				int color = foreground;
				for (int yn = -maskRadius; yn <= maskRadius; yn++)
					for (int xn = -maskRadius; xn <= maskRadius; xn++)
						if (mask[yn + maskRadius][xn + maskRadius] == 1
								&& getPixel(x + xn, y + yn) == background)
							color = background;
				newData[y][x] = color;
			}

		this.imgData = newData;
	}

	private double estimateStrokeWidth() {
		Set<Point> strokeEdges = new HashSet<Point>();
		Map<Point, Vector2D> edgeNormals = new HashMap<Point, Vector2D>();

		for (int y = 0; y < this.imgHeigth; y++)
			for (int x = 0; x < this.imgWidth; x++) {
				if (this.imgData[y][x] == white)
					continue;

				boolean isOnEdge = false;
				Vector2D normal = new Vector2D(0, 0);
				for (int yn = -1; yn <= 1; yn++)
					for (int xn = -1; xn <= 1; xn++)
						if (getPixel(x + xn, y + yn) == white)
							isOnEdge = true;
						else
							normal.add(xn, yn);

				if (isOnEdge && !normal.isNull()) {
					Point p = new Point(x, y);
					strokeEdges.add(p);
					edgeNormals.put(p, normal);
				}
			}

		double sum = 0;
		for (Point p : strokeEdges) {
			Vector2D normal = edgeNormals.get(p);
			int localWidth = 1;
			double nearest = Double.MAX_VALUE;

			while (nearest == Double.MAX_VALUE) {
				for (int yn = -localWidth; yn <= localWidth; yn++) {
					int xStep = (yn == -localWidth || yn == -localWidth) ? 1
							: 2 * localWidth;
					for (int xn = -localWidth; xn <= localWidth; xn += xStep) {
						if (normal.dot(xn, yn) <= 0)
							continue;
						if (!strokeEdges
								.contains(new Point(p.x + xn, p.y + yn)))
							continue;

						double dist = p.distance(p.x + xn, p.y + yn);
						if (dist < nearest)
							nearest = dist;
					}
				}
				localWidth++;
			}

			sum += nearest;
		}
		return sum / strokeEdges.size();
	}

	/**
	 * Vraæa "boju" pixela na zadanim koordinatama s time da za pixele van slike
	 * vraæa boju pozadine.
	 * 
	 * @param x
	 * @param y
	 * @return Boja pixela na (x, y)
	 */
	private int getPixel(int x, int y) {
		if (x < 0 || x >= this.imgWidth || y < 0 || y >= this.imgHeigth)
			return white;
		return this.imgData[y][x];
	}

	private void rasterToArray(WritableRaster raster) {
		this.imgData = new int[raster.getHeight()][raster.getWidth()];
		this.imgHeigth = raster.getHeight();
		this.imgWidth = raster.getWidth();

		int[] color = new int[4];
		for (int y = 0; y < raster.getHeight(); y++)
			for (int x = 0; x < raster.getWidth(); x++)
				this.imgData[y][x] = raster.getPixel(x, y, color)[0];
	}

	@Override
	public BufferedImage transform(BufferedImage img) {
		rasterToArray(img.getRaster());
		double strokeWidth = estimateStrokeWidth();

		if (strokeWidth > desiredWidth)
			erosion(strokeWidth - desiredWidth);
		else
			dilatation(desiredWidth - strokeWidth);

		BufferedImage result = new BufferedImage(img.getWidth(), img
				.getHeight(), img.getType());
		arrayToRaster(result.getRaster());
		return result;
	}
}
