package hr.fer.nm_projekt.utilities;

import hr.fer.nm_projekt.Category;
import hr.fer.nm_projekt.featureExtraction.Crossings;
import hr.fer.nm_projekt.featureExtraction.DensityFeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.FeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.HistogramFeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.HuInvariantMoments;
import hr.fer.nm_projekt.featureExtraction.ProfileFeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.RadialHistogramFeatureExtractor;
import hr.fer.nm_projekt.preprocessing.ImageBinarizer;
import hr.fer.nm_projekt.preprocessing.ImageLineThinning;
import hr.fer.nm_projekt.preprocessing.ImageNoiseRemover;
import hr.fer.nm_projekt.preprocessing.ImageScaler;
import hr.fer.nm_projekt.preprocessing.ImageStainRemover;
import hr.fer.nm_projekt.preprocessing.ImageTransformer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

public class ClassifyUtils {
	
	public static void createTrainigAndTestImageList(File file, double sizeOfTestSet)  throws IOException {
		
		// load images
		System.out.println("Loading images...");
		Map<Integer, List<String>> imageMap = new HashMap<Integer, List<String>>();
		loadImages(file, imageMap);
		
		// split training and test set
		System.out.println("Splitting training and test set...");
		List<String> trainImageList = new ArrayList<String>();
		List<Integer> trainCategoryList = new ArrayList<Integer>();
		List<String> testImageList = new ArrayList<String>();
		List<Integer> testCategoryList = new ArrayList<Integer>();
		splitSets(imageMap, sizeOfTestSet, trainImageList, trainCategoryList, testImageList, testCategoryList);
		saveIntegerListToFile(trainCategoryList, new File("data/train_categories.txt"));
		saveIntegerListToFile(testCategoryList, new File("data/test_categories.txt"));
		saveStringListToFile(trainImageList, new File("data/train_images.txt"));
		saveStringListToFile(testImageList, new File("data/test_images.txt"));
		saveDoubleArrayListToFile(integerListToDoubleArrayList(trainCategoryList, Category.CATEGORY_COUNT), new File("data/train_categories_matlab.txt"));
		saveDoubleArrayListToFile(integerListToDoubleArrayList(testCategoryList, Category.CATEGORY_COUNT), new File("data/test_categories_matlab.txt"));
	}
	
	public static void updateSamples() throws IOException {
		
		// load images
		List<BufferedImage> trainImageList = new ArrayList<BufferedImage>();
		List<BufferedImage> testImageList = new ArrayList<BufferedImage>();
		for (String trainImage : loadStringListFromFile(new File("data/train_images.txt"))) {
			trainImageList.add(ImageIO.read(new File(trainImage)));
		}
		for (String trainImage : loadStringListFromFile(new File("data/test_images.txt"))) {
			testImageList.add(ImageIO.read(new File(trainImage)));
		}
		
		// preprocess images
		System.out.println("Preprocessing images...");
		List<BufferedImage> preprocessedTrainImageList = new ArrayList<BufferedImage>();
		List<BufferedImage> thinLineTrainImageList = new ArrayList<BufferedImage>();
		List<BufferedImage> preprocessedTestImageList = new ArrayList<BufferedImage>();
		List<BufferedImage> thinLineTestImageList = new ArrayList<BufferedImage>();
		preprocessImages(trainImageList, preprocessedTrainImageList, thinLineTrainImageList);
		preprocessImages(testImageList, preprocessedTestImageList, thinLineTestImageList);
		
		// extract crossing features
		System.out.println("Extracting crossing features...");
		File crossingTrainFile = new File("data/features/crossing_train.txt");
		File crossingTestFile = new File("data/features/crossing_test.txt");
		File crossingPcaFile = new File("data/pca/crossing_pca.dat");
		File crossingNormalizeFile = new File("data/features/crossing_normalize.txt");
		extractFeaturesWithPCA(new Crossings(), thinLineTrainImageList, thinLineTestImageList, 20, crossingTrainFile, crossingTestFile, crossingPcaFile, crossingNormalizeFile);
		
		// extract density features
		System.out.println("Extracting density features...");
		File densityTrainFile = new File("data/features/density_train.txt");
		File densityTestFile = new File("data/features/density_test.txt");
		File densityPcaFile = new File("data/pca/density_pca.dat");
		File densityNormalizeFile = new File("data/features/density_normalize.txt");
		extractFeaturesWithPCA(new DensityFeatureExtractor(20, 20), preprocessedTrainImageList, preprocessedTestImageList, 20, densityTrainFile, densityTestFile, densityPcaFile, densityNormalizeFile);
		
		// extract histogram features
		System.out.println("Extracting histogram features...");
		File histogramTrainFile = new File("data/features/histogram_train.txt");
		File histogramTestFile = new File("data/features/histogram_test.txt");
		File histogramPcaFile = new File("data/pca/histogram_pca.dat");
		File histogramNormalizeFile = new File("data/features/histogram_normalize.txt");
		extractFeaturesWithPCA(new HistogramFeatureExtractor(), preprocessedTrainImageList, preprocessedTestImageList, 20, histogramTrainFile, histogramTestFile, histogramPcaFile, histogramNormalizeFile);
		
		// extract huInvariant features
		System.out.println("Extracting huInvariant features...");
		File huInvariantTrainFile = new File("data/features/huInvariant_train.txt");
		File huInvariantTestFile = new File("data/features/huInvariant_test.txt");
		File huInvariantNormalizeFile = new File("data/features/huInvariant_normalize.txt");
		extractFeatures(new HuInvariantMoments(), thinLineTrainImageList, thinLineTestImageList, huInvariantTrainFile, huInvariantTestFile, huInvariantNormalizeFile);
		
		// extract profile features
		System.out.println("Extracting profile features...");
		File profileTrainFile = new File("data/features/profile_train.txt");
		File profileTestFile = new File("data/features/profile_test.txt");
		File profilePcaFile = new File("data/pca/profile_pca.dat");
		File profileNormalizeFile = new File("data/features/profile_normalize.txt");
		extractFeaturesWithPCA(new ProfileFeatureExtractor(), thinLineTrainImageList, thinLineTestImageList, 20, profileTrainFile, profileTestFile, profilePcaFile, profileNormalizeFile);
		
		// extract radial features
		System.out.println("Extracting radial features...");
		File radialTrainFile = new File("data/features/radial_train.txt");
		File radialTestFile = new File("data/features/radial_test.txt");
		File radialNormalizeFile = new File("data/features/radial_normalize.txt");
		extractFeatures(new RadialHistogramFeatureExtractor(20), preprocessedTrainImageList, preprocessedTestImageList, radialTrainFile, radialTestFile, radialNormalizeFile);
			
		System.out.println("Done");
	}
	
	protected static void extractFeatures(FeatureExtractor featureExtractor, List<BufferedImage> trainList, List<BufferedImage> testList, 
			File trainFeaturesFile, File testFeaturesFile, File normalizeFile) throws IOException {
		
		// load features for train data
		List<double[]> trainFeatures = new ArrayList<double[]>();
		for (BufferedImage image : trainList) {
			trainFeatures.add(featureExtractor.extract(image));
		}
		
		// load features for test data
		List<double[]> testFeatures = new ArrayList<double[]>();
		for (BufferedImage image : testList) {
			testFeatures.add(featureExtractor.extract(image));
		}
		
		// get min and max for normalize
		List<double[]> maxMinList = new ArrayList<double[]>();
		double[] firstFeature = trainFeatures.get(0);
		int featureCount = firstFeature.length;
		for (int i = 0; i < featureCount; i++) {
			double[] maxMin = new double[] { firstFeature[i], firstFeature[i] };
			maxMinList.add(maxMin);
		}
		for (int i = 1; i < trainFeatures.size(); i++) {
			double[] features = trainFeatures.get(i);
			for (int j = 0; j < featureCount; j++) {
				double[] maxMin = maxMinList.get(j);
				if (features[j] > maxMin[0]) {
					maxMin[0] = features[j];
				}
				if (features[j] < maxMin[1]) {
					maxMin[1] = features[j];
				}
			}
		}
		
		// normalize
		normalize(maxMinList, trainFeatures);
		normalize(maxMinList, testFeatures);
		
		// save data to files
		saveDoubleArrayListToFile(maxMinList, normalizeFile);
		saveDoubleArrayListToFile(trainFeatures, trainFeaturesFile);
		saveDoubleArrayListToFile(testFeatures, testFeaturesFile);
	}
	
	private static void extractFeaturesWithPCA(FeatureExtractor featureExtractor, List<BufferedImage> trainList, List<BufferedImage> testList, int pcaDimension, 
			File trainFeaturesFile, File testFeaturesFile, File pcaFile, File normalizeFile) throws IOException {
		
		// load features for train data
		List<double[]> trainFeatures = new ArrayList<double[]>();
		for (BufferedImage image : trainList) {
			trainFeatures.add(featureExtractor.extract(image));
		}
		
		// load features for test data
		List<double[]> testFeatures = new ArrayList<double[]>();
		for (BufferedImage image : testList) {
			testFeatures.add(featureExtractor.extract(image));
		}
		
		// create PCA
		PCA pca = new PCA();
		pca.setup(trainFeatures.size(), trainFeatures.get(0).length);
		for (double[] features : trainFeatures) {
			pca.addSample(features);
		}
		pca.computeBasis(pcaDimension);
		
		// transform train data with PCA
		for (int i = 0; i < trainFeatures.size(); i++) {
			double[] features = trainFeatures.get(i);
			double[] newFeatures = pca.sampleToEigenSpace(features);
			trainFeatures.set(i, newFeatures);
		}
		
		// transform test data with PCA
		for (int i = 0; i < testFeatures.size(); i++) {
			double[] features = testFeatures.get(i);
			double[] newFeatures = pca.sampleToEigenSpace(features);
			testFeatures.set(i, newFeatures);
		}
		
		// get min and max for normalize
		List<double[]> maxMinList = new ArrayList<double[]>();
		double[] firstFeature = trainFeatures.get(0);
		int featureCount = firstFeature.length;
		for (int i = 0; i < featureCount; i++) {
			double[] maxMin = new double[] { firstFeature[i], firstFeature[i] };
			maxMinList.add(maxMin);
		}
		for (int i = 1; i < trainFeatures.size(); i++) {
			double[] features = trainFeatures.get(i);
			for (int j = 0; j < featureCount; j++) {
				double[] maxMin = maxMinList.get(j);
				if (features[j] > maxMin[0]) {
					maxMin[0] = features[j];
				}
				if (features[j] < maxMin[1]) {
					maxMin[1] = features[j];
				}
			}
		}
		
		// normalize
		normalize(maxMinList, trainFeatures);
		normalize(maxMinList, testFeatures);
		
		// save data to files
		savePCA(pca, pcaFile);
		saveDoubleArrayListToFile(maxMinList, normalizeFile);
		saveDoubleArrayListToFile(trainFeatures, trainFeaturesFile);
		saveDoubleArrayListToFile(testFeatures, testFeaturesFile);
	}
	
	public static void normalize(List<double[]> maxMinList, List<double[]> featureList) {
		for (double[] feature : featureList) {
			normalize(maxMinList, feature);
		}
	}
	
	public static void normalize(List<double[]> maxMinList, double[] data) {
		for (int i = 0; i < data.length; i++) {
			double oldValue = data[i];
			double[] maxMin = maxMinList.get(i);
			double max = maxMin[0];
			double min = maxMin[1];
			data[i] =(oldValue - min) / (max - min);
		}
	}

	protected static void preprocessImages(List<BufferedImage> imageList, List<BufferedImage> preprocessedImageList, List<BufferedImage> thinLineImageList) {
		List<ImageTransformer> normalFeatureTransformers = new LinkedList<ImageTransformer>();
		normalFeatureTransformers.add(new ImageBinarizer());
		normalFeatureTransformers.add(new ImageNoiseRemover());
		normalFeatureTransformers.add(new ImageStainRemover());
		normalFeatureTransformers.add(new ImageScaler(50, 50));
		
		List<ImageTransformer> thinLineTransformers = new LinkedList<ImageTransformer>();
		thinLineTransformers.add(new ImageLineThinning());
		
		for (BufferedImage bufferedImage : imageList) {
			for (ImageTransformer transformer : normalFeatureTransformers) {
				bufferedImage = transformer.transform(bufferedImage);
			}
			preprocessedImageList.add(bufferedImage);
			for (ImageTransformer transformer : thinLineTransformers) {
				bufferedImage = transformer.transform(bufferedImage);
			}
			thinLineImageList.add(bufferedImage);
		}
	}
	
	private static void splitSets(Map<Integer, List<String>> imageMap, double sizeOfTestSet, List<String> trainImageList, List<Integer> trainCategoryList, 
			List<String> testImageList, List<Integer> testCategoryList) {
		
		for (int category : imageMap.keySet()) {
			List<String> categoryImageList = imageMap.get(category);
			Collections.shuffle(categoryImageList);
			int testSetSize = (int) (categoryImageList.size() * sizeOfTestSet);
			for (int i = 0; i < testSetSize; i++) {
				testImageList.add(categoryImageList.get(i));
				testCategoryList.add(category);
			}
			for (int i = testSetSize; i < categoryImageList.size(); i++) {
				trainImageList.add(categoryImageList.get(i));
				trainCategoryList.add(category);
			}
		}
		
		Random random = new Random();
		
        // shuffle train set
        for (int i = trainImageList.size(); i>1; i--) {
        	int index1 = i - 1;
        	int index2 = random.nextInt(i);
            Collections.swap(trainImageList, index1, index2);
            Collections.swap(trainCategoryList, index1, index2);
        }
		
		// shuffle test set
        for (int i = testImageList.size(); i>1; i--) {
        	int index1 = i - 1;
        	int index2 = random.nextInt(i);
            Collections.swap(testImageList, index1, index2);
            Collections.swap(testCategoryList, index1, index2);
        }
	}
	
	private static void loadImages(File file, Map<Integer, List<String>> imageMap) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			
			while(true) {
				String folder = in.readLine();
				if (folder == null) {
					break;
				}
				
				String categoryString = folder.substring(folder.lastIndexOf("/") + 1);
				int category = Category.fromString(categoryString);
				
				List<String> imageList = imageMap.get(category);
				if (imageList == null) {
					imageList = new ArrayList<String>();
					imageMap.put(category, imageList);
				}
				
				int fileCount = Integer.parseInt(in.readLine());
				for (int i = 0; i < fileCount; i++) {
					String filename = in.readLine();
					imageList.add("data/" + folder + "/" + filename);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	private static List<double[]> integerListToDoubleArrayList(List<Integer> integerList, int maxIndex) {
		List<double[]> arrayList = new ArrayList<double[]>();
		for (int value : integerList) {
			double[] array = new double[maxIndex];
			array[value] = 1;
			arrayList.add(array);
		}
		return arrayList;
	}
	
	private static void saveIntegerListToFile(List<Integer> integerList, File file) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file));
			for (Integer integer : integerList) {
				out.append(integer.toString());
				out.newLine();
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static List<Integer> loadIntegerListFromFile(File file) throws IOException {
		BufferedReader in = null;
		List<Integer> integerList = new ArrayList<Integer>();
		try {
			in = new BufferedReader(new FileReader(file));
			while(true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				integerList.add(Integer.parseInt(line));
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return integerList;
	}
	
	private static void saveStringListToFile(List<String> stringList, File file) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file));
			for (String string : stringList) {
				out.append(string);
				out.newLine();
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static List<String> loadStringListFromFile(File file) throws IOException {
		BufferedReader in = null;
		List<String> integerList = new ArrayList<String>();
		try {
			in = new BufferedReader(new FileReader(file));
			while(true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				integerList.add(line);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return integerList;
	}
	
	protected static void saveDoubleArrayListToFile(List<double[]> data, File file) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file));
			for (double[] array : data) {
				for (int i = 0; i < array.length; i++) {
					out.append(String.valueOf(array[i]));
					if (i != array.length - 1) {
						out.append(" ");
					}
				}
				out.newLine();
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static List<double[]> loadDoubleArrayListFromFile(File file) throws IOException {
		BufferedReader in = null;
		List<double[]> doubleArrayList = new ArrayList<double[]>();
		try {
			in = new BufferedReader(new FileReader(file));
			while(true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				String[] stringArray = line.split(" ");
				double[] doubleArray = new double[stringArray.length];
				for (int i = 0; i < stringArray.length; i++) {
					doubleArray[i] = Double.parseDouble(stringArray[i]);
				}
				doubleArrayList.add(doubleArray);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return doubleArrayList;
	}
	
	private static void savePCA(PCA pca, File file) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
        try {
            stream.writeObject(pca);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
	}
	
	public static PCA loadPCA(File file) throws IOException {
		PCA pca = null;
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
		try {
			pca = (PCA) stream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return pca;
	}
	
	public static int getMaxIndex(double[] array) {
		int maxIndex = 0;
		for (int i = 1; i < array.length; i++)
			if( array[i] > array[maxIndex] ) maxIndex = i;
		return maxIndex;
	}
	
	public static double getTwoBestRatio( double[] array ) {
		int m1 = getMaxIndex( array );
		int m2 = (m1+1)%array.length;
		for( int i = 0; i < array.length; ++i )
			if( i != m1 && array[i] > array[m2] ) m2 = i;
		
		if( array[m2] < 1e-3 ) return 1000;
		
		return array[m1]/array[m2];
	}
	
	public static void mulArrayElems(double[] array,double factor,int length){
		for (int i = 0; i < length; i++) {
			array[i]*=factor;
		}
	}
	
	public static void addArray1ToArray2(double[] array1, double[] array2, int length) {
		for (int i = 0; i < length; i++) {
			array2[i] += array1[i];
		}
	}
	
	public static void main(String[] args) throws IOException {
//		LearningUtils.createTrainigAndTestImageList(new File("data/selected images.txt"), 0.2);
		ClassifyUtils.updateSamples();
	}
	
}