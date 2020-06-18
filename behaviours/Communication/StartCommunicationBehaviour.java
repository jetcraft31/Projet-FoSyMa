package eu.su.mas.dedaleEtu.mas.behaviours.Communication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.util.Pair;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.CoalitionChase;


public class StartCommunicationBehaviour  extends CommunicationBehaviour {
	private static final long serialVersionUID = 8567689731496787661L;

	private List<String> speakingWith = new ArrayList<String>();
	private List<String> ReceiveMessageFrom = new ArrayList<String>();

	public int AskInProgressForOpenNode = 0;
	public int AskInProgressForMap = 0;

	private Integer countChasingMsg=0;
	
	private LocalDateTime timeLastChasePosition = LocalDateTime.now();
	
	/**
	 * 
	 * @param myagent the Agent this behaviour is linked to
	 * @param nbValues the number of messages that should be sent to the receiver
	 * @param receiverName The local name of the receiver agent
	 */
	public StartCommunicationBehaviour(final Agent myagent) {
		super(myagent);
	}

	public void action() {
		
		final MessageTemplate PingProtocol = MessageTemplate.MatchProtocol("Ping");
		final MessageTemplate AgreeProtocol = MessageTemplate.MatchProtocol("AgreeProtocol");
		final MessageTemplate startCoalitionProtocol = MessageTemplate.MatchProtocol("startCoalition");
		final MessageTemplate ChasingInformationProtocol = MessageTemplate.MatchProtocol("chasingInformation");
		final MessageTemplate requestDetermineIfCatchedProtocol = MessageTemplate.MatchProtocol("requestInformPositionToDetermineIfCatched");
		//final MessageTemplate InformCatchedProtocol = MessageTemplate.MatchProtocol("informCatched");
		
		final ACLMessage msgRequest = this.myAgent.receive(MessageTemplate.and(Request, PingProtocol));
		final ACLMessage msgAgree = this.myAgent.receive(MessageTemplate.and(Inform, AgreeProtocol));
		final ACLMessage msgCoalition = this.myAgent.receive(MessageTemplate.and(Request, startCoalitionProtocol));
		//final ACLMessage msgCatched = this.myAgent.receive(MessageTemplate.and(Inform, InformCatchedProtocol));
		final ACLMessage getChasingInformationMsg = this.myAgent.receive(MessageTemplate.and(Inform,ChasingInformationProtocol));
		
		
		// On a reçu un message pour commencer une conversation et on n'est pas déjà entrain de 
		// recevoir des messages de sa part.
		if (msgRequest != null && 
				!this.ReceiveMessageFrom.contains(msgRequest.getSender().getLocalName())  ) {	
			((ExploreSoloAgent) this.myAgent).Waiting = true;
			
			System.out.println(this.myAgent.getLocalName()+"<----Hello message received from "+
					msgRequest.getSender().getLocalName()+" ,content = "+msgRequest.getContent());
			
			// On lui envoie un message pour dire qu'on est d'accord de parler avec lui
			this.sendMessageTo(ACLMessage.INFORM, "AgreeProtocol", Collections.singletonList(msgRequest.getSender().getLocalName()),
					((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			System.out.println(this.myAgent.getLocalName()+" ----> agrees to receive message from "+msgRequest.getSender().getLocalName());
			
			// On ajoute le behaviour de Receiver 
			this.myAgent.addBehaviour(new GiveInformationBehaviour(this,this.myAgent,
					msgRequest.getSender().getLocalName()));
			this.ReceiveMessageFrom.add(msgRequest.getSender().getLocalName());
		
		//on recoie un message pour faire une coalition
		}else if(msgCoalition != null && !this.ReceiveMessageFrom.contains(msgCoalition.getSender().getLocalName()) &&
				!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("Explo")) {
			((ExploreSoloAgent) this.myAgent).Waiting = true;
			
			System.out.println(this.myAgent.getLocalName()+"<----Hello coalition message received from "+
					msgCoalition.getSender().getLocalName()+" ,content = "+msgCoalition.getContent());
			
			// On lui envoie un message pour dire qu'on est d'accord de parler avec lui
			this.sendMessageTo(ACLMessage.INFORM, "AgreeProtocol", Collections.singletonList(msgCoalition.getSender().getLocalName()),
					((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			System.out.println(this.myAgent.getLocalName()+" ----> agrees to receive message from "+msgCoalition.getSender().getLocalName());
			
			// On ajoute le behaviour de Receiver 
			this.myAgent.addBehaviour(new GiveInformationBehaviour(this,this.myAgent,
					msgCoalition.getSender().getLocalName()));
			this.ReceiveMessageFrom.add(msgCoalition.getSender().getLocalName());
			
		// Ici, quelqu'un est d'accord pour discuter avec nous
		}else if(msgAgree != null && this.speakingWith.size()<1 &&
				!this.speakingWith.contains(msgAgree.getSender().getLocalName()) ) {
			
			// On ajoute le behaviour pour envoyer nos demandes
			((ExploreSoloAgent) this.myAgent).Waiting = true;
			if(((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("createCoal")){
				this.myAgent.addBehaviour(new InviteCoalitionBehaviour(this,this.myAgent,
						msgAgree.getSender().getLocalName()));
			}else {
				this.myAgent.addBehaviour(new NeedInformationBehaviour(this,this.myAgent,
						msgAgree.getSender().getLocalName()));

			}
			this.speakingWith.add(msgAgree.getSender().getLocalName());
			System.out.println(this.myAgent.getLocalName()+" ----> agrees to speak with "+msgAgree.getSender().getLocalName());
		}
		// Si on a quelque chose à dire et qu'on est pas déjà entrain de parler avec tous le monde, alors 
		// on envoie des pings
		if( ((ExploreSoloAgent)this.myAgent).getNeedHelpOrNeedToSaySomething() &&
				( this.speakingWith.size() <1)){
		
			List<String> receivers=new ArrayList<String>();
			for(String receiver : ((ExploreSoloAgent)this.myAgent).getListAllAgent()) {
				if ( !receiver.equalsIgnoreCase(this.myAgent.getLocalName()) && !this.speakingWith.contains(receiver) &&
						 ((ExploreSoloAgent)this.myAgent).getLastDiscussionTime().get(receiver) == 0 ){
					receivers.add(receiver);
					((ExploreSoloAgent)this.myAgent).AddTimeToAgent(receiver,6*((ExploreSoloAgent)this.myAgent).speed);
				}
			}
			this.sendMessageTo(ACLMessage.REQUEST, "Ping", receivers);
						
		// si on veut créer une coalition avec des agents, on leur envoie des messages
		}else if(((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("createCoal") &&
				( this.speakingWith.size() <1)) {
			List<String> receivers=new ArrayList<String>();
			for(String receiver : ((ExploreSoloAgent)this.myAgent).getListAvailableAgent()) {
				if ( !receiver.equalsIgnoreCase(this.myAgent.getLocalName()) && !this.speakingWith.contains(receiver)  &&
						 ((ExploreSoloAgent)this.myAgent).getLastDiscussionTime().get(receiver) == 0 ){
					receivers.add(receiver);
				}
			}
			this.sendMessageTo(ACLMessage.REQUEST, "startCoalition", receivers);
			
		// si on est en train de chasser un golem avec sa coalition, on envoie sa position en boucle
		}else if(((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("coalChase") &&
				!((ExploreSoloAgent)this.myAgent).determiningIfCatched) {

			if(((ExploreSoloAgent)this.myAgent).lastSent.size()>1 && 
					Duration.between(this.timeLastChasePosition, LocalDateTime.now()).getSeconds() > 1) {
				List<String> receivers=new ArrayList<String>();
				for(String receiver : ((ExploreSoloAgent)this.myAgent).getListAvailableAgent()) {
					if (!receiver.equalsIgnoreCase(this.myAgent.getLocalName())){
						receivers.add(receiver);
					}
				}
				
				//on envoie le message avec un compteur pour savoir si un message est plus vieux
				//qu'un autre
				this.sendMessageTo(ACLMessage.INFORM, "chasingInformation", receivers, 
						new Pair<Integer, String>(this.countChasingMsg,((AbstractDedaleAgent)this.myAgent).getCurrentPosition()));
				//System.out.println(this.myAgent.getLocalName()+" ----> chasing information sent, number "+this.countChasingMsg);	
				this.countChasingMsg++;
				this.timeLastChasePosition=LocalDateTime.now();
			}
			
			
			//et on recoit en boucle les positions des autres agents
			for(String sender : ((ExploreSoloAgent)this.myAgent).getListAvailableAgent()) {
				if (!sender.equalsIgnoreCase(this.myAgent.getLocalName())){
					final ACLMessage getChasingInformationMsgAll = this.myAgent.receive(MessageTemplate.and(Inform,
							MessageTemplate.and(MessageTemplate.MatchSender(new AID(sender, AID.ISLOCALNAME)), ChasingInformationProtocol)));
					if (getChasingInformationMsgAll!=null) {
						this.updateChaseAgentsPosition(getChasingInformationMsgAll);
					}
				}
			}	
		//si on recoit un message nous demandant notre position pour déterminer si un autre agent
		//a attrapé un golem, on lui envoie notre position
		}else if(((ExploreSoloAgent)this.myAgent).determiningIfCatched ||
				((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("catchedGolem")){
			for(String sender : ((ExploreSoloAgent)this.myAgent).getListAvailableAgent()) {
				final ACLMessage requestDetermineMsg = this.myAgent.receive(MessageTemplate.and(Request,
						MessageTemplate.and(MessageTemplate.MatchSender(new AID(sender, AID.ISLOCALNAME)), requestDetermineIfCatchedProtocol)));
				if(requestDetermineMsg!=null) {
					this.sendMessageTo(ACLMessage.INFORM, "informPositionToDetermineIfCatched", 
							Collections.singletonList(sender),((ExploreSoloAgent)this.myAgent).getCurrentPosition());
				}
			}	
		}
		
		//si un agent près de nous chasse un golem, alors on le chasse aussi
		if(getChasingInformationMsg!=null &&
				!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("catchedGolem") &&
				!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("Explo") &&
				!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("coalChase") && 
				this.speakingWith.size()<1) {
			Pair<Integer, String> content=null;
			try {
				content = (Pair<Integer, String>) getChasingInformationMsg.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myAgent.addBehaviour(new CoalitionChase(this.myAgent,content.getValue()));
			this.updateChaseAgentsPosition(getChasingInformationMsg);
		}
	}
	
	//on met  à jour les positions des autres agents en train de chasser
	private void updateChaseAgentsPosition(ACLMessage getChasingInformationMsg) {
		String sender=getChasingInformationMsg.getSender().getLocalName();
		Pair<Integer, String> content=null;
		try {
			content = (Pair<Integer, String>) getChasingInformationMsg.getContentObject();
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(((ExploreSoloAgent)this.myAgent).chaseAgentsPosition!=null &&
				((ExploreSoloAgent)this.myAgent).chaseAgentsPosition.containsKey(sender) &&
				((ExploreSoloAgent)this.myAgent).chaseAgentsPosition.get(sender).getKey()>content.getKey()) {
			//System.out.println(this.myAgent.getLocalName()+" got message "+content.getKey()+" from "+sender+
			//		" but I already got message "+((ExploreSoloAgent)this.myAgent).chaseAgentsPosition.get(sender).getKey());
		}else {
			//System.out.println(this.myAgent.getLocalName()+" got message "+content.getKey()+" from "+sender);
			((ExploreSoloAgent)this.myAgent).chaseAgentsPosition.put(sender, content);
		}
		//System.out.println(this.myAgent.getLocalName()+" ----> I know that "+sender+" was in "+content.getValue());		
	}

	public void deleteSpeaking(String agentName) {
		((ExploreSoloAgent)this.myAgent).AddTimeToAgent(agentName,10*((ExploreSoloAgent)this.myAgent).speed);
		// On ne parle plus à l'agent pendant 10 secondes
		this.speakingWith.remove(agentName);
		canWalk();
	}
	
	public void deleteReceiver(String agentName) {
		this.ReceiveMessageFrom.remove(agentName);
		canWalk();
	}	
	
	public void canWalk() {
		if(this.speakingWith.size() == 0 && this.ReceiveMessageFrom.size()==0) {
			((ExploreSoloAgent)this.myAgent).Waiting = false;
		}
	}
}

