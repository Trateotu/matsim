package org.matsim.contrib.minibus.routeDesignScoring;

import org.matsim.contrib.minibus.operator.PPlan;

public interface RouteDesignScoringFunction {
	
	public double score(PPlan pplan);
	
	/**
	 * @return The name of the scoring function
	 */
	public String getScoringFunctionName();
		

}
