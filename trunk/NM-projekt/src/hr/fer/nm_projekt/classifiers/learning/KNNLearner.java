package hr.fer.nm_projekt.classifiers.learning;

import hr.fer.nm_projekt.classifiers.KNNClassifier;
import hr.fer.nm_projekt.utilities.ClassifyUtils;
import hr.fer.nm_projekt.utilities.PCA;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KNNLearner {

	private static String TRAIN_FILE="data/train_images.txt";
	private static String[][] fileData = {{"data/features/density_train.txt","data/nn/knn_density.dat","4"},
							   			 {"data/features/histogram_train.txt","data/nn/knn_histogram.dat","6"},
							   			 {"data/features/profile_train.txt","data/nn/knn_profile.dat","4"}};

	public static KNNClassifier createKNN(String trainFile,String featuresFile,Integer k) throws IOException {
		
		List<String> trainNames = ClassifyUtils.loadStringListFromFile(new File(trainFile));
		List<double[]> trainData=ClassifyUtils.loadDoubleArrayListFromFile(new File(featuresFile));
		
		Map<String,List<double[]>> trainMap = new HashMap<String, List<double[]>>();
		Map<String,List<double[]>> normalizedTrainMap = new HashMap<String, List<double[]>>(); 
		
		for(int i=0;i<trainNames.size();i++){
			String filename=trainNames.get(i);
			String _class=filename.substring(12,13);
			
			double[] features=trainData.get(i);
			
			if(trainMap.containsKey(_class)){
				trainMap.get(_class).add(features);
			}else{
				List<double[]> featuresList = new LinkedList<double[]>();
				featuresList.add(features);
				trainMap.put(_class,featuresList);
			}
		}
		
		int minFeatures=Integer.MAX_VALUE;
		
		for(List<double[]> featureList : trainMap.values()){
			minFeatures=featureList.size()<minFeatures?featureList.size():minFeatures;
		}
		
		for(Map.Entry<String,List<double[]>> entry : trainMap.entrySet()){
			Collections.shuffle(entry.getValue());
			List<double[]> normalizedFeatureList = entry.getValue().subList(0, minFeatures);
			normalizedTrainMap.put(entry.getKey(),normalizedFeatureList);
		}
		
		KNNClassifier knn = new KNNClassifier(normalizedTrainMap);
		knn.setK(k);
		
		return knn;
	}
	
	public static void main(String[] args) throws IOException {
		
		for(String[] data : fileData){	
			KNNClassifier knn = createKNN(TRAIN_FILE,data[0],Integer.valueOf(data[2]));
			knn.saveTo(new File(data[1]));
			System.out.println(data[1]+" created");
		}
	}
}
