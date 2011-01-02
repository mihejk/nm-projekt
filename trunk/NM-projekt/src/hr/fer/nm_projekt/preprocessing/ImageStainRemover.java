package hr.fer.nm_projekt.preprocessing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ImageStainRemover implements ImageTransformer {
	
	// options
	private static final double MAX_PERCENT_OF_PIXELS_IN_STAIN = 0.15;
	private static final double MAX_PERCENT_DISTANCE = 0.3;
	private static final double THRESHOLD = 10.0;
	
	
	private static final int ON = 0;
	private static final int OFF = 255;
	private static final int[] OFF_PIXEL = new int[] { OFF };
	private final int[] pixel = new int[1];
	private int width;
	private int height;
	
	@Override
	public BufferedImage transform(BufferedImage img) {
		width = img.getWidth();
		height = img.getHeight();
		final BufferedImage transformedImage = new BufferedImage(width, height, img.getType());
		final WritableRaster outRaster = transformedImage.getRaster();
		transformedImage.getGraphics().drawImage(img, 0, 0, null);
		
		final int[][] groups = new int[width][height];
		int sumOfPixels = 0;
		List<Group> groupList = new LinkedList<Group>();
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (groups[i][j] == 0) {
					outRaster.getPixel(i, j, pixel);
					if (pixel[0] == ON) {
						int groupNumber = groupList.size() + 1;
						Group group = new Group(groupNumber);
						groups[i][j] = groupNumber;
						selectWholeGroup(i, j, groups, group, outRaster);
						groupList.add(group);
						sumOfPixels += group.getPixelCount();
					} else {
						groups[i][j] = -1;
					}
				}
			}
		}
		
		int maxGroupNumber = groupList.size();
		double[][] groupDistances = new double[maxGroupNumber][maxGroupNumber];
		int step = 0;
		int maxSteps = (int) (Math.max(img.getWidth(), img.getHeight()) * MAX_PERCENT_DISTANCE / 2);
		int distancesToCalculate = maxGroupNumber * (maxGroupNumber - 1) / 2;
		while(distancesToCalculate != 0 && step < maxSteps) {
			step++;
			for (Group g : groupList) {
				g.expand(groups);
			}
			
			for (int i = 0; i < width - 1; i++) {
				for (int j = 0; j < height - 1; j++) {
					int currentValue = groups[i][j];
					if (currentValue == -1) continue;
					int leftValue = groups[i + 1][j];
					int belowValue = groups[i][j + 1];
					if (leftValue != -1 && currentValue != leftValue && groupDistances[currentValue - 1][leftValue - 1] == 0) {
						groupDistances[currentValue - 1][leftValue - 1] = (double) groupList.get(leftValue - 1).getPixelCount() / step;
						groupDistances[leftValue - 1][currentValue - 1] = (double) groupList.get(currentValue - 1).getPixelCount() / step;
						distancesToCalculate--;
					}
					if (belowValue != -1 && currentValue != belowValue && groupDistances[currentValue - 1][belowValue - 1] == 0) {
						groupDistances[currentValue - 1][belowValue - 1] = (double) groupList.get(belowValue - 1).getPixelCount() / step;
						groupDistances[belowValue - 1][currentValue - 1] = (double) groupList.get(currentValue - 1).getPixelCount() / step;
						distancesToCalculate--;
					}
				}
			}
		}
		
		if( groupList.size() == 0 ) return transformedImage;
		
		Collections.sort(groupList);
		List<Group> relevantGroups = new ArrayList<Group>();
		relevantGroups.add(groupList.remove(0));
		for (int i = 0; i < groupList.size(); i++) {
			Group group = groupList.get(i);
			if ((double) group.getPixelCount() / sumOfPixels > MAX_PERCENT_OF_PIXELS_IN_STAIN) {
				relevantGroups.add(group);
			}
		}
		groupList.removeAll(relevantGroups);
		
		
		List<Group> groupsToSetRelevant = new ArrayList<Group>();
		
		while(true) {
			groupsToSetRelevant.clear();
			for (Group relevantGroup : relevantGroups) {
				for (Group group : groupList) {
					if (groupDistances[relevantGroup.getGroupNumber() - 1][group.getGroupNumber() - 1] > THRESHOLD && !groupsToSetRelevant.contains(group)) {
						groupsToSetRelevant.add(group);
					}
				}
			}
			if (groupsToSetRelevant.isEmpty()) {
				break;
			} else {
				relevantGroups.addAll(groupsToSetRelevant);
				groupList.removeAll(groupsToSetRelevant);
			}
		}
		
		for (Group toDelete : groupList) {
			delete(toDelete.getFirstPoint().x, toDelete.getFirstPoint().y, outRaster);
		}
		
		return transformedImage;
	}
	
	private class Group implements Comparable<Group> {
		
		private final int groupNumber;
		private int count = 0;
		
		private Point firstPoint = null;
		private int zoneLeft;
		private int zoneRight;
		private int zoneTop;
		private int zoneBottom;
		
		public Group(int groupNumber) {
			this.groupNumber = groupNumber;
		}

		public void addPoint(int x, int y) {
			count++;
			
			if (firstPoint == null) {
				firstPoint = new Point(x, y);
				zoneLeft = x;
				zoneRight = x;
				zoneTop = y;
				zoneBottom = y;
			} else {
				zoneLeft = Math.min(zoneLeft, x);
				zoneRight = Math.max(zoneRight, x);
				zoneTop = Math.min(zoneTop, y);
				zoneBottom = Math.max(zoneBottom, y);
			}
		}
		
		public Point getFirstPoint() {
			return firstPoint;
		}

		public int getGroupNumber() {
			return groupNumber;
		}
		
		public void expand(int[][] groups) {
			zoneLeft = Math.max(0, zoneLeft - 1);
			zoneRight = Math.min(width - 1, zoneRight + 1);
			zoneBottom = Math.max(0, zoneBottom - 1);
			zoneTop = Math.min(height - 1, zoneTop + 1);
			int to = Math.max(1, zoneLeft);
			for (int i = zoneRight; i >= to; i--) {
				for (int j = zoneTop; j < zoneBottom; j++) {
					if (groups[i][j] == -1 && groups[i - 1][j] == groupNumber) {
						groups[i][j] = groupNumber;
					}
				}
			}
			to = Math.min(width - 2, zoneRight);
			for (int i = zoneLeft; i < to; i++) {
				for (int j = zoneTop; j < zoneBottom; j++) {
					if (groups[i][j] == -1 && groups[i + 1][j] == groupNumber) {
						groups[i][j] = groupNumber;
					}
				}
			}
			for (int i = zoneLeft; i < zoneRight; i++) {
				for (int j = Math.max(1, zoneBottom); j >= zoneTop; j--) {
					if (groups[i][j] == -1 && groups[i][j - 1] == groupNumber) {
						groups[i][j] = groupNumber;
					}
				}
			}
			for (int i = zoneLeft; i < zoneRight; i++) {
				for (int j = Math.min(height - 2, zoneTop); j < zoneBottom; j++) {
					if (groups[i][j] == -1 && groups[i][j + 1] == groupNumber) {
						groups[i][j] = groupNumber;
					}
				}
			}
		}
		
		public int getPixelCount() {
			return count;
		}
		
		@Override
		public int compareTo(Group o) {
			return o.count - count;
		}
		
		@Override
		public String toString() {
			return "Zone: [" + zoneLeft + "-" + zoneRight + ", " + zoneTop + "-" + zoneBottom + "], pixels=" + count;
		}
		
	}
	
	private int selectWholeGroup(int x, int y, int[][] groups, Group group, WritableRaster raster) {
		group.addPoint(x, y);
		int count = 0;
		if (x - 1 >= 0 && groups[x - 1][y] == 0) {
			raster.getPixel(x - 1, y, pixel);
			if (pixel[0] == ON) {
				groups[x - 1][y] = group.getGroupNumber();
				count += selectWholeGroup(x - 1, y, groups, group, raster);
			} else {
				groups[x - 1][y] = -1;
			}
		}
		if (x + 1 < width && groups[x + 1][y] == 0) {
			raster.getPixel(x + 1, y, pixel);
			if (pixel[0] == ON) {
				groups[x + 1][y] = group.getGroupNumber();
				count += selectWholeGroup(x + 1, y, groups, group, raster);
			} else {
				groups[x + 1][y] = -1;
			}
		}
		if (y - 1 >= 0 && groups[x][y - 1] == 0) {
			raster.getPixel(x, y - 1, pixel);
			if (pixel[0] == ON) {
				groups[x][y - 1] = group.getGroupNumber();
				count += selectWholeGroup(x, y - 1, groups, group, raster);
			} else {
				groups[x][y - 1] = -1;
			}
		}
		if (y + 1 < height && groups[x][y + 1] == 0) {
			raster.getPixel(x, y + 1, pixel);
			if (pixel[0] == ON) {
				groups[x][y + 1] = group.getGroupNumber();
				count += selectWholeGroup(x, y + 1, groups, group, raster);
			} else {
				groups[x][y + 1] = -1;
			}
		}
		return count;
	}
	
	private void delete(int x, int y, WritableRaster raster) {
		raster.setPixel(x, y, OFF_PIXEL);
		if (x - 1 >= 0) {
			raster.getPixel(x - 1, y, pixel);
			if (pixel[0] == ON) {
				delete(x - 1, y, raster);
			}
		}
		if (x + 1 < width) {
			raster.getPixel(x + 1, y, pixel);
			if (pixel[0] == ON) {
				delete(x + 1, y, raster);
			}
		}
		
		if (y - 1 >= 0) {
			raster.getPixel(x, y - 1, pixel);
			if (pixel[0] == ON) {
				delete(x, y - 1, raster);
			}
			
		}
		if (y + 1 < height) {
			raster.getPixel(x, y + 1, pixel);
			if (pixel[0] == ON) {
				delete(x, y + 1, raster);
			}
			
		}
		
	}

}
