package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Determine.DeterminePatrouille;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;


public class ExploBehaviour extends WalkBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private HashSet<String> NodePersoToExplore;

	private String lastMove;
	

	public ExploBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent,"Explo");
		this.nbEtapePourEtreBloque=20;
	}

	public void actionChild(String myPosition) {
		if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("Explo")) {
			this.finished=true;
		}else {
	
			update(myPosition);
			
			//3) while openNodes is not empty, continues.
			if (this.myMap.getOpenNode(myPosition,((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll()).isEmpty()){
				
				//Explo finished
				System.out.println("Exploration successfully done, behaviour removed.");
				this.myAgent.addBehaviour(new DeterminePatrouille(this.myAgent,
						((ExploreSoloAgent)this.myAgent).getListAvailableAgent(), 
						((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll(),true));
				this.finished=true;
				
			}else{
				String nextNode = null;
				List<String> road = null;
				String nodeTmp =null;

				if( this.NodePersoToExplore.size() == 0 ) {	
					
					nodeTmp = this.myMap.nearNode(this.myMap.getOpenNode(myPosition,((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll())
							,myPosition,((ExploreSoloAgent)this.myAgent));
					if(nodeTmp != null) {
						road=this.myMap.getShortestPath(myPosition, nodeTmp);
					}
					
				}else {
					
					nodeTmp = this.myMap.nearNode(this.NodePersoToExplore,myPosition,
							((ExploreSoloAgent)this.myAgent));
					if(nodeTmp != null) {
						road=this.myMap.getShortestPath(myPosition, nodeTmp);
					}
				}
				
				if(road != null && !road.isEmpty()) {
					nextNode = road.get(0);
				}
				
				move(nextNode);
				this.lastMove=nextNode;
				
			}
		}
	}
	
	private void update(String myPosition) {
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		
		/*for(String node:((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll()) {
			this.myMap.addNode(node,MapAttribute.closed);
		}*/
		
		
		this.myMap.addNode(myPosition,MapAttribute.closed);
		
		if(this.NodePersoToExplore.contains(myPosition)) {
			this.NodePersoToExplore.remove(myPosition);
		}

		//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
		while(iter.hasNext()){
			String nodeId=iter.next().getLeft();
			if (!this.myMap.getClosedNode().contains(nodeId)){
				if (!this.myMap.getOpenNode().contains(nodeId)){
					this.myMap.addNode(nodeId, MapAttribute.open);
					this.myMap.addEdge(myPosition, nodeId);	
					
					if(this.NodePersoToExplore.size() != 0) {
						this.NodePersoToExplore.add(nodeId);
					}
				}else{
					//the node exist, but not necessarily the edge
					this.myMap.addEdge(myPosition, nodeId);
				}
			}
		}
	}
	
	protected void Initialisation() {
		super.Initialisation();
		if(this.NodePersoToExplore == null) {
			this.NodePersoToExplore = ((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer;
		}
		
	}
	
	
	void blockChild() {
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		if(lobs.get(0).getRight().size()==1) {
			this.myMap.addNode(this.lastMove,MapAttribute.closed);
			this.myAgent.addBehaviour(new CatchedGolemBehaviour(this.myAgent,this.lastMove));
		}
		this.finished=true;
	}
	
	void unblockChild() {
		
	}

}