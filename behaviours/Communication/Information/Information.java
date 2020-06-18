package eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information;

import java.time.LocalDateTime;

import eu.su.mas.dedaleEtu.mas.behaviours.Communication.CommunicationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.ConversationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.StartCommunicationBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class Information extends CommunicationBehaviour{

	private static final long serialVersionUID = 1L;

	protected StartCommunicationBehaviour com; // Le behavior global pour la communication
	
	// ********* Info sur l'autre Agent
	protected String otherAgent; 
	protected final MessageTemplate Sender;
	
	protected LocalDateTime timeConversation;
	protected ConversationBehaviour conversation;
	
	public Information(ConversationBehaviour conv) {
		super(conv.getAgent()) ;
		this.conversation = conv;
		this.otherAgent = conv.otherAgent;
		this.timeConversation = conv.timeConversation;
		this.com= conv.com;
		this.Sender = conv.Sender;
		
	}
	// Fonction pour celui qui demande ou information
	public abstract boolean SendNeed();
	public abstract boolean ReceiveNeed();
	
	// Fonction pour celui qui recoie une demande
	public abstract boolean AswerGive(ACLMessage message);
}
