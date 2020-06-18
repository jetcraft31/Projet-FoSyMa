package eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.ConversationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Determine.DetermineNextPosition;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class OpenNodeInformation extends Information{

	private static final long serialVersionUID = 1L;
	
	private HashSet<String> SendNoeudAExplorerAgent2;

	public OpenNodeInformation(ConversationBehaviour conv) {
		super(conv);
	}

	public boolean SendNeed() {
		
		this.timeConversation = LocalDateTime.now();
		
		ArrayList<Object> array = new ArrayList<Object>();
		
		// On ajoute notre position et on ajoute nos noeuds perso ouvert à explorer
		// (Permet de s'organiser avce plusieurs personne )
		array.add(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
		array.add(((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer);

		((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer.clear();
		
		this.conversation.sendMessageToOtherAgent(ACLMessage.REQUEST, "PlanificationOpenNode", array);
		System.out.println(this.myAgent.getLocalName()+" ----> PlanificationOpenNode Request send");
		
		return true;
	}

	public boolean ReceiveNeed() {
				
		final ACLMessage msgPlanificationOpenNode = this.myAgent.receive(MessageTemplate.and(Inform, MessageTemplate.and(Sender, PlanificationOpenNodeProtocol)));

		if(msgPlanificationOpenNode != null){
			this.timeConversation = LocalDateTime.now();
			HashSet<String> msg = new HashSet<String>();
			try {
				msg = (HashSet<String>) msgPlanificationOpenNode.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if( msg != null) {
				((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer.addAll(msg);
			}
			
			System.out.println(this.myAgent.getLocalName()+" ----> PlanificationOpenNode received");
			this.com.AskInProgressForOpenNode --;
			
			return true;
		}
		return false;
	}

	public boolean AswerGive(ACLMessage message) {
		
		System.out.println(this.myAgent.getLocalName()+" ----> PlanificationOpenNode receive");
			
		ArrayList<Object> infoAnswer = null;
		try {
			infoAnswer = (ArrayList<Object>) message.getContentObject();
		} catch (UnreadableException e1) {
			e1.printStackTrace();
		}
		
		// On calcule la planification
		DetermineNextPosition determine = new DetermineNextPosition(
				this.myAgent,(String) infoAnswer.get(0),(HashSet<String>) infoAnswer.get(1),this);
	
		determine.action();
		
		// On envoie la planification
		this.conversation.sendMessageToOtherAgent(ACLMessage.INFORM, "PlanificationOpenNode", this.SendNoeudAExplorerAgent2);
		
		System.out.println(this.myAgent.getLocalName()+" ----> PlanificationOpenNode sent");
		
		this.timeConversation = LocalDateTime.now();
		
		return true;
	}
	
	public void setNoeudAExplorerAgent2(HashSet<String> newSet) {
		this.SendNoeudAExplorerAgent2 = newSet;
	}
	
	

}
