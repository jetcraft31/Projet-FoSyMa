package eu.su.mas.dedaleEtu.mas.behaviours.Determine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Walk.PatrouilleWalkBehaviour;

public class DeterminePatrouille extends OneShotBehaviour{

	private static final long serialVersionUID = 1L;
	
	private MapRepresentation map;
	private HashMap<String,List<String>> dist1; 
	private boolean runWalkPatrouille;

	private HashSet<String> ensembleNoeudObservable = new HashSet<String>();
	private ArrayList<ArrayList<String>> Partition = new ArrayList<ArrayList<String>>();
	
	private List<String> agentListAvailable = new ArrayList<String>();
	private Set<String> nodeNotAvailable = new HashSet<String>();
	
	public DeterminePatrouille(final Agent myagent, List<String> AgentListAvailable, Set<String> NodeNotAvailable, 
			boolean RunWalkPaatrouille) {
		super(myagent);
		this.map = ((ExploreSoloAgent)this.myAgent).myMap;	
		this.agentListAvailable = AgentListAvailable;
		this.nodeNotAvailable = NodeNotAvailable;
		this.dist1 = this.map.getDist1BeetweenNode();
		this.runWalkPatrouille = RunWalkPaatrouille;
	}
	
	public void action() {

		// Initialisation *************************
		
		String bestNodeIni = null;
		float bestNodeIniValue = 0;
		
		for( int agentI=0; agentI<this.agentListAvailable.size();agentI++){
			
			for(String i : this.map.getClosedNode()) {
				
				if(!this.nodeNotAvailable.contains(i)) {
					float valueTmp = calculPointNoeudI(i, 3,ensemblePartition(this.Partition),this.ensembleNoeudObservable);
					
					if(bestNodeIniValue< valueTmp) {
						bestNodeIni = i;
						bestNodeIniValue = valueTmp;
					}
				}
			}

			if(bestNodeIni != null) {
				this.Partition.add( new ArrayList<>(Arrays.asList(bestNodeIni)) );
				miseAjourNoeudObservable(this.ensembleNoeudObservable,bestNodeIni);
				
				bestNodeIni = null;
				bestNodeIniValue = 0;
			}
			
		}
		
		System.out.println("Partition Init"+this.Partition);
		
		ArrayList<ArrayList<String>> PartitionTmp = new ArrayList<ArrayList<String>>();
		HashSet<String> ensembleNoeudObservableTmp = new HashSet<String>();
		
		PartitionTmp.addAll(Partition);
		ensembleNoeudObservableTmp.addAll(ensembleNoeudObservable);
		
		int rangeRecherche = 2;
		boolean Modification = true;
		
		while(!IlResteDesNoeuds(ensemblePartition(PartitionTmp), ensembleNoeudObservableTmp)) 
		{
			// Si on a fait aucune modification, alors on agrandi notre rayon de recherche
			if(!Modification) {
				
				PartitionTmp = new ArrayList<ArrayList<String>>();
				PartitionTmp.addAll(Partition);
				
				ensembleNoeudObservableTmp = new HashSet<String>();
				ensembleNoeudObservableTmp.addAll(ensembleNoeudObservable);
				
				rangeRecherche ++;
			}
			
			Modification = false;
					
			for(ArrayList<String> i : PartitionTmp) 
			{
				String bestNode = chercheNouveauNoeud(i.size()-1,i, ensembleNoeudObservableTmp,
						rangeRecherche,ensemblePartition(PartitionTmp));
				if (bestNode != null) {

					i.add(bestNode);
					miseAjourNoeudObservable(ensembleNoeudObservableTmp,bestNode);
					Modification = true;
				}
			}
		}
				
		System.out.println("Partion crée"+PartitionTmp);
		
		Partition = PartitionTmp;
		
		//Determiner la partition pour notre agent
				
		HashMap<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
		int i =0;
		for (String individu : this.agentListAvailable) {
			map.put(individu,Partition.get(i));
			i++;
		}
		((ExploreSoloAgent)this.myAgent).setNodeToPatrouille(map);
		
		if(this.runWalkPatrouille)
			this.myAgent.addBehaviour(new PatrouilleWalkBehaviour(this.myAgent));
	}
	
	private float calculPointNoeudI(String nomNoeudI, int taillePropagation,
			HashSet<String> NoeudParcouru, HashSet<String> NoeudSentie) {
		
		HashSet<String> noeudParcouruTmp = new HashSet<String>();
		
		noeudParcouruTmp.addAll(NoeudParcouru);
		
		float valueNoeudI = 0;
		
		if( taillePropagation > 1) {
			noeudParcouruTmp.add(nomNoeudI);			
			
			for(String i : dist1.get(nomNoeudI)) {
				if( !noeudParcouruTmp.contains(i)) {
					// Je divise pour dire les noeuds des voisins des voisins sont moins importants
					valueNoeudI += calculPointNoeudI(i,taillePropagation-1,noeudParcouruTmp,NoeudSentie)/2;					
				}
			}
			valueNoeudI += calculPointNoeudI(nomNoeudI,1,noeudParcouruTmp,NoeudSentie);
		}else { 

			int nombreVoisinNonSentie = 0;
			for (String voisin : dist1.get(nomNoeudI)) {
				
				if(!NoeudSentie.contains(voisin) && !this.nodeNotAvailable.contains(voisin)) {
					nombreVoisinNonSentie ++;
				}
			}
			
			if(nombreVoisinNonSentie != 0) {
				int tmpI= 1;
				while(true) {
					if(nombreVoisinNonSentie == tmpI) {
						valueNoeudI += 0.25*Math.pow(2, tmpI-1);
						break;
					}
					tmpI ++;
				}
			}
		}	
		return valueNoeudI;		
	}
	
	
	private boolean IlResteDesNoeuds(HashSet<String> Partition,HashSet<String> ensembleNoeudObservable) {
		
		HashSet<String> tmp = new HashSet<String>();
		tmp.addAll(ensembleNoeudObservable);
		tmp.addAll(Partition);
		tmp.addAll(this.nodeNotAvailable);
		
		if(tmp.containsAll(this.map.getClosedNode())) {
			// Si tous les noeuds sont observable ou sur le parcours d'un agent, alors on retourne vrai
			return true;
		}else {
			return false;
		}
		
	}
	
	private String chercheNouveauNoeud(int indiceRecherche,ArrayList<String> partitionActuelle,
			HashSet<String> ensembleNoeudObservable, int rangeRecherche,HashSet<String> ensemblePartition) {
		
		HashSet<String> NoeudParcouru = new HashSet<String>();
		NoeudParcouru.addAll(partitionActuelle);
		
		float max = 0;
		String bestNode = null;
		
		for(String t :  map.getDist1BeetweenNode().get(partitionActuelle.get(indiceRecherche) )) {
			
			float valueTmp = 0;
			
			if(!ensemblePartition.contains(t)) {
				valueTmp = calculPointNoeudI(t, rangeRecherche,NoeudParcouru, ensembleNoeudObservable);
			}
			
			if( max< valueTmp) {
				max = valueTmp;
				bestNode = t;
			}
			
		}
		
		if(bestNode == null) {
			// Ici, ça veut dire qu'aucun noeud n'a possible à une valeur supérieur à 0 (soit aucun voisin)
			// (soit on peut déjà sentir toute ses cases)
			// Donc, on cherche un autre point dans notre ensemble de partition actuelle
			if(indiceRecherche > 0) { 
				bestNode = chercheNouveauNoeud(indiceRecherche-1,partitionActuelle,ensembleNoeudObservable,
						rangeRecherche,ensemblePartition);
			}
		}
		return bestNode;
	}
	
	private void miseAjourNoeudObservable(HashSet<String> noeudSentie,String pos){
		noeudSentie.addAll(map.getDist1BeetweenNode().get(pos));
		noeudSentie.add(pos);
	}	
	
	private HashSet<String> ensemblePartition(ArrayList<ArrayList<String>> Partition){
		
		HashSet<String> ensemblePartition = new HashSet<String>();
		for(ArrayList<String> i : Partition) {
			ensemblePartition.addAll(i);
		}
		return ensemblePartition;
	}
	
	
}