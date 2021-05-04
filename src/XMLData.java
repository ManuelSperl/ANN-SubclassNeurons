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
        Element root = doc.getDocumentElement();
        //System.out.println(root.getTagName());
        inputData = new ArrayList<>();
        targetData = new ArrayList<>();

        this.loadXmlItems(doc);

        //itemList.displayItemList();
    }

       /**
     * loads the elements of the XML-File as items(Book, cd or list) into a given itemList
     * @return the updated ItemList
     */

    private void loadXmlItems(Document doc) throws Exception {

        NodeList nl = doc.getElementsByTagName("pattern");
        System.out.println("Child length: " + nl.getLength());

        for (int i = 0; i < 5; i++) {
            Node node = nl.item(i);
            System.out.println("Node Name: " + node.getNodeName());

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
                System.out.println("\n ------ INPUT DATA -----");
                arrayToString(inputData);

                targetData.add(targetDataDouble);
                System.out.println("\n ------ TARGET DATA -----");
                arrayToString(targetData);
            }
            /*int type = n.getNodeType();

            if (type == Node.ELEMENT_NODE) {
                Element e = (Element) n;

                //add item to the current list
                switch (e.getTagName()) {
                    case "book" -> itemList.addItem(new Book(e.getAttribute("name"), Double.parseDouble(e.getAttribute("price")), Long.parseLong(e.getAttribute("isbn"))));
                    case "cd" -> itemList.addItem(new Cd(e.getAttribute("name"), Double.parseDouble(e.getAttribute("price"))));
                    case "list" -> itemList.addItem(this.loadXmlItems(e, new ItemList(e.getAttribute("name"))));
                    default -> throw new Exception("Unknown Type : " + e.getTagName());
                }
            }*/
        }
    }

    private void arrayToString(ArrayList<ArrayList<Double>> array){
        for(ArrayList<Double> a : array) {
            System.out.print("{ " );
            for (Double d : a) {
                System.out.print(d + ", ");
            }
            System.out.println("}");
        }
    }

}
