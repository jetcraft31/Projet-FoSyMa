package eu.su.mas.dedaleEtu.mas.behaviours.Communication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.EndInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.Information;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.LookForTheGolem;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.util.Pair;

public class InviteCoalitionBehaviour  extends ConversationBehaviour {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean askedCoordinates = false;
	private boolean receivedCoordinates = false;
	private boolean receivedAnswerPropose = false;
	private HashMap<String, String> actions;
	
	//ce behaviour permet d'envoyer les messages permettants d'inviter un agent
	//dans sa coalition
	public InviteCoalitionBehaviour(StartCommunicationBehaviour com, final Agent myAgent,String receiver) {
		super(com, myAgent, receiver);
	}
	
	public void action() {
		if(Duration.between(this.timeConversation, LocalDateTime.now()).getSeconds() <= 5 &&
				(!this.receivedAnswerPropose && ((ExploreSoloAgent)this.myAgent).getCurrentWalkBehavior().equalsIgnoreCase("createCoal"))) {
			if(!this.receivedAnswerPropose) {
				if(!this.askedCoordinates) {
					this.timeConversation = LocalDateTime.now(); // On mets Ã  jour le time car, on a envoyÃ© une demande
					
					// On envoie une demande de coordonnees
					this.sendMessageToOtherAgent(ACLMessage.REQUEST, "CoordinatesRequest");

					System.out.println(this.myAgent.getLocalName()+" ----> asked coordinates to "+this.otherAgent);
					this.askedCoordinates = true;
				}else {
					if(!this.receivedCoordinates) {
		
						final ACLMessage msgCoord = this.myAgent.receive(MessageTemplate.and(Inform, MessageTemplate.and(Sender, CoordinatesRequestProtocol)));
						
						final ACLMessage msgCoordRefuse = this.myAgent.receive(MessageTemplate.and(Refuse, MessageTemplate.and(Sender, CoordinatesRequestProtocol)));
						
						// On a recu une réponse
						if(msgCoord != null){
							
							// On mets à jour le temps
							this.timeConversation = LocalDateTime.now();
							
							System.out.println(this.myAgent.getLocalName()+"<---- coordinates received from "+msgCoord.getSender().getLocalName());
							Pair<String, String> coordinates = null;
							try {
								coordinates = (Pair<String, String>) msgCoord.getContentObject();
							} catch (UnreadableException e1) {
								e1.printStackTrace();
							}
							
							this.receivedCoordinates = true; // On a reÃ§u une rÃ©ponse
							
							// On calcule les actions a  effectuer en fonction des donnees recues
							this.actions = this.determineActions(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),
									coordinates.getKey(),coordinates.getValue(),this.otherAgent);
							if(this.actions.get(this.otherAgent).equalsIgnoreCase("bringOtherAgents")) {
								Pair<String, Pair<Integer, ArrayList<String>>> content=
										new Pair<String, Pair<Integer, ArrayList<String>>>(((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint, 
												new Pair<Integer, ArrayList<String>>(((ExploreSoloAgent)this.myAgent).getNbAgentToBring()-1,
														((ExploreSoloAgent)this.myAgent).centers));
								this.sendMessageToOtherAgent(ACLMessage.PROPOSE, "ProposeBringOtherAgents", content);
							}else {
								String content=((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint;
								this.sendMessageToOtherAgent(ACLMessage.PROPOSE, "ProposeChase", content);
							}
							System.out.println(this.myAgent.getLocalName()+" ----> sent propose to "+this.otherAgent);
							
						}else if(msgCoordRefuse!=null) {
							System.out.println(this.myAgent.getLocalName()+"<---- refuse received from "+msgCoordRefuse.getSender().getLocalName());
							((ExploreSoloAgent)this.myAgent).updateNextCenter();
							this.receivedCoordinates = true;
							this.receivedAnswerPropose=true;
						}
					}else {
						final ACLMessage msgAgree = this.myAgent.receive(MessageTemplate.and(Agree, MessageTemplate.and(Sender, answerProposeProtocol)));
						
						// On a recu une reponse positive
						if(msgAgree != null){
							this.timeConversation = LocalDateTime.now();
							System.out.println(this.myAgent.getLocalName()+" ----> Agreement received");
							((ExploreSoloAgent)this.myAgent).reduceNbAgentToBring();
							if(this.actions.get(this.myAgent.getLocalName()).equalsIgnoreCase("chaseGolem")) {
								this.myAgent.addBehaviour(new LookForTheGolem(this.myAgent));
							}else {
								((ExploreSoloAgent)this.myAgent).updateNextCenter();
							}
							
							this.receivedAnswerPropose=true;
						}else {
							final ACLMessage msgRefuse = this.myAgent.receive(MessageTemplate.and(Refuse, MessageTemplate.and(Sender, answerProposeProtocol)));
							// On a recu une reponse negative
							if(msgRefuse != null){
								this.timeConversation = LocalDateTime.now(); // On mets Ã  jour le time car, on a envoyÃ© une demande
								System.out.println(this.myAgent.getLocalName()+" ----> Refused received");
							}
							((ExploreSoloAgent)this.myAgent).updateNextCenter();
							this.receivedAnswerPropose=true;
						}
					}
				}
			}
		// La conversation est terminee donc on lui envoie un message pour le prevenir
		}else {
			
			Information end = new EndInformation(this);
			
			end.SendNeed();
			this.finished = true;
		}
	}
	
	private HashMap<String, String> determineActions(String currentPosition, String otherAgentPosition,
			String otherAgentCenter, String otherAgentName) {
		HashMap<String, String> actions = new HashMap<String, String>();
		if(((ExploreSoloAgent)this.myAgent).nextCenter!=null && ((ExploreSoloAgent)this.myAgent).nextCenter.equalsIgnoreCase(otherAgentCenter)) {
			((ExploreSoloAgent)this.myAgent).removeCenter(otherAgentCenter);
		}
		if(((ExploreSoloAgent)this.myAgent).centers.isEmpty()) {
			actions.put(this.myAgent.getLocalName(),"chaseGolem");
			actions.put(otherAgentName, "chaseGolem");
		}else {
			int sumUnchange=((ExploreSoloAgent)this.myAgent).myMap.getShortestPath(currentPosition, 
					((ExploreSoloAgent)this.myAgent).determineClosestCenter(currentPosition)).size();
			int sumChange=((ExploreSoloAgent)this.myAgent).myMap.getShortestPath(currentPosition, 
					((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint).size();
			sumUnchange+=((ExploreSoloAgent)this.myAgent).myMap.getShortestPath(otherAgentPosition, 
					((ExploreSoloAgent)this.myAgent).coalitionMeetingPoint).size();
			sumChange+=((ExploreSoloAgent)this.myAgent).myMap.getShortestPath(otherAgentPosition, 
					((ExploreSoloAgent)this.myAgent).determineClosestCenter(otherAgentPosition)).size();
			if(sumChange>sumUnchange) {
				actions.put(this.myAgent.getLocalName(), "bringOtherAgents");
				actions.put(otherAgentName, "chaseGolem");
			}else {
				actions.put(this.myAgent.getLocalName(), "chaseGolem");
				actions.put(otherAgentName, "bringOtherAgents");
			}	
		}
		return actions;
	}
}
