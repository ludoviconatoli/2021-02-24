package it.polito.tdp.PremierLeague.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer, Player> idMap ; 
	
	public Model() {
		this.dao = new PremierLeagueDAO();
		this.idMap = new HashMap<>();
		this.dao.listAllPlayers(idMap);
	}
	
	public void creaGrafo(Match m) {
		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		Graphs.addAllVertices(grafo, dao.getVertici(m, idMap));
		
		for(Adiacenza a: dao.getAdiacenze(m, idMap)) {
			if(a.getPeso() >= 0) {
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(grafo, a.getP1(), a.getP2(), a.getPeso());
				}
			}else {
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(grafo, a.getP2(), a.getP1(), (-1)*a.getPeso());
				}
			}
		}
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Match> getTuttiMatch(){
		List<Match> m = dao.listAllMatches();
		Collections.sort(m, new Comparator<Match>() {

			@Override
			public int compare(Match o1, Match o2) {
				// TODO Auto-generated method stub
				return o1.getMatchID().compareTo(o2.getMatchID());
			}
			
		});
		
		return m;
	}
	
	public GiocatoreMigliore getMigliore() {
		if(grafo == null) {
			return null;
		}
		
		Player best = null;
		Double maxDelta = (double) Integer.MIN_VALUE;
		
		for(Player p: this.grafo.vertexSet()) {
			
			double pesoUscente = 0.0;
			double pesoEntrante = 0.0;
			
			for(DefaultWeightedEdge edge: this.grafo.outgoingEdgesOf(p)) {
				pesoUscente += this.grafo.getEdgeWeight(edge);
			}
			
			for(DefaultWeightedEdge edge: this.grafo.incomingEdgesOf(p)) {
				pesoEntrante += this.grafo.getEdgeWeight(edge);
			}
			
			double delta = pesoUscente - pesoEntrante;
			if(delta > maxDelta) {
				best = p;
				maxDelta = delta;
			}
		}
		
		return new GiocatoreMigliore(best, maxDelta);
	}
	
	public Graph<Player, DefaultWeightedEdge> getGrafo() {
		return this.grafo;
	}
}
