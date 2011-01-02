package hr.fer.nm_projekt.utilities;

import hr.fer.nm_projekt.Category;
import hr.fer.nm_projekt.MainClassifier;
import hr.fer.nm_projekt.MainClassifierImpl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class Report {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		List<String> trainSet = ClassifyUtils.loadStringListFromFile(new File("data/train_images.txt"));
		List<Integer> trainResults = ClassifyUtils.loadIntegerListFromFile(new File("data/train_categories.txt"));
		List<String> testSet = ClassifyUtils.loadStringListFromFile(new File("data/test_images.txt"));
		List<Integer> testResults = ClassifyUtils.loadIntegerListFromFile(new File("data/test_categories.txt"));
		
		List<String> allSet = new ArrayList<String>();
		List<Integer> allResults = new ArrayList<Integer>();
		FilenameFilter pngFiles = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".png");
			}
		};
		for (int i = 0; i < Category.CATEGORY_COUNT; i++) {
			for (File file : new File("data/200dpi/" + Category.toString(i)).listFiles(pngFiles)) {
				allSet.add(file.getAbsolutePath());
				allResults.add(i);
			}
			for (File file : new File("data/300dpi/" + Category.toString(i)).listFiles(pngFiles)) {
				allSet.add(file.getAbsolutePath());
				allResults.add(i);
			}
		}
		
		
		
		int dimension = 8;
		List<String> imgPathList = new ArrayList<String>();
//		imgPathList.addAll(trainSet);
		imgPathList.addAll(testSet);
//		imgPathList.addAll(allSet);
		List<Integer> resultList = new ArrayList<Integer>();
//		resultList.addAll(trainResults);
		resultList.addAll(testResults);
//		resultList.addAll(allResults);
		
//		int countAll = 0;
//		for (int i = 0; i < Category.CATEGORY_COUNT; i++) {
//			int count = 0;
//			for (Integer integer : resultList) {
//				if (integer == i) count++;
//			}
//			countAll += count;
//			System.out.println(Category.toString(i) + " " + count);
//		}
//		System.out.println(countAll);
//		if (true) return;
		
		int correct = 0, incorrect = 0;
		int v2_correct = 0, v2_incorrect = 0, v2_unsure = 0;
		MainClassifier mainClassifier = new MainClassifierImpl();
		int[] correctByProbability = new int[20];
		int[] incorrectByProbability = new int[correctByProbability.length];
		int[] correctByRatio = new int[correctByProbability.length];
		int[] incorrectByRatio = new int[correctByProbability.length];
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < imgPathList.size(); i++) {
			int realResult = resultList.get(i);
			if (realResult >= dimension) continue;
			
			BufferedImage img = ImageIO.read(new File(imgPathList.get(i)));
			double[] resultProbabilities = mainClassifier.classify(img, dimension);
			int result = ClassifyUtils.getMaxIndex(resultProbabilities);
			double ratio = 1 / ClassifyUtils.getTwoBestRatio(resultProbabilities);
			if (result == realResult) {
				correct++;
				correctByProbability[(int) Math.round(resultProbabilities[result] * correctByProbability.length - 1)]++;
				correctByRatio[(int) Math.round(ratio * (correctByProbability.length - 1))]++;
				if (1 / ratio < 4 && resultProbabilities[result] < 0.75) {
					stringBuilder.append(resultProbabilities[result] + " " + (1 / ratio) + ";");
				}
			} else {
				incorrect++;
				System.out.println(resultProbabilities[result] + " " + (1 / ratio) + ";");
				incorrectByProbability[(int) Math.round(resultProbabilities[result] * correctByProbability.length - 1)]++;
				incorrectByRatio[(int) Math.round(ratio * (correctByProbability.length - 1))]++;
			}
			
			if (mainClassifier.isReliable(resultProbabilities)) {
				if (result == realResult) {
					v2_correct++;
				} else {
//					System.out.println(imgPathList.get(i));
					v2_incorrect++;
				}
			} else {
				v2_unsure++;
			}
		}
		System.out.println("correct=" + correct + " incorrect=" + incorrect);
		System.out.println(((double) correct) / (incorrect + correct));
		System.out.println(Arrays.toString(correctByProbability));
		System.out.println(Arrays.toString(incorrectByProbability));
		System.out.println("--");
		System.out.println(Arrays.toString(correctByRatio));
		System.out.println(Arrays.toString(incorrectByRatio));
		System.out.println("--");
		
		int sum = v2_unsure + v2_correct + v2_incorrect;
		System.out.println("v2_unsure=" + v2_unsure + " v2_correct=" + v2_correct + " v2_incorrect=" + v2_incorrect);
		System.out.println("unsure=" + ((double) v2_unsure / sum) + " error=" + v2_incorrect + "/" + sum + " " + ((double) v2_incorrect / sum));
		System.out.println(((double) v2_correct) / (v2_correct + v2_incorrect));
		
		System.out.println("--");
		System.out.print(((double) correct) / (incorrect + correct));
		System.out.println(" " + ((double) v2_correct) / (v2_correct + v2_incorrect));
//		System.out.println(stringBuilder.toString());
		
//		for (dimension = 1; dimension <= Category.CATEGORY_COUNT; dimension++) {
//			correct = 0;
//			incorrect = 0;
//			for (int i = 0; i < imgPathList.size(); i++) {
//				int realResult = resultList.get(i);
//				if (realResult >= dimension) continue;
//
//				BufferedImage img = ImageIO.read(new File(imgPathList.get(i)));
//				double[] resultProbabilities = mainClassifier.classify(img, dimension);
//				int result = ClassifyUtils.getMaxIndex(resultProbabilities);
//				if (mainClassifier.isReliable(resultProbabilities)) {
//					if (result == realResult) {
//						correct++;
//					} else {
//						incorrect++;
//					}
//				}
//			}
//			System.out.println("dimensions=" + dimension + " " + ((double) correct) / (incorrect + correct));
//		}
		
	}

}
