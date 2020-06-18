package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import jade.core.Agent;
import javafx.util.Pair;

public class CreateCoalition extends WalkBehaviour {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//permet de créer une coalition pour attraper un golem
	public CreateCoalition (final Agent myAgent, int nbAgentToBring) {
		super(myAgent,"createCoal");
		
		((ExploreSoloAgent)this.myAgent).nbAgentToBring=nbAgentToBring;
		((ExploreSoloAgent)this.myAgent).centers=this.determineCenters();
		((ExploreSoloAgent)this.myAgent).updateNextCenter();
		((ExploreSoloAgent)this.myAgent).chaseAgentsPosition=new HashMap<String, Pair<Integer,String>>();
	}
	 
	public CreateCoalition (final Agent myAgent, int nbAgentToBring, ArrayList<String> centers) {
		super(myAgent,"createCoal");
		
		((ExploreSoloAgent)this.myAgent).nbAgentToBring=nbAgentToBring;
		((ExploreSoloAgent)this.myAgent).centers=centers;
		((ExploreSoloAgent)this.myAgent).updateNextCenter();
	}
	
	//on calcul les centres des patrouilles pour aller chercher les
	//autres agents
	private ArrayList<String> determineCenters() {
		ArrayList<String> centers = new ArrayList<String>();
		for (Entry<String, ArrayList<String>> entry : ((ExploreSoloAgent)this.myAgent).getNodeToPatrouille().entrySet()) {
			if(!entry.getKey().equalsIgnoreCase(myAgent.getLocalName())) {
				Set<String> patrouille = new HashSet<String>(entry.getValue());
				centers.add(((ExploreSoloAgent)this.myAgent).myMap.determineCenter(patrouille));
			}
		}
		return centers;
	}

	@Override
	public void actionChild(String myPosition) {
		if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("createCoal")) {
			this.finished=true;
		}else if(!((ExploreSoloAgent)this.myAgent).Waiting) {
			if(((ExploreSoloAgent)this.myAgent).updateBehaviour) {
				((ExploreSoloAgent)this.myAgent).centers.removeAll(((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll());
				((ExploreSoloAgent)this.myAgent).updateNextCenter();
				((ExploreSoloAgent)this.myAgent).updateBehaviour=false;
			}
			if(((ExploreSoloAgent)this.myAgent).nbAgentToBring==0 || ((ExploreSoloAgent)this.myAgent).centers.size()==0) {
				this.myAgent.addBehaviour(new LookForTheGolem(this.myAgent));
				this.finished=true;
			}else if(((ExploreSoloAgent)this.myAgent).nextCenter.equalsIgnoreCase(myPosition)) {
				//si on est au centre d'une patrouille, on attend qu'un agent arrive pour communiquer avec
				//System.out.println(this.myAgent.getLocalName()+" : I arrived, waiting : "+((ExploreSoloAgent)this.myAgent).countWaitingInCenter);
				((ExploreSoloAgent)this.myAgent).countWaitingInCenter++;
				if(((ExploreSoloAgent)this.myAgent).countWaitingInCenter>50) {
					//System.out.println(this.myAgent.getLocalName()+" : I'll look for another agent");

					((ExploreSoloAgent)this.myAgent).centers.remove(((ExploreSoloAgent)this.myAgent).nextCenter);
					((ExploreSoloAgent)this.myAgent).updateNextCenter();
				}
			}else {
				//sinon on va au centre d'une patrouille
				String nextNode=this.myMap.getShortestPath(myPosition,((ExploreSoloAgent)this.myAgent).nextCenter).get(0);	
				move(nextNode);
				if(((ExploreSoloAgent)this.myAgent).getCurrentPosition().equalsIgnoreCase(((ExploreSoloAgent)this.myAgent).nextCenter)) {
					((ExploreSoloAgent)this.myAgent).countWaitingInCenter=0;
				}
			}
			
		}
	}
	
	void blockChild() {		
	}
	
	@Override
	void unblockChild() {
		// TODO Auto-generated method stub
		
	}
}
