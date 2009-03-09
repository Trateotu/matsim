/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.basic.signalsystemsconfig;

import org.matsim.interfaces.basic.v01.Id;
/**
 * 
 * @author dgrether
 *
 */
public interface BasicSignalGroupSettings {

	public Integer getRoughCast();

	public void setRoughCast(Integer roughCast);

	public Integer getDropping();

	public void setDropping(Integer dropping);

	public Integer getInterimTimeRoughcast();

	public void setInterimTimeRoughcast(Integer interimTimeRoughcast);

	public Integer getInterimTimeDropping();

	public void setInterimTimeDropping(Integer interimTimeDropping);

	public Id getReferencedSignalGroupId();

}