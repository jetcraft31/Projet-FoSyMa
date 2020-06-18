package eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information;

import java.time.Duration;
import java.time.LocalDateTime;

import eu.su.mas.dedaleEtu.mas.behaviours.Communication.ConversationBehaviour;
import jade.lang.acl.ACLMessage;

public class EndInformation extends Information{

	private static final long serialVersionUID = 1L;

	public EndInformation(ConversationBehaviour conv) {
		super(conv);
	}

	public boolean SendNeed() {
		
		if(Duration.between(this.timeConversation, LocalDateTime.now()).getSeconds() > 5) {
			System.out.println(this.myAgent.getLocalName()+" ---->Temps de réponse trop long");
		}
		this.conversation.sendMessageToOtherAgent(ACLMessage.INFORM, "EndProtocol");
		System.out.println(this.myAgent.getLocalName()+" ----> End the conversation, with the ID");
		this.com.deleteSpeaking(this.otherAgent);

		return true;
	}

	public boolean ReceiveNeed() {
		return false;
	}

	public boolean AswerGive(ACLMessage message) {
		
		if(Duration.between(this.timeConversation, LocalDateTime.now()).getSeconds() > 5) {
			System.out.println(this.myAgent.getLocalName()+" ---->Temps de réponse trop long");
		}
		System.out.println(this.myAgent.getLocalName()+" ----> End the conversation, with the ID");
		this.com.deleteReceiver(this.otherAgent);
		return true;
	}
	
}
