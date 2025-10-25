import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to load, edit, and write XML files.
 *
 * @author Ben Rappleye
 */
public class XMLDoc {

    //------------------Private Members-------------------------
    /**
     * Contains the path to the original file read from.
     */
    private String sourcePath;

    /**
     * Contains the root node of the XML tree.
     */
    private XMLNode root;

    /**
     * Holds the version as stated in the XML file.
     */
    private String version;

    /**
     * Holds the encoding used by the XML file.
     */
    private String encoding;

    /**
     * Converts an XML file into 1 line for parsing.
     *
     * @param file
     *            the XML file to read
     *
     * @return the XML file formatted to 1 line removing all whitespace
     *
     * @throws IOException
     */
    private static String convertToOneLine(BufferedReader file) throws IOException {
        int c = file.read();
        String oneLine = "";
        while (c != -1) {
            if ((char) c != '\r' && (char) c != '\n') {
                oneLine += (char) c;
            } else {
                while ((char) c == ' ' || (char) c == '\n' || (char) c == '\r') {
                    c = file.read();
                }
                oneLine += (char) c;
            }
            c = file.read();
        }

        return oneLine;
    }

    /**
     * Parses a line of XML into an XMLNode.
     *
     * @param source
     *            The line of XML to be parsed
     *
     * @return an XMLNode containing all features of the source XML line
     */
    private static XMLNode parseXML(StringBuilder source) {

        XMLNode n = new XMLNode();

        String tagHeader = source.substring(1, source.indexOf(">"));
        if (tagHeader.indexOf(" ") == -1) {
            n.setTag(tagHeader);
            source.delete(0, source.indexOf(">") + 1);
        } else {
            n.setTag(source.substring(1, source.indexOf(" ")));
            tagHeader = tagHeader.substring(tagHeader.indexOf(' '));

            //extract attributes
            while (tagHeader.indexOf('=') != -1) {
                tagHeader = tagHeader.substring(1);
                String attributeName = tagHeader.substring(0, tagHeader.indexOf('='));
                tagHeader = tagHeader.substring(tagHeader.indexOf('=') + 2);
                String attributeValue = tagHeader.substring(0, tagHeader.indexOf('\"'));
                tagHeader = tagHeader.substring(tagHeader.indexOf('\"') + 1);

                n.addAttribute(attributeName, attributeValue);
            }
            source.delete(0, source.indexOf(">") + 1);
        }

        if (source.charAt(0) != '<') {
            String textContent = source.substring(0, source.indexOf("<"));
            n.setText(textContent);
            source.delete(0, source.indexOf(">") + 1);
        } else {
            HashMap<String, List<XMLNode>> children = n.swapChildren(new HashMap<>());
            while (source.indexOf("</" + n.getTagName() + ">") != 0) {
                XMLNode child = parseXML(source);

                if (children.containsKey(child.getTagName())) {
                    children.get(child.getTagName()).add(child);
                } else {
                    List<XMLNode> childList = new LinkedList<>();
                    childList.add(child);
                    children.put(child.getTagName(), childList);
                }
            }

            n.swapChildren(children);

            source.delete(0, source.indexOf(">") + 1);
        }

        return n;
    }

    /**
     * Writes this to the given file.
     *
     * @param file
     *            The file to write this to
     */
    private void writeXML(BufferedWriter file) throws IOException {

        //write header
        if (!this.encoding.equals("")) {
            file.write("<?xml version=\"" + this.version + "\" encoding=\""
                    + this.encoding + "\"?>");
            file.newLine();
            file.newLine();
        } else {
            file.write("<?xml version=\"" + this.version + "\"?>");
            file.newLine();
            file.newLine();
        }

        //start writing nodes
        generateXMLText(this.root, file, 0);

        file.close();
    }

    /**
     * Generates a string ready for printing.
     *
     * @param root
     *            The root node of the XML tree
     *
     * @param file
     *            the file to write the XML data to
     *
     * @param indent
     *            The indentation to begin writing at
     * @throws IOException
     */
    private static void generateXMLText(XMLNode root, BufferedWriter file, int indent)
            throws IOException {
        for (int i = 0; i < indent; i++) {
            file.write("    ");
        }
        file.write('<');
        file.write(root.getTagName());

        if (root.numberOfAttributes() > 0) {
            HashMap<String, String> attributes = root.getAttributes();
            Set<Map.Entry<String, String>> pairs = attributes.entrySet();
            for (Map.Entry<String, String> pair : pairs) {
                file.write(" " + pair.getKey() + "=\"" + pair.getValue() + "\"");
            }
        }
        file.write(">");

        if (root.hasChildren()) {
            file.newLine();
            HashMap<String, List<XMLNode>> children = root.swapChildren(new HashMap<>());
            Set<Map.Entry<String, List<XMLNode>>> pairs = children.entrySet();
            for (Map.Entry<String, List<XMLNode>> pair : pairs) {
                List<XMLNode> nodeList = pair.getValue();
                for (XMLNode node : nodeList) {
                    generateXMLText(node, file, indent + 1);
                }
            }
            root.swapChildren(children);

            for (int i = 0; i < indent; i++) {
                file.write("    ");
            }
            file.write("</" + root.getTagName() + ">");
            file.newLine();
        } else {
            file.write(root.getText());
            file.write("</" + root.getTagName() + ">");
            file.newLine();
        }

    }

    //------------------Constructors----------------------------
    /**
     * No args constructor used to create a empty XMLDoc to later publish.
     */
    public XMLDoc() {
        this.root = new XMLNode();
        this.sourcePath = "";
        this.version = "1.0";
        this.encoding = "utf-8";
    }

    /**
     * Used to load all data from an XML file into an XMLDoc for reading or
     * editing.
     *
     * @param path
     *            The location of the XML file
     *
     * @throws IOException
     *
     * @requires The file is a valid XML file with a maximum of 1 tag per line
     */
    public XMLDoc(String path) throws IOException {
        final int four = 4;
        assert path.substring(path.length() - four).equals(".xml")
                : "[!] Not an XML file";

        this.sourcePath = path;
        BufferedReader file = new BufferedReader(new FileReader(path));

        //grab version and encoding of XML file and move the stream for parsing
        String formatTag = file.readLine();
        formatTag = formatTag.substring(formatTag.indexOf('\"') + 1);
        String version = formatTag.substring(0, formatTag.indexOf('\"'));
        this.version = version;
        formatTag = formatTag.substring(formatTag.indexOf('\"') + 1);

        if (formatTag.indexOf('\"') != -1) {
            formatTag = formatTag.substring(formatTag.indexOf('\"') + 1);
            String encoding = formatTag.substring(0, formatTag.indexOf('\"'));
            this.encoding = encoding;
        } else {
            this.encoding = "";
        }

        this.root = parseXML(new StringBuilder(convertToOneLine(file)));
    }

    //------------------------Methods--------------------------------

    /**
     * Saves the XML doc to the source XML file. (This will overwrite the
     * original)
     */
    public void save() throws IOException {
        assert !this.sourcePath.equals("") : "[!] Save location not specified";
        BufferedWriter file = new BufferedWriter(new FileWriter(this.sourcePath));

        this.writeXML(file);
    }

    /**
     * Writes an XML file.
     *
     * @param path
     *            The location to write the XML
     *
     * @throws IOException
     */
    public void save(String path) throws IOException {
        BufferedWriter file = new BufferedWriter(new FileWriter(path));

        this.writeXML(file);
    }

    /**
     * Sets the encoding of the XML file.
     *
     * @param encoding
     *            The encoding to be set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Getter method for the XML encoding.
     *
     * @return The encoding of this
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Sets the version of the XML file.
     *
     * @param version
     *            The version to be set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Getter method for the XML version.
     *
     * @return the version of this
     */
    public String getVersion() {
        return this.version;
    }
}
