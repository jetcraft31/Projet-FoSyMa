package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.HashSet;
import java.util.Set;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import jade.core.Agent;

public class CatchedGolemBehaviour extends WalkBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//un agent qui a attrapé un golem reste dans ce bahaviour et ne bouge plus
	public CatchedGolemBehaviour(Agent myAgent, String golemPosition) {
		super(myAgent,"catchedGolem");
		((ExploreSoloAgent)this.myAgent).endCoalition();
		((ExploreSoloAgent)this.myAgent).addCapturedGolemPosition(golemPosition);
		Set<String> nodesNotAvailable= new HashSet<String>();
		nodesNotAvailable.add(golemPosition);
		nodesNotAvailable.add(((ExploreSoloAgent)this.myAgent).getCurrentPosition());
		//on met à jour les agents et noeuds indisponibles
		((ExploreSoloAgent)this.myAgent).removeAvailableAgentAndPos(this.myAgent.getLocalName(),
				nodesNotAvailable,golemPosition);
		//System.out.println(this.myAgent.getLocalName()+" : new behavior : catched a golem");
	}

	@Override
	void actionChild(String myPosition) {
		System.out.println(this.myAgent.getLocalName()+" : I catched a golem");
		//System.out.println(this.myAgent.getLocalName()+" : agent not aware "+((ExploreSoloAgent)this.myAgent).getListAgentNotAware());
		this.myAgent.doWait(2000);
	}

	@Override
	void blockChild() {
		// TODO Auto-generated method stub

	}

	@Override
	void unblockChild() {
		// TODO Auto-generated method stub

	}

}
