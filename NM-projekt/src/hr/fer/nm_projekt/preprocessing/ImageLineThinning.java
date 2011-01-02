package hr.fer.nm_projekt.preprocessing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashSet;
import java.util.Set;

public class ImageLineThinning implements ImageTransformer {

	private final int black = 0;
	private final int white = 255;
	private final Point[] neighbourPositions = new Point[] { new Point(-1, -1),
			new Point(0, -1), new Point(1, -1), new Point(1, 0),
			new Point(1, 1), new Point(0, 1), new Point(-1, 1),
			new Point(-1, 0), new Point(-1, -1) };

	private int[][] imgData;
	private int imgHeigth;
	private int imgWidth;

	private void arrayToRaster(WritableRaster r) {
		for (int y = 0; y < this.imgHeigth; ++y)
			for (int x = 0; x < this.imgWidth; ++x) {
				int[] col = new int[] { this.imgData[y][x] };
				r.setPixel(x, y, col);
			}
	}

	private int countNeighbours(int x, int y, int color) {
		int count = 0;
		for (int dy = -1; dy <= 1; ++dy)
			for (int dx = -1; dx <= 1; ++dx)
				if (getPixel(x + dx, y + dy) == color && (dx != 0 || dy != 0))
					count++;
		return count;
	}

	private int countTransitions(int x, int y) {
		int count = 0;
		int lastColor = getPixel(x + neighbourPositions[0].x, y
				+ neighbourPositions[0].y);
		for (Point point : neighbourPositions) {
			int color = getPixel(x + point.x, y + point.y);
			if (color != lastColor)
				count++;
			lastColor = color;
		}

		return count;
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

	private boolean isSkeletalPixelEven(Point p) {
		int neighbours = countNeighbours(p.x, p.y, black);

		if (neighbours <= 1 || neighbours >= 7)
			return true;
		if (countTransitions(p.x, p.y) >= 4)
			return true;
		
		boolean p2 = getPixel(p.x, p.y - 1) == white;
		boolean p4 = getPixel(p.x + 1, p.y) == white;
		boolean p6 = getPixel(p.x, p.y + 1) == white;
		boolean p8 = getPixel(p.x - 1, p.y) == white;
		
		if (p2 || p8 || p4 && p6)
			return false;
		
		return true;
	}
	
	private boolean isSkeletalPixelOdd(Point p) {
		int neighbours = countNeighbours(p.x, p.y, black);

		if (neighbours <= 1 || neighbours >= 7)
			return true;
		if (countTransitions(p.x, p.y) >= 4)
			return true;
		
		boolean p2 = getPixel(p.x, p.y - 1) == white;
		boolean p4 = getPixel(p.x + 1, p.y) == white;
		boolean p6 = getPixel(p.x, p.y + 1) == white;
		boolean p8 = getPixel(p.x - 1, p.y) == white;
		
		if (p4 || p6 || p2 && p8)
			return false;
		
		return true;
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

	private void setPixel(int x, int y, int color) {
		if (x < 0 || x >= this.imgWidth || y < 0 || y >= this.imgHeigth)
			return;
		this.imgData[y][x] = color;
	}

	@Override
	public BufferedImage transform(BufferedImage img) {
		rasterToArray(img.getRaster());

		Set<Point> blackPixels = new HashSet<Point>();
		for (int y = 0; y < this.imgHeigth; y++)
			for (int x = 0; x < this.imgWidth; x++)
				if (this.getPixel(x, y) == black)
					blackPixels.add(new Point(x, y));

		boolean repeatThinning = true;
		Set<Point> deletedPixels = new HashSet<Point>();
		while (repeatThinning) {
			repeatThinning = false;
			
			deletedPixels.clear();
			for (Point point : blackPixels)
				if (!isSkeletalPixelOdd(point))
					deletedPixels.add(point);
			for (Point point : deletedPixels)
				setPixel(point.x, point.y, white);
			blackPixels.removeAll(deletedPixels);
			repeatThinning = deletedPixels.size() > 0;
			
			deletedPixels.clear();
			for (Point point : blackPixels)
				if (!isSkeletalPixelEven(point))
					deletedPixels.add(point);
			for (Point point : deletedPixels)
				setPixel(point.x, point.y, white);
			blackPixels.removeAll(deletedPixels);
			repeatThinning = repeatThinning | (deletedPixels.size() > 0);
		}

		BufferedImage result = new BufferedImage(img.getWidth(), img
				.getHeight(), img.getType());
		arrayToRaster(result.getRaster());
		return result;
	}

}
