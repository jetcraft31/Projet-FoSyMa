package eu.su.mas.dedaleEtu.mas.behaviours.Determine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.K_means;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Communication.Information.OpenNodeInformation;


public class DetermineNextPosition extends OneShotBehaviour{

	private static final long serialVersionUID = 1L;
	
	private String positionAgent2 ;
	private MapRepresentation map;
	private OpenNodeInformation giveInformation;
	private HashSet<String> openNode = new HashSet<String>();
	
	private K_means ensembleAgent1et2 = new K_means();
	
	public DetermineNextPosition(final Agent myagent, String posAgent2,HashSet<String> NoeudPersoAExplorerAgent2,
			OpenNodeInformation giveInfor) {
		
		super(myagent);
		this.positionAgent2=posAgent2;
		this.map = ((ExploreSoloAgent)this.myAgent).myMap;
		this.giveInformation = giveInfor;
		
		// Si on avait déjà des noeuds, alors, on les prends
		if( ((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer.size() != 0){
			this.openNode.addAll( ((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer);
			((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer.clear();

		// Sinon on prend les noeuds de l'ensemble des noeuds ouverts
		}else {
			this.openNode.addAll(map.getOpenNode(((ExploreSoloAgent)this.myAgent).getCurrentPosition(),
					((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll()));
		}
		
		if(!NoeudPersoAExplorerAgent2.isEmpty()) {
			
			// Vérification, pour pas qu'il y ait de bug
			for(String i : NoeudPersoAExplorerAgent2) {
				if(!this.map.getClosedNode().contains(i) && this.map.getOpenNode(((ExploreSoloAgent)this.myAgent).getCurrentPosition(),
						((ExploreSoloAgent)this.myAgent).getNodeNotAvailableAll()).contains(i)) {
					this.openNode.add(i);
				}
			}
			
		}
		
	}
	
	public void action() {
				
		// Si il n'y a pas assez de noeud ouvert 
		// Pas besoin de diviser les taches
		if(this.openNode.size()>=2) { 			
			
			// On détermine les ensembles 1 et 2 qui contiennent tous les noeuds ouverts
			HashMap<String,HashSet<String>> ensembleBestNoeud =  ensembleAgent1et2.EnsembleAgent1et2(this.openNode,2,this.map);			
			
			//  On détermine le meilleurs ensembles pour l'agent 1 et pour l'agent 2 en fonction de leur position
			Iterator<String> iterator = ensembleBestNoeud.keySet().iterator();
			 
			int BestRepartionAgent1[]= new int[2];
			int BestRepartionAgent2[]= new int[2];
			String keyValue[]= new String[2];
			int i = 0;
			 
			while(iterator.hasNext()) {
				keyValue[i]=iterator.next();
				
				List<String> tmp = this.map.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),
						keyValue[i] );
				if(tmp !=null) {
					BestRepartionAgent1[i] = this.map.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),
							keyValue[i] ).size();
				}
				
				 tmp = this.map.getShortestPath(this.positionAgent2, keyValue[i] );
				if(tmp !=null) {
					BestRepartionAgent2[i] = tmp.size();
				}
				i++;
			}
			 
			if(BestRepartionAgent1[0]+BestRepartionAgent2[1]<BestRepartionAgent1[1]+BestRepartionAgent2[0]) {
				 // L'agent 1 prend l'ensemble 1 et l'agent 2 prend l'ensemble 2
				 
				((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer.addAll(ensembleBestNoeud.get(keyValue[0]));
				this.giveInformation.setNoeudAExplorerAgent2(ensembleBestNoeud.get(keyValue[1]));
				 
			}else {
				((ExploreSoloAgent)this.myAgent).OpenNodeToExplorer.addAll(ensembleBestNoeud.get(keyValue[1]));
				this.giveInformation.setNoeudAExplorerAgent2(ensembleBestNoeud.get(keyValue[0]));
	
			}
		}
	}
	
}
