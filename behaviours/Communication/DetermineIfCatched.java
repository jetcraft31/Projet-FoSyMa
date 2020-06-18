package eu.su.mas.dedaleEtu.mas.behaviours.Communication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import dataStructures.tuple.Couple;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.CatchedGolemBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.PatrouilleWalkBehaviour;


public class DetermineIfCatched  extends CommunicationBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LocalDateTime timeConversation;
	
	private String potentialGolemPosition;
	
	private boolean sentRequest=false;

	//lorsqu'un agent est bloqué et qu'il croit avoir attrapé un golem
	//ce behaviour lui permet de déterminer si la case qui le bloque est bien celle d'un golem
	public DetermineIfCatched(final Agent myagent, String position) {
		super(myagent);
		// on garde en mémoire le moment où on crée le behavior
		this.timeConversation = LocalDateTime.now();
		this.potentialGolemPosition=position;
		((ExploreSoloAgent)this.myAgent).positionsToDetermineIfCatched=new HashSet<String>();
		((ExploreSoloAgent)this.myAgent).determiningIfCatched=true;
		
	}

	public void action() {
		System.out.println(this.myAgent.getLocalName()+" : determining if I catched a golem");
		if(!this.sentRequest) {
			// on envoie une demande pour obtenir les positions des autres agents
			List<String> receivers=new ArrayList<String>();
			for(String receiver : ((ExploreSoloAgent)this.myAgent).getListAllAgent()) {
				if (!receiver.equalsIgnoreCase(this.myAgent.getLocalName())){
					receivers.add(receiver);
				}
			}
			this.sendMessageTo(ACLMessage.REQUEST,"requestInformPositionToDetermineIfCatched",
				receivers);
			this.sentRequest=true;
		}
		
		final MessageTemplate informPositionToDetermineIfCatchedProtocol = MessageTemplate.MatchProtocol("informPositionToDetermineIfCatched");
		
		//on recoit les messages les positions des autres agents
		if(Duration.between(this.timeConversation, LocalDateTime.now()).getSeconds() <= 15) {
			for(String sender : ((ExploreSoloAgent)this.myAgent).getListAvailableAgent()) {
				if (!sender.equalsIgnoreCase(this.myAgent.getLocalName())){
					final ACLMessage getPositionToDetermineIfCatchedMsg = this.myAgent.receive(MessageTemplate.and(Inform,
							MessageTemplate.and(MessageTemplate.MatchSender(new AID(sender, AID.ISLOCALNAME)), informPositionToDetermineIfCatchedProtocol)));
					if (getPositionToDetermineIfCatchedMsg!=null) {
						//System.out.println(this.myAgent.getLocalName()+" : received message to determine if I catched");

						String content=getPositionToDetermineIfCatchedMsg.getContent();
						((ExploreSoloAgent)this.myAgent).positionsToDetermineIfCatched.add(content);
					}
				}
			}	
			//si l'un des autres agents est dans la case où l'on arrive pas à aller, alors on n'a pas 
			//attrapé de golem donc on abandonne
			if(((ExploreSoloAgent)this.myAgent).positionsToDetermineIfCatched.contains(this.potentialGolemPosition)) {
				this.myAgent.addBehaviour(new PatrouilleWalkBehaviour(this.myAgent));
				((ExploreSoloAgent)this.myAgent).determiningIfCatched=false;
				this.finished=true;
			}
		}else {
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			//si aucun agent ne nous a dit qu'il était dans la case où l'on arrive pas à aller, et que l'on
			//sent toujours l'odeur du golem alors on a attrapé un golem
			if(lobs.get(0).getRight().size()==1) {
				this.myAgent.addBehaviour(new CatchedGolemBehaviour(this.myAgent,this.potentialGolemPosition));
				((ExploreSoloAgent)this.myAgent).determiningIfCatched=false;
				this.finished=true;
			}else {
				this.myAgent.addBehaviour(new PatrouilleWalkBehaviour(this.myAgent));
				((ExploreSoloAgent)this.myAgent).determiningIfCatched=false;
				this.finished=true;
			}
			
		}
		this.myAgent.doWait(1000);
	}
	

}

