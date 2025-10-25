import java.util.HashMap;
import java.util.List;

/**
 * A data structured used to model an XML tag.
 *
 * @author Ben Rappleye
 */
public class XMLNode {

    /**
     * The tag name of the XML element.
     */
    private String tagName;

    /**
     * A list of all attributes of this.
     */
    private HashMap<String, String> attributes;

    /**
     * The text content of this if this is a leaf node.
     */
    private String text;

    /**
     * A list of all the children of this.
     */
    private HashMap<String, List<XMLNode>> children;

    /**
     * initializes all private members.
     */
    private void createNewRep() {
        this.tagName = "";
        this.attributes = new HashMap<>();
        this.text = "";
        this.children = new HashMap<>();
    }

    /**
     * No args constructor.
     */
    public XMLNode() {
        this.createNewRep();
    }

    /**
     * Used to set the name of this.
     *
     * @param s
     *            the name to set this to
     */
    public void setTag(String s) {
        this.tagName = s;
    }

    /**
     * Returns the name of the tag that this represents.
     *
     * @return the name of this
     */
    public String getTagName() {
        return this.tagName;
    }

    /**
     * Adds an attribute to this.
     *
     * @param key
     *            The attribute name
     *
     * @param value
     *            The attribute value
     *
     * @requires The attribute is not already in this
     */
    public void addAttribute(String key, String value) {
        assert !this.attributes.containsKey(key) : "[!] Duplicate attribute";

        this.attributes.put(key, value);
    }

    /**
     * Gives the value of an attribute.
     *
     * @param key
     *            The attribute to look at
     *
     * @return The value of the attribute
     *
     * @requires This contains the attribute
     */
    public String getAttributeValue(String key) {
        assert this.attributes.containsKey(key) : "[!] Attribute not found";

        return this.attributes.get(key);
    }

    /**
     * Tells whether or not this contains a certain attribute.
     *
     * @param key
     *            The attribute to look for
     * @return true if this contains that attribute, false otherwise
     */
    public boolean hasAttribute(String key) {
        return this.attributes.containsKey(key);
    }

    /**
     * Sets the text content of the tag.
     *
     * @param s
     *            the text to set the content to
     * @requires this is a leaf node
     */
    public void setText(String s) {
        assert this.children.size() == 0 : "[!] Must be leaf node to have text content";

        this.text = s;
    }

    /**
     * Gives the value of the text in this.
     *
     * @return the text content of this
     */
    public String getText() {
        return this.text;
    }

    /**
     * Used to get an edit the list of children.
     *
     * @param newContext
     *            A list of children to be used by this
     * @return The previous list of children
     */
    public HashMap<String, List<XMLNode>> swapChildren(
            HashMap<String, List<XMLNode>> newContext) {
        HashMap<String, List<XMLNode>> temp = this.children;
        this.children = newContext;
        return temp;
    }

    /**
     * Getter method for the XML node's attributes.
     *
     * @return The attributes of this
     */
    public HashMap<String, String> getAttributes() {
        return this.attributes;
    }

    /**
     * Reports the number of attributes of this.
     *
     * @return The number of attributes in this
     */
    public int numberOfAttributes() {
        return this.attributes.size();
    }

    /**
     * Reports whether or not this has any children.
     *
     * @return true if this has children, false otherwise
     */
    public boolean hasChildren() {
        return this.children.size() > 0;
    }
}
