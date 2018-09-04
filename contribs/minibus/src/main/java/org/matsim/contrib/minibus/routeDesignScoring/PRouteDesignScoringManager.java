/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.contrib.minibus.routeDesignScoring;

import org.apache.log4j.Logger;
import org.matsim.contrib.minibus.PConfigGroup;
import org.matsim.contrib.minibus.PConfigGroup.PScoringSettings;
import org.matsim.contrib.minibus.PConfigGroup.PStrategySettings;
import org.matsim.contrib.minibus.fare.StageContainerCreator;
import org.matsim.contrib.minibus.fare.TicketMachineI;
import org.matsim.contrib.minibus.operator.TimeProvider;
import org.matsim.core.gbl.MatsimRandom;

import java.util.ArrayList;

/**
 * Loads route design scoring functions from config.
 * 
 * @author gleich
 *
 */
public final class PRouteDesignScoringManager {
	
	private final static Logger log = Logger.getLogger(PRouteDesignScoringManager.class);
	
	private final ArrayList<RouteDesignScoringFunction> scoringFunctions = new ArrayList<>();
	private final ArrayList<Double> weights = new ArrayList<>();
	private final ArrayList<Integer> disableInIteration = new ArrayList<>();

	public void init(PConfigGroup pConfig, TimeProvider timeProvider) {
		for (PScoringSettings settings : pConfig.getScoringSettings()) {
			String classname = settings.getModuleName();
			double rate = settings.getWeight();
			if (rate == 0.0) {
				log.info("The following strategy has a weight set to zero. Will drop it. " + classname);
				continue;
			}
			RouteDesignScoringFunction scoringFunction = loadScoring(classname, settings, timeProvider);
			this.addScoringFunction(scoringFunction, rate, settings.getDisableInIteration());
		}
		
		log.info("enabled with " + this.scoringFunctions.size()  + " strategies");
	}

	private RouteDesignScoringFunction loadScoring(final String name, final PScoringSettings settings, TimeProvider timeProvider) {
		RouteDesignScoringFunction scoringFunction = null;
		
		if (name.equals(AreaBetweenStopsScoring.SCORING_NAME)) {
			scoringFunction = new AreaBetweenStopsScoring(settings.getParametersAsArrayList());
		} else if (name.equals(AreaBetweenNetworkLinksScoring.SCORING_NAME)) {
			scoringFunction = new AreaBetweenNetworkLinksScoring(settings.getParametersAsArrayList());
		} else if (name.equals(RouteLengthVsBeelineScoring.SCORING_NAME)) {
			scoringFunction = new RouteLengthVsBeelineScoring(settings.getParametersAsArrayList());
		}
		
		if (scoringFunction == null) {
			log.error("Could not initialize scoringFunction named " + name);
		}
		
		return scoringFunction;
	}

	private void addScoringFunction(final RouteDesignScoringFunction strategy, final double weight, int disableInIteration) {
		this.scoringFunctions.add(strategy);
		this.weights.add(weight);
		this.disableInIteration.add(disableInIteration);
	}

	/**
	 * Changes the weights of each strategy to zero and removes it from the choice set if it needs to be disabled
	 * 
	 * @param iteration
	 */
	public void updateScoringFunctions(int iteration) {
		for (int i = 0; i < this.disableInIteration.size(); i++) {
			if (this.disableInIteration.get(i) == iteration) {
				this.weights.set(i, 0.0);
				this.scoringFunctions.set(i, null);
			}
		}
	}
	
	public boolean routeDesignScoringIsActive() {
		for (double weight: weights) {
			if (weight != 0) {
				return true;
			}
		}
		// either no routeDesignScoring set in config or weight is 0, rendering it useless
		return false;
	}
	
	public getRouteDesignScoring(int iteration) {
		
		for (int i = 0; i < disableInIteration.size(); i++) {
			if (disableInIteration >= iteration) {
				
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("Strategies: ");
		strBuffer.append(this.scoringFunctions.get(0).getScoringFunctionName()); strBuffer.append(" ("); strBuffer.append(this.weights.get(0)); strBuffer.append(")");
		
		for (int i = 1; i < this.scoringFunctions.size(); i++) {
			strBuffer.append(", "); strBuffer.append(this.scoringFunctions.get(i).getScoringFunctionName()); strBuffer.append(" ("); strBuffer.append(this.weights.get(i)); strBuffer.append(")");
		}
		return strBuffer.toString();
	}
}