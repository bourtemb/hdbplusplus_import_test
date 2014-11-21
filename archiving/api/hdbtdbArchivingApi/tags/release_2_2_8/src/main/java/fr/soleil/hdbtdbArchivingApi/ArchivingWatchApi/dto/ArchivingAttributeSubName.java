/*	Synchrotron Soleil
 *
 *   File          :  ArchivingAttributeSubName.java
 *
 *   Project       :  archiving_watcher
 *
 *   Description   :
 *
 *   Author        :  CLAISSE
 *
 *   Original      :  17 janv. 2006
 *
 *   Revision:  					Author:
 *   Date: 							State:
 *
 *   Log: ArchivingAttributeSubName.java,v
 *
 */
/*
 * Created on 17 janv. 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.soleil.hdbtdbArchivingApi.ArchivingWatchApi.dto;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import fr.soleil.hdbtdbArchivingApi.ArchivingTools.Tools.Chaine;
import fr.soleil.hdbtdbArchivingApi.ArchivingWatchApi.dto.comparators.ArchivingAttributeComparator;
import fr.soleil.hdbtdbArchivingApi.ArchivingWatchApi.tools.Tools;

/**
 * An equivalence class for all the archiving attributes that have the same
 * attribute name, ie. the final, attribute-specific part of an attribute's
 * complete name.
 * 
 * @author CLAISSE
 */
public class ArchivingAttributeSubName {

    private String name = "";

    private Hashtable<String, ArchivingAttribute> KOAttributes;
    private final ArchivingAttributeComparator comparator;

    /**
     * Default constructor
     */
    public ArchivingAttributeSubName() {
	KOAttributes = new Hashtable<String, ArchivingAttribute>();
	comparator = new ArchivingAttributeComparator();
    }

    /**
     * Returns the kOAttributes.
     * 
     * @return The list of KO attributes, which keys are the attributes complete
     *         names and which values are ArchivingAttribute objects
     */
    public Hashtable<String, ArchivingAttribute> getKOAttributes() {
	return KOAttributes;
    }

    /**
     * Sets the kOAttributes.
     * 
     * @param attributes
     *            The list of KO attributes, which keys are the attributes
     *            complete names and which values are ArchivingAttribute objects
     */
    public void setKOAttributes(final Hashtable<String, ArchivingAttribute> attributes) {
	KOAttributes = attributes;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(final String name) {
	this.name = name;
    }

    public void buildKOAttributes(final Map<String, ArchivingAttribute> _errorAttributes) {
	if (_errorAttributes == null || _errorAttributes.size() == 0) {
	    return;
	}
	for (final String key : _errorAttributes.keySet()) {
	    final ArchivingAttribute attribute = _errorAttributes.get(key);
	    final String attributeSubName = attribute.getAttributeSubName();

	    if (attributeSubName != null && attributeSubName.equals(name)) {
		addKOAttribute(attribute);
	    }
	}
    }

    /**
     * @param attribute
     */
    private void addKOAttribute(final ArchivingAttribute attribute) {
	if (attribute.isDetermined()) {
	    KOAttributes.put(attribute.getCompleteName(), attribute);
	}
    }

    public String getReport() {
	final StringBuffer buff = new StringBuffer();

	final String inLine = getName() + " :  VVVVVVVVVVVVVVVVVVVVVVVVVVVV";
	buff.append(inLine);
	buff.append(Tools.CRLF);

	final List<ArchivingAttribute> list = new Vector<ArchivingAttribute>();
	list.addAll(KOAttributes.values());
	Collections.sort(list, comparator);
	final Iterator<ArchivingAttribute> it = list.iterator();

	while (it.hasNext()) {
	    final ArchivingAttribute attribute = it.next();
	    final String attributeCompleteName = attribute.getCompleteName();
	    buff.append("    " + attributeCompleteName);
	    buff.append(Tools.CRLF);
	}

	final int size = inLine.length();
	final String outLine = Chaine.repeter("^", size);
	buff.append(outLine);

	return buff.toString();
    }

    public boolean hasKOAttributes() {
	if (KOAttributes == null) {
	    return false;
	}
	if (KOAttributes.size() == 0) {
	    return false;
	}

	return true;
    }
}