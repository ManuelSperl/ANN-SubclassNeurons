import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

/**
 * Class to read an XML-File and
 * build a data structure to manage a list of different items (Book, Cd or List)
 * based on the Composite Pattern
 */
public class XMLData {

    private ArrayList<ArrayList<Double>> inputData;
    private ArrayList<ArrayList<Double>> targetData;

    public void loadXml(File input) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(input);
        //Element root = doc.getDocumentElement();
        inputData = new ArrayList<>();
        targetData = new ArrayList<>();

        this.loadXmlItems(doc);
    }

       /**
     * loads the elements of the XML-File as items(Book, cd or list) into a given itemList
     */

    private void loadXmlItems(Document doc){

        NodeList nl = doc.getElementsByTagName("pattern");
        //System.out.println("Child length: " + nl.getLength());

        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            //System.out.println("Node Name: " + node.getNodeName());

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                //System.out.println("input: " + element.getElementsByTagName("input").item(0).getTextContent());
                //System.out.println("target: " + element.getElementsByTagName("target").item(0).getTextContent());

                String inputXMLData = element.getElementsByTagName("input").item(0).getTextContent();
                String targetXMLData = element.getElementsByTagName("target").item(0).getTextContent();

                String[] inputArray = inputXMLData.split(", ");
                String[] targetArray = targetXMLData.split(", ");

                ArrayList<Double> inputDataDouble = new ArrayList<>();
                ArrayList<Double> targetDataDouble = new ArrayList<>();

                for(String s : inputArray)
                    inputDataDouble.add(Double.parseDouble(s));

                for(String s : targetArray)
                    targetDataDouble.add(Double.parseDouble(s));

                inputData.add(inputDataDouble);
                targetData.add(targetDataDouble);

            }
        }
        //System.out.println("\n ------ INPUT DATA (ArrayList<ArrayList>)-----");
        //arrayListToString(inputData);
        //System.out.println("\n ------ TARGET DATA (ArrayList<ArrayList>)-----");
        //arrayListToString(targetData);
    }

    public double[][] getInputPatterns() {
        double[][] inPatterns;
        int i = 0, j = 0;

        inPatterns = new double[this.getSizeOfInputData()][this.getNumberOfInputs()];

        for(ArrayList<Double> arr : this.inputData){
           for(double d : arr){
               inPatterns[i][j] = d;
               j++;
           }
           i++;
           j=0;
        }

        //System.out.println("\n ------ INPUT DATA (double[][])-----");
        //arrayToString(inPatterns);
        return inPatterns;
    }

    public double[][] getOuputPatterns() {
        double[][] outPatterns;
        int i = 0, j = 0;

        outPatterns = new double[this.getSizeOfOutputData()][this.getNumberOfTargets()];

        for(ArrayList<Double> arr : this.targetData){
            for(double d : arr){
                outPatterns[i][j] = d;
                j++;
            }
            i++;
            j=0;
        }

        //System.out.println("\n ------ INPUT DATA (double[][])-----");
        //arrayToString(inPatterns);
        return outPatterns;
    }

    public int getNumberOfInputs(){
        return this.inputData.get(0).size();
    }

    public int getNumberOfTargets(){
        return this.targetData.get(0).size();
    }

    public int getSizeOfInputData(){
        return this.inputData.size();
    }

    public int getSizeOfOutputData(){
        return this.inputData.size();
    }

    private void arrayListToString(ArrayList<ArrayList<Double>> array){
        for(ArrayList<Double> a : array) {
            System.out.print("{ " );
            for (Double d : a) {
                System.out.print(d + ", ");
            }
            System.out.println("}");
        }
    }

    private void arrayToString(double[][] array){
        for(double[] a : array) {
            System.out.print("{ " );
            for (Double d : a) {
                System.out.print(d + ", ");
            }
            System.out.println("}");
        }
    }
}
