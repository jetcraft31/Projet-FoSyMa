package eu.su.mas.dedaleEtu.mas.behaviours.Communication;

import java.time.Duration;
import java.time.LocalDateTime;


import jade.core.Agent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.Information;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.EndInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.InformAgentAndPosNotAvailableInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.InformAgentToChangePatrolInformation;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.InformAgentToExchangeNoodLookFor;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.MapInformationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.OpenNodeInformation;


public class NeedInformationBehaviour  extends ConversationBehaviour {
	private static final long serialVersionUID = 8567689731496787661L;

	// ********* Info sur l'autre Agent
	
	// ************ Les différents états possibles
	private boolean SendHelpMap = false; // Est ce qu'il a envoyé une demande
	private boolean ReceiveAnswerForTheMap = false; // Est ce qu'il a deja reçu une réponse
	
	private boolean SendPlanificationOpenNode = false;
	private boolean ReceiveAnswerPlanificationOpenNode = false;
	
	private boolean SendInformAgentToChange = false;
	private boolean ReceiveAnswerInformAgentToChange = false;	
	
	private boolean SendInformAgentAndPosNotAvailable = false;
	private boolean ReceiveInformAgentAndPosNotAvailable = false;

	private boolean SendInformAgentToExchangeNoodLookFor = false;
	private boolean ReceiveInformAgentToExchangeNoodLookFor = false;	
	
	public NeedInformationBehaviour(StartCommunicationBehaviour com, final Agent myAgent,String receiver) {
		super(com, myAgent, receiver);
	}

	public void action() {		
		
		// ************** Partie envoie de demande
		
		/* Si on attend pas trop longtemps, et ( si on a besoin d'une information et qu'on a pas déjà
		 * reçu une réponse pour cette demande ), alors .... 
		 */
		if(Duration.between(this.timeConversation, LocalDateTime.now()).getSeconds() <= 5 &&
				( (!this.ReceiveAnswerForTheMap && ((ExploreSoloAgent)this.myAgent).getNeedMap()) || 
				(!this.ReceiveAnswerPlanificationOpenNode && ((ExploreSoloAgent)this.myAgent).getNeedPlanificationOpenNode()) || 
				(!this.ReceiveAnswerInformAgentToChange && ((ExploreSoloAgent)this.myAgent).getInformAgentToChangePatrol()) ||
				(!this.ReceiveInformAgentAndPosNotAvailable && ((ExploreSoloAgent)this.myAgent).getInformAgentAndPosNotAvailable()) )) {

			Information info;
			// On cherche plus précisément la partie 
			
			// Si on a besoin de la carte et qu'on n'a pas déjà demandé 
			if(((ExploreSoloAgent)this.myAgent).getNeedMap() & !this.ReceiveAnswerForTheMap) {		

				info = new MapInformationBehaviour(this);
				// Si on a jamais envoyé de demande de map, alors on envoie cette demande
				if(!this.SendHelpMap) {
					if(info.SendNeed()) {
						this.SendHelpMap = true;
					}
				}
				// Si on a déjà demandé la carte, on attend une réponse
				else {	
					
					if(info.ReceiveNeed()) {
						this.ReceiveAnswerForTheMap = true; // On a reçu une réponse
					}
					
				}
				
			// Si besoin d'avoir des noeuds perso ouverts
			}else if(((ExploreSoloAgent)this.myAgent).getNeedPlanificationOpenNode() &&
					!this.ReceiveAnswerPlanificationOpenNode && this.ReceiveAnswerForTheMap ) {

				info = new OpenNodeInformation(this);
				
				// Si on a jamais envoyé de demande en envoie une demande
				if(!this.SendPlanificationOpenNode) {
					
					if(info.SendNeed()) {
						this.SendPlanificationOpenNode = true;
					}
					
				}else {
					
					if(info.ReceiveNeed()) {
						this.ReceiveAnswerPlanificationOpenNode = true;
					}
				}	
				
			// Prevenir un agent qu'on a pris sa patrouille
			}else if(((ExploreSoloAgent)this.myAgent).getInformAgentToChangePatrol() &&
					!this.ReceiveAnswerInformAgentToChange) {

				info = new InformAgentToChangePatrolInformation(this);

				
				if(!this.SendInformAgentToChange) {
					
					if(info.SendNeed()) {
						this.SendInformAgentToChange = true;
					}
					
				}else {
					
					if(info.ReceiveNeed()) {
						this.ReceiveAnswerInformAgentToChange = true;
					}
				}		
			// Informer les autres agents que certaines positions et agents ne sont plus disponibles
				
			}else if(((ExploreSoloAgent)this.myAgent).getInformAgentAndPosNotAvailable() &&
				!this.ReceiveInformAgentAndPosNotAvailable ) {

				// On prévient les agents que d'autres agents et d'autres position ne sont plus disponible
				
				info = new InformAgentAndPosNotAvailableInformation(this);

			
				// Si on a jamais envoyé de demande en envoie une demande
				if(!this.SendInformAgentAndPosNotAvailable) {
					
					if(info.SendNeed()) {
						this.SendInformAgentAndPosNotAvailable = true;
					}
					
				}else {
					
					if(info.ReceiveNeed()) {
						this.ReceiveInformAgentAndPosNotAvailable = true;
					}
					
				}
			}else if(((ExploreSoloAgent)this.myAgent).getNeedExchangeNodeLookFor() &&
					!this.ReceiveInformAgentToExchangeNoodLookFor ) {
				
				info = new InformAgentToExchangeNoodLookFor(this);
				
				// Si on a jamais envoyé de demande en envoie une demande
				if(!this.SendInformAgentToExchangeNoodLookFor ) {
					
					if(info.SendNeed()) {
						this.SendInformAgentToExchangeNoodLookFor = true;
					}
					
				}else {
					
					if(info.ReceiveNeed()) {
						this.ReceiveInformAgentToExchangeNoodLookFor = true;
					}
					
				}
			}
			// On arrête la conversation, si on a plus rien à demander, ou si on a pas eu de réponse
			// pendant 5 secondes
		}else {
			Information end = new EndInformation(this);
			
			if(end.SendNeed()) {
				this.finished = true;
			}			
		}
	}
}	