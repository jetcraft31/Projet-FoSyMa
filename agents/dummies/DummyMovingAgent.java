package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.RandomWalkBehaviour;
import jade.core.behaviours.Behaviour;

public class DummyMovingAgent extends AbstractDedaleAgent{

	private static final long serialVersionUID = -2991562876411096907L;
	
	protected void setup(){
		
		super.setup();

		//get the parameters given into the object[]
		final Object[] args = getArguments();
		System.out.println("Arg given by the user to "+this.getLocalName()+": "+args[2]);
		
		//use them as parameters for your behaviours is you want
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		lb.add(new RandomWalkBehaviour(this));
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		addBehaviour(new startMyBehaviours(this,lb));

	}

	protected void takeDown(){
		super.takeDown();
	}
	
	protected void beforeMove(){
		super.beforeMove();
	}
	
	protected void afterMove(){
		super.afterMove();
	}

}