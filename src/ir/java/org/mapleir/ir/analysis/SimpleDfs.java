package org.mapleir.ir.analysis;

import org.mapleir.stdlib.cfg.edge.FlowEdge;
import org.mapleir.stdlib.collections.graph.FastGraphVertex;
import org.mapleir.stdlib.collections.graph.flow.FlowGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class SimpleDfs<N extends FastGraphVertex> {
	public List<N> preorder;
	public List<N> postorder;

	public SimpleDfs(FlowGraph<N, FlowEdge<N>> graph, N entry, boolean pre, boolean post) {
		if (pre)
			preorder = new ArrayList<>();
		if (post)
			postorder = new ArrayList<>();

		Set<N> visited = new HashSet<>();
		Stack<N> preStack = new Stack<>();
		Stack<N> postStack = null;
		if (post)
			postStack = new Stack<>();

		preStack.push(entry);
		while (!preStack.isEmpty()) {
			N current = preStack.pop();
			if (visited.contains(current))
				continue;
			visited.add(current);
			if (pre)
				preorder.add(current);
			if (post)
				postStack.push(current);
			for (FlowEdge<N> succ : graph.getEdges(current))
				preStack.push(succ.dst);
		}
		if (post)
			while (!postStack.isEmpty())
				postorder.add(postStack.pop());
	}
}