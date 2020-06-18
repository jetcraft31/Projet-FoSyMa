package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;


public abstract class WalkBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 1L;
	
	protected boolean finished = false;
	
	protected MapRepresentation myMap;
	protected LinkedList<String> lastPos = new LinkedList<String>();
	protected int nbEtapePourEtreBloque = 10;
	
	public WalkBehaviour(final Agent myAgent, String behaviorName) {
		super(myAgent);
		this.myMap = ((ExploreSoloAgent)this.myAgent).myMap;
		((ExploreSoloAgent)this.myAgent).setCurrentWalkBehavior(behaviorName);
	}

	public void action() {
		
		Initialisation();
	
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		
		if (myPosition!=null)
			actionChild(myPosition);
		
		// Permet de mieux voir les déplacements de l'agent
		this.myAgent.doWait(((ExploreSoloAgent)this.myAgent).speed);
	}

	
	public boolean done() {
		return this.finished;
	}
	

	
	protected void move(String nextNode) {

		if(!( ((ExploreSoloAgent)this.myAgent).Waiting ) && nextNode != null){ // Si on ne patiente pas
								
			// L'agent se déplace
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			
			// On ajoute la dernier position de l'agent
			if(this.lastPos.size()==this.nbEtapePourEtreBloque) {
				this.lastPos.removeFirst();
			}
			this.lastPos.add(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());		
			
			if(bloque()) { // Si on est bloqué
				blockChild();
			}else {
				unblockChild();
			}
		}
	}
	
	//pour détecter si l'agent est bloqué
	protected boolean bloque() {
		
		if(this.lastPos.size()<this.nbEtapePourEtreBloque) {
			return false;
		}

		boolean bloque = true;
		for( int i = 0;i<this.lastPos.size() -1; i++) {
			if(!this.lastPos.get(i).equalsIgnoreCase(this.lastPos.get(i+1))) {
				bloque = false;
				break;
			}
		}				
		return bloque;
	}
	
	//on sent autour de soi pour repérer le golem, si l'une des cases que l'on sent est
	//proche d'un golem déjà capturé, on l'ignore
	protected boolean smell(String myPosition) {
		//System.out.println(this.myAgent.getLocalName()+" : I smell around me !");
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
		((ExploreSoloAgent)this.myAgent).lastSent=new ArrayList<Couple<String,List<Couple<Observation,Integer>>>>();
		((ExploreSoloAgent)this.myAgent).lastSent.add(lobs.get(0));
		boolean found=false;
		for (Couple<String,List<Couple<Observation,Integer>>> obs:lobs){
			String SentPos=obs.getLeft();
			if(obs.getRight().size()==1 && !SentPos.equals(myPosition)) {
				if(!this.closeToCapturedGolem(obs.getLeft())) {
					((ExploreSoloAgent)this.myAgent).lastSent.add(obs);
					found=true;
				}
			}
		}
		return found;
	}
	
	//pour tester si une case est proche d'un golem capturé
	private boolean closeToCapturedGolem(String stenchPosition) {
		for(String golemPosition:((ExploreSoloAgent)this.myAgent).getCapturedGolemPosition()) {
			if(this.myMap.getShortestPath(stenchPosition, golemPosition).size()
					<=((ExploreSoloAgent)this.myAgent).stenchDistanceEstimate) {
				return true;
			}
		}
		return false;
	}

	abstract void actionChild(String myPosition);
	abstract void blockChild();
	abstract void unblockChild();
	
	protected void Initialisation() {
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			((ExploreSoloAgent)this.myAgent).myMap = this.myMap;
		}
	}
}

