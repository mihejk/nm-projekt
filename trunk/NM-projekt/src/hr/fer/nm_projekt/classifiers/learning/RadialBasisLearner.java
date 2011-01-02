package hr.fer.nm_projekt.classifiers.learning;

import hr.fer.nm_projekt.classifiers.RadialBasisNetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.ejml.data.DenseMatrix64F;

public class RadialBasisLearner {
	
	private static DenseMatrix64F loadMarixFromFile(String fileName, String sizeFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(sizeFile));
		
		String[] sizeStr = reader.readLine().trim().split(" ");
		int rows = Integer.parseInt(sizeStr[0]);
		int cols = Integer.parseInt(sizeStr[1]);
		DenseMatrix64F res = new DenseMatrix64F(rows, cols);
		
		reader = new BufferedReader(new FileReader(fileName));
		for(int row = 0; row < rows; row++){
			String[] line = reader.readLine().trim().split(" ");
			for(int col = 0; col < cols; col++)
				res.set(row, col, Double.parseDouble(line[col]));
		}
		
		return res;
	}
	
	public static void main(String[] args) throws IOException {
		DenseMatrix64F centers = loadMarixFromFile("data/learning/centri.txt", "data/learning/centri_dim.txt");
		DenseMatrix64F weigths = loadMarixFromFile("data/learning/tezine.txt", "data/learning/tezine_dim.txt");
		
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("data/nn/rb_histogram.dat"));
		stream.writeObject(centers);
		stream.writeObject(new Double(0.5));
		stream.writeObject(weigths);
		stream.close();
		
		/*RadialBasisNetwork rbNN = new RadialBasisNetwork(20, 12);
		rbNN.loadFrom(new File("data/nn/rb_histogram.dat"));
		
		double[] input = new double[]{
				0.6240408511174743, 0.8334271129398652, 0.5656374191528796,
				0.4840983443061405, 0.6446540840688806, 0.12611515425639486,
				0.6418445106713878, 0.41627728151410487, 0.49679882517707874,
				0.5204592231472313, 0.3931789487601178, 0.36922748143753287,
				0.3508494212504845, 0.43895634492991636, 0.3626612857267626,
				0.4716267384113158, 0.5596985900323389, 0.5795900192936161,
				0.29367963659809376, 0.46563149101697215};
		double[] output = new double[20]; 
		rbNN.classify(input, 12, output);
		rbNN.saveTo(new File("data/nn/rb_test"));*/
	}
}
