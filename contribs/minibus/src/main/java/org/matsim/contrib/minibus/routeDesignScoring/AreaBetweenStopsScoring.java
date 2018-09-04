package org.matsim.contrib.minibus.routeDesignScoring;

import java.util.ArrayList;

import org.matsim.contrib.minibus.operator.PPlan;

public class AreaBetweenStopsScoring implements RouteDesignScoringFunction {

	public static final String SCORING_NAME = "AreaBetweenStops";
	
	public AreaBetweenStopsScoring(ArrayList<String> parametersAsArrayList) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return The name of the scoring function
	 */
	public String getScoringFunctionName() {
		return SCORING_NAME;
	}

	@Override
	public double score(PPlan pplan) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
