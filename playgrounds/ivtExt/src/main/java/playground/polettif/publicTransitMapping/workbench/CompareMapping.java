/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.polettif.publicTransitMapping.workbench;

import org.matsim.api.core.v01.network.Network;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import playground.polettif.publicTransitMapping.config.PublicTransitMappingConfigGroup;
import playground.polettif.publicTransitMapping.mapping.PTMapper;
import playground.polettif.publicTransitMapping.plausibility.StopFacilityHistogram;
import playground.polettif.publicTransitMapping.tools.NetworkTools;
import playground.polettif.publicTransitMapping.tools.ScheduleCleaner;
import playground.polettif.publicTransitMapping.tools.ScheduleShapeFileWriter;
import playground.polettif.publicTransitMapping.tools.ScheduleTools;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class CompareMapping {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		String inputGtfs = "data/gtfs/zvv/";
		String ct = "EPSG:2056";
		String unmappedMTS = "data/mts/fromGtfs/zvv_mostServices.xml.gz";
		String networkFile = "data/network/mm/zurich_detail.xml.gz";
		String output = args[0];


		// gtfs 2 matsim
//		Gtfs2TransitSchedule.run(inputGtfs, Gtfs2TransitSchedule.ServiceParam.dayWithMostServices.toString(), ct, unmappedMTS, null, output+"gtfs.shp");

		TransitSchedule schedule = ScheduleTools.readTransitSchedule(unmappedMTS);
		Network network = NetworkTools.readNetwork(networkFile);

		// cleanup
		Set<String> modesToRemove = new HashSet<>();
		modesToRemove.add("rail");
//		modesToRemove.add("tram");
		modesToRemove.add("gondola");
		modesToRemove.add("funicular");
		modesToRemove.add("ferry");
		ScheduleCleaner.removeTransitRouteByMode(schedule, modesToRemove);
		ScheduleCleaner.removeNotUsedStopFacilities(schedule);

		// ptm
		PublicTransitMappingConfigGroup ptmConfig = PublicTransitMappingConfigGroup.createDefaultConfig();
		ptmConfig.setTravelCostType(PublicTransitMappingConfigGroup.TravelCostType.travelTime);
		ptmConfig.setMaxNClosestLinks(6);
		ptmConfig.setLinkDistanceTolerance(1.2);
		ptmConfig.setMaxLinkCandidateDistance(60);
		ptmConfig.setNumOfThreads(4);
		ptmConfig.setModeRoutingAssignmentStr("bus:bus,car|tram:tram");
		ptmConfig.setOutputNetworkFile(output + "ptm_network.xml.gz");
		ptmConfig.setOutputScheduleFile(output + "ptm_schedule.xml.gz");

		PTMapper.run(ptmConfig, schedule, network);

		// shapeFile
		ScheduleShapeFileWriter.run(schedule, network, ct, output);

		// stop facilities histogram
		StopFacilityHistogram histogram = new StopFacilityHistogram(schedule);
		histogram.createCsv(output+"stopFacilities.csv");

		// check and shapefile
//		PlausibilityCheck check = new PlausibilityCheck(schedule, network, ct);
//		check.runCheck();
//		check.writeCsv(output+"allPlausibilityWarnings.csv");
//		check.writeResultShapeFiles(output);
	}
}
