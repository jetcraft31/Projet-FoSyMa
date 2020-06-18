package eu.su.mas.dedaleEtu.mas.behaviours.Communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class CommunicationBehaviour  extends SimpleBehaviour {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected boolean finished = false;
	
	// Les différents type de message qu'on peut recevoir
	protected final MessageTemplate Request = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	protected final MessageTemplate Inform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	protected final MessageTemplate Propose = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
	protected final MessageTemplate Agree = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
	protected final MessageTemplate Refuse = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
	protected final MessageTemplate Confirm = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
	protected final MessageTemplate Disconfirm = MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM);
	
	//Les différents protocoles
	protected final MessageTemplate MapProtocol = MessageTemplate.MatchProtocol("MapRequest");
	protected final MessageTemplate EndProtocol = MessageTemplate.MatchProtocol("EndProtocol");
	protected final MessageTemplate PlanificationOpenNodeProtocol = MessageTemplate.MatchProtocol("PlanificationOpenNode");
	protected final MessageTemplate PatrolBlockedProtocol = MessageTemplate.MatchProtocol("PatrolBlocked");
	protected final MessageTemplate InformAgentToChange = MessageTemplate.MatchProtocol("InformAgentToChange");
	protected final MessageTemplate InformAgentAndPosNotAvailable = MessageTemplate.MatchProtocol("InformAgentAndPosNotAvailable");
	protected final MessageTemplate CoordinatesRequestProtocol = MessageTemplate.MatchProtocol("CoordinatesRequest");
	protected final MessageTemplate ProposeChaseProtocol = MessageTemplate.MatchProtocol("ProposeChase");
	protected final MessageTemplate ProposeBringOtherAgentsProtocol = MessageTemplate.MatchProtocol("ProposeBringOtherAgents");
	protected final MessageTemplate answerProposeProtocol = MessageTemplate.MatchProtocol("answerPropose");
	protected final MessageTemplate RequestAgentToExchangeLookForProtocol = MessageTemplate.MatchProtocol("RequestAgentToExchangeLookFor");
	
	public CommunicationBehaviour(final Agent myagent) {
		super(myagent);
	}

	@Override
	public void action() {}
	
	public boolean done() {
		return finished;
	}
	
	//fonctions pour envoyer des messages à un set de destinataires
	protected void sendMessageTo(int messageType, String protocol, List<String> receivers, Serializable content) {
		final ACLMessage message = new ACLMessage(messageType);
		message.setProtocol(protocol);
		message.setSender(this.myAgent.getAID());
		for(String receiver : receivers) {
			message.addReceiver(new AID(receiver, AID.ISLOCALNAME)); 
		}
		try {
			message.setContentObject(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(message);
	}
	
	protected void sendMessageTo(int messageType, String protocol, List<String> receivers, String content) {
		final ACLMessage message = new ACLMessage(messageType);
		message.setProtocol(protocol);
		message.setSender(this.myAgent.getAID());
		for(String receiver : receivers) {
			message.addReceiver(new AID(receiver, AID.ISLOCALNAME)); 
		}
		message.setContent(content);
		((AbstractDedaleAgent)this.myAgent).sendMessage(message);
	}
	
	protected void sendMessageTo(int messageType, String protocol, List<String> receivers) {
		final ACLMessage message = new ACLMessage(messageType);
		message.setProtocol(protocol);
		message.setSender(this.myAgent.getAID());
		for(String receiver : receivers) {
			message.addReceiver(new AID(receiver, AID.ISLOCALNAME)); 
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(message);
	}
	
}
