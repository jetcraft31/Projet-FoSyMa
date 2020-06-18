package eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.ConversationBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class InformAgentAndPosNotAvailableInformation extends Information {

	private static final long serialVersionUID = 1L;

	public InformAgentAndPosNotAvailableInformation(ConversationBehaviour conv) {
		super(conv);
	}

	//si on a des informations concernant des agents ou des noeuds non disponibles, on les envoies
	public boolean SendNeed() {
		
		this.timeConversation = LocalDateTime.now();
		
		this.conversation.sendMessageToOtherAgent(ACLMessage.INFORM, "InformAgentAndPosNotAvailable", prepareInfo());
		System.out.println(this.myAgent.getLocalName()+" ----> InformAgentAndPosNotAvailable Request send");
		return true;
	}

	public boolean ReceiveNeed() {
		
		// Si on a déjà envoyé une demande, on attend une réponse
		final ACLMessage msgInformAgentAndPosNotAvailable = this.myAgent.receive(MessageTemplate.and(
				Inform, MessageTemplate.and(Sender, InformAgentAndPosNotAvailable)));
		
		final ACLMessage msgConfirmAgentAndPosNotAvailable = this.myAgent.receive(MessageTemplate.and(
				Confirm, MessageTemplate.and(Sender, InformAgentAndPosNotAvailable)));
		
		// Si on reçoit un inform, cela signifie que lui aussi avait des informations
		if(msgInformAgentAndPosNotAvailable != null){
			this.timeConversation = LocalDateTime.now();
			
			ArrayList<Object> info = null;
			try {
				info = (ArrayList<Object>) msgInformAgentAndPosNotAvailable.getContentObject();
			} catch (UnreadableException e1) {
				e1.printStackTrace();
			}
			
			receiveInfo(info);
			
			((ExploreSoloAgent)this.myAgent).removeAgentAware(this.otherAgent);

			System.out.println(this.myAgent.getLocalName()+" ----> InformAgentAndPosNotAvailable received");
			return true;
		}
		
		// Si on reçoit un confirm cela signifie qu'il n'avait pas d'information supplémentaire
		else if(msgConfirmAgentAndPosNotAvailable != null) 
		{
			
			this.timeConversation = LocalDateTime.now();
			((ExploreSoloAgent)this.myAgent).removeAgentAware(this.otherAgent);
			
			System.out.println(this.myAgent.getLocalName()+" ----> InformAgentAndPosNotAvailable received");
			return true;
		}
		return false;
	}

	public boolean AswerGive(ACLMessage message) {
		System.out.println(this.myAgent.getLocalName()+" ----> InformAgentAndPosNotAvailable receive");
				
		ArrayList<Object> info = null;
		try {
			info = (ArrayList<Object>) message.getContentObject();
		} catch (UnreadableException e1) {
			e1.printStackTrace();
		}
		
		receiveInfo(info);
		
		// Si j'ai des informations en plus, on lui envoie (Car potentiellement, on en a plus)
		if(!((Set<String>) info.get(1)).containsAll(((ExploreSoloAgent)this.myAgent).getNodeNotAvailable()) ||
				!(((ExploreSoloAgent)this.myAgent).getListAvailableAgent()).containsAll((List<String>) info.get(0)) ||
				!((Set<String>) info.get(2)).containsAll(((ExploreSoloAgent)this.myAgent).getCapturedGolemPosition()) ) {
			
			this.conversation.sendMessageToOtherAgent(ACLMessage.INFORM, "InformAgentAndPosNotAvailable", prepareInfo());
		}else {
			
			this.conversation.sendMessageToOtherAgent(ACLMessage.CONFIRM,"InformAgentAndPosNotAvailable");
		}
		
		((ExploreSoloAgent)this.myAgent).removeAgentAware(this.otherAgent);
		
		System.out.println(this.myAgent.getLocalName()+" ----> InformAgentAndPosNotAvailable sent");
		
		this.timeConversation = LocalDateTime.now();
		return true;
	}
	
	private ArrayList<Object> prepareInfo(){
		
		ArrayList<Object> array = new ArrayList<Object>();
		// On ajoute les agents disponibles et les positions non disponibles
		array.add(((ExploreSoloAgent)this.myAgent).getListAvailableAgent());
		array.add(((ExploreSoloAgent)this.myAgent).getNodeNotAvailable());
		array.add(((ExploreSoloAgent)this.myAgent).getCapturedGolemPosition());
		return array;
	}
	
	private void receiveInfo(ArrayList<Object> info) {
		
		Set<String> removeAgent = new HashSet<String>();
		for(String i :((ExploreSoloAgent)this.myAgent).getListAvailableAgent()) {
			if( !((List<String>) info.get(0)).contains(i) ) {
				removeAgent.add(i);
			}
		}
		
		((ExploreSoloAgent)this.myAgent).removeAvailableAgentAndPos(removeAgent ,
				new HashSet<String> ((Set<String>)info.get(1)), new HashSet<String> ((Set<String>)info.get(2)));
	}

}
