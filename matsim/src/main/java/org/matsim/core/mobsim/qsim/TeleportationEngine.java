package org.matsim.core.mobsim.qsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.comparators.TeleportationArrivalTimeComparator;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.vis.snapshotwriters.AgentSnapshotInfo;
import org.matsim.vis.snapshotwriters.TeleportationVisData;
import org.matsim.vis.snapshotwriters.TeleportationVisData.Cache;
import org.matsim.vis.snapshotwriters.VisLink;
import org.matsim.vis.snapshotwriters.VisMobsim;

public class TeleportationEngine implements DepartureHandler, MobsimEngine {
	/**
	 * Includes all agents that have transportation modes unknown to the
	 * QueueSimulation (i.e. != "car") or have two activities on the same link
	 */
	private Queue<Tuple<Double, MobsimAgent>> teleportationList = new PriorityQueue<Tuple<Double, MobsimAgent>>(30, new TeleportationArrivalTimeComparator());
	private final LinkedHashMap<Id, TeleportationVisData> teleportationData = new LinkedHashMap<Id, TeleportationVisData>();
	private InternalInterface internalInterface;
	private final Set<Id> trackedAgents = new HashSet<Id>();
	private final Map<Id, MobsimAgent> agents = new HashMap<Id, MobsimAgent>();
	
	/**
	 * I need a cache in TeleportationVisData that is not per Object, but for the run, and I don't want to use a 
	 * static variable.  Thus this cache.  kai, jul'12
	 */
	private Cache cache = null ;
	private boolean doVisualizeTeleportedAgents;
	private Collection<AgentSnapshotInfo> snapshots = new ArrayList<AgentSnapshotInfo>();
	
	public void removeTrackedAgent(Id id) {
		trackedAgents.remove(id);
	}

	public void addTrackedAgent(Id agentId) {
		trackedAgents.add(agentId);
	}


	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id linkId) {
		double arrivalTime = now + agent.getExpectedTravelTime();
		this.teleportationList.add(new Tuple<Double, MobsimAgent>(arrivalTime, agent));
		Id agentId = agent.getId();
		Link currLink = this.internalInterface.getMobsim().getScenario().getNetwork().getLinks().get(linkId);
		Link destLink = this.internalInterface.getMobsim().getScenario().getNetwork().getLinks().get(agent.getDestinationLinkId()) ;
		double travTime = agent.getExpectedTravelTime();
		TeleportationVisData agentInfo = new TeleportationVisData( now, agentId, currLink, destLink, travTime, this.cache  );
		this.teleportationData.put( agentId , agentInfo );
		return true;
	}
	
	public Collection<AgentSnapshotInfo> getTrackedAndTeleportedAgentsView() {
		return snapshots;
	}

	private void updateSnapshots() {
		snapshots.clear();
		for (TeleportationVisData agentInfo : teleportationData.values()) {
			if (this.doVisualizeTeleportedAgents || trackedAgents.contains(agentInfo.getId())) {
				snapshots.add(agentInfo);
			}
		}
		for (Id personId : trackedAgents) {
			Collection<AgentSnapshotInfo> positions = new ArrayList<AgentSnapshotInfo>();
			MobsimAgent agent = agents.get(personId);
			VisLink visLink = internalInterface.getMobsim().getNetsimNetwork().getVisLinks().get(agent.getCurrentLinkId());
			visLink.getVisData().getVehiclePositions(positions);
			for (AgentSnapshotInfo position : positions) {
				if (position.getId().equals(personId)) {
					snapshots.add(position);
				}
			}
		}
	}

	@Override
	public void doSimStep(double time) {
		this.updateTeleportedAgents(time);
		updateSnapshots();
		handleTeleportationArrivals();
	}
	
	private void updateTeleportedAgents(double time) {
		for (TeleportationVisData teleportationVisData : teleportationData.values()) {
			if (this.doVisualizeTeleportedAgents || trackedAgents.contains(teleportationVisData.getId())) {
				teleportationVisData.calculatePosition(time);
			}
		}
	}

	private void handleTeleportationArrivals() {
		double now = internalInterface.getMobsim().getSimTimer().getTimeOfDay();
		while (teleportationList.peek() != null) {
			Tuple<Double, MobsimAgent> entry = teleportationList.peek();
			if (entry.getFirst().doubleValue() <= now) {
				teleportationList.poll();
				MobsimAgent personAgent = entry.getSecond();
				personAgent.notifyArrivalOnLinkByNonNetworkMode(personAgent.getDestinationLinkId());
				personAgent.endLegAndComputeNextState(now);
				this.teleportationData.remove(personAgent.getId());
				internalInterface.arrangeNextAgentState(personAgent) ;
			} else {
				break;
			}
		}
	}

	@Override
	public void onPrepareSim() {
		this.doVisualizeTeleportedAgents = internalInterface.getMobsim().getScenario().getConfig().otfVis().isShowTeleportedAgents();
		for (MobsimAgent agent : ((VisMobsim) internalInterface.getMobsim()).getAgents()) {
			agents.put(agent.getId(), agent);
		}
	}

	@Override
	public void afterSim() {
		double now = internalInterface.getMobsim().getSimTimer().getTimeOfDay();
		for (Tuple<Double, MobsimAgent> entry : teleportationList) {
			MobsimAgent agent = entry.getSecond();
			EventsManager eventsManager = internalInterface.getMobsim().getEventsManager();
			eventsManager.processEvent(eventsManager.getFactory().createAgentStuckEvent(now, agent.getId(), agent.getDestinationLinkId(), agent.getMode()));
		}
		teleportationList.clear();
	}

	@Override
	public void setInternalInterface(InternalInterface internalInterface) {
		this.internalInterface = internalInterface;
	}

}