package org.mapleir.ir.code.expr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mapleir.ir.cfg.BasicBlock;
import org.mapleir.ir.code.stmt.Statement;
import org.mapleir.ir.code.stmt.copy.CopyPhiStatement;
import org.mapleir.stdlib.cfg.util.TabbedStringWriter;
import org.mapleir.stdlib.ir.transform.impl.CodeAnalytics;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class PhiExpression extends Expression {

	private final Map<BasicBlock, Expression> arguments;
	private Type type;
	
	public PhiExpression(Map<BasicBlock, Expression> arguments) {
		super(PHI);
		this.arguments = arguments;
	}
	
	public int getArgumentCount() {
		return arguments.size();
	}
	
	public Set<BasicBlock> getSources() {
		return new HashSet<>(arguments.keySet());
	}
	
	public Map<BasicBlock, Expression> getArguments() {
		return arguments;
	}
	
	public Expression getArgument(BasicBlock b) {
		return arguments.get(b);
	}
	
	public void setArgument(BasicBlock b, Expression e) {
		if(arguments.containsKey(b)) {
			Expression old = arguments.put(b, e);
			getBlock().getGraph().updated(getBlock(), (CopyPhiStatement) getParent(), b, old, e);
		} else {
			throw new IllegalStateException("phi has a fixed size of " + arguments.size() + ": " + b + ", " + e);
		}
	}
	
	@Override
	public void onChildUpdated(int ptr) {
		
	}

	@Override
	public Expression copy() {
		Map<BasicBlock, Expression> map = new HashMap<>();
		for(Entry<BasicBlock, Expression> e : arguments.entrySet()) {
			map.put(e.getKey(), e.getValue());
		}
		return new PhiExpression(map);
	}

	@Override
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public void toString(TabbedStringWriter printer) {
		printer.print("\u0278" + arguments);
	}

	@Override
	public void toCode(MethodVisitor visitor, CodeAnalytics analytics) {
		throw new UnsupportedOperationException("Phi is not executable.");
	}

	@Override
	public boolean canChangeFlow() {
		return false;
	}

	@Override
	public boolean canChangeLogic() {
		return true;
	}

	@Override
	public boolean isAffectedBy(Statement stmt) {
		return false;
	}

	@Override
	public boolean equivalent(Statement s) {
		if(s instanceof PhiExpression) {
			PhiExpression phi = (PhiExpression) s;
			
			Set<BasicBlock> sources = new HashSet<>();
			sources.addAll(arguments.keySet());
			sources.addAll(phi.arguments.keySet());
			
			if(sources.size() != arguments.size()) {
				return false;
			}
			
			for(BasicBlock b : sources) {
				Expression e1 = arguments.get(b);
				Expression e2 = phi.arguments.get(b);
				if(e1 == null || e2 == null) {
					return false;
				}
				if(!e1.equivalent(e2)) {
					return false;
				}
			}
			
			return true;
		}
		return false;
	}
}