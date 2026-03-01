package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a charged skill variant with a required charge duration.
 * Used in skill templates where holding the skill key charges to a more powerful version.
 *
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargedSkill")
public class ChargedSkill {

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(required = true)
	protected int time;

	/**
	 * Gets the charge duration in milliseconds.
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Gets the skill id of the charged variant.
	 */
	public int getId() {
		return id;
	}
}
