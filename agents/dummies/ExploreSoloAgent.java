package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.TimeDiscussion;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.StartCommunicationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Determine.DeterminePatrouille;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.ExploBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import javafx.util.Pair;

public class ExploreSoloAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	
	public MapRepresentation myMap;
	public boolean Waiting = false;
	public boolean ExplorationRandom = false;
	public int speed=500;
		
	//Gestion des golems capturés et agents inactifs 
	private List<String> listAvailableAgent;
	private Set<String> listAgentNotAware = new HashSet<String>();
	private Set<String> capturedGolemPosition = new HashSet<String>();
	public int stenchDistanceEstimate = 1;
	
	private String currentWalkBehavior = "explo";
	
	// Time avec chaque personne pour ne pas s'harceler
	private HashMap<String,Integer> LastDiscussionTime= new HashMap<String,Integer>();
	
	// ******** Variable besoin d'aide 
	private boolean NeedHelpOrNeedToSaySomething = true;
	private boolean needMap = true;	
	private boolean needPlanificationOpenNode = true;
	private boolean needToPreventOtherAgentToChangePatrol = false; 

	
	// ************** Ensemble à visiter
	
	// Noeud perso à explorer après avoir determiner la planification pour observer les noeuds ouverts
	public HashSet<String> OpenNodeToExplorer = new HashSet<String>();
	
	// Faire une partition pour la patrouille
	private HashMap<String,ArrayList<String>> NodeToPatrouille = new HashMap<String,ArrayList<String>>();
	private HashSet<String> nodeNotAvailable = new HashSet<String>(); 
	
	public boolean updatePatrouille = false; 
	private int nbVisitedSamePatrol = 0;
	public boolean updateBehaviour = false;

	
	// Pour la coalition
	private int nbAgentToBringIfSmell=2;
	public List<Couple<String,List<Couple<Observation,Integer>>>> lastSent;
	public String coalitionMeetingPoint = null;
	public String nextCenter = null;
	public ArrayList<String> centers;
	public int nbAgentToBring;
	public int countWaitingInCenter=0;
	
	//pour échanger les noeuds de look for the golem
	public String nextNodeToVisitLookFor=null;
	private boolean needExchangeNodeLookFor=false;
	
	// Pour la chasse
	public HashMap<String,Pair<Integer,String>> chaseAgentsPosition = null;
	public Set<String> positionsToDetermineIfCatched;
	public boolean determiningIfCatched=false;
	
	//pour activer/désactiver l'option chasse seul
	public boolean activateSearchtree=true;
	
	
	
	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		List<Behaviour> lb=new ArrayList<Behaviour>(); 
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd .setName(getAID());
		
		try {
			DFService. register ( this , dfd ) ;
		} catch (FIPAException fe) {
			fe . printStackTrace () ; 
		}
		
		lb.add(new ExploBehaviour(this));
		
		lb.add(new StartCommunicationBehaviour(this));
		
		addBehaviour(new TimeDiscussion(this,speed));
		addBehaviour(new startMyBehaviours(this,lb));
		System.out.println("The  agent "+this.getLocalName()+ " is started");
	}
	
	// ********** Need Global Information
	public boolean getNeedHelpOrNeedToSaySomething() {
		if (this.getNeedMap() || this.getNeedPlanificationOpenNode() ||
				this.getInformAgentToChangePatrol() || this.getInformAgentAndPosNotAvailable() ||
				this.getNeedExchangeNodeLookFor()) { 
			this.NeedHelpOrNeedToSaySomething = true;
		}else {
			this.NeedHelpOrNeedToSaySomething = false;
		}
		
		return this.NeedHelpOrNeedToSaySomething;
	}
	
	// ********* Need information for the map
	public boolean getNeedMap() {
		
		if(this.myMap.getOpenNode(this.getCurrentPosition(),this.getNodeNotAvailableAll()).isEmpty()) {
			this.needMap = false;
		}else {
			this.needMap = true;
		}
		return this.needMap;
	}
	
	// ********  Need information for open node planning
	public boolean getNeedPlanificationOpenNode() {	
		
		if(this.myMap.getOpenNode(this.getCurrentPosition(),this.getNodeNotAvailableAll()).isEmpty() ) {
			this.needPlanificationOpenNode = false;
		}else {
			this.needPlanificationOpenNode = true;
		}
		
		return this.needPlanificationOpenNode;
	}
	
	// *********** Inform other agent that we have taken his patrol 
	public boolean getInformAgentToChangePatrol() {
		
		if(this.needToPreventOtherAgentToChangePatrol) {
			return true;
		}else {
			return false;
		}
	}
	// Information Delivred
	public void setInformAgentToChangePatrolDelivred() {
		this.needToPreventOtherAgentToChangePatrol = false;
	}
	
	
	// ************ Patrouille 
	public HashMap<String,ArrayList<String>> getNodeToPatrouille(){
		return (HashMap<String, ArrayList<String>>) this.NodeToPatrouille.clone();
	}
	
	public void setNodeToPatrouille(HashMap<String,ArrayList<String>> PartitionAgent) {
		this.NodeToPatrouille = PartitionAgent;	
	}
	
	public ArrayList<String> getNodeToPatrouillePerso(){
		ArrayList<String> res = new ArrayList<String>();
		if(this.NodeToPatrouille != null && !this.NodeToPatrouille.isEmpty()) {
			res.addAll(this.NodeToPatrouille.get(this.getLocalName()));
		}
		return res;
	}
	
	public void EchangeNodeToPatrouille(String nameAgent1,String nameAgent2) {
		
		ArrayList<String> PatrouilleAgent2 = this.NodeToPatrouille.get(nameAgent1);
		ArrayList<String> PatrouilleAgent1 = this.NodeToPatrouille.get(nameAgent2);		
		
		this.NodeToPatrouille.put(nameAgent1, PatrouilleAgent1);	
		this.NodeToPatrouille.put(nameAgent2, PatrouilleAgent2);	
		
		this.updatePatrouille = true;
		this.newPatrouilleToVisite();
		this.needToPreventOtherAgentToChangePatrol = true;


	}
	
	public void addOnePatrouilleVisited() {
		this.nbVisitedSamePatrol ++;
		if(this.nbVisitedSamePatrol ==1) {
			setInformAgentToChangePatrolDelivred();
		}
	}
	
	public int getNbVisitedSamePatrol() {
		return this.nbVisitedSamePatrol;
	}
	
	public void newPatrouilleToVisite() {
		this.nbVisitedSamePatrol = 0;
	}
	
	// On prend une nouvelle patrouille de façon aléatoire
	public void NextPatrouille() {
		
		int index = getListAvailableAgent().indexOf(this.getLocalName());
		
		Random r = new Random();
		int value = r.nextInt(this.getListAvailableAgent().size());
		
		index = (value+index)%(getListAvailableAgent().size());
		
		EchangeNodeToPatrouille(this.getLocalName(),getListAvailableAgent().get(index));
		System.out.println("Next Patrol for :" + this.getLocalName());
	}
	
	//  **** A utiliser après avoir fait merger la carte, pour mettre les noeuds ouverso perso à jour
	public void MiseAjourOpenNodePerso() {
		HashSet<String> NodeClose =this.myMap.getClosedNode();
		if(this.OpenNodeToExplorer != null && this.OpenNodeToExplorer.size()>0) {
			
			HashSet<String> set = new HashSet<String>();
			set.addAll(this.OpenNodeToExplorer);
			for(String i : set) {
				
				if(NodeClose.contains(i)) {
					this.OpenNodeToExplorer.remove(i);
				}
				
			}
		}
	}

	
	// *********** ListAgent
	public List<String> getListAllAgent() { 
			
		List<String> listAgent = new ArrayList<String>();
		
		DFAgentDescription[] result = null ;
		try {
			result = DFService.search( this , new DFAgentDescription()) ;
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		for (DFAgentDescription i : result) {
			if( !listAgent.contains(i.getName().getLocalName()) ) {
				listAgent.add(i.getName().getLocalName());
				AddTimeToAgent(i.getName().getLocalName(),0);
			}
		}
		Collections.sort(listAgent);
		
		return listAgent;
	}
	
	// Tous les agents encore disponibles (Donc ceux qui n'ont pas capturés de golem)
	public List<String> getListAvailableAgent(){
		
		List<String> list = new ArrayList<String>();
		
		if(this.listAvailableAgent == null) {
			this.listAvailableAgent = new ArrayList<String>();
			this.listAvailableAgent.addAll(this.getListAllAgent());
		}else {
			list.addAll(this.listAvailableAgent);
		}	
		
		return list;
	}
	
	// Remove the agents not available and their position and other position (Golem, ...)
	public void removeAvailableAgentAndPos(Set<String> name,Set<String> posNotAvailable,Set<String> golemPosition){
		
		this.getListAvailableAgent();
		Set<String> intersectSet = new HashSet<String>(this.listAvailableAgent);
		intersectSet.retainAll(name);
		if(intersectSet.size()!=0 || !this.nodeNotAvailable.containsAll(posNotAvailable) ||
				!this.capturedGolemPosition.containsAll(golemPosition)) {

			this.listAvailableAgent.removeAll(name);
			this.nodeNotAvailable.addAll(posNotAvailable);
			this.capturedGolemPosition.addAll(golemPosition);
		
			this.listAgentNotAware.addAll(this.listAvailableAgent);
			this.removeAgentAware(name);
			this.removeAgentAware(this.getLocalName());
			this.updatePatrouille = true;
			this.updateBehaviour = true;
		
			if(!this.getCurrentWalkBehavior().equalsIgnoreCase("catchedGolem")) {
				this.addBehaviour(new DeterminePatrouille(this,this.getListAvailableAgent(), 
					this.getNodeNotAvailableAll(), false));
			}
		}
	}
	
	// Remove the agents not available and their position and other position (Golem, ...)
	public void removeAvailableAgentAndPos(String name,Set<String> posNotAvailable,String golemPosition){
			
		this.getListAvailableAgent();
		if(this.listAvailableAgent.contains(name) || !this.nodeNotAvailable.containsAll(posNotAvailable) ||
					!this.capturedGolemPosition.contains(golemPosition)) {
			//System.out.println(this.getLocalName()+" : I learned something (from me)");
			this.listAvailableAgent.remove(name);
			this.nodeNotAvailable.addAll(posNotAvailable);
			this.capturedGolemPosition.add(golemPosition);
			this.listAgentNotAware.addAll(this.listAvailableAgent);
			this.removeAgentAware(name);
			this.removeAgentAware(this.getLocalName());
			this.updatePatrouille = true;
			this.updateBehaviour = true;
			
			if(!this.getCurrentWalkBehavior().equalsIgnoreCase("catchedGolem")) {
				this.addBehaviour(new DeterminePatrouille(this,this.getListAvailableAgent(), 
					this.getNodeNotAvailableAll(), false));
			}
		}
	}

	public boolean getInformAgentAndPosNotAvailable() {
		if(this.listAgentNotAware.isEmpty()) {
			return false;
		}else {
			return true;
		}
	}
	
	public Set<String> getNodeNotAvailable(){
		Set<String> set = new HashSet<String>();
		set.addAll(this.nodeNotAvailable);
		return set;
	}
	// Noeud non disponible + noeuds perso non disponible
	public Set<String> getNodeNotAvailableAll(){
		Set<String> set = new HashSet<String>();
		set.addAll(this.nodeNotAvailable);
		set.addAll(this.determineAllNodeNotAccesible());
		return set;
	}
	
	private Set<String> determineAllNodeNotAccesible() {
		
		Set<String> node =  this.myMap.getAllNode();
		Set<String> NodeNotAccessible = new  HashSet<String>();
		String pos = this.getCurrentPosition();
		
		for (String i : node) {
			if(this.myMap.getShortestPath(pos, i, this.nodeNotAvailable) == null){
				NodeNotAccessible.add(i);
			}
			
		}
		return NodeNotAccessible;
	}

	public void removeAgentAware(Set<String> allAgentName) {
		this.listAgentNotAware.removeAll(allAgentName);
	}
	public void removeAgentAware(String AgentName) {
		this.listAgentNotAware.remove(AgentName);
	}
	public Set<String> getListAgentNotAware() {
		Set<String> set = new HashSet<String>();
		set.addAll(this.listAgentNotAware);
		return set;
	}
	
	// ***** Discussion Time
	public void AddTimeToAgent(String nameAgent,int time) {
		
		if(this.LastDiscussionTime.get(nameAgent) != null) {
			this.LastDiscussionTime.put(nameAgent,this.LastDiscussionTime.get(nameAgent) +time);
		}else {
			this.LastDiscussionTime.put(nameAgent,time);
		}
	}
	
	public HashMap<String,Integer> getLastDiscussionTime(){
		return this.LastDiscussionTime;
	}
	
	// ************* 
	
	public void removeCenter(String center) {
		if(this.centers.contains(center)) {
			this.centers.remove(center);
		}
	}
	
	public void updateNextCenter() {
		if(this.centers.size()==0) {
			this.nextCenter=null;
		}else {
			this.nextCenter=this.determineClosestCenter(this.getCurrentPosition());
		}
	}
	
	public String determineClosestCenter(String myPosition) {
		return this.myMap.nearNode(new HashSet<String>(this.centers),myPosition,this);
	}
	
	public ArrayList<String> getCenters(){
		return this.centers;
	}
	
	public int getNbAgentToBring(){
		return this.nbAgentToBring;
	}
	
	public void reduceNbAgentToBring(){
		this.nbAgentToBring--;
	}
	
	public String getCurrentWalkBehavior(){
		return this.currentWalkBehavior;
	}
	
	public void setCurrentWalkBehavior(String behavior){
		this.currentWalkBehavior=behavior;
	}
	
	public Set<String> getCapturedGolemPosition(){
		return this.capturedGolemPosition;
	}
	
	public void addCapturedGolemPosition(Set<String> positions){
		this.capturedGolemPosition.addAll(positions);
	}
	
	public void addCapturedGolemPosition(String position){
		this.capturedGolemPosition.add(position);
	}

	public Set<String> getListCoalitionAgents() {
		return this.chaseAgentsPosition.keySet();
	}
	
	public void startCoalition(String meetingPoint) {
		this.coalitionMeetingPoint=meetingPoint;
		this.chaseAgentsPosition=new HashMap<String, Pair<Integer,String>>();
	}

	public void endCoalition() {
		this.coalitionMeetingPoint=null;
		this.chaseAgentsPosition=null;
	}

	public int getNbAgentToBringIfSmell() {
		return this.nbAgentToBringIfSmell;
	}

	public void setNeedExchangeNodeLookFor(boolean b) {
		this.needExchangeNodeLookFor=b;
	}
	
	public boolean getNeedExchangeNodeLookFor() {
		return this.needExchangeNodeLookFor;
	}
}
