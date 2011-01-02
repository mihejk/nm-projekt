package hr.fer.nm_projekt.classifiers;

import hr.fer.nm_projekt.Category;
import hr.fer.nm_projekt.MainClassifier;
import hr.fer.nm_projekt.MainClassifierImpl;
import hr.fer.nm_projekt.classifiers.learning.KNNLearner;
import hr.fer.nm_projekt.utilities.ClassifyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

public class KNNClassifier implements Classifier,Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<String,List<DenseMatrix64F>> neighbours;
	
	private int n=-1;
	private int p=-1;
	private int tot=-1;
	
	private int K=3;
	
	private boolean DEBUG=false;
	
	public KNNClassifier() {
		// TODO Auto-generated constructor stub
	}
	
	public KNNClassifier(Map<String,List<double[]>> trainMap){
		
		this.p=trainMap.size();
		this.tot=0;
		
		neighbours=new HashMap<String, List<DenseMatrix64F>>(trainMap.size());
		
		for(Map.Entry<String,List<double[]>> entry : trainMap.entrySet()){
			
			List<DenseMatrix64F> featureMatrixList = new LinkedList<DenseMatrix64F>();
			
			for(double[] features : entry.getValue()){
				if(this.n==-1){
					this.n=features.length;
				}
				DenseMatrix64F a = new DenseMatrix64F(n,1);
				System.arraycopy(features, 0, a.data, 0, n);
				featureMatrixList.add(a);
			}
			this.tot+=featureMatrixList.size();
			neighbours.put(entry.getKey(),featureMatrixList);
		}
	}
	
	@Override
	public void classify(double[] input, int dimensions, double[] output) {
		
//		System.out.println("in:"+Arrays.toString(input));
		
		DenseMatrix64F inputMat = new DenseMatrix64F(n,1);
		System.arraycopy(input, 0, inputMat.data, 0, n);
		
		if(DEBUG){
			System.out.println("in:");
			inputMat.print();
		}
		
		List<StateError> neighbourList = new ArrayList<StateError>(this.tot);
		
		for(Map.Entry<String,List<DenseMatrix64F>> entry : neighbours.entrySet()){
			for(DenseMatrix64F state : entry.getValue()){
				DenseMatrix64F dif = new DenseMatrix64F(n,1);
				CommonOps.sub(state, inputMat, dif);
				double error=CommonOps.elementSumAbs(dif);
				neighbourList.add(new StateError(entry.getKey(), error));
			}
		}
		
		Collections.sort(neighbourList);
		
		double[] errArray = new double[Category.CATEGORY_COUNT];
		double sum=0;
		
		for(int i=0;i<this.K;i++){
			String _class=neighbourList.get(i)._class;
			double error=neighbourList.get(i).error;
			errArray[Category.fromString(_class)]+=(10000-error);
		}
		
		for(int i=0;i<errArray.length;i++){
			sum+=errArray[i];
		}
		
		for(int i=0;i<errArray.length;i++){
			errArray[i]/=sum;
		}
		
		System.arraycopy(errArray,0,output,0, dimensions);
	}

	@Override
	public void saveTo(File file) {
        ObjectOutputStream stream = null;
        try{
        	stream = new ObjectOutputStream(new FileOutputStream(file));
            stream.writeObject(this);
        } catch (IOException e) {
			e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
					stream.close();
				} catch (IOException e) {}
            }
        }
	}

	@Override
	public void loadFrom(File file) {
		
		ObjectInputStream stream = null;
        try {
        	stream = new ObjectInputStream(new FileInputStream(file));
        	
        	KNNClassifier knn = (KNNClassifier) stream.readObject();
        	this.setNeighbours(knn.getNeighbours());
        	this.setN(knn.getN());
        	this.setP(knn.getP());
        	this.setTot(knn.getTot());
        	this.setK(knn.getK());
        	
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (stream != null) {
                try {
					stream.close();
				} catch (IOException e) {}
            }
        }
	}
	
	private static int maxIndex(double[] array){
		int maxIndex=-1;
		double max=-1*Double.MAX_VALUE;
		for(int i=0;i<array.length;i++){
			if(array[i]>max){
				max=array[i];
				maxIndex=i;
			}
		}
		return maxIndex;
	}
	
	public Map<String, List<DenseMatrix64F>> getNeighbours() {
		return neighbours;
	}
	
	public void setNeighbours(Map<String, List<DenseMatrix64F>> neighbours) {
		this.neighbours = neighbours;
	}
	
	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getTot() {
		return tot;
	}

	public void setTot(int tot) {
		this.tot = tot;
	}

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
	}

	private class StateError implements Comparable<StateError>{
		
		public String _class;
		public double error;
		
		public StateError(String _class, double error) {
			this._class = _class;
			this.error = error;
		}

		@Override
		public int compareTo(StateError o) {
			return Double.valueOf(error).compareTo(o.error);
		}
		
		@Override
		public String toString() {
			return "("+_class+","+error+")";
		}
	}
	
	public static void main(String[] args) throws IOException {
	
		String[][] fileData = {{"data/features/density_test.txt","data/features/density_normalize.txt","data/nn/knn_density.dat"},
	   			 			   {"data/features/histogram_test.txt","data/features/histogram_normalize.txt","data/nn/knn_histogram.dat"},
	   			               {"data/features/profile_test.txt","data/features/profile_normalize.txt","data/nn/knn_profile.dat"}};
		
		for (String[] data : fileData) {
			System.out.println(data[2]);
			for(int k=1;k<50;k++){
				double res=test(data[0],data[1],data[2],k);
				System.out.println(k+"->"+res);
			}
		}
	}

	private static double test(String testFile,String normalizeFile,String classifierFile,int k) throws IOException {
		
		MainClassifier classifier = new MainClassifierImpl();
		
		KNNClassifier knn = new KNNClassifier();
		knn.loadFrom(new File(classifierFile));
		knn.setK(k);
		
		List<double[]> testData = ClassifyUtils.loadDoubleArrayListFromFile(new File(testFile));
		
		double[] output = new double[Category.CATEGORY_COUNT];
		int dim = Category.CATEGORY_COUNT;
		
		List<String> testNames = ClassifyUtils.loadStringListFromFile(new File("data/test_images.txt"));
		int suc = 0;
		
		for (int i = 0; i < testNames.size(); i++) {
			
//			System.out.println("******");

			double[] feature = testData.get(i);

			String exp = String.valueOf(testNames.get(i).charAt(12));

//			System.out.println(testNames.get(i));
//			System.out.println("knn input:"+Arrays.toString(feature));
			
			Arrays.fill(output, 0);
			knn.classify(feature, dim, output);
			
//			double[] classifierResult = classifier.classify(ImageIO.read(new File(testNames.get(i))), dim);
			
//			System.out.println("knn output:"+Arrays.toString(output));
//			System.out.println("cla output:"+Arrays.toString(classifierResult));

			int maxIndex = maxIndex(output);

			String res = Category.toString(maxIndex);
//			System.out.println(res);

			if (res.equals(exp)) {
				suc++;
			}
		}
		double result=((double) suc) / testNames.size();
//		System.out.println(result);
		return result;
	}	
}

