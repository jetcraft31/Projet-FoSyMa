package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import java.util.Map.Entry;


public class TimeDiscussion extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TimeDiscussion(Agent a, long period) {
		super(a, period);
	}

	protected void onTick() {
		
		for(Entry<String, Integer> i : ((ExploreSoloAgent)this.myAgent).getLastDiscussionTime().entrySet()) {
			if(i.getValue() >0) {
				((ExploreSoloAgent)this.myAgent).getLastDiscussionTime().put(i.getKey(), i.getValue()-
						((ExploreSoloAgent)this.myAgent).speed);
			}
		}
		
		//System.out.println("Time discussion : "+((ExploreSoloAgent)this.myAgent).getLastDiscussionTime().toString());
	}

}

