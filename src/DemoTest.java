/*
 * Main.java
 *
 * Copyright (C) August Mayer, 2001-2004. All rights reserved.
 * Please consult the Boone LICENSE file for additional rights granted to you.
 *
 * Created on 25. November 2002, 14:47
 */

import boone.*;
import boone.training.BackpropTrainer;
import boone.training.RpropTrainer;
import boone.util.Conversion;

import java.io.File;
import java.io.IOException;

/**
 * Test program: the XOR-Problem
 *
 * @author August Mayer
 * @version $Id: DemoTest.java 2292 2016-01-15 16:52:18Z helmut $
 */
public class DemoTest {

	/**
	 * XOR programs network, using back-propagation
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("*** Creating feed forward network...");
		XMLData xmlData = new XMLData();

		xmlData.loadXml(new File("Dataset/dataset"));
		int numberOfInputNeurons = 2;

		//von XML lesen

		NeuralNet net = NetFactory.createFeedForward(
				new int[]{numberOfInputNeurons, 2, 2},	//#input neurons, #hidden neurons, #output neuron
				false,								//net fully connected?
				new boone.map.Function.Sigmoid(),		//activation-function
				new RpropTrainer(),						//Trainer
				null, null);				//

		double[][] inPatterns = new double[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		double[][] outPatterns = new double[][]{{0, 0}, {1, 0}, {1, 0}, {0, 0}};
		PatternSet patterns = new PatternSet();
		for (int i = 0; i < inPatterns.length; i++) {
			patterns.getInputs().add(Conversion.asList(inPatterns[i]));
			patterns.getTargets().add(Conversion.asList(outPatterns[i]));
		}
		int steps = 100;
		int epochs = 10;
		Trainer trainer = net.getTrainer();
		trainer.setTrainingData(patterns);
		trainer.setTestData(patterns);
		trainer.setEpochs(epochs);
		trainer.setStepMode(true);											// training in steps
		System.out.println("*** Training " + (steps * epochs) + " epochs...");
		//trainer.setShuffle(true);
		((BackpropTrainer)trainer).setMomentum(0.8);
		//System.out.println("Error: ");

		//for(int e=0; e<3; e++ ) {
			for (int i = 0; i < steps; i++) {
				trainer.train();
				System.out.println((i * epochs) + ". - " + net.getTrainer().test());
			}

			System.out.println("\n*** Testing the network...");

			System.out.println();

			int indexOfMaxError = -1;
			double error = -1;
			for (int i = 0; i < patterns.size(); i++){
				error = net.getTrainer().test(patterns.getInputs().get(i), patterns.getTargets().get(i));
				System.out.println("Error " + i + " = " + error);

				//System.out.println("Anzahl-Outputneuron: " + net.getOutputNeuronCount());

				//if(error > indexOfMaxError)
				//	indexOfMaxError = i;
			}
			//if(indexOfMaxError != -1){
				//patterns.getTargets().get(indexOfMaxError)
			//}
		//}

		System.out.println("Saving network");

			/*
		try {
			net.save(new File("DemoTest.xnet"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		System.out.println("Printing the net:\n" + net);
		System.out.println("Done.");
	}

}
