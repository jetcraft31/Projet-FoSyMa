package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.DetermineIfCatched;
import jade.core.Agent;

public class CoalitionChase extends WalkBehaviour {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String lastMove;
	private String goTo=null;

	//quand des agents sont en train de chasser un golem à plusieurs (coalition)
	public CoalitionChase (final Agent myAgent) {
		super(myAgent,"coalChase");
		this.nbEtapePourEtreBloque=20;
		//System.out.println(this.myAgent.getLocalName()+" : Follow golem");
	}
		
	public CoalitionChase (final Agent myAgent,String pos) {
		super(myAgent,"coalChase");
		this.nbEtapePourEtreBloque=40;
		this.goTo=pos;
		//System.out.println(this.myAgent.getLocalName()+" : Follow golem");
	}

	@Override
	public void actionChild(String myPosition) {
		if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("coalChase")) {
			this.finished=true;
		}else if(!((ExploreSoloAgent)this.myAgent).Waiting) {
			if(myPosition.equalsIgnoreCase(this.goTo)) {
				this.goTo=null;
			}
			if(!smell(myPosition)) {
				if(this.goTo!=null) {
					String nextNode=this.myMap.getShortestPath(myPosition,this.goTo).get(0);	
					move(nextNode);
				}else {
					this.myAgent.addBehaviour(new LookForTheGolem(this.myAgent,myPosition));
					this.finished=true;
				}
			}else {
				//System.out.println(this.myAgent.getLocalName()+" : Follow golem, last sent was "+((ExploreSoloAgent)this.myAgent).lastSent);
				String nextNode=this.computeNodeToMove(myPosition);
				move(nextNode);
				this.lastMove=nextNode;
			}
		}
	}

	//en fonction des noeuds où on sent le golem, de notre dernière position et de la 
	//position des autres agents, on choisit vers quel noeud ce déplacer
	private String computeNodeToMove(String myPosition) {
		int maxDistToOthers=-1;
		String bestNodeToMove=null;
		for(Couple<String, List<Couple<Observation, Integer>>> node:((ExploreSoloAgent)this.myAgent).lastSent) {
			if(!node.getLeft().equalsIgnoreCase(myPosition)) {
				int dist=0;
				for(String agent:((ExploreSoloAgent)this.myAgent).chaseAgentsPosition.keySet()) {
					if(!agent.equalsIgnoreCase(this.myAgent.getLocalName()) && ((ExploreSoloAgent)this.myAgent).chaseAgentsPosition.get(agent)!=null) {
						dist+=this.myMap.getShortestPath(((ExploreSoloAgent)this.myAgent).chaseAgentsPosition.get(agent).getValue(),node.getLeft()).size();
					}
				}
				if(this.lastPos.size()>1) {
					dist+=this.myMap.getShortestPath(this.lastPos.get(this.lastPos.size()-2),node.getLeft()).size();
				}
				
				if(dist>maxDistToOthers) {
					maxDistToOthers=dist;
					bestNodeToMove=node.getLeft();
				}
			}
		}
		
		return bestNodeToMove;
	}

	void blockChild() {
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		if(this.goTo!=null && lobs.get(0).getRight().size()==0) {
			//System.out.println(this.myAgent.getLocalName()+" I lost it");
			this.myAgent.addBehaviour(new LookForTheGolem(this.myAgent,((ExploreSoloAgent)this.myAgent).getCurrentPosition()));
			this.finished=true;
		}else {
			if(lobs.get(0).getRight().size()==1) {
				//this.myAgent.addBehaviour(new CatchedGolemBehaviour(this.myAgent,this.lastMove));
				this.myAgent.addBehaviour(new DetermineIfCatched(this.myAgent,this.lastMove));
			}else {
				this.myAgent.addBehaviour(new PatrouilleWalkBehaviour(this.myAgent));
			}
			this.finished=true;
		}
		
	}

	@Override
	void unblockChild() {
		// TODO Auto-generated method stub
		
	}
}