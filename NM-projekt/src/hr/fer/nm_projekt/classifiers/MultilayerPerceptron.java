package hr.fer.nm_projekt.classifiers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MultilayerPerceptron implements Classifier, Serializable {
	
	private static final long serialVersionUID = 1L;
	protected final int inputLayerDim;
	protected final int hiddenLayerDim;
	protected final int outputLayerDim;
	protected final double[][] w1;
	protected final double[] b1;
	protected final double[][] w2;
	protected final double[] b2;
	
	private final double[] y1;
	
	public MultilayerPerceptron(int inputLayerDim, int hiddenLayerDims, int outputLayerDim) {
		this.inputLayerDim = inputLayerDim;
		this.hiddenLayerDim = hiddenLayerDims;
		this.outputLayerDim = outputLayerDim;
		
		w1 = new double[hiddenLayerDim][inputLayerDim];
		b1 = new double[hiddenLayerDim];
		w2 = new double[outputLayerDim][hiddenLayerDim];
		b2 = new double[outputLayerDim];
		y1 = new double[hiddenLayerDim];
	}
	
	@Override
	public void classify(double[] input, int dimensions, double[] output) {
		getOutput(input, output);
		
//		double[] perceptronOutput = new double[outputLayerDim];
//		getOutput(input, perceptronOutput);
//		double sum = 0;
//		for (int i = 0; i < dimensions; i++) {
//			sum += perceptronOutput[i];
//		}
//		for (int i = 0; i < dimensions; i++) {
//			output[i] = perceptronOutput[i] / sum;
//		}
	}
	
	public void getOutput(double[] input, double[] output) {
		for (int i = 0; i < hiddenLayerDim; i++) {
			double value = b1[i];
			for (int j = 0; j < inputLayerDim; j++) {
				value += w1[i][j] * input[j];
			}
			y1[i] = 1.0 / (1.0 + Math.exp(-value));
		}
		for (int i = 0; i < outputLayerDim; i++) {
			double value = b2[i];
			for (int j = 0; j < hiddenLayerDim; j++) {
				value += w2[i][j] * y1[j];
			}
			output[i] = 1.0 / (1.0 + Math.exp(-value));
		}
	}
	
	@Override
	public void saveTo(File file) {
        ObjectOutputStream stream = null;
        try {
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
        	MultilayerPerceptron multilayerPerceptron = (MultilayerPerceptron) stream.readObject();
        	if (inputLayerDim != multilayerPerceptron.inputLayerDim) {
        		System.out.println("Invalid input layer dimension");
        	} else if (hiddenLayerDim != multilayerPerceptron.hiddenLayerDim) {
        		System.out.println("Invalid hidden layer dimension");
        	} else if (outputLayerDim != multilayerPerceptron.outputLayerDim) {
        		System.out.println("Invalid output layer dimension");
        	} else {
        		for (int i = 0; i < w1.length; i++) {
        			System.arraycopy(multilayerPerceptron.w1[i], 0, w1[i], 0, w1[i].length);
        		}
        		System.arraycopy(multilayerPerceptron.b1, 0, b1, 0, b1.length);
        		for (int i = 0; i < w2.length; i++) {
        			System.arraycopy(multilayerPerceptron.w2[i], 0, w2[i], 0, w2[i].length);
        		}
        		System.arraycopy(multilayerPerceptron.b2, 0, b2, 0, b2.length);
        	}
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

}
