import java.util.ArrayList;

public class Subclass {
    private ArrayList<Double> subclassError;
    private ArrayList<Integer> inde

    public Subclass() {
        this.subclassError = new ArrayList<>();
    }

    public void add(double error){
        this.subclassError.add(error);
    }

    public double getSubclassError(){
        double sum = 0;

        for(double d : this.subclassError)
            sum += d;

        return sum / this.subclassError.size();
    }


}
