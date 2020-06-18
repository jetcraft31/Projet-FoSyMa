package eu.su.mas.dedaleEtu.mas.behaviours.Communication;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class ConversationBehaviour extends CommunicationBehaviour {

	private static final long serialVersionUID = 1L;
	
	public String otherAgent;
	public LocalDateTime timeConversation;
	public StartCommunicationBehaviour com;

	public MessageTemplate Sender;
	
	//permet d'envoyer des messages à l'agent avec qui on est en train de communiquer
	public ConversationBehaviour(StartCommunicationBehaviour com, Agent myagent,String receiver) {
		super(myagent);
		this.otherAgent = receiver;
		this.timeConversation = LocalDateTime.now();
		this.com=com;
		
		this.Sender = MessageTemplate.MatchSender(new AID(this.otherAgent, AID.ISLOCALNAME));

	}
	
	public void sendMessageToOtherAgent(int messageType, String protocol, Serializable content) {
		final ACLMessage message = new ACLMessage(messageType);
		message.setProtocol(protocol);
		message.setSender(this.myAgent.getAID());
		message.addReceiver(new AID(this.otherAgent, AID.ISLOCALNAME)); 
		try {
			message.setContentObject(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(message);
	}
	
	public void sendMessageToOtherAgent(int messageType, String protocol, String content) {
		final ACLMessage message = new ACLMessage(messageType);
		message.setProtocol(protocol);
		message.setSender(this.myAgent.getAID());
		message.addReceiver(new AID(this.otherAgent, AID.ISLOCALNAME)); 
		message.setContent(content);
		((AbstractDedaleAgent)this.myAgent).sendMessage(message);
	}
	
	public void sendMessageToOtherAgent(int messageType, String protocol) {
		final ACLMessage message = new ACLMessage(messageType);
		message.setProtocol(protocol);
		message.setSender(this.myAgent.getAID());
		message.addReceiver(new AID(this.otherAgent, AID.ISLOCALNAME)); 
		((AbstractDedaleAgent)this.myAgent).sendMessage(message);
	}

}
