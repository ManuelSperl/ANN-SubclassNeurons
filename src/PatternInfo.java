import boone.Neuron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatternInfo {
    private int indexWithValueOne; // index of the pattern value, where the value is 1
    public List<Subclass> subclassPerTarget; // the right subclass gets assigned to every target (represents a neuron)
    public HashMap<Neuron,Integer> outputNeuronHashMap; // every neuron gets hashed to a target-value

    public PatternInfo() {
        this.indexWithValueOne = -1;
        this.subclassPerTarget = new ArrayList<>();
        this.outputNeuronHashMap = new HashMap<>();
    }

    public void setIndexWithValueOne(int indexWithValueOne){
        this.indexWithValueOne = indexWithValueOne;
    }

    public int getIndexWithValueOne() {
        return this.indexWithValueOne;
    }
}
