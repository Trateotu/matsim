package org.matsim.contrib.minibus.routeDesignScoring;

import java.util.ArrayList;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.minibus.genericUtils.TerminusStopDetector;
import org.matsim.contrib.minibus.operator.PPlan;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

/**
 * A route design scoring approach based on circuity (real network route length vs. beeline ditsnace)
 * 
 * @author gregor
 *
 */
public class RouteLengthVsBeelineScoring implements RouteDesignScoringFunction {

	public static final String SCORING_NAME = "RouteLengthVsBeelineScoring";
	/* maximum allowable circuity with score 0, if circuity scoring starts  */
	public final double maxCircuityWOPenalty;
	
	public RouteLengthVsBeelineScoring(ArrayList<String> parametersAsArrayList) {
		maxCircuityWOPenalty = Double.parseDouble(parametersAsArrayList.get(0));
	}

	/**
	 * @return The name of the scoring function
	 */
	public String getScoringFunctionName() {
		return SCORING_NAME;
	}

	@Override
	public double score(PPlan pplan) {
		// each PPlan should have exactly one TransitRoute
		NetworkRoute netRoute = pplan.getLine().getRoutes().values().iterator().next().getRoute();
		double lengthNetworkRoute = netRoute.getDistance();
		
//		double lengthNetworkRoute = netRoute.getStartLinkId();
//		for (Id<Link> linkId: netRoute.getLinkIds()) {
//			lengthNetworkRoute = lengthNetworkRoute + linkId;
//		}
//		lengthNetworkRoute = lengthNetworkRoute + netRoute.getEndLinkId();

		TransitStopFacility terminus1 = pplan.getStopsToBeServed().get(0);
		TransitStopFacility terminus2 = pplan.getStopsToBeServed().get(TerminusStopDetector.findStopIndexWithLargestDistance(pplan.getStopsToBeServed()));
		double lengthBeeline = CoordUtils.calcEuclideanDistance(terminus1.getCoord(), terminus2.getCoord());
		
		return ((lengthNetworkRoute/lengthBeeline) / maxCircuityWOPenalty - 1.0);
	}
	
}
