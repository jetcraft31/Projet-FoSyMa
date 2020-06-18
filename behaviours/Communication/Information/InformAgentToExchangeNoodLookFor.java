package eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information;

import java.time.LocalDateTime;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.ConversationBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InformAgentToExchangeNoodLookFor extends Information {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InformAgentToExchangeNoodLookFor(ConversationBehaviour conv) {
		super(conv);
	}

	//on demande à un agent d'échanger un noeud (du behaviour LookForTheGolem) pour se débloquer
	public boolean SendNeed() {
		this.timeConversation = LocalDateTime.now();
		
		this.conversation.sendMessageToOtherAgent(ACLMessage.REQUEST, "RequestAgentToExchangeLookFor",
				((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor);
		
		System.out.println(this.myAgent.getLocalName()+" ----> request exchange node look for sent");
		return true;
	}

	//l'autre agent nous informe qu'il a refusé/accepté d'échanger un noeud
	public boolean ReceiveNeed() {
		
		final ACLMessage msgAgreeExchangeNodeLookFor = this.myAgent.receive(
				MessageTemplate.and(Agree, MessageTemplate.and(Sender, RequestAgentToExchangeLookForProtocol)));
		
		final ACLMessage msgRefuseExchangeNodeLookFor = this.myAgent.receive(
				MessageTemplate.and(Refuse, MessageTemplate.and(Sender, RequestAgentToExchangeLookForProtocol)));
		
		if(msgAgreeExchangeNodeLookFor != null){
			this.timeConversation = LocalDateTime.now();
			 
			System.out.println(this.myAgent.getLocalName()+" ----> He agreed");
			((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=msgAgreeExchangeNodeLookFor.getContent();
			((ExploreSoloAgent)this.myAgent).setNeedExchangeNodeLookFor(false);
			return true;
		}
		
		if(msgRefuseExchangeNodeLookFor != null){
			this.timeConversation = LocalDateTime.now();
			 
			System.out.println(this.myAgent.getLocalName()+" ----> He refused");
			return true;
		}
		
		return false;
	}

	//on recoit une requete pour échanger un noeud et on y répond
	public boolean AswerGive(ACLMessage message) {
		System.out.println(this.myAgent.getLocalName()+" ----> msgExchangeNodeLookFor receive");
		
		if(message.getContent()!=null && 
				((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("LookForTheGolem")) {
			this.conversation.sendMessageToOtherAgent(ACLMessage.AGREE, "RequestAgentToExchangeLookFor",
					((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor);
			((ExploreSoloAgent)this.myAgent).nextNodeToVisitLookFor=message.getContent();
			((ExploreSoloAgent)this.myAgent).setNeedExchangeNodeLookFor(false);
		}else {
			this.conversation.sendMessageToOtherAgent(ACLMessage.REFUSE, "RequestAgentToExchangeLookFor");
		}
		
		return true;
	}

}
