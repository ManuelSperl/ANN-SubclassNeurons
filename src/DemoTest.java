/*
 * Main.java
 *
 * Copyright (C) August Mayer, 2001-2004. All rights reserved.
 * Please consult the Boone LICENSE file for additional rights granted to you.
 *
 * Created on 25. November 2002, 14:47
 */

import boone.*;
import boone.io.BooneFilter;
import boone.io.IOFilter;
import boone.io.Storable;
import boone.training.BackpropTrainer;
import boone.training.RpropTrainer;
import boone.util.Conversion;
import org.w3c.dom.Document;


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

		//XMLData xmlData = new XMLData();
		int numberOfInputNeurons = 0;
		int numberOfOutputNeurons = 0;
		int numberOfHiddenNeurons = 17; // 17 should be the best amount of neurons for the specific data

		System.out.println("*** Creating feed forward network...");

		/**small Dataset loaded**/
		//xmlData.loadXml(new File("Dataset/dataset"));
		/**big Dataset loaded**/
		//xmlData.loadXml(new File("Dataset/dataset_big"));
		//numberOfInputNeurons = xmlData.getNumberOfInputs();
		//numberOfOutputNeurons = xmlData.getNumberOfTargets();

		//von XML lesen
		PatternSet.load(new File("Dataset/dataset"), new BooneFilter());

		NeuralNet net = NetFactory.createFeedForward(
				new int[]{numberOfInputNeurons, numberOfHiddenNeurons, numberOfOutputNeurons},	//#input neurons, #hidden neurons, #output neuron
				false,								//net fully connected?
				new boone.map.Function.Sigmoid(),		//activation-function
				new RpropTrainer(),						//Trainer
				null, null);				//

		//xmlData.getInputPatterns();

		/*
		for (int i = 0; i < xmlData.getSizeOfInputData(); i++) {
			patterns.getInputs().add(Conversion.asList(xmlData.getInputPatterns()[i]));
			patterns.getTargets().add(Conversion.asList(xmlData.getOuputPatterns()[i]));
		}
		*/


		int epochs = 10; //steps = 1
		Trainer trainer = net.getTrainer();
		trainer.setTrainingData(patterns);
		trainer.setTestData(patterns);
		trainer.setEpochs(epochs);
		trainer.setStepMode(true);
		System.out.println("*** Training " + epochs + " epochs...");
		//trainer.setShuffle(true);
		((BackpropTrainer)trainer).setMomentum(0.8);
		//System.out.println("Error: ");

		trainer.train();
		System.out.println(epochs + ". - " + net.getTrainer().test());

		System.out.println("\n*** Testing the network...");
		System.out.println();

		//int indexOfMaxError = -1;
		double error = -1;
		for (int i = 0; i < patterns.size(); i++){
			error = net.getTrainer().test(patterns.getInputs().get(i), patterns.getTargets().get(i));
			System.out.println("Error " + i + " = " + error);
		}

		System.out.println("\n\n---- Coordinates Input-Neurons--------");
		for(int i=0; i < net.getInputNeuronCount(); i++){
			System.out.println("Input-Neuron " + (i+1) + "x= " + net.getInputNeuron(i).getPosition().getXPos()
					+ "  y= " + net.getInputNeuron(i).getPosition().getYPos()
					+ "  z= " + net.getInputNeuron(i).getPosition().getZPos());
		}

		System.out.println("\n\n---- Coordinates Ouput-Neurons--------");
		for(int i=0; i < net.getOutputNeuronCount(); i++){
			System.out.println("Output-Neuron " + (i+1) + "x= " + net.getOutputNeuron(i).getPosition().getXPos()
					+ "  y= " + net.getOutputNeuron(i).getPosition().getYPos()
					+ "  z= " + net.getOutputNeuron(i).getPosition().getZPos());
		}


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
