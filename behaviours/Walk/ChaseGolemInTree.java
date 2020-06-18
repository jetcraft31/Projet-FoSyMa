package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import jade.core.Agent;

public class ChaseGolemInTree extends WalkBehaviour {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String lastMove;

	//ce behaviour permet de tenter d'attraper seul un golem (si le sous-graphe dans lequel il est
	//est un arbre)
	public ChaseGolemInTree (final Agent myAgent) {
		super(myAgent,"treeChase");
		this.nbEtapePourEtreBloque=20;
	}

	@Override
	public void actionChild(String myPosition) {
		if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("treeChase")) {
			this.finished=true;
		}else if(!((ExploreSoloAgent)this.myAgent).Waiting) {
			if(!smell(myPosition)) {
				//si on perd le golem, on va chercher d'autres agents en renfort
				((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint=myPosition;
				this.myAgent.addBehaviour(new CreateCoalition(this.myAgent,((ExploreSoloAgent)this.myAgent).getNbAgentToBringIfSmell()));
				this.finished=true;
			}else {
				//sinon on le suit
				//System.out.println(this.myAgent.getLocalName()+" : Follow golem, last sent was "+((ExploreSoloAgent)this.myAgent).lastSent);
				move(((ExploreSoloAgent)this.myAgent).lastSent.get(1).getLeft());
				this.lastMove=((ExploreSoloAgent)this.myAgent).lastSent.get(1).getLeft();
			}
		}
	}

	void blockChild() {
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		if(lobs.get(0).getRight().size()==1) {
			this.myAgent.addBehaviour(new CatchedGolemBehaviour(this.myAgent,this.lastMove));
		}else {
			this.myAgent.addBehaviour(new PatrouilleWalkBehaviour(this.myAgent));
		}
		this.finished=true;
	}

	@Override
	void unblockChild() {
		// TODO Auto-generated method stub
		
	}
}