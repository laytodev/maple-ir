package org.rsdeob.stdlib.ir.transform.impl;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.rsdeob.stdlib.cfg.edge.FlowEdge;
import org.rsdeob.stdlib.collections.NullPermeableHashMap;
import org.rsdeob.stdlib.collections.SetCreator;
import org.rsdeob.stdlib.ir.Local;
import org.rsdeob.stdlib.ir.StatementGraph;
import org.rsdeob.stdlib.ir.StatementVisitor;
import org.rsdeob.stdlib.ir.api.ICodeListener;
import org.rsdeob.stdlib.ir.expr.VarExpression;
import org.rsdeob.stdlib.ir.stat.CopyVarStatement;
import org.rsdeob.stdlib.ir.stat.Statement;

public class UsesAnalyser implements ICodeListener<Statement> {

	private final StatementGraph graph;
	private final DefinitionAnalyser definitions;
	private final NullPermeableHashMap<CopyVarStatement, Set<Statement>> uses;
	private final NullPermeableHashMap<Statement, Set<VarExpression>> used;
	
	public UsesAnalyser(StatementGraph graph, DefinitionAnalyser defs) {
		this.graph = graph;
		definitions = defs;
		uses = new NullPermeableHashMap<>(new SetCreator<>());
		used = new NullPermeableHashMap<>(new SetCreator<>());
		build();
	}
	
	public Set<Statement> getUses(CopyVarStatement def) {
		return uses.get(def);
	}
	
	public Set<VarExpression> getVarsUsed(Statement stmt) {
		return used.get(stmt);
	}

	@Override
	public void update(Statement stmt) {
		Set<VarExpression> set = used.getNonNull(stmt);
		set.clear();
		
		StatementVisitor vis = new StatementVisitor(stmt) {
			@Override
			public Statement visit(Statement s) {
				if(s instanceof VarExpression) {
					VarExpression var = (VarExpression) s;
					set.add(var);
					
					Local local = var.getLocal();
					NullPermeableHashMap<Local, Set<CopyVarStatement>> defMaps = definitions.in(stmt);
					Set<CopyVarStatement> defs = defMaps.get(local);
					for(CopyVarStatement def : defs) {
						uses.getNonNull(def).add(stmt);
					}
				}
				return s;
			}
		};
		vis.visit();
	}

	@Override
	public void preRemove(Statement stmt) {
		uses.remove(stmt);
		used.remove(stmt);
		
		for(Entry<CopyVarStatement, Set<Statement>> e : uses.entrySet()) {
			e.getValue().remove(stmt);
		}
		
		if(stmt instanceof VarExpression) {
			for(Entry<Statement, Set<VarExpression>> e : used.entrySet()) {
				e.getValue().remove(stmt);
			}
		}
	}
	
	@Override
	public void postRemove(Statement stmt) {
		definitions.commit();
		rebuild(stmt);
	}

	public void build(Statement stmt) {
		Set<VarExpression> set = used.getNonNull(stmt);
		
		StatementVisitor vis = new StatementVisitor(stmt) {
			@Override
			public Statement visit(Statement s) {
				if(s instanceof VarExpression) {
					VarExpression var = (VarExpression) s;
					set.add(var);

					Local local = var.getLocal();
					NullPermeableHashMap<Local, Set<CopyVarStatement>> defMaps = definitions.in(stmt);
					Set<CopyVarStatement> defs = defMaps.get(local);
					try {
						for(CopyVarStatement def : defs) {
							uses.getNonNull(def).add(stmt);
						}
					} catch(Exception e) {
						System.out.println("at " + stmt.getId() + " " + stmt);
						System.out.println("  > " + s);
						throw e;
					}
				}
				return s;
			}
		};
		vis.visit();
	}

	private void rebuild(Statement start) {
		rebuild(start, new HashSet<>());
	}
	
	private void rebuild(Statement start, Set<Statement> vis) {
		if(vis.contains(start)) {
			return;
		}
		vis.add(start);
		build(start);
		for(FlowEdge<Statement> e : graph.getEdges(start)) {
			rebuild(e.dst, vis);
		}
	}
	
	@Override
	public void replaced(Statement old, Statement n) {
		preRemove(old);
		rebuild(n);
	}

	@Override
	public void commit()
	{
		// nop; this listener is not queued
	}

	private void build() {
		for(Statement stmt : graph.vertices()) {
			build(stmt);
		}
	}

	@Override
	public void insert(Statement p, Statement s, Statement n) {
		rebuild(n);
	}
}