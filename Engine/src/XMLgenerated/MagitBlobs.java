//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.08.10 at 01:11:01 PM IDT 
//


package XMLgenerated;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}MagitBlob" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "magitBlob"
})
@XmlRootElement(name = "MagitBlobs")
public class MagitBlobs {

    @XmlElement(name = "MagitBlob")
    protected List<MagitBlob> magitBlob;

    /**
     * Gets the value of the magitBlob property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the magitBlob property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMagitBlob().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MagitBlob }
     */
    public List<MagitBlob> getMagitBlob() {
        if (magitBlob == null) {
            magitBlob = new ArrayList<MagitBlob>();
        }
        return this.magitBlob;
    }

}
