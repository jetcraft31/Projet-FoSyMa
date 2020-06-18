package eu.su.mas.dedaleEtu.mas.behaviours.Walk;

import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.TickerBehaviour;

/**************************************
 * 
 * 
 * 				BEHAVIOUR RandomWalk : Illustrates how an agent can interact with, and move in, the environment
 * 
 * 
 **************************************/


public class RandomWalkBehaviour extends TickerBehaviour{
	
	private static final long serialVersionUID = 9088209402507795289L;

	public RandomWalkBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent, 240);
	}

	public void onTick() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if (myPosition!=null){
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move

			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}

	}

}