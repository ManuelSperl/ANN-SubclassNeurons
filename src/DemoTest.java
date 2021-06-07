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
public class DemoTest {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {

		List<Subclass> subclassList = new ArrayList<>(); // stores all subclass objects which are available in this net
		List<PatternInfo> patternInfoList = new ArrayList<>(); // info of every pattern (winning subclass per pattern, which target represents which neuron
		Subclass subclassHighestError = null;
		double lastNetAccuracy = Double.MAX_VALUE;

		Subclass expectedSubclass;
		Neuron neuron;

		XMLData xmlData = new XMLData();
		int numberOfInputNeurons;
		int numberOfOutputNeurons;
		int numberOfHiddenNeurons = 17; // 17 should be the best amount of neurons for the specific data

		System.out.println("*** Creating feed forward network...");

		/**small Dataset loaded**/
		xmlData.loadXml(new File("Dataset/dataset_without_zero_targets"));
		/**big Dataset loaded**/
		//xmlData.loadXml(new File("Dataset/dataset_big"));
		numberOfInputNeurons = xmlData.getNumberOfInputs();
		numberOfOutputNeurons = xmlData.getNumberOfTargets();

		//read from XML
		//PatternSet.load(new File("Dataset/dataset"), new BooneFilter());

		int rounds = 0;
		NeuralNet net = null;

		PatternSet patterns = new PatternSet();

		while(rounds < 100 || (1 - subclassHighestError.errorRate()) > lastNetAccuracy) {
			if(rounds > 0)
				lastNetAccuracy = 1 - subclassHighestError.errorRate();

			subclassHighestError = null;

			net = NetFactory.createFeedForward(
					new int[]{numberOfInputNeurons, numberOfHiddenNeurons, numberOfOutputNeurons},    //#input neurons, #hidden neurons, #output neuron
					false,                                //net fully connected?
					new boone.map.Function.Sigmoid(),        //activation-function
					new RpropTrainer(),                      //Trainer
					null, null);                //

			// Initialization
			PatternInfo patternInfo;
			if (rounds == 0) {
				for(int i=0; i < xmlData.getNumberOfTargets(); i++){
					Subclass sc = new Subclass(i);
					subclassList.add(sc);
				}

				for(int i = 0; i < xmlData.getSizeOfPatternSet(); i++){
					patternInfo = new PatternInfo();

					for(int j=0; j < xmlData.getNumberOfTargets(); j++){
						patternInfo.subclassPerTarget.add(subclassList.get(j));
						patternInfo.outputNeuronHashMap.put(net.getOutputNeuron(j), j);
					}
					patterns.getInputs().add(Conversion.asList(xmlData.getInputPatterns()[i]));
					patterns.getTargets().add(Conversion.asList(xmlData.getOutputPatterns()[i]));

					// safe index of the pattern-value, which have the value 1
					patternInfo.setIndexWithValueOne(
								xmlData.getPosOfWinningNeuron(
									xmlData.getOutputPatterns()[i]));

					patternInfoList.add(patternInfo);
				}
			} else {
				// set all neurons new in hashmap
				for (int i = 0; i < patterns.size(); i++) {
					//patternInfoList.get(i).outputNeuronHashMap.put(net.getOutputNeuron(net.getOutputNeuronCount() - 1), patternInfoList.get(i).outputNeuronHashMap.size());
					patternInfoList.get(i).outputNeuronHashMap.clear();
					for(int j=0; j < numberOfOutputNeurons; j++){
						patternInfoList.get(i).outputNeuronHashMap.put(net.getOutputNeuron(j), j);
					}
				}
			}

			for(Subclass s : subclassList) {
				s.resetError();
				s.indexOfWrongPatterns.clear();
			}

			int epochs = 30; //steps = 1
			Trainer trainer = net.getTrainer();
			trainer.setTrainingData(patterns);
			trainer.setTestData(patterns);
			trainer.setEpochs(epochs);
			trainer.setStepMode(true);
			//System.out.println("*** Training " + epochs + " epochs...");
			((BackpropTrainer) trainer).setMomentum(0.8);

			trainer.train();
			//System.out.println(epochs + ". - " + net.getTrainer().test());

			//System.out.println("\n*** Testing the network...");
			//System.out.println();

			for (int i = 0; i < patterns.size(); i++) {
				PatternInfo actualPattern = patternInfoList.get(i);

				neuron = net.getTrainer().getWinningNeuron(patterns.getInputs().get(i));
				//System.out.println("winning neuron " + neuron.getBias());

				int neuronIndex;
				if(actualPattern.outputNeuronHashMap.containsKey(neuron))
					neuronIndex = actualPattern.outputNeuronHashMap.get(neuron);
				else {
					for(Neuron n : actualPattern.outputNeuronHashMap.keySet())
						System.out.println("Hashmap-Neuron: " + n.getBias());

					throw new RuntimeException("Neuron not exists in hashmap:  " + neuron.getBias());
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
						//xmlData.printPattern(patterns.getTargets().get(i));
						//System.out.println("expected Index: " + expectedNeuronIndex + " --> neuron index: " + neuronIndex);
						patterns.getTargets().get(i).set(expectedNeuronIndex, 0.0);
						patterns.getTargets().get(i).set(neuronIndex, 1.0);
						actualPattern.setIndexWithValueOne(neuronIndex);

						//xmlData.printPattern(patterns.getTargets().get(i));
						//System.out.println("-----------------------------------------");
					}
				}
				// wrong subclass won
				else {
					expectedSubclass.increaseError();
					//System.out.println("WRONG SUBCLASS");
					if(subclassHighestError == null || expectedSubclass.errorRate() > subclassHighestError.errorRate())
						subclassHighestError = expectedSubclass;

					expectedSubclass.indexOfWrongPatterns.add(i);
				}
			}
			// when subclassHighestError = null, than every pattern was correctly detected
			if (subclassHighestError == null) {
				System.out.println("NET ACCURACY should be 100%");
				break;
			}

			else {
				int wrongPatternIndex = 0;
				for(int i=0; i < patterns.size(); i++){
					// set value in target list for new neuron
					// if in this pattern, the value of the neuron is 1
					if(wrongPatternIndex < subclassHighestError.indexOfWrongPatterns.size() && subclassHighestError.indexOfWrongPatterns.get(wrongPatternIndex) == i){
						//System.out.println("--- add neuron to subclass with highest error: Subclass " + subclassHighestError.getIndex() + " ---");
						//xmlData.printPattern(patterns.getTargets().get(i));
						patterns.getTargets().get(i).add(1.0);
						patterns.getTargets().get(i).set(patternInfoList.get(i).getIndexWithValueOne(), 0.0);
						patternInfoList.get(i).setIndexWithValueOne(patterns.getTargets().get(i).size() - 1);
						//xmlData.printPattern(patterns.getTargets().get(i));
						//System.out.println("---- Pattern: " + i + " changed ---------------------------------------------------------------------");

						wrongPatternIndex++;
					}
					// in this pattern, the value of the neuron is 0
					else
						patterns.getTargets().get(i).add(0.0);

					patternInfoList.get(i).subclassPerTarget.add(subclassHighestError);
				}
			}

			System.out.println("Round " + rounds + ": net accuracy = " + (1-subclassHighestError.errorRate())*100 + "%" + "   Subclass with highest Error was Nr.: " + subclassHighestError.getIndex());
			numberOfOutputNeurons++;
			rounds++;
		}

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
			net.save(new File("DemoTest.xnet"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		System.out.println("Printing the net:\n" + net);
		System.out.println("Done.");
	}

}
