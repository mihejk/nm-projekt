package hr.fer.nm_projekt.classifiers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;

public class RadialBasisNetwork implements Classifier {

	protected final int inputLayerDim;
	protected int hiddenLayerDim;
	protected final int outputLayerDim;
	protected DenseMatrix64F weights;
	protected DenseMatrix64F[] centers;
	protected double[] centerBiases;
	protected double distribution;

	public RadialBasisNetwork(int inputLayerDim, int outputLayerDim) {
		super();
		this.inputLayerDim = inputLayerDim;
		this.outputLayerDim = outputLayerDim;
	}

	@Override
	public void classify(double[] input, int dimensions, double[] output) {
		getOutput(input, output);
		for (int i = 0; i < dimensions; i++) {
			if (output[i] < 0)
				output[i] = 0;
		}
		
//		double[] rbOutput = new double[outputLayerDim];
//		getOutput(input, rbOutput);
//
//		double sum = 0;
//		for (int i = 0; i < dimensions; i++) {
//			if (rbOutput[i] < 0)
//				rbOutput[i] = 0;
//			sum += rbOutput[i];
//		}
//		sum = 1;
//		for (int i = 0; i < dimensions; i++) {
//			output[i] = rbOutput[i] / sum;
//		}
	}

	@Override
	public void saveTo(File file) {

		DenseMatrix64F centers = new DenseMatrix64F(inputLayerDim + 1, hiddenLayerDim);
		for (int center = 0; center < hiddenLayerDim; center++) {
			for (int input = 0; input < this.inputLayerDim; input++)
				centers.set(input, center, this.centers[center].get(input));
			
			centers.set(inputLayerDim, center, centerBiases[center]);
		}
		
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new FileOutputStream(file));
			stream.writeObject(centers);
			stream.writeObject(new Double(0.5));
			stream.writeObject(weights);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public void loadFrom(File file) {
		ObjectInputStream stream = null;
		try {
			stream = new ObjectInputStream(new FileInputStream(file));

			DenseMatrix64F centers = (DenseMatrix64F) stream.readObject();
			this.hiddenLayerDim = centers.getNumCols();
			this.centers = new DenseMatrix64F[this.hiddenLayerDim];
			this.centerBiases = new double[this.hiddenLayerDim];
			
			for (int center = 0; center < this.hiddenLayerDim; center++) {
				this.centers[center] = new DenseMatrix64F(this.inputLayerDim, 1);
				for (int input = 0; input < this.inputLayerDim; input++)
					this.centers[center].set(input, centers.get(input, center));
				
				this.centerBiases[center] = centers.get(this.inputLayerDim, center);
			}

			this.distribution = (Double) stream.readObject();
			this.weights = (DenseMatrix64F) stream.readObject();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public void getOutput(double[] input, double[] output) {
		DenseMatrix64F inputVector = new DenseMatrix64F(input.length, 1, true,
				input);
		DenseMatrix64F result = null;

		result = calculateHiddenLayer(inputVector);
		result = calculateOutputLayer(result);

		System.arraycopy(result.data, 0, output, 0, outputLayerDim);
	}

	private DenseMatrix64F calculateHiddenLayer(DenseMatrix64F input) {
		//double alpha = -0.5 * (Math.pow(distribution, -2.0));

		DenseMatrix64F x = new DenseMatrix64F(input.getNumRows(), 1);
		System.arraycopy(input.data, 0, x.data, 0, input.getNumRows());

		DenseMatrix64F result = new DenseMatrix64F(this.hiddenLayerDim, 1);
		DenseMatrix64F distance = new DenseMatrix64F(input.getNumRows(), 1);

		for (int i = 0; i < hiddenLayerDim; i++) {
			CommonOps.sub(x, centers[i], distance);
			double currentR = NormOps.normF(distance) * centerBiases[i];

			result.set(i, Math.exp(-currentR * currentR));
		}

		return result;
	}

	private DenseMatrix64F calculateOutputLayer(DenseMatrix64F input) {
		DenseMatrix64F x = new DenseMatrix64F(input.getNumRows() + 1, 1);
		System.arraycopy(input.data, 0, x.data, 1, input.getNumRows());
		x.set(0, 0, 1.0);

		DenseMatrix64F result = new DenseMatrix64F(this.outputLayerDim, 1);
		CommonOps.mult(weights, x, result);

		return result;
	}

}
