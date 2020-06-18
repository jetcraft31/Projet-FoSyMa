package eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information;

import java.time.LocalDateTime;
import java.util.HashMap;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.ConversationBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MapInformationBehaviour extends Information {
	private static final long serialVersionUID = 1L;

	public MapInformationBehaviour(ConversationBehaviour conv) {
		super(conv);
	}

	// On demande la map d'un autre agent, et on lui envoi notre map si il en a besoin
	public boolean SendNeed() {
		
		this.timeConversation = LocalDateTime.now(); // On mets à jour le time car, on a envoyé une demande
			
		MapRepresentation myMap = ((ExploreSoloAgent)this.myAgent).myMap;
		HashMap<String, Object> sg = myMap.getGraphData(); // On sérialize la map
		this.conversation.sendMessageToOtherAgent(ACLMessage.REQUEST, "MapRequest", sg);

		System.out.println(this.myAgent.getLocalName()+" ----> MapRequest sent");
		return true;
	}

	public boolean ReceiveNeed() {
		
		final ACLMessage msgMap = this.myAgent.receive(MessageTemplate.and(Inform, MessageTemplate.and(Sender, MapProtocol)));
		
		// On a reçu une réponse
		if(msgMap != null){
			
			ReceiveMap(msgMap);
			System.out.println(this.myAgent.getLocalName()+"<---- Map received from "+msgMap.getSender().getLocalName());
			
			// On mets à jours le temps
			this.timeConversation = LocalDateTime.now();
			
			return true;
		}
		return false;
	}

	public boolean AswerGive(ACLMessage message) {
		
		// Si on a besoin de sa map, on la merge
		if(((ExploreSoloAgent)this.myAgent).getNeedMap()) {
			ReceiveMap(message);
		}
		
		MapRepresentation myMap = ((ExploreSoloAgent)this.myAgent).myMap;
		HashMap<String, Object> sg = myMap.getGraphData(); // On sérialize la map
		this.conversation.sendMessageToOtherAgent(ACLMessage.INFORM, "MapRequest", sg);
		
		System.out.println(this.myAgent.getLocalName()+" ----> Map sent");
		this.timeConversation = LocalDateTime.now();
		return true;
	}
	
	private void ReceiveMap(ACLMessage msgMap) {
		HashMap<String, Object> otherMap = null;
		try {
			otherMap= (HashMap<String, Object>) msgMap.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		
		if(otherMap!=null) {
			// On merge la carte
			((ExploreSoloAgent)this.myAgent).myMap.mergeGraph(otherMap);
			((ExploreSoloAgent)this.myAgent).MiseAjourOpenNodePerso();
		}
	}

}
