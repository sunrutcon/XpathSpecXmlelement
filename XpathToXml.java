import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XpathToXml {

  public static void main(String[] args) throws ParserConfigurationException, TransformerException {
    
    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

    Document document = documentBuilder.newDocument();
    
    String fileName;
    
    //fileName = "sftr_spec_1.1_csv.txt";
    //fileName = "sftr_spec_1.3_csv.txt";
    //fileName = "sftr_spec_1.4.csv";
    fileName = "sftr_spec_1.7.csv";
    
    //fileName = "coll_data.txt";
    
    int initialDepth = 3;
    
    System.out.println("-- " + fileName);
    
    //Node rootNode = GenerateXPathXmlElements(document,"/RootNode/FirstChild/SecondChild/ThirdChild");
    //Document document1 = GenerateXPathXmlElements(document,"/RootNode/FirstChild/SecondChild/ThirdChild");
    //rootNode = GenerateXPathXmlElements(document,"/RootNode/FirstChild/SecondChild/FourthChild");
    //rootNode = GenerateXPathXmlElements(document,"/RootNode/FirstChild/SecondChild/FifthChild");
    //Node rootNode = GenerateXPathXmlElementsFromFile(document);
    
    Node rootNode = GenerateXmlDocumentFromSpec(document, fileName);

    //readAllLines("sftr_spec_1.1_csv.txt");
    
    //printSftrSpec("sftr_spec_1.1_csv.txt");
    
    // ispiši generirani među xml
    //preetyPrintXml(rootNode);
    
    //ispiši XMLELEMENT kod 
    printTreeXmlElement( rootNode, initialDepth, "sequence");
    
    System.out.println("\n-- END " + fileName);
    
    
    // ispiši stablo tagova sa atributima
    //printTree( rootNode, 0);
    
    //countGroupBy();

  }


  private static void preetyPrintXml(Node rootNode)
      throws TransformerFactoryConfigurationError,
      TransformerConfigurationException, TransformerException {
    /* */ 

    // root element
    // Element root = document.createElement("company");
    // document.appendChild(root);

    // create the xml file
    // transform the DOM Object to an XML File
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    // DOMSource domSource = new DOMSource(document);
    DOMSource domSource = new DOMSource(rootNode);
    StreamResult streamResult = new StreamResult(System.out);

    // If you use
    // StreamResult result = new StreamResult(System.out);
    // the output will be pushed to the standard output ...
    // You can use that for debugging

    transformer.transform(domSource, streamResult);
    
  }


  /**
   * 
   * Ispiši željene kolone i retke iz CSV datoteke
   * 
   * @param fileName - naziv datoteke
   * 
   */
  private static void printSftrSpec(String fileName) {
      BufferedReader reader;
      List<Integer> list = new ArrayList<Integer>();
      
      int col_RTS = 0;
      int col_field = 1;
      int col_path = 2;
      int col_pub_sftr_recors = 3;
      int col_data_type = 4;
      int col_is_mandatory = 5;
      int col_data_group = 6;
      int col_field_type = 7;
      
      try {
        reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        // preskoči prvu jer je header
        line = reader.readLine();
        while (line != null) {
          //System.out.println(line);
          
          String[] fields = line.split(";");
          
          //System.out.println(fields.length);
          
          if ( !(fields.length<8) && !fields[col_field_type].equals("SKIP") && !fields[col_field_type].equals("")) {
            
            //System.out.println("[" + fields[5] + "]" + " ==> " + fields[2]);
            System.out.println(fields[col_path] + " [" + fields[col_field_type] + "]");
          }
          
          list.add(fields.length);
          
          // read next line
          line = reader.readLine();
        }
        reader.close();
        
        countGroupBy(list);
        
      } catch (IOException e) {
        e.printStackTrace();
      }
    
  }
  
  /**
   * 
   * Generiraj xml dokument iz fajla u kojem je lista xpath zapisa
   * 
   * @param document - 
   * @param fileName - "myfile.txt" - naziv datoteke
   * @return
   */
  private static Node GenerateXmlDocumentFromFileXpath(Document document, String fileName) {
    BufferedReader reader;
    Node node = null;
    try {
      reader = new BufferedReader(new FileReader(fileName));
      String line = reader.readLine();
      while (line != null) {
        //System.out.println(line);
        
        node = AddXmlNodeFromXpath(document,line, "value");
        // read next line
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return node;
  }
  
  /**
   * 
   * Generiraj xml dokument iz fajla u kojem je lista xpath zapisa i vrijednosti tagova u csv formatu
   * 
   * @param document - prazan XML dokument
   * @param fileName - "sftr.spec"
   * @return - popunjen xml dokument
   * 
   */
  private static Node GenerateXmlDocumentFromSpec(Document document, String fileName) {
    
    BufferedReader reader;
    Node node = null;
    
    int col_RTS = 0;
    int col_field = 1;
    int col_path = 2;
    int col_pub_sftr_recors = 3;
    int col_data_type = 4;
    int col_is_mandatory = 5;
    int col_data_group = 6;
    int col_field_type = 7;
    
    try {
      // učitaj file
      reader = new BufferedReader(new FileReader(fileName));

      // učitaj prvu liniju
      String line = reader.readLine();
      // preskoči prvu jer je header
      line = reader.readLine();

      while (line != null) {
        // System.out.println(line);

        String[] fields = line.split(";");

        //if (!fields[5].equals("SKIP")) {
        // očekujemo 8 kolona/vrijednosti csv zapisa, u obzir ulaze DATA, CHOICE, FLAG
        if ( !(fields.length<8) && (
              fields[col_field_type].equals("DATA") 
              || fields[col_field_type].equals("CHOICE")
              || fields[col_field_type].equals("FLAG")
            ) 
           ) {

          //System.out.println(fields[2] + " ==> [" + fields[5] + "] == > " + fields[4]);
          //node = AddXmlNodeFromXpath(document, fields[2], fields[5]);
          node = AddXmlNodeFromXpath(document, 
            fields[col_path], 
            fields[col_pub_sftr_recors], 
            fields[col_is_mandatory], 
            fields[col_field_type],
            fields[col_data_type]
          );
        }

        // read next line
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return node;
  }
  
  
  private static Document AddXmlNodeFromXpath(Document xmlDocument,String xpath, String nodeValue) {
    return AddXmlNodeFromXpath(xmlDocument,xpath, nodeValue, "", "","");
  }
  
  /**
   * Dodaje XML Element/Node u postojeći XML dokuemnt iz xpath varijable
   * 
   */
  private static Document AddXmlNodeFromXpath(Document xmlDocument,String xpath, String meta_nodeValue, String meta_isMandatory, String meta_fieldType, String meta_dataType) {

    Node parentNode = xmlDocument;
    String xmlString = "";

    if (xmlDocument != null && !xpath.isEmpty()) {
      String[] partsOfXPath = xpath.split("/");

      String xPathSoFar = "";

      for (int j=0; j<partsOfXPath.length; j++) {
        
        String xPathElement = partsOfXPath[j];

        if (xPathElement.isEmpty())
          continue;

        xPathSoFar += "/" + xPathElement.trim();

        Node childNode = null;
        NodeList nl = parentNode.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {

          if (nl.item(i).getNodeName().equals(xPathElement)) {
            childNode = nl.item(i);
          }

        }

        if (childNode == null) {
          if (xPathElement.startsWith("@")) {
            
            // DODAJ ATRIBUT iz xpath @ polja
            // ako xpath element počinje sa @, dodaj atribut roditeljskom tagu
            ((Element)parentNode).setAttribute(xPathElement.substring(1), meta_nodeValue);
          } else {
            
            // DODAJ TAG/NODE
            // pravimo roditelje
            childNode = xmlDocument.createElement(xPathElement);
            
            // ako smo na zadnjem elementu XPATH stringa, ubacujemo vrijednost
            if(partsOfXPath.length == j+1) {
              
              if (meta_fieldType.equals("CHOICE")) {
                // ako je tip polja choice onda nemoj puniti vrijednost nego ga stvi u atribut
                ((Element)childNode).setAttribute("meta_choice", meta_nodeValue);
              }
              else {
                // prije smo punili tekst kao sadržaj taga, ali je kasnije bilo teško to čitat 
                // jer roditeljski tagovi prividno imaju tekst koji je zapravo masu razmaka prije njihvoe djece
                //childNode.setTextContent(meta_nodeValue);
              }
              
              ((Element)childNode).setAttribute("meta_isMandatory", meta_isMandatory);
              ((Element)childNode).setAttribute("meta_fieldType", meta_fieldType);
              ((Element)childNode).setAttribute("meta_nodeValue", meta_nodeValue);
              ((Element)childNode).setAttribute("meta_dataType", meta_dataType);
              
            }
            
            parentNode.appendChild(childNode);
          }
        }
        parentNode = childNode;

      }
    }

    return xmlDocument;
  }

  /**
   * Ispiši sve retke nekog fajla
   * 
   */
  public static void readAllLines(String fileName) {
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(fileName));
      String line = reader.readLine();
      while (line != null) {
        System.out.println(line);
        // read next line
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Ispiši statistiku brojeva u nekoj listi, koliko ima kojih
   * 
   */
  public static void countGroupBy(List<Integer> p_list) {
    
//    Integer[] intArray = {1,1,2,3,4,4,4,5,5,5,5,5,5};
//    
    List<Integer> list = new ArrayList<Integer>();
//    
//    for (int i=0; i<intArray.length; i++) {
//      list.add(intArray[i]);
//    }
    
    list = p_list;

    Map<Integer, Long> counted = new HashMap<Integer, Long>();
    
    for (Integer l_num : list) {
      
      if(counted.containsKey(l_num)) {
        Long cnt = counted.get(l_num) + 1;
        counted.put(l_num, cnt);
      } else {
        counted.put(l_num, (long) 1);
      }
    }

    System.out.println(counted);
  }
  
  public static void printTree( Node el, int depth ) {
    
    int maxDepth = 10;
    Element childNode;
    
    if (depth > maxDepth)
      return;
    
    NodeList nl = el.getChildNodes();
    String tagName = "";
    String tagType = "";
    
    //System.out.println("printTree");
    
    for (int i=0; i<nl.getLength(); i++) {
      
      // izbaci #text nodove - to je whitespace iza tagova koji nemaju vrijednost, nego su roditelji
      if(!nl.item(i).getNodeName().startsWith("#")) {
        System.out.println(getIndentXml(depth) + nl.item(i).getNodeName());
        
        if (nl.item(i).hasAttributes()) {
          for(int j=0; j<nl.item(i).getAttributes().getLength(); j++) {
            System.out.print("----------------------------------");
            System.out.println(nl.item(i).getAttributes().item(j));
          }
        }
      }
      
      printTree(nl.item(i), depth + 1);
      
    }
  }
  
  // Generiranje XmlElement koda, sa atribudima, case when...
  public static void printTreeXmlElement( Node el, int depth, String seqOrChoice ) {
    
    int maxDepth = 30;
    Element childNode;
    
    if (depth > maxDepth) {
      System.out.println("DEPTH LIMIT " + maxDepth);
      
      System.out.println();
      System.out.println("-- ## DEPTH LIMIT ##" + maxDepth);
      return;
    }
    
    int l_depth;
    
    NodeList nl = el.getChildNodes();
    String tagName = "";
    String tagType = "";
    
    //System.out.println("printTree");
    
    int sibilingsNo = 0;
    
    int brackedClosed = 0;
    int atts = 0;
    
    boolean meta_isMandatory=true;
    
    String meta_nodeValue = "";
    String meta_fieldType = "";
    String meta_dataType = "";
    
    
    for (int i=0; i<nl.getLength(); i++) {
      
      l_depth = depth;
      
      atts = 0;
      
      meta_fieldType = "";
      meta_dataType = "";
      
      // izbaci #text nodove - to je whitespace iza tagova koji nemaju vrijednost, nego su roditelji
      Node childItem = nl.item(i);
      if(!childItem.getNodeName().startsWith("#")) {
        
        sibilingsNo++;
        brackedClosed = 0;
        
        // ako iama braće stavi zarez iza zadnjeg inače samo novi red
        // ako je choice, onda je case when i ne ide zarez
        if(sibilingsNo>1 && !seqOrChoice.equals("choice"))
          System.out.print(",");
        
        // prebaci u novi red
        if(sibilingsNo>0)
          System.out.println("");
        
        if (seqOrChoice.equals("choice")) {
          //System.out.println(getIndentXml(l_depth) + "when '" + el.getAttributes().getNamedItem("meta_choice").getNodeValue() + "' = '" + childItem.getNodeName() + "' or 1=1 then ");
          System.out.println(getIndentXml(l_depth) + "when " + el.getAttributes().getNamedItem("meta_choice").getNodeValue() + " = '" + childItem.getNodeName() + "' then ");
          
          //System.out.println(childItem.getNodeName());
        }
        
        if (hasAttribute(childItem, "meta_nodeValue")) {
          meta_nodeValue = childItem.getAttributes().getNamedItem("meta_nodeValue").getNodeValue();
          if (meta_nodeValue.equals("")) meta_nodeValue = "null"; 
        } else {
          meta_nodeValue = "null";
        }
        
        if (hasAttribute(childItem, "meta_fieldType")) {
          meta_fieldType = childItem.getAttributes().getNamedItem("meta_fieldType").getNodeValue();
        }
        
        if (hasAttribute(childItem, "meta_dataType")) {
          meta_dataType = childItem.getAttributes().getNamedItem("meta_dataType").getNodeValue();
        }
        
        
        // provjera da li je polje opcionalno, ako je ubaci case when dio i dodatno uvuci xmlelement za dva razmaka
        if (hasAttribute(childItem,"meta_isMandatory") && childItem.getAttributes().getNamedItem("meta_isMandatory").getNodeValue().equals("N")) {
          
          meta_isMandatory=false;
          l_depth = depth + 1;
          System.out.println(getIndentXml(depth) + "-- optional");
          
          //System.out.println(childItem.getChildNodes().item(0).getTextContent());
          //System.out.println(childItem.getChildNodes().getLength());
          
          //System.out.println(getIndentXml(depth) + "case when '" + meta_nodeValue + "' is not null  then " );
          System.out.println(getIndentXml(depth) + "case when " + meta_nodeValue + " is not null  then " );
          System.out.print(getIndentXml(l_depth) + "XmlElement(\"" + childItem.getNodeName() + "\",");
        } 
        
        else {
          meta_isMandatory=true;
          System.out.print(getIndentXml(depth) + "XmlElement(\"" + childItem.getNodeName() + "\",");
        }
        
        //if (childItem.getChildNodes().getLength() == 1 && childItem.getChildNodes().item(0).getNodeName().equals("#text")) {
        if (meta_fieldType.equals("DATA")) {
                 
          // ATRIBUTI
          // vidi da li ima atributa osim meta_atributa pa ih stavi u xmlAttributes
          if (childItem.hasAttributes()) {
            for(int attCnt=0; attCnt<childItem.getAttributes().getLength(); attCnt++) {
              //System.out.print(childItem.getAttributes().item(attCnt) + " ; ");
              String attName = childItem.getAttributes().item(attCnt).getNodeName();
              String attValue = childItem.getAttributes().item(attCnt).getNodeValue();
              //if (!attName.equals("meta_isMandatory") && !attName.equals("meta_fieldType")) {
              if (!attName.startsWith("meta_")) {
                if(atts==0) {
                  System.out.println();
                  System.out.print(getIndentXml(l_depth+1) + "XmlAttributes(");
                }
                
                if(atts>0)
                  System.out.print(",");
                System.out.println();
                //System.out.print(getIndentXml(l_depth+2) +  "'" + attValue + "' as \"" + attName + "\"");
                System.out.print(getIndentXml(l_depth+2) +  "" + attValue + " as \"" + attName + "\"");
                
                atts++;
              }
            }
            
            // ako smo imali atribute, zatvori zagradu
            if(atts>0) {
              System.out.println();
              System.out.println(getIndentXml(l_depth+1) + "),");
            }
            
          } else {
            atts = 0;
          }
          
          // TEKST - ovdje punimo sasržaj taga koji ima vrijednost
          
          // TODO: 
          //  - ako je tip datum pkg_xml_common.format_xml_date
          //  - ako je tip number pkg_xml_common.format_decimal_point
          //  - kreirati DDL za oracle bazu
          
          if (atts>0) {
            //System.out.print(getIndentXml(l_depth+1) + "'" + meta_nodeValue + "'");
            //System.out.print(getIndentXml(l_depth+1) + meta_nodeValue);
            System.out.print(getIndentXml(l_depth+1));
          }
          
          // -------------------------
          // Punjenje vrijednosti taga
          // -------------------------
          
          
          if (meta_dataType.startsWith("NUMBER")) {
            System.out.print(" pkg_xml_common.format_decimal_point( ");
          }
          
          else if (meta_dataType.startsWith("DATETIME")) {
            System.out.print(" pkg_xml_common.format_xml_date_time( ");
          }
          
          else if (meta_dataType.startsWith("DATE")) {
            System.out.print(" pkg_xml_common.format_xml_date( ");
          }
          
          
          
          //System.out.print("'" + meta_nodeValue + "'");
          System.out.print(meta_nodeValue);
          
          // zatvori zagradu od formatiranja datuma ili broja
          if ( meta_dataType.startsWith("NUMBER")
              || meta_dataType.startsWith("DATE") 
              || meta_dataType.startsWith("DATETIME")
          ) {
            System.out.print(" ) ");
          }
          
          if (atts==0) {
            //System.out.print("'" + meta_nodeValue + "'");
            //System.out.print(meta_nodeValue);
            
            // ako je teskt od elementa onda zatvori i zagradu u istoj liniji, 
            // osim ako smo imali atribute, odna ide zagrada u novu liniju, al to kasnije
            System.out.print(")");
            brackedClosed = 1;
          }
        }
        else {
          
          if (hasAttribute(childItem,"meta_choice")) {
            
            System.out.println();
            System.out.print(getIndentXml(l_depth+1) + "case ");
            
            // REKURZIJA - punjenje djece CHOICE
            printTreeXmlElement(childItem, l_depth + 2, "choice");
            
            System.out.println(getIndentXml(l_depth+1) + "end");
            
            System.out.println(getIndentXml(l_depth+1) + "-- CASEWHEN za CHOICE tag " + childItem.getNodeName() );
            
          } else {
            
            // REKURZIJA - punjenje djece SEQUENCE
            printTreeXmlElement(childItem, l_depth + 1, "sequence");
          }
          
        }
        
        // zatvori zagradu od XmlElement u novoj liniji, nema vrijednost 
        if (brackedClosed == 0) {
          if(atts>0)
            System.out.println();
          System.out.print(getIndentXml(l_depth) + ")");
        }
        
        // ako je polje bilo opcionalno zatvori case when sa end
        if (!meta_isMandatory) {
          System.out.println();
          System.out.print(getIndentXml(depth) + "end");
        }
        
//        if (seqOrChoice.equals("choice")) {
//          System.out.println();
//          
//        }
      }      
    }
    
    if(sibilingsNo>0)
      System.out.println();
  }
  
  public static String getIndentXml(int indent) {
    if (indent == 0 )
      return "";
    else
      return String.format("%" + (indent*2) + "c", ' ');
  }
  
  public static boolean hasAttribute(Node node2, String value) {
    NamedNodeMap attributes = node2.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
        Node node = attributes.item(i);
        //System.out.print(" ==> " + node.getNodeName() + " ==> " + node.getNodeValue());
        if (value.equals(node.getNodeName())) {
          //System.out.println("============================= ima ga ====================================" );  
          return true;
        }
    }
    return false;
  }

}
