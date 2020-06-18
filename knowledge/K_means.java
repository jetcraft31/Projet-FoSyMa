package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class K_means {

	public HashMap<String,HashSet<String>> EnsembleAgent1et2(Set<String> idNoeud,
			int numberKmean, MapRepresentation map) {	
		
		HashMap<String, HashSet<String>> HashMapFinal = new HashMap<String, HashSet<String>>();		
		HashMap<String, List<String>> Dist1BetweenNode = map.getDist1BeetweenNode();

		HashMap<String,HashSet<String>> ResultatEtapeN0 = new HashMap<String,HashSet<String>>();
		boolean finished = false;
				
		ResultatEtapeN0= InitialitionN0(idNoeud,Dist1BetweenNode,idNoeud);
		HashMapFinal = FindBestPartition(ResultatEtapeN0,idNoeud,numberKmean);
		
		if(HashMapFinal.size() != 0 ) {
			finished = true;
			DeleteDoublonPartition(HashMapFinal);
		}
		
		while (!finished) {
		
			// Calcul des distances +1
			HashMap<String,HashSet<String>> ResultatEtapeN1 = new HashMap<String,HashSet<String>>();
			ResultatEtapeN1 = ResultatEtapeN1(ResultatEtapeN0,Dist1BetweenNode,idNoeud);

			// **************** Partie vérification ****************//
			// On vérifie si à la distance N+1, il y a 2 points qui permettent d'observer tous les points ouverts
			HashMapFinal = FindBestPartition(ResultatEtapeN1,idNoeud,numberKmean);
			
			if(HashMapFinal.size() != 0 ) {
				finished = true;
				DeleteDoublonPartition(HashMapFinal);
			}

			ResultatEtapeN0 = new HashMap<String,HashSet<String>>();
			ResultatEtapeN0.putAll(ResultatEtapeN1);
		}
		return HashMapFinal;        
    } 
	
	// ********** Initialisation
	// Pour chaque noeud voisin d'un noeud ouvert, on l'ajoute dans un dictionnaire
	// avec le noeud voisin la clé et le/les noeud ouvert voisins les valeurs 
	private HashMap<String,HashSet<String>> InitialitionN0(Set<String> idNoeud,
			HashMap<String, List<String>> Dist1BetweenNode, Set<String> idNoeud2 ){
		
		HashMap<String,HashSet<String>> ResultatEtapeN0 = new HashMap<String,HashSet<String>>();
		
		for(String i : idNoeud) {
			if(Dist1BetweenNode.get(i) != null) {
				for(String j : Dist1BetweenNode.get(i)) 	{
					
					HashSet<String> setTmp = new HashSet<String>();
		
					if(ResultatEtapeN0.containsKey(j)) {
						setTmp.addAll(ResultatEtapeN0.get(j));
					}
					
					/* Ne devrait jamais arriver
					if(NoeudOuvert.contains(j)) {
						setTmp.add(j);
					}*/
					
					if(idNoeud2.contains(i)) {
						setTmp.add(i);
						ResultatEtapeN0.put(j,setTmp);
					}else {
						ResultatEtapeN0.put(i,setTmp);
					}
				}
			}
			
		}
		return ResultatEtapeN0;
	}
	
	
	// ***************Partie calcul des distances +1 ***************//
	//En pouvant avancer d'une distance supplémentaire, est ce qu'on a accès à des nouveaux noeuds ouverts ?
	private HashMap<String,HashSet<String>> ResultatEtapeN1(HashMap<String,HashSet<String>> ResultatEtapeN0,
			HashMap<String, List<String>> Dist1BetweenNode, Set<String> idNoeud){
				
		// On crée l'ensemble à l'étape N+1
		HashMap<String,HashSet<String>> ResultatEtapeN1 = new HashMap<String,HashSet<String>>();
		
		// Si il y a un nouveau noeud à une distance +1 qui a accès à un noeud ouvert, alors
		// il va falloir l'ajouter
		HashSet<String> NouveauAExplorer = new HashSet<String>();
		
		// On regarde ceux qu'on a trouvé dans l'étape N, pour construire l'étape N+1
		for(Entry<String, HashSet<String>> i : ResultatEtapeN0.entrySet()) {
			
			HashSet<String> tmp = new HashSet<String>();
			tmp.addAll(i.getValue()); // Pour une clé i, on ajoute toutes ses valeurs de l'étape N
			
			// En rajoutant une distance, est ce qu'il a accès à de nouveaux noeuds ouverts? 
			// Si oui on ajoute ces informations, et si on découvre un nouveau noeuds, alors
			// on l'ajoute à l'ensemble des poitns à découvrir
			for(String j : Dist1BetweenNode.get(i.getKey())) 	{
				if(ResultatEtapeN0.containsKey(j)) {
					
					tmp.addAll(ResultatEtapeN0.get(j));
				}else if(!idNoeud.contains(j)) {
					NouveauAExplorer.add(j);
				}
			}
			ResultatEtapeN1.put(i.getKey(),tmp );
		}
		
		// On ajoute les nouveaux noeuds qu'on a découvert.
		ResultatEtapeN1.putAll(InitialitionN0(NouveauAExplorer,Dist1BetweenNode,idNoeud));
		return ResultatEtapeN1;
	}
	
	private HashMap<String, HashSet<String>> FindBestPartition(HashMap<String,HashSet<String>> ResultatEtapeN1,
			Set<String> idNoeud, int numberKmean) {
		HashMap<String, HashSet<String>> HashMapFinal = new HashMap<String, HashSet<String>>();		

		for(Entry<String, HashSet<String>> i : ResultatEtapeN1.entrySet()) {
			if(i.getValue().size()>= idNoeud.size()/numberKmean) {
				
				HashSet<String> ensembleNode = new HashSet<String>();
				ensembleNode.addAll(i.getValue());
				
				HashMap<String, HashSet<String>> resultTmp = new HashMap<String, HashSet<String>>();
				
				if( numberKmean > 1 ) {
					HashSet<String> newIdNoeud = new HashSet<String>();
					newIdNoeud.addAll(idNoeud);
					newIdNoeud.removeAll(i.getValue());
					
					resultTmp = FindBestPartition(ResultatEtapeN1,newIdNoeud,numberKmean-1);
					
					if(resultTmp.size() != 0) {
						ensembleNode.addAll(newIdNoeud);
					}
					
				}
				
				if(ensembleNode.containsAll(idNoeud) && !resultTmp.containsKey(i.getKey())) {
					HashMapFinal.putAll(resultTmp);
					HashMapFinal.put(i.getKey(),i.getValue());
					break;
				}
				
			}
		}
		return HashMapFinal;
	}
	
	private void DeleteDoublonPartition(HashMap<String, HashSet<String>> HashMapFinal){
		
		for(Entry<String, HashSet<String>> PartitionI : HashMapFinal.entrySet()) {
			for(Entry<String, HashSet<String>> PartitionJ : HashMapFinal.entrySet()) {
				
				if(!PartitionI.equals(PartitionJ)) {
					HashSet<String> PartitionJTmp = new HashSet<String>();
					PartitionJTmp.addAll(PartitionJ.getValue());
					
					for(String elementJ : PartitionJ.getValue()) {
						
						if(PartitionI.getValue().contains(elementJ)) {
	
							if(PartitionI.getValue().size() > PartitionJTmp.size()) {
								PartitionI.getValue().remove(elementJ);
							}else {
								PartitionJTmp.remove(elementJ);
							}
						}
					}
					
					HashMapFinal.put(PartitionJ.getKey(), PartitionJTmp);
				}
			}
		}
	}
}
