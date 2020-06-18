package eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information;

import java.time.LocalDateTime;
import java.util.ArrayList;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.ConversationBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class InformAgentToChangePatrolInformation extends Information{

	private static final long serialVersionUID = 1L;

	public InformAgentToChangePatrolInformation(ConversationBehaviour conv) {
		super(conv);
	}

	public boolean SendNeed() {
		
		
		this.timeConversation = LocalDateTime.now();
		
		this.conversation.sendMessageToOtherAgent(ACLMessage.INFORM, "InformAgentToChange",
				((ExploreSoloAgent)this.myAgent).getNodeToPatrouillePerso());
		
		System.out.println(this.myAgent.getLocalName()+" ----> Inform Agent to chance Request send");
		return true;
	}

	public boolean ReceiveNeed() {
		
		final ACLMessage msgAgreeInformAgentToChange = this.myAgent.receive(
				MessageTemplate.and(Agree, MessageTemplate.and(Sender, InformAgentToChange)));
		
		final ACLMessage msgDisconfirmInformAgentToChange = this.myAgent.receive(
				MessageTemplate.and(Disconfirm, MessageTemplate.and(Sender, InformAgentToChange)));
		
		if(msgAgreeInformAgentToChange != null){
			this.timeConversation = LocalDateTime.now();
			 
			System.out.println(this.myAgent.getLocalName()+" ----> He is the right agent");
			((ExploreSoloAgent)this.myAgent).setInformAgentToChangePatrolDelivred();
			return true;
		}
		
		if(msgDisconfirmInformAgentToChange != null){
			this.timeConversation = LocalDateTime.now();
			 
			System.out.println(this.myAgent.getLocalName()+" ----> He is not the right agent");
			return true;
		}
		
		return false;
	}

	@Override
	public boolean AswerGive(ACLMessage message) {
		System.out.println(this.myAgent.getLocalName()+" ----> msgInformAgentToChange receive");
		
		ArrayList<String> content = null;
		try {
			content = (ArrayList<String>) message.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		
		
		if(content != null && content.containsAll( ((ExploreSoloAgent)this.myAgent).getNodeToPatrouillePerso() )) {
			
			((ExploreSoloAgent)this.myAgent).NextPatrouille();
			this.conversation.sendMessageToOtherAgent(ACLMessage.AGREE, "InformAgentToChange");			
		}else {
			this.conversation.sendMessageToOtherAgent(ACLMessage.DISCONFIRM, "InformAgentToChange");
		}
				
		System.out.println(this.myAgent.getLocalName()+" ----> msgInformAgentToChange sent");
		
		this.timeConversation = LocalDateTime.now();
		return true;
	}

}
