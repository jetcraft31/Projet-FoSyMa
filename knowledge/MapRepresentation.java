package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;

import dataStructures.serializableGraph.*;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import javafx.application.Platform;

public class MapRepresentation implements Serializable {

	public enum MapAttribute {
		agent,open,closed
	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration
	
	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		Platform.runLater(() -> {
			openGui();
		});
		//this.viewer = this.g.display();

		this.nbEdges=0;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id Id of the node
	 * @param mapAttribute associated state of the node
	 */
	public void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
			
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());		
		n.setAttribute("ui.label",id);
		
	}

	/**
	 * Add the edge if not already existing.
	 * @param idNode1 one side of the edge
	 * @param idNode2 the other side of the edge
	 */
	public void addEdge(String idNode1,String idNode2){
		try {
			this.nbEdges++;
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
			
		}catch (EdgeRejectedException e){
			//Do not add an already existing one
			this.nbEdges--;
		}

	}

	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow
	 */
	public List<String> getShortestPath(String idFrom,String idTo){
		
		List<String> shortestPath=new ArrayList<String>();
		
		if(g.getNode(idFrom) != null && g.getNode(idTo) != null) {
			Dijkstra dijkstra = new Dijkstra();//number of edge
			dijkstra.init(g);
			dijkstra.setSource(g.getNode(idFrom));
			dijkstra.compute();//compute the distance to all nodes from idFrom
			List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
			Iterator<Node> iter=path.iterator();
			while (iter.hasNext()){
				shortestPath.add(iter.next().getId());
			}
			dijkstra.clear();
			if(!shortestPath.isEmpty()) {
				shortestPath.remove(0);//remove the current position
			}else {
				shortestPath = null;
			}
		}else {
			shortestPath = null;
		}
		return shortestPath;
	}

	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),(MapAttribute)n.getAttribute("ui.class"));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
			
		}

		closeGui();

		this.g=null;

	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}
	
	// Permet de merger avec une autre Map (Après discussion)
	public void mergeGraph(HashMap<String,Object> otherMap) {
		
		for(Entry<String, Object> allMapInfo :otherMap.entrySet()) {
			
			if (allMapInfo.getKey().equalsIgnoreCase("NODES")) {
				for(Entry<String,String> nodeInfo : ((HashMap<String,String>)allMapInfo.getValue()).entrySet()) {
					
					if(this.g.getNode(nodeInfo.getKey()) == null || 
							!(this.g.getNode(nodeInfo.getKey()).getAttribute("ui.class").toString().equalsIgnoreCase("closed")) ) {
						this.addNode(nodeInfo.getKey(), MapAttribute.valueOf(nodeInfo.getValue()));
					}
					
				}
			}else {
				Iterator<LinkedList<String>> iterEdgeInfo =((LinkedList<LinkedList<String> >)allMapInfo.getValue()).iterator();
				while (iterEdgeInfo.hasNext()) {
					LinkedList<String> edgeInfo = iterEdgeInfo.next();
					this.addEdge(edgeInfo.get(1),edgeInfo.get(2));
				}
			}
		}		
	}
	
	//detecte si un cycle est présent dans un sous-graphe donné
	public boolean isCycle(String Current, HashMap<String,Boolean> nodes, String Parent) {
		nodes.put(Current, true);
		//System.out.println("Testing "+Current);
		for(Node adjacent:this.g.getNode(Current).neighborNodes().toArray(Node[]::new)) {
			String NodeId=adjacent.getId();
			//System.out.println("Adj : "+NodeId);
			if(!nodes.getOrDefault(NodeId,false)) {
				if(isCycle(NodeId,nodes,Current)) {
					return true;
				}
			}else if(!NodeId.equals(Parent)) {
				return true;
			}
		}
		return false;
	}
	
	//détermine si le sous-graphe donné est un arbre
	public boolean isTree(String GraphStart,String myPosition) {
		HashMap<String,Boolean> nodes = new HashMap<>();
		nodes.put(myPosition, true);
		//System.out.println("I am in "+myPosition);
		if(isCycle(GraphStart,nodes,myPosition)) {
			//System.out.println("It's a cycle !");
			return false;
		}
		
		return true;
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);////GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);
		g.display();
	}
	
	// Permet de tranformer toute les informations de notre graphe actuelle en un HashMap 
	// (pour l'envoie de message)
	public HashMap<String,Object> getGraphData(){

		HashMap<String,Object> data = new HashMap<>();


		HashMap<String,String> nodes = new HashMap<>();

		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			nodes.put(n.getId(),n.getAttribute("ui.class").toString());
		}

		LinkedList<LinkedList<String> > edges = new LinkedList<>();
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			LinkedList<String> edge = new LinkedList<>();
			edge.add(e.getId());
			edge.add(sn.getId());
			edge.add(tn.getId());
			edges.add(edge);
		}

		data.put("NODES",nodes);
		data.put("EDGES",edges);
		return data;
	}	
	
	// List des noeuds ouverts
	public HashSet<String> getOpenNode(){
		
		HashSet<String> OpenNode = new HashSet<String>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			if(n.getAttribute("ui.class").toString().equalsIgnoreCase("open")) {
				OpenNode.add(n.getId());
			}
		}
		return OpenNode;
	}
	
	// List des noeuds ouverts avc certaine position non disponible
	// (Si capture d'un golem lorsqu'on n'a pas fini l'exploration
	public HashSet<String> getOpenNode(String fromPosition, Set<String> nodeNotAvailable){
		
		HashSet<String> OpenNode = new HashSet<String>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			if(n.getAttribute("ui.class").toString().equalsIgnoreCase("open") &&
					this.getShortestPath(fromPosition, n.getId(), nodeNotAvailable)!=null) {
				OpenNode.add(n.getId());
			}
		}
		return OpenNode;
	}
	
	// List des noeuds fermé
	public HashSet<String> getClosedNode(){
		HashSet<String> ClosedNode = new HashSet<String>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			if(n.getAttribute("ui.class").toString().equalsIgnoreCase("closed")) {
				ClosedNode.add(n.getId());
			}
		}
		return ClosedNode;
	}
	
	// List de tous les noeuds
	public Set<String> getAllNode(){
		Set<String> allNode = new HashSet<String>();
		allNode.addAll(getOpenNode());
		allNode.addAll(getClosedNode());
		return allNode;
	}
	
	// Map avec pour chaque noeud leur voisin accessible en 1 déplacmeent
	public HashMap<String, List<String>> getDist1BeetweenNode(){
		
		HashMap<String, List<String>> Dist1BeetweenNode = new HashMap<String,List<String>>();
		
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			String sn=e.getSourceNode().toString();
			String tn=e.getTargetNode().toString();
			
			List<String> array = new ArrayList<String>();
			
			if(Dist1BeetweenNode.get(sn) == null) {
				array.add(tn);
			}else {
				array = Dist1BeetweenNode.get(sn);
				if(!array.contains(tn)) {
					array.add(tn);
				}
			}
			Collections.sort(array);
			Dist1BeetweenNode.put(sn, array);

			List<String> array2 = new ArrayList<String>();
			
			if(Dist1BeetweenNode.get(tn) == null) {
				array2.add(sn);
			}else {
				array2 = Dist1BeetweenNode.get(tn);
				if(!array2.contains(sn)) {
					array2.add(sn);
				}
			}
			Collections.sort(array2);
			Dist1BeetweenNode.put(tn, array2);
		
		}

		return Dist1BeetweenNode;
	}
	
	// Permet de déterminer les centres d'un ensemble de noeud
	public String determineCenter(Set<String> Node) {
		
		K_means k = new K_means();
		
		return k.EnsembleAgent1et2(Node, 1, this).keySet().iterator().next();
	}
	
	// Le graphe avec certain noeuds non disponible
	private Graph graphWithoutNode(Set<String> nodeNotAvailable) {
		Graph gTmp= new SingleGraph("tmp");
		
		HashMap<String,Object> gInfo = getGraphData();
		
		for(Entry<String, Object> allMapInfo :gInfo.entrySet()) {
			
			if (allMapInfo.getKey().equalsIgnoreCase("NODES")) {
				for(Entry<String,String> nodeInfo : ((HashMap<String,String>)allMapInfo.getValue()).entrySet()) {
					
					if(!nodeNotAvailable.contains(nodeInfo.getKey())) {
						gTmp.addNode(nodeInfo.getKey());
					}
				}
			}else {
				Iterator<LinkedList<String>> iterEdgeInfo =((LinkedList<LinkedList<String> >)allMapInfo.getValue()).iterator();
				while (iterEdgeInfo.hasNext()) {
					LinkedList<String> edgeInfo = iterEdgeInfo.next();
					
					if(!nodeNotAvailable.contains(edgeInfo.get(1)) && !nodeNotAvailable.contains(edgeInfo.get(2))){
						gTmp.addEdge(edgeInfo.get(0),edgeInfo.get(1),edgeInfo.get(2));
					}
				}
			}
		}	
		return gTmp;
		
	}
	// Permet d'obtenir le chemin le plus court pour accéder à un autre noeuds avec certain point non disponible
	public List<String> getShortestPath(String idFrom,String idTo,Set<String> nodeNotAvailable){
		
		Graph tmp = graphWithoutNode(nodeNotAvailable);
		
		List<String> shortestPath=new ArrayList<String>();
		
		if(tmp.getNode(idFrom) != null && tmp.getNode(idTo) != null) {
			Dijkstra dijkstra = new Dijkstra();//number of edge
			dijkstra.init(tmp);
			dijkstra.setSource(tmp.getNode(idFrom));
			dijkstra.compute();//compute the distance to all nodes from idFrom
			List<Node> path=dijkstra.getPath(tmp.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
			Iterator<Node> iter=path.iterator();
			while (iter.hasNext()){
				shortestPath.add(iter.next().getId());
			}
			dijkstra.clear();
			if(!shortestPath.isEmpty()) {
				shortestPath.remove(0);//remove the current position
			}else {
				shortestPath = null;
			}
		}else {
			shortestPath = null;
		}
		return shortestPath;
	}		
	
	// Obtenir les noeuds les plus proches parmi un ensemble de noeuds et d'une position
	public String nearNode(HashSet<String> SetToSearch, String myPosition, ExploreSoloAgent myAgent) {
		
		Iterator<String> iterTmp =SetToSearch.iterator();
		ArrayList<String> bestNode = new ArrayList<String>();
		int valueBest = -1;
		
		while(iterTmp.hasNext()) {
			String NodeTmp= iterTmp.next();
			
			List<String> tmp = getShortestPath(myPosition, NodeTmp);
			
			if(tmp != null ) {
				int sizeTmp = tmp.size();
				if(valueBest>sizeTmp || valueBest ==-1) {
					valueBest = sizeTmp;
					bestNode.clear();
					bestNode.add(NodeTmp);
				}else if(valueBest==sizeTmp ) {
					bestNode.add(NodeTmp);
				}
			}
			
		}
		
		String FinalBestNode = null; 

		if(bestNode.size() == 1) {
			FinalBestNode = bestNode.get(0);
		}else if(bestNode.size()>1){
			
			if( myAgent.ExplorationRandom ) {
				
				Random r = new Random();
				FinalBestNode = bestNode.get(r.nextInt(bestNode.size()-1));
				
			}else {
				FinalBestNode = bestNode.get( 
						(myAgent.getListAllAgent().indexOf(myAgent.getLocalName()) ) %
						bestNode.size());
			}
			
		}
		return FinalBestNode;
		
	}
	
	// Obtenir les noeuds les plus proches parmi un ensemble de noeuds et d'une position et en enlevant certain noeuds
	// non accessible
	public String nearNode(HashSet<String> SetToSearch, String myPosition,ExploreSoloAgent myAgent,
			HashSet<String> nodeNotAvailable) {
		
		Iterator<String> iterTmp =SetToSearch.iterator();
		ArrayList<String> bestNode = new ArrayList<String>();
		int valueBest = -1;
		
		while(iterTmp.hasNext()) {
			String NodeTmp= iterTmp.next();
			List<String> tmp =  getShortestPath(myPosition, NodeTmp,
					nodeNotAvailable);
			if(tmp != null ) {
				int sizeTmp = tmp.size();
				if(valueBest>sizeTmp || valueBest ==-1) {
					valueBest = sizeTmp;
					bestNode.clear();
					bestNode.add(NodeTmp);
				}else if(valueBest==sizeTmp ) {
					bestNode.add(NodeTmp);
				}
			}
		}
		
		String FinalBestNode = null; 

		if(bestNode.size() == 1) {
			FinalBestNode = bestNode.get(0);
		}else if(bestNode.size()>1){
			
			if( myAgent.ExplorationRandom ) {
				
				Random r = new Random();
				FinalBestNode = bestNode.get(r.nextInt(bestNode.size()-1));
				
			}else {
				FinalBestNode = bestNode.get( 
						(myAgent.getListAllAgent().indexOf(myAgent.getLocalName()) ) %
						bestNode.size());
			}
			
		}
		
		return FinalBestNode;
		
	}

	
}