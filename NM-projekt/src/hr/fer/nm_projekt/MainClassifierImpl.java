package hr.fer.nm_projekt;

import hr.fer.nm_projekt.classifiers.Classifier;
import hr.fer.nm_projekt.classifiers.KNNClassifier;
import hr.fer.nm_projekt.classifiers.MultilayerPerceptron;
import hr.fer.nm_projekt.classifiers.RadialBasisNetwork;
import hr.fer.nm_projekt.featureExtraction.Crossings;
import hr.fer.nm_projekt.featureExtraction.DensityFeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.FeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.HistogramFeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.ProfileFeatureExtractor;
import hr.fer.nm_projekt.featureExtraction.RadialHistogramFeatureExtractor;
import hr.fer.nm_projekt.preprocessing.BlankImage;
import hr.fer.nm_projekt.preprocessing.ImageBinarizer;
import hr.fer.nm_projekt.preprocessing.ImageLineThinning;
import hr.fer.nm_projekt.preprocessing.ImageNoiseRemover;
import hr.fer.nm_projekt.preprocessing.ImageScaler;
import hr.fer.nm_projekt.preprocessing.ImageStainRemover;
import hr.fer.nm_projekt.preprocessing.ImageTransformer;
import hr.fer.nm_projekt.utilities.ClassifyUtils;
import hr.fer.nm_projekt.utilities.PCA;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainClassifierImpl implements MainClassifier {
	
	private final BlankImage blankImage;
	private final ImageTransformer binarizer;
	private final ImageTransformer noiseRemover;
	private final ImageTransformer stainRemover;
	private final ImageTransformer scaler;
	private final ImageTransformer lineThinning;
	private final Classifier crossingPerceptronClassifier;
	private final Classifier densityPerceptronClassifier;
	private final Classifier histogramPerceptronClassifier;
	private final Classifier profilePerceptronClassifier;
	private final Classifier radialPerceptronClassifier;
	private final Classifier crossingRbClassifier;
	private final Classifier densityRbClassifier;
	private final Classifier histogramRbClassifier;
//	private final Classifier huInvariantRbClassifier;
	private final Classifier profileRbClassifier;
	private final Classifier radialRbClassifier;
	private final Classifier histogramKNNClassifier;
	private final Classifier profileKNNClassifier;
	private final Classifier densityKNNClassifier;
	private final FeatureExtractor crossingFeatureExtractor;
	private final FeatureExtractor densityFeatureExtractor;
	private final FeatureExtractor histogramFeatureExtractor;
//	private final FeatureExtractor huInvariantFeatureExtractor;
	private final FeatureExtractor profileFeatureExtractor;
	private final FeatureExtractor radialFeatureExtractor;
	private final PCA crossingPCA;
	private final PCA densityPCA;
	private final PCA histogramPCA;
	private final PCA profilePCA;
	private final List<double[]> crossingNormalize;
	private final List<double[]> densityNormalize;
	private final List<double[]> histogramNormalize;
//	private final List<double[]> huInvariantNormalize;
	private final List<double[]> profileNormalize;
	private final List<double[]> radialNormalize;
	private final double[] buffer;
	
	public MainClassifierImpl() throws IOException {
		
		// create transformers
		blankImage = new BlankImage(0.995);
		binarizer = new ImageBinarizer();
		noiseRemover = new ImageNoiseRemover();
		stainRemover = new ImageStainRemover();
		scaler = new ImageScaler(50, 50);
		lineThinning = new ImageLineThinning();
		
		// create classifiers
		crossingPerceptronClassifier = new MultilayerPerceptron(20, 10, 12);
		densityPerceptronClassifier = new MultilayerPerceptron(20, 10, 12);
		histogramPerceptronClassifier = new MultilayerPerceptron(20, 10, 12);
		profilePerceptronClassifier = new MultilayerPerceptron(20, 10, 12);
		radialPerceptronClassifier = new MultilayerPerceptron(20, 10, 12);
		crossingRbClassifier = new RadialBasisNetwork(20, 12);
		densityRbClassifier = new RadialBasisNetwork(20, 12);
		histogramRbClassifier = new RadialBasisNetwork(20, 12);
//		huInvariantRbClassifier = new RadialBasisNetwork(6, 12);
		profileRbClassifier = new RadialBasisNetwork(20, 12);
		radialRbClassifier = new RadialBasisNetwork(20, 12);
		histogramKNNClassifier = new KNNClassifier();
		profileKNNClassifier = new KNNClassifier();
		densityKNNClassifier = new KNNClassifier();
		
		// load configurations from file
		crossingPerceptronClassifier.loadFrom(new File("data/nn/perceptron_crossing.dat"));
		densityPerceptronClassifier.loadFrom(new File("data/nn/perceptron_density.dat"));
		histogramPerceptronClassifier.loadFrom(new File("data/nn/perceptron_histogram.dat"));
		profilePerceptronClassifier.loadFrom(new File("data/nn/perceptron_profile.dat"));
		radialPerceptronClassifier.loadFrom(new File("data/nn/perceptron_radial.dat"));
		crossingRbClassifier.loadFrom(new File("data/nn/rb_crossing.dat"));
		densityRbClassifier.loadFrom(new File("data/nn/rb_density.dat"));
		histogramRbClassifier.loadFrom(new File("data/nn/rb_histogram.dat"));
//		huInvariantRbClassifier.loadFrom(new File("data/nn/rb_huInvariant.dat"));
		profileRbClassifier.loadFrom(new File("data/nn/rb_profile.dat"));
		radialRbClassifier.loadFrom(new File("data/nn/rb_radial.dat"));
		histogramKNNClassifier.loadFrom(new File("data/nn/knn_histogram.dat"));
		profileKNNClassifier.loadFrom(new File("data/nn/knn_profile.dat"));
		densityKNNClassifier.loadFrom(new File("data/nn/knn_density.dat"));
		
		// create feature extractors
		crossingFeatureExtractor = new Crossings();
		densityFeatureExtractor = new DensityFeatureExtractor(20, 20);
		histogramFeatureExtractor = new HistogramFeatureExtractor();
//		huInvariantFeatureExtractor = new HuInvariantMoments();
		profileFeatureExtractor = new ProfileFeatureExtractor();
		radialFeatureExtractor = new RadialHistogramFeatureExtractor(20);
		
		// load configurations of PCA
		crossingPCA = ClassifyUtils.loadPCA(new File("data/pca/crossing_pca.dat"));
		densityPCA = ClassifyUtils.loadPCA(new File("data/pca/density_pca.dat"));
		histogramPCA = ClassifyUtils.loadPCA(new File("data/pca/histogram_pca.dat"));
		profilePCA = ClassifyUtils.loadPCA(new File("data/pca/profile_pca.dat"));
		
		// load data for normalization
		crossingNormalize = ClassifyUtils.loadDoubleArrayListFromFile(new File("data/features/crossing_normalize.txt"));
		densityNormalize = ClassifyUtils.loadDoubleArrayListFromFile(new File("data/features/density_normalize.txt"));
		histogramNormalize = ClassifyUtils.loadDoubleArrayListFromFile(new File("data/features/histogram_normalize.txt"));
//		huInvariantNormalize = ClassifyUtils.loadDoubleArrayListFromFile(new File("data/features/huInvariant_normalize.txt"));
		profileNormalize = ClassifyUtils.loadDoubleArrayListFromFile(new File("data/features/profile_normalize.txt"));
		radialNormalize = ClassifyUtils.loadDoubleArrayListFromFile(new File("data/features/radial_normalize.txt"));
		
		// create buffer
		buffer = new double[Category.CATEGORY_COUNT];
	}
	
	@Override
	public double[] classify(BufferedImage image, int dimensions) {;
		double outputDim[] = new double[dimensions];
		
		// preprocess image
		BufferedImage imageAfterBinarizer = binarizer.transform(image);
		if (blankImage.isBlank(imageAfterBinarizer)) {
			outputDim[Category.EMPTY] = 1;
			return outputDim;
		}
		BufferedImage imageAfterNoiseRemover = noiseRemover.transform(imageAfterBinarizer);
		BufferedImage imageAfterStainRemover = stainRemover.transform(imageAfterNoiseRemover);
		BufferedImage imageAfterScaler = scaler.transform(imageAfterStainRemover);
		BufferedImage imageAfterLineThinning = lineThinning.transform(imageAfterScaler);
		
		// get features
		double[] crossingFeature = crossingPCA.sampleToEigenSpace(crossingFeatureExtractor.extract(imageAfterLineThinning));
		double[] densityFeature = densityPCA.sampleToEigenSpace(densityFeatureExtractor.extract(imageAfterScaler));
		double[] histogramFeature = histogramPCA.sampleToEigenSpace(histogramFeatureExtractor.extract(imageAfterScaler));
//		double[] huInvariantFeature = huInvariantFeatureExtractor.extract(imageAfterLineThinning);
		double[] profileFeature = profilePCA.sampleToEigenSpace(profileFeatureExtractor.extract(imageAfterLineThinning));
		double[] radialFeature = radialFeatureExtractor.extract(imageAfterScaler);
		
		// normalize
		ClassifyUtils.normalize(crossingNormalize, crossingFeature);
		ClassifyUtils.normalize(densityNormalize, densityFeature);
		ClassifyUtils.normalize(histogramNormalize, histogramFeature);
//		ClassifyUtils.normalize(huInvariantNormalize, huInvariantFeature);
		ClassifyUtils.normalize(profileNormalize, profileFeature);
		ClassifyUtils.normalize(radialNormalize, radialFeature);
		
		boolean[] bool = new boolean[14];
		bool[1] = true;
		bool[2] = true;
		bool[3] = true;
		bool[4] = true;
		bool[5] = true;
		bool[6] = true;
		bool[7] = true;
		bool[8] = true;
		bool[9] = true;
		bool[10] = true;
		bool[11] = true;
		bool[12] = true;
		bool[13] = true;
		
		
		// classify
		crossingPerceptronClassifier.classify(crossingFeature, dimensions, buffer); // 1
		if (bool[1]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		densityPerceptronClassifier.classify(densityFeature, dimensions, buffer); // 2
		if (bool[2]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		histogramPerceptronClassifier.classify(histogramFeature, dimensions, buffer); // 3
		if (bool[3]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		profilePerceptronClassifier.classify(profileFeature, dimensions, buffer); // 4
		if (bool[4]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		radialPerceptronClassifier.classify(radialFeature, dimensions, buffer); // 5
		if (bool[5]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
//
		crossingRbClassifier.classify(crossingFeature, dimensions, buffer); // 6
		if (bool[6]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		densityRbClassifier.classify(densityFeature, dimensions, buffer); // 7
		if (bool[7]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		histogramRbClassifier.classify(histogramFeature, dimensions, buffer); // 8
		if (bool[8]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
//		huInvariantRbClassifier.classify(huInvariantFeature, dimensions, buffer);
//		ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		profileRbClassifier.classify(profileFeature, dimensions, buffer); // 9
		if (bool[9]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		radialRbClassifier.classify(radialFeature, dimensions, buffer); // 10
		if (bool[10]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
	
		histogramKNNClassifier.classify(histogramFeature, dimensions, buffer); // 11
//		ClassifyUtils.mulArrayElems(buffer, 0.5, dimensions);
		if (bool[11]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
	
		densityKNNClassifier.classify(densityFeature, dimensions, buffer); // 12
		if (bool[12]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);

		profileKNNClassifier.classify(profileFeature, dimensions, buffer); // 13
//		ClassifyUtils.mulArrayElems(buffer, 0.5, dimensions);
		if (bool[13]) ClassifyUtils.addArray1ToArray2(buffer, outputDim, dimensions);
		
		double sum = 0;
		for (int i = 0; i < dimensions; i++) {
			sum += outputDim[i];
		}
		for (int i = 0; i < dimensions; i++) {
			outputDim[i] = outputDim[i] / sum;
		}
		
		return outputDim;
	}
	
	@Override
	public boolean isReliable(double[] classification) {
		double ratio = ClassifyUtils.getTwoBestRatio(classification);
		double maxProbability = classification[ClassifyUtils.getMaxIndex(classification)];
		return ratio > 1.75 || maxProbability > 0.53;
	}

}
