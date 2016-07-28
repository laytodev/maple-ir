package org.mapleir.stdlib.ir.transform;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.mapleir.stdlib.cfg.edge.FlowEdge;
import org.mapleir.stdlib.collections.graph.FastGraphVertex;
import org.mapleir.stdlib.collections.graph.flow.FlowGraph;

public abstract class DataAnalyser<N extends FastGraphVertex, E extends FlowEdge<N>, S> {
	
	protected final FlowGraph<N, E> graph;
	protected final Map<N, S> in;
	protected final Map<N, S> out;
	protected final LinkedList<N> queue;
	
	public DataAnalyser(FlowGraph<N, E> graph) {
		this(graph, true);
	}
	
	public DataAnalyser(FlowGraph<N, E> graph, boolean commit) {
		this.graph = graph;
		this.queue = new LinkedList<>();
		in = new HashMap<>();
		out = new HashMap<>();
		run(commit);
	}
	
	private void run(boolean commit) {
		init();
		if(commit) analyse();
	}
	
	public void analyse() {
		processImpl();
	}
	
	protected void init() {
		// set initial data states
		for(N n : graph.vertices()) {
			in.put(n, newState());
			out.put(n, newState());
		}
	}
	
	public S in(N n) {
		return in.get(n);
	}
	
	public S out(N n) {
		return out.get(n);
	}
	
	public FlowGraph<N, E> getGraph() {
		return graph;
	}
	
	protected void merge(N mainBranch, S mainBranchS, N mergingBranch, S mergingBranchS) {
		S result = newState();
		merge(mainBranch, mainBranchS, mergingBranch, mergingBranchS, result);
		copy(result, mainBranchS);
	}
	
	public void appendQueue(N n) {
		if(!queue.contains(n)) {
			queue.add(n);
		}
	}

	public void remove(N n) {
		in.remove(n);
		out.remove(n);
		while(queue.remove(n));
	}	
	
	protected abstract S newState();

	protected abstract S newEntryState();

	protected abstract void copy(S src, S dst);
	
	protected abstract boolean equals(S s1, S s2);
	
	protected void flowThrough(N src, S srcS, N dst, S dstS) {
		copy(srcS, dstS);
	}
	
	protected void flowException(N src, S srcS, N dst, S dstS) {
		copy(srcS, dstS);
	}
	
	protected abstract void merge(N nIn1, S in1, N nIn2, S in2, S out);
	
	protected abstract void execute(N n, S currentOut, S currentIn);
	
	protected abstract void processImpl();
}