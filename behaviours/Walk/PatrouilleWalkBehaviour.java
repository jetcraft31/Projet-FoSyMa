package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import jade.core.Agent;

public class PatrouilleWalkBehaviour extends WalkBehaviour {
	
	private static final long serialVersionUID = 1L;
	
	
	private  HashMap<String,List<String>> dist1BeetweenNode;
	private ArrayList<String> patrouille;
	private int nextNodeToExplore;
	private ArrayList<String> roadToTheNextNode = new ArrayList<String>();
	
	private int maxVisitedSamePatrol = 5;
	
	public PatrouilleWalkBehaviour(final Agent myAgent) {
		super(myAgent,"Patrol");
		
		this.dist1BeetweenNode=this.myMap.getDist1BeetweenNode();
		this.patrouille = ((ExploreSoloAgent)this.myAgent).getNodeToPatrouillePerso();		
		this.roadToTheNextNode = chercherProchePointPatrouille(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
		this.nextNodeToExplore = this.patrouille.indexOf(this.roadToTheNextNode.get(this.roadToTheNextNode.size()-1));
		((ExploreSoloAgent)this.myAgent).endCoalition();
		((ExploreSoloAgent)this.myAgent).setCurrentWalkBehavior("Patrol");
	}

	public void actionChild(String myPosition) {
		if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("Patrol")) {
			this.finished=true;
		}else if(this.smell(myPosition)) {
			//si on sent un golem
			if(((ExploreSoloAgent)this.myAgent).lastSent.size()==2 && 
					((ExploreSoloAgent)this.myAgent).myMap.isTree(((ExploreSoloAgent)this.myAgent).lastSent.get(1).getLeft(),myPosition)) {
				if(((ExploreSoloAgent)this.myAgent).activateSearchtree) {
					//si le golem est dans un sous-graphe qui est un arbre, et que la chasse seul
					//est activée, on essaye de l'attraper seul
					this.myAgent.addBehaviour(new ChaseGolemInTree(this.myAgent));
				}else {
					//sinon on crée une coalition
					((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint=((ExploreSoloAgent)this.myAgent).lastSent.get(1).getLeft();
					this.myAgent.addBehaviour(new CreateCoalition(this.myAgent,((ExploreSoloAgent)this.myAgent).getNbAgentToBringIfSmell()));
				}
			}else {
				//sinon on crée une coalition
				((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint=((ExploreSoloAgent)this.myAgent).lastSent.get(1).getLeft();
				this.myAgent.addBehaviour(new CreateCoalition(this.myAgent,((ExploreSoloAgent)this.myAgent).getNbAgentToBringIfSmell()));
			}
			this.finished=true;
		}else{
			if(myPosition.equalsIgnoreCase(this.patrouille.get(this.nextNodeToExplore))) {
				
				if(this.patrouille.size() == (this.nextNodeToExplore + 1) ) {
					this.nextNodeToExplore = 0;
					((ExploreSoloAgent)this.myAgent).addOnePatrouilleVisited();

				}else {
					this.nextNodeToExplore ++;
				}
				this.roadToTheNextNode = determineRoad(myPosition);				
			}
			//Modif
			if(((ExploreSoloAgent)this.myAgent).getNbVisitedSamePatrol() == this.maxVisitedSamePatrol){
				((ExploreSoloAgent)this.myAgent).NextPatrouille();
			}else if(!this.roadToTheNextNode.isEmpty()) {
				
				move(this.roadToTheNextNode.get(0));

				if(((AbstractDedaleAgent)this.myAgent).getCurrentPosition() != myPosition) {
					// Si on a réussi à se déplacer alors on enlève ce point à explorer
					this.roadToTheNextNode.remove(0);		
				}
			}
		}
	}

	public ArrayList<String> determineRoad(String myPosition){
		
		ArrayList<String> road = new ArrayList<String>();
		String myPosTmp = myPosition;

		boolean finished;
		
		if(this.dist1BeetweenNode.get(this.patrouille.get(this.nextNodeToExplore)).contains(myPosition)) {
			road.add(this.patrouille.get(this.nextNodeToExplore));
			finished = true;
		}else {
			finished = false;
		}
		

		while(!finished) {

			for (int i =this.patrouille.indexOf(myPosition)-1;i>=0; i--) {
				
				if(this.dist1BeetweenNode.get(this.patrouille.get(i)).contains(myPosTmp)) {
					road.add(this.patrouille.get(i));
					myPosTmp = this.patrouille.get(i);
				}		
				
				if(this.dist1BeetweenNode.get(myPosTmp).contains(this.patrouille.get(this.nextNodeToExplore))) {
					road.add(this.patrouille.get(this.nextNodeToExplore));
					finished = true;
					break;
				}
				
			}
		}		
		return road;
	}
	
	private ArrayList<String> chercherProchePointPatrouille(String myPosition) {
		
		int bestDistance = -1;
		ArrayList<String> roadToPatrouille = new ArrayList<String>();
		
		for(String i : this.patrouille) {
			List<String> listTmp =((ExploreSoloAgent)this.myAgent).myMap.getShortestPath(myPosition, i);

			if(listTmp.size()<bestDistance || bestDistance == -1) {
				bestDistance = listTmp.size();
				roadToPatrouille = (ArrayList<String>) listTmp; 
			}
			
		}
		
		if(roadToPatrouille.isEmpty()) {
			roadToPatrouille.add(myPosition);
		}
			
			
		return roadToPatrouille;
	}

	void blockChild() {	
		((ExploreSoloAgent)this.myAgent).NextPatrouille();
	}

	
	void unblockChild() {
	}
	
	protected void Initialisation() {
		super.Initialisation();
		
		if(((ExploreSoloAgent)this.myAgent).updatePatrouille == true) {
			this.patrouille = ((ExploreSoloAgent)this.myAgent).getNodeToPatrouillePerso();		
			this.roadToTheNextNode = chercherProchePointPatrouille(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			this.nextNodeToExplore = this.patrouille.indexOf(this.roadToTheNextNode.get(this.roadToTheNextNode.size()-1));
			((ExploreSoloAgent)this.myAgent).updatePatrouille = false;
		}
	}
	
}

