package model;

/* 	Created By Rico Zhu
 * 	The Sequential object is used to represent a single neuron network (similar to Keras)
 * 		There is a train method that takes in 2 vectors, input and truth and computes the changes over a given number of epochs
 * 		There is an array of Dense objects to keep track of the entire network and it is used in propagation
 */

import java.util.ArrayList;

import layer.Dense;
import layer.Layer;
import layer.Neuron;
import functions.CostFunction;

import qsor.Vector;

public class Sequential {

	ArrayList<Dense> layers;
	Dense[] propLayers;

	String optimizer;
	String costFunction;

	double error;
	double learningRate;

	double[] predictions;
	double[] truth;

	public Sequential () {
		layers = new ArrayList<Dense>();
	}
	
	public Sequential (double learningRate) {
		layers = new ArrayList<Dense>();
		this.learningRate = learningRate;
	}

	public void add (Dense layer) {
		layers.add(layer);
	}

	public void add (Dense layer, String type) {
		layers.add(layer);
	}

	public boolean isEmpty () {
		return layers.isEmpty();
	}
	
	public void compile (String opt, String costF) {
		optimizer = opt;
		costFunction = costF;
	}

	public void train (Vector input, Vector truth, String costFunction, double learningRate) {

		if (input.isEmpty() || truth.isEmpty()) {
			throw new IllegalArgumentException("Input vector cannot be empty");
		}

		this.learningRate = learningRate;
		this.costFunction = costFunction;
		this.truth = truth.getValue();
		
		initPropLayers();
		
		forwardProp(input, true);
		
		updateCurrError(costFunction);

		backProp(input, 10, 0.3);
		
	}
	
	public void train (Vector input, Vector truth, String costFunction, int epochs, double learningRate) {

		if (input.isEmpty() || truth.isEmpty()) {
			throw new IllegalArgumentException("Input vector cannot be empty");
		}
		
		System.out.println("Input Vector: " + input + "\nTruth Vector: " + truth + "\n");

		this.learningRate = learningRate;
		this.costFunction = costFunction;
		this.truth = truth.getValue();
		
		initPropLayers();
		
		forwardProp(input, true);
		
		updateCurrError(costFunction);

		backProp(input, epochs, 0.01);
		
	}

	private void forwardProp (Vector input, boolean first) {
		// Initial Layer
		
		double[] prevA = propLayers[0].feedAndGetA(input, first);

		for (int i=1; i<propLayers.length; i++) {
			// Repeatedly feed forward a new Vector created from the a values of the previous layer
			prevA = propLayers[i].feedAndGetA(new Vector(prevA), first);
		}

		Vector output = new Vector(propLayers[propLayers.length-1].getAArray());
		predictions = output.getValue();
		
		System.out.println("Output: " + output);
	}
	
	private void backProp (Vector input, int epochs, double minDiff) {
		
		if (propLayers.length == 0) {
			// Forward Propagation hasn't been called yet
			throw new IllegalArgumentException("Must call forward propagation first");
		}
		
		/* 
		 * During each epoch, iterate backwards through the network and update values
		 * then calculate the new error term and repeat
		 */
		
		for (int e=0; e<epochs; e++) {
			
			for (int i=propLayers.length-1; i>=1; i--) {
				
				Dense lastL = propLayers[i-1];
				
				propLayers[i].updateWeights(lastL.getAArray(), learningRate, error);
				
			}
			
			propLayers[0].updateWeights(input.getValue(), learningRate, error);
			
			forwardProp(input, false);
			
			updateCurrError(costFunction);
			
			System.out.println("Epoch: " + e + "\t Error: " + error);
			
			if (Math.abs(error) <= minDiff) {
				break;
			}
			
		}
		
	}
	
	private void updateCurrError (String costFunction) {
		
		if (predictions.length == 0 || truth.length == 0) {
			throw new IllegalArgumentException("Model has not been trained yet");
		}
		
		switch (costFunction) {
			case ("MSE"):
				error = CostFunction.MSE(predictions, truth);
				break;
			case ("Cross Entrophy"):
				break;
			case ("diff"):
				// Assumes that the predictions and truth are both only one value
				error = CostFunction.diff(predictions[0], truth[0]);
				break;
		}
		
	}

	private void initPropLayers() {
		// Initialize the Dense Layer array where propagation will happen
		propLayers = new Dense[layers.size()];

		for (int i=0; i<layers.size(); i++) {
			propLayers[i] = layers.get(i);
		}

	}
	
	private void printWeights () {
		// Prints the weights of the layers of neurons
		for (int i=0; i<propLayers.length; i++) {
			propLayers[i].printNeurons();
		}
	}
}
