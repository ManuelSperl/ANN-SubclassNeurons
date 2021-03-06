import boone.*;
import boone.training.BackpropTrainer;
import boone.training.RpropTrainer;
import boone.util.Conversion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Hangler, Marie Moser-Schwaiger
 */
public class AdaptiveModel {

	/** true, if we add a neuron to a new net with reseted values und train the same net again
	 * 	false, if we create a new network with another output neuron for every training run */
	static final boolean TRAIN_SAME_NET = false;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		XMLData xmlTrainingPatterns = new XMLData();
		XMLData xmlTestPatterns = new XMLData();

		/**------------------------IMPORTANT parameters for experiments-------------------------**/
		final int MIN_ROUNDS = 10;
		final int ROUNDS_WITHOUT_HIGHEST_ACCURACY = 5;

		int numberOfHiddenNeurons = 17; // with 0 hidden neurons we should get the biggest difference between ANN with subclasses and without subclasses

		int epochs = 1000; //steps = 1

		/**load Trainings-Dataset**/
		xmlTrainingPatterns.loadXml(new File("Dataset/dataset_test"));

		/**load Test-Dataset**/
		xmlTestPatterns.loadXml(new File("Dataset/dataset_training"));
		/**-------------------------------------------------------------------------------------**/

		List<Subclass> subclassList = new ArrayList<>(); // stores all subclass objects which are available in this net
		List<PatternInfo> trainingPatternInfoList = new ArrayList<>(); // info of every pattern (winning subclass per pattern, which target represents which neuron)
		List<PatternInfo> testPatternInfoList = new ArrayList<>(); // info of every pattern (winning subclass per pattern, which target represents which neuron)

		Subclass subclassHighestError;
		Subclass expectedSubclass;
		Neuron neuron;

		int numberOfInputNeurons;
		int numberOfOutputNeurons;

		double actualHighestAccuracy = 0.0;
		int roundsSinceHighestAccuracy = 0;
		double numberOfWrongOutputs;
		double accuracyOfThisPatternSet;

		System.out.println("*** Creating feed forward network...");

		numberOfInputNeurons = xmlTrainingPatterns.getNumberOfInputs();
		numberOfOutputNeurons = xmlTrainingPatterns.getNumberOfTargets();

		//read from XML
		//PatternSet.load(new File("Dataset/dataset"), new BooneFilter());

		int rounds = 0;
		NeuralNet net = null;
		NeuralNet bestNet = null;

		PatternSet trainingBoonePatterns = new PatternSet();
		PatternSet testBoonePatterns = new PatternSet();

		while(rounds < MIN_ROUNDS || roundsSinceHighestAccuracy <= ROUNDS_WITHOUT_HIGHEST_ACCURACY) {

			subclassHighestError = null;
			numberOfWrongOutputs = 0;

			if(!TRAIN_SAME_NET || rounds == 0) {
				net = NetFactory.createFeedForward(
						new int[]{numberOfInputNeurons, numberOfHiddenNeurons, numberOfOutputNeurons},    //#input neurons, #hidden neurons, #output neuron
						false,                                //net fully connected?
						new boone.map.Function.Sigmoid(),        //activation-function
						new RpropTrainer(),                      //Trainer
						null, null);
			} else {
				// add new neuron to net
				Neuron neuronToCopy = net.getOutputNeuron(0);

				Neuron newNeuron = neuronToCopy.clone();
				newNeuron.reset();
				//newNeuron.setBias(Math.random() * 0.2D - 0.1D); //random bias besser oder schlechter?
				newNeuron.setName("Neuron" + rounds);
				net.addNeuron(newNeuron);
			}

			// Initialization
			PatternInfo patternInfo;

			if (rounds == 0) {
				for(int i=0; i < xmlTrainingPatterns.getNumberOfTargets(); i++){
					Subclass sc = new Subclass(i);
					subclassList.add(sc);
				}

				for(int i = 0; i < xmlTrainingPatterns.getSizeOfPatternSet(); i++){
					patternInfo = new PatternInfo();

					for(int j=0; j < xmlTrainingPatterns.getNumberOfTargets(); j++){
						patternInfo.subclassPerTarget.add(subclassList.get(j));
						patternInfo.outputNeuronHashMap.put(net.getOutputNeuron(j), j);
					}
					trainingBoonePatterns.getInputs().add(Conversion.asList(xmlTrainingPatterns.getInputPatterns()[i]));
					trainingBoonePatterns.getTargets().add(Conversion.asList(xmlTrainingPatterns.getOutputPatterns()[i]));

					// safe index of the pattern-value, which has the value 1
					patternInfo.setIndexWithValueOne(
								xmlTrainingPatterns.getPosOfWinningNeuron(
									xmlTrainingPatterns.getOutputPatterns()[i]));

					trainingPatternInfoList.add(patternInfo);
				}

				for(int i = 0; i < xmlTestPatterns.getSizeOfPatternSet(); i++) {
					patternInfo = new PatternInfo();

					testBoonePatterns.getInputs().add(Conversion.asList(xmlTestPatterns.getInputPatterns()[i]));
					testBoonePatterns.getTargets().add(Conversion.asList(xmlTestPatterns.getOutputPatterns()[i]));

					// safe index of the pattern-value, which has the value 1
					patternInfo.setIndexWithValueOne(
							xmlTestPatterns.getPosOfWinningNeuron(
									xmlTestPatterns.getOutputPatterns()[i]));

					testPatternInfoList.add(patternInfo);
				}
			} else {
				// set all neurons new in hashmap
				for (int i = 0; i < trainingBoonePatterns.size(); i++) {
					//patternInfoList.get(i).outputNeuronHashMap.put(net.getOutputNeuron(net.getOutputNeuronCount() - 1), patternInfoList.get(i).outputNeuronHashMap.size());
					trainingPatternInfoList.get(i).outputNeuronHashMap.clear();
					for (int j = 0; j < numberOfOutputNeurons; j++)
						trainingPatternInfoList.get(i).outputNeuronHashMap.put(net.getOutputNeuron(j), j);
				}
			}

			for(Subclass s : subclassList) {
				s.resetError();
				s.indexOfWrongPatterns.clear();
			}

			Trainer trainer = net.getTrainer();
			trainer.setTrainingData(trainingBoonePatterns);
			trainer.setTestData(testBoonePatterns);
			trainer.setEpochs(epochs);
			trainer.setStepMode(true);
			//System.out.println("*** Training " + epochs + " epochs...");
			//((BackpropTrainer) trainer).setMomentum(0.8);

			trainer.train();
			//System.out.println(epochs + ". - " + net.getTrainer().test());

			//System.out.println("\n*** Testing the network...");
			//System.out.println();

			/**Testing the network with trainings-data and add subclass-neurons if needed*/
			for (int i = 0; i < trainingBoonePatterns.size(); i++) {
				PatternInfo actualPattern = trainingPatternInfoList.get(i);

				neuron = net.getTrainer().getWinningNeuron(trainingBoonePatterns.getInputs().get(i));
				//System.out.println("winning neuron " + neuron.);

				int neuronIndex;
				if(actualPattern.outputNeuronHashMap.containsKey(neuron))
					neuronIndex = actualPattern.outputNeuronHashMap.get(neuron);
				// error part
				else {
					for(Neuron n : actualPattern.outputNeuronHashMap.keySet())
						System.out.println("Hashmap-Neuron: " + n.getName());

					throw new RuntimeException("Neuron does not exist in hashmap:  " + neuron.getName());
				}
				expectedSubclass = actualPattern.subclassPerTarget.get(actualPattern.getIndexWithValueOne());
				expectedSubclass.increaseNumberOfUses();

				Subclass winningSubclass = actualPattern.subclassPerTarget.get(neuronIndex);

				// Check if correct subclass won
				if(winningSubclass.getIndex() == expectedSubclass.getIndex()){
					//System.out.println(winningSubclass.getIndex() + " = " + expectedSubclass.getIndex());
					// check if the right neuron of the subclass won
					int expectedNeuronIndex = actualPattern.getIndexWithValueOne();
					if(neuronIndex != expectedNeuronIndex) {
						//System.out.println("--- Correct Subclass but wrong neuron ---");
						//xmlTrainingPatterns.printPattern(patterns.getTargets().get(i));
						//System.out.println("expected Index: " + expectedNeuronIndex + " --> neuron index: " + neuronIndex);
						trainingBoonePatterns.getTargets().get(i).set(expectedNeuronIndex, 0.0);
						trainingBoonePatterns.getTargets().get(i).set(neuronIndex, 1.0);
						actualPattern.setIndexWithValueOne(neuronIndex);

						//xmlTrainingPatterns.printPattern(patterns.getTargets().get(i));
						//System.out.println("-----------------------------------------");
					}
				}
				// wrong subclass won
				else {
					numberOfWrongOutputs++;
					expectedSubclass.increaseError();
					//System.out.println("WRONG SUBCLASS");
					if(subclassHighestError == null || expectedSubclass.errorRate() > subclassHighestError.errorRate())
						subclassHighestError = expectedSubclass;

					expectedSubclass.indexOfWrongPatterns.add(i);
				}
			}
			// when subclassHighestError = null, than every pattern was correctly detected
			if (subclassHighestError == null) {
					for(Subclass s : subclassList){
						if(s.errorRate() != 0.0)
							throw new RuntimeException("Error-Rate of Subclass " + s.getIndex() + " is " + s.errorRate()*100 + "%, but should be 0%");
					}
				System.out.println("NET ACCURACY IS 100%");
				break;
			}
			else {
				int wrongPatternIndex = 0;
				for(int i=0; i < trainingBoonePatterns.size(); i++){
					// set value in target list for new neuron
					// if in this pattern, the value of the neuron is 1
					if(wrongPatternIndex < subclassHighestError.indexOfWrongPatterns.size() && subclassHighestError.indexOfWrongPatterns.get(wrongPatternIndex) == i){
						//System.out.println("--- add neuron to subclass with highest error: Subclass " + subclassHighestError.getIndex() + " ---");
						//xmlTrainingPatterns.printPattern(patterns.getTargets().get(i));
						trainingBoonePatterns.getTargets().get(i).add(1.0);
						trainingBoonePatterns.getTargets().get(i).set(trainingPatternInfoList.get(i).getIndexWithValueOne(), 0.0);
						trainingPatternInfoList.get(i).setIndexWithValueOne(trainingBoonePatterns.getTargets().get(i).size() - 1);
						//xmlTrainingPatterns.printPattern(patterns.getTargets().get(i));
						//System.out.println("---- Pattern: " + i + " changed ---------------------------------------------------------------------");

						wrongPatternIndex++;
					}
					// in this pattern, the value of the neuron is 0
					else
						trainingBoonePatterns.getTargets().get(i).add(0.0);

					trainingPatternInfoList.get(i).subclassPerTarget.add(subclassHighestError);
				}
				subclassHighestError.increaseNumberOfSubclasses();
			}

			accuracyOfThisPatternSet = 1 - (numberOfWrongOutputs / (double) trainingBoonePatterns.size());

			if(accuracyOfThisPatternSet > actualHighestAccuracy){
				actualHighestAccuracy = accuracyOfThisPatternSet;
				roundsSinceHighestAccuracy = 0;

				bestNet = net;
			} else
				roundsSinceHighestAccuracy ++;

			System.out.println("Round " + rounds + ": net accuracy = " + accuracyOfThisPatternSet*100 + "%" + "   Subclass with highest Error was Nr.: " + subclassHighestError.getIndex());
			numberOfOutputNeurons++;
			rounds++;

			/*
			System.out.println("Subclass-Errorlist: ");
			for(Subclass s : subclassList)
				System.out.println("Subclass " + s.getIndex() + ": " + s.errorRate()*100 + "% Error");
			 */

		}

		for(Subclass s : subclassList) {
			s.resetError();
			s.indexOfWrongPatterns.clear();
		}

		numberOfWrongOutputs = 0;

		/**Testing the subclass network with test-data*/
		PatternInfo actualPattern = trainingPatternInfoList.get(0);

		for(int i = 0; i < testBoonePatterns.size(); i++){

			neuron = bestNet.getTrainer().getWinningNeuron(testBoonePatterns.getInputs().get(i));

			int neuronIndex;
			if(actualPattern.outputNeuronHashMap.containsKey(neuron))
				neuronIndex = actualPattern.outputNeuronHashMap.get(neuron);
			// error part
			else {
				for(Neuron n : actualPattern.outputNeuronHashMap.keySet())
					System.out.println("Hashmap-Neuron: " + n.getName());

				throw new RuntimeException("Neuron does not exist in hashmap:  " + neuron.getName());
			}

			Subclass winningSubclass = actualPattern.subclassPerTarget.get(neuronIndex);
			int expectedSubclassIndex = xmlTestPatterns.getPosOfWinningNeuron(testBoonePatterns.getTargets().get(i));

			subclassList.get(expectedSubclassIndex).increaseNumberOfUses();

			if(expectedSubclassIndex != winningSubclass.getIndex()) {
				subclassList.get(expectedSubclassIndex).increaseError();
				numberOfWrongOutputs++;
			}
		}

		/**Display results of tested subclass-network*/
		subclassHighestError = null;
		System.out.println("Results of tested subclass-network with test-dataset: ");
		for(Subclass s : subclassList){
			System.out.println("Class" + (s.getIndex()+1) + " - error-rate = " + s.errorRate() + " - Number of Subclasses: " + s.getNumberOfSubclasses());

			if (subclassHighestError == null || subclassHighestError.errorRate() < s.errorRate())
				subclassHighestError = s;
		}

		accuracyOfThisPatternSet = 1 - (double) (numberOfWrongOutputs / testBoonePatterns.size());
		System.out.println("\nNET ACCURACY = " + accuracyOfThisPatternSet*100 + "%");

		System.out.println("Number of Targets: " + testBoonePatterns.size());
		System.out.println("Number of Errors: " + numberOfWrongOutputs);

		//System.out.println("% of correct subclass recognition: " + (100 - ((double)(nrOfError / testBoonePatterns.size()) * 100)) + "%");

		/*
		System.out.println("\n\n---- Coordinates Input-Neurons--------");
		for(int i=0; i < net.getInputNeuronCount(); i++){
			System.out.println("Input-Neuron " + (i+1) + "x= " + net.getInputNeuron(i).getPosition().getXPos()
					+ "  y= " + net.getInputNeuron(i).getPosition().getYPos()
					+ "  z= " + net.getInputNeuron(i).getPosition().getZPos());
		}


		System.out.println("\n\n---- Coordinates Output-Neurons--------");
		for(int i=0; i < net.getOutputNeuronCount(); i++){
			System.out.println("Output-Neuron " + (i+1) + "x= " + net.getOutputNeuron(i).getPosition().getXPos()
					+ "  y= " + net.getOutputNeuron(i).getPosition().getYPos()
					+ "  z= " + net.getOutputNeuron(i).getPosition().getZPos());
		}
		 */


		System.out.println("Saving network");

		/*
		try {
			net.save(new File("AdaptiveModel.xnet"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		System.out.println("Printing the net:\n" + bestNet);
		System.out.println("Done.");
	}

}
