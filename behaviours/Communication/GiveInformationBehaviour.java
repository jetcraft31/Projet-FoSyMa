package eu.su.mas.dedaleEtu.mas.behaviours.Communication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.util.Pair;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.Information;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.EndInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.InformAgentAndPosNotAvailableInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.InformAgentToChangePatrolInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.InformAgentToExchangeNoodLookFor;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.MapInformationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.OpenNodeInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.CreateCoalition;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.LookForTheGolem;


public class GiveInformationBehaviour  extends ConversationBehaviour {
	private static final long serialVersionUID = 8567689731496787661L;
	
	public GiveInformationBehaviour(StartCommunicationBehaviour com, final Agent myagent,String receiver) {
		super(com, myagent, receiver);
	}

	public void action() {
		
		//***************Partie Reception de demande 		
		Information info;
		
		final ACLMessage msgRequestMap = this.myAgent.receive(MessageTemplate.and(Request,
				MessageTemplate.and(Sender, MapProtocol)));
		
		final ACLMessage msgEnd = this.myAgent.receive(MessageTemplate.and(Inform,
				MessageTemplate.and(Sender, EndProtocol)));
		
		ACLMessage msgPlanificationOpenNode = this.myAgent.receive(MessageTemplate.and(Request,
					MessageTemplate.and(Sender, PlanificationOpenNodeProtocol)));
		
		final ACLMessage msgInformAgentToChange = this.myAgent.receive(MessageTemplate.and(Inform,
				MessageTemplate.and(Sender, InformAgentToChange)));
		
		final ACLMessage msgInformAgentAndPosNotAvailable = this.myAgent.receive(MessageTemplate.and(Inform,
				MessageTemplate.and(Sender, InformAgentAndPosNotAvailable)));
		
		final ACLMessage msgExchangeNodeLookFor = this.myAgent.receive(MessageTemplate.and(Request,
				MessageTemplate.and(Sender, RequestAgentToExchangeLookForProtocol)));
		
		final ACLMessage msgCoordinatesRequest = this.myAgent.receive(MessageTemplate.and(Request,
				MessageTemplate.and(Sender, CoordinatesRequestProtocol)));
		
		final ACLMessage msgProposeChase = this.myAgent.receive(MessageTemplate.and(Propose,
				MessageTemplate.and(Sender, ProposeChaseProtocol)));
		
		final ACLMessage msgProposeBringOtherAgents = this.myAgent.receive(MessageTemplate.and(Propose,
				MessageTemplate.and(Sender, ProposeBringOtherAgentsProtocol)));
		
				
		// Si on reçoit une demande pour la map, alors on lui envoie nos informations
		if(msgRequestMap != null) {
			info = new MapInformationBehaviour(this);
			
			info.AswerGive(msgRequestMap);
		}
		
		// Si je ne suis pas entrain de demander une map, alors je peux te faire une patrouille			
		else if(msgPlanificationOpenNode != null) {
			
			info = new OpenNodeInformation(this);
			
			info.AswerGive(msgPlanificationOpenNode);
		}
		else if(msgInformAgentToChange != null) {
			
			info = new InformAgentToChangePatrolInformation(this);
			
			info.AswerGive(msgInformAgentToChange);
			
		}
		
		else if(msgInformAgentAndPosNotAvailable != null){
			
			info = new InformAgentAndPosNotAvailableInformation(this);
			
			info.AswerGive(msgInformAgentAndPosNotAvailable);
			
		}
		
		else if(msgExchangeNodeLookFor != null){
			
			info = new InformAgentToExchangeNoodLookFor(this);
			
			info.AswerGive(msgExchangeNodeLookFor);
			
		}
		
		//si on recoit une demande de coordonnées (pour créer une coalition)
		//alors on les envoies
		else if(msgCoordinatesRequest != null) {
			if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("catchedGolem")) {
				Set<String> myPatrol = new HashSet<String>(((ExploreSoloAgent)this.myAgent).getNodeToPatrouille().get(myAgent.getLocalName()));
				this.sendMessageToOtherAgent(ACLMessage.INFORM, "CoordinatesRequest", new Pair<String, String>(
							((ExploreSoloAgent)this.myAgent).getCurrentPosition(),
							((ExploreSoloAgent)this.myAgent).myMap.determineCenter(myPatrol)));
				
				System.out.println(this.myAgent.getLocalName()+" ----> coordinates sent");
				this.timeConversation = LocalDateTime.now();
			}else {
				this.sendMessageToOtherAgent(ACLMessage.REFUSE, "CoordinatesRequest");
			}

		}
		
		//on accepte la coalition et l'action à effectuer
		else if(msgProposeChase != null) {
			//si on veut que 2 agents dans 2 coalitions s'ignorent
//			if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("catchedGolem") && 
//					((ExploreSoloAgent)this.myAgent).chaseAgentsPosition==null) {
//				System.out.println(this.myAgent.getLocalName()+"<---- propose received from "+msgProposeChase.getSender().getLocalName());
//				String content = msgProposeChase.getContent();
//				((ExploreSoloAgent)this.myAgent).startCoalition(content);
//				this.myAgent.addBehaviour(new LookForTheGolem(this.myAgent));
//				System.out.println(this.myAgent.getLocalName()+" : I'll chase the golem near "+content);
//				this.sendMessageToOtherAgent(ACLMessage.AGREE, "answerPropose");
//			}else {
//				this.sendMessageToOtherAgent(ACLMessage.REJECT_PROPOSAL, "answerPropose");
//			}
			System.out.println(this.myAgent.getLocalName()+"<---- propose received from "+msgProposeChase.getSender().getLocalName());
			String content = msgProposeChase.getContent();
			((ExploreSoloAgent)this.myAgent).startCoalition(content);
			this.myAgent.addBehaviour(new LookForTheGolem(this.myAgent));
			//System.out.println(this.myAgent.getLocalName()+" : I'll chase the golem near "+content);
			this.sendMessageToOtherAgent(ACLMessage.AGREE, "answerPropose");
			this.timeConversation = LocalDateTime.now();
		}
		
		else if(msgProposeBringOtherAgents != null) {
			//si on veut que 2 agents dans 2 coalitions s'ignorent
//			if(!((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("catchedGolem") && 
//					((ExploreSoloAgent)this.myAgent).chaseAgentsPosition==null) {
//				System.out.println(this.myAgent.getLocalName()+"<---- proposal received from "+msgProposeBringOtherAgents.getSender().getLocalName());
//				Pair<String, Pair<Integer, ArrayList<String>>> content = null;
//				try {
//					content = (Pair<String, Pair<Integer, ArrayList<String>>>) msgProposeBringOtherAgents.getContentObject();
//				} catch (UnreadableException e1) {
//					e1.printStackTrace();
//				}
//				
//				((ExploreSoloAgent)this.myAgent).startCoalition(content.getKey());
//				this.myAgent.addBehaviour(new CreateCoalition(this.myAgent, content.getValue().getKey(), 
//						content.getValue().getValue()));
//				System.out.println(this.myAgent.getLocalName()+" : I'll bring "+content.getValue().getKey()+" more agents !");
//				this.sendMessageToOtherAgent(ACLMessage.AGREE, "answerPropose");
//			}else {
//				this.sendMessageToOtherAgent(ACLMessage.REJECT_PROPOSAL, "answerPropose");
//			}

			System.out.println(this.myAgent.getLocalName()+"<---- proposal received from "+msgProposeBringOtherAgents.getSender().getLocalName());
			Pair<String, Pair<Integer, ArrayList<String>>> content = null;
			try {
				content = (Pair<String, Pair<Integer, ArrayList<String>>>) msgProposeBringOtherAgents.getContentObject();
			} catch (UnreadableException e1) {
				e1.printStackTrace();
			}
			
			((ExploreSoloAgent)this.myAgent).startCoalition(content.getKey());
			this.myAgent.addBehaviour(new CreateCoalition(this.myAgent, content.getValue().getKey(), 
					content.getValue().getValue()));
			//System.out.println(this.myAgent.getLocalName()+" : I'll bring "+content.getValue().getKey()+" more agents !");
			this.sendMessageToOtherAgent(ACLMessage.AGREE, "answerPropose");
			this.timeConversation = LocalDateTime.now();
		}
		
		// Si on reçoit un message pour finir la conversation ou on a pas eu de réponse depuis
		// plus de 5 secondes, on arrête de parler 
		else if(msgEnd != null ||  Duration.between(this.timeConversation, LocalDateTime.now()).getSeconds() >5 ) {

			info = new EndInformation(this);
			if(info.AswerGive(msgEnd)) {
				this.finished = true;
			}
		}		
		
	}		
	
}