package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.HashSet;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import jade.core.Agent;

public class LookForTheGolem extends WalkBehaviour {

	private static final long serialVersionUID = 1L;
	private boolean arrived;
	
	private int distanceMaxNodesToVisit=8;
	
	private HashSet<String> nodesToVisit;
	
	private String centerPoint;
	
	//si on sait qu'un agent se trouve probablement près d'un noeud, alors on explore les
	//noeuds près de celui-ci
	public LookForTheGolem(final Agent myAgent) {
		super(myAgent,"LookForTheGolem");
		((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=null;
		this.centerPoint=((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint;
		this.initiate();
	}
	
	public LookForTheGolem(final Agent myAgent, String center) {
		super(myAgent,"LookForTheGolem");
		((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=null;
		this.centerPoint=center;
		this.initiate();
	}
	
	//initialises les noeuds à visiter
	private void initiate() {
		this.nodesToVisit=this.computeNodesToVisit();
		this.determineNextNodeToVisit();
		((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=this.myMap.nearNode(this.nodesToVisit,this.centerPoint,
				((ExploreSoloAgent)this.myAgent));
		//System.out.println(this.myAgent.getLocalName()+" : size look for : "+this.nodesToVisit.size());
		this.arrived=this.centerPoint.equalsIgnoreCase(((ExploreSoloAgent)this.myAgent).getCurrentPosition());
	}

	//détermine le prochain noeud à visiter
	private void determineNextNodeToVisit() {
		if(this.nodesToVisit.isEmpty()) {
			((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=null;
		}else if(((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor==null){
			((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=this.myMap.nearNode(this.nodesToVisit,this.centerPoint,
					((ExploreSoloAgent)this.myAgent));
			List<String> neighbors = this.myMap.getDist1BeetweenNode().get(this.centerPoint);
			int maxDist=-1;
			for(String node:neighbors) {
				List<String> path=this.myMap.getShortestPath(((ExploreSoloAgent)this.myAgent).getCurrentPosition(),
						node, ((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll());
				if(path!=null) {
					int dist=path.size();
					if(dist>=maxDist) {
						maxDist=dist;
						((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=node;
					}
				}

			}
		}else {
			((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=this.myMap.nearNode(this.nodesToVisit,((ExploreSoloAgent)this.myAgent).getCurrentPosition(),
					((ExploreSoloAgent)this.myAgent));
		}
	}

	//calcul la liste des noeuds à visiter 
	//il s'agit des noeuds dans un rayon de taille distanceMaxNodesToVisit autour de centerPoint
	private HashSet<String> computeNodesToVisit() {
		HashSet<String> nodes= new HashSet<String>();
		for(String node:((ExploreSoloAgent)this.myAgent).myMap.getClosedNode()) {
			if(!((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll().contains(node)) {
				if(((ExploreSoloAgent)this.myAgent).myMap.getShortestPath(
						this.centerPoint,node).size()<=this.distanceMaxNodesToVisit
						&& !node.equalsIgnoreCase(this.centerPoint)) {
					nodes.add(node);
				}
			}
		}
		return nodes;
	}
	
	void actionChild(String myPosition) {
		if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("LookForTheGolem")) {
			this.finished=true;
			
		//si on sent le golem, on le chasse
		}else if(this.smell(myPosition)) {
			this.myAgent.addBehaviour(new CoalitionChase(this.myAgent));
			this.finished=true;
			//System.out.println(this.myAgent.getLocalName()+" : I smelt it !");
			
		}else {
			if(((ExploreSoloAgent)this.myAgent).updateBehaviour) {
				this.nodesToVisit.removeAll(((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll());
				this.determineNextNodeToVisit();
				((ExploreSoloAgent)this.myAgent).updateBehaviour=false;
			}
			if(myPosition.equalsIgnoreCase(((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor)) {
				this.nodesToVisit.remove(((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor);
				this.determineNextNodeToVisit();
			}
			if(this.nodesToVisit.isEmpty()) {
				this.myAgent.addBehaviour(new PatrouilleWalkBehaviour(this.myAgent));
				this.finished=true;
				//System.out.println(this.myAgent.getLocalName()+" : The golem went to far, I give up");
			}else if(!((ExploreSoloAgent)this.myAgent).Waiting) {
				if(this.arrived) {
					
					move(this.myMap.getShortestPath(myPosition, ((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor).get(0));
					
				}else {
					//si on est pas arrivé au centerPoint, on y va
					if(!((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll().contains(this.centerPoint)) {
						String nextNode=this.myMap.getShortestPath(myPosition,this.centerPoint).get(0);	
						move(nextNode);
						
						myPosition=((ExploreSoloAgent)this.myAgent).getCurrentPosition();
						
						if(((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint.equalsIgnoreCase(myPosition)) {
							//System.out.println(this.myAgent.getLocalName()+" : arrived !");
							this.arrived=true;
						}
					}else {
						this.myAgent.addBehaviour(new PatrouilleWalkBehaviour(this.myAgent));
						this.finished=true;
						//System.out.println(this.myAgent.getLocalName()+" : The center isn't available anymore");
					}
				}
			}
		}
			
			
		
	} 

	//si l'agent est bloqué contre un autre agent il va essayer d'échanger un noeud à visiter
	//pour ce débloquer
	void blockChild() {
		((ExploreSoloAgent)this.myAgent).setNeedExchangeNodeLookFor(true);
		//System.out.println(this.myAgent.getLocalName()+" : i'm stuck");
	}

	void unblockChild() {
		((ExploreSoloAgent)this.myAgent).setNeedExchangeNodeLookFor(false);
	}

}

