import java.util.ArrayList;

public class Subclass {
    private final int index;
    private int error;
    private int numberOfUses;
    public ArrayList<Integer> indexOfWrongPatterns;

    public Subclass(int index) {
        this.index = index;
        this.error = 0;
        this.numberOfUses = 0;
        this.indexOfWrongPatterns = new ArrayList<>();
    }

    public int getIndex() {
        return this.index;
    }

    public void increaseError() {
        this.error++;
    }

    public void increaseNumberOfUses() {
        this.numberOfUses++;
    }

    public double errorRate(){
        return this.numberOfUses != 0 ?
                (double) this.error / this.numberOfUses : 0;
    }

    public void resetError(){
        this.error=0;
        this.numberOfUses=0;
    }
}
