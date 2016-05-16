package org.rsdeob.stdlib.cfg.stat;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.rsdeob.stdlib.cfg.expr.Expression;
import org.rsdeob.stdlib.cfg.util.TabbedStringWriter;

public class ThrowStatement extends Statement {

	private Expression expression;

	public ThrowStatement(Expression expression) {
		setExpression(expression);
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
		overwrite(expression, 0);
	}

	@Override
	public void onChildUpdated(int ptr) {
		setExpression((Expression) read(ptr));
	}

	@Override
	public void toString(TabbedStringWriter printer) {
		printer.print("throw ");
		expression.toString(printer);
		printer.print(';');		
	}

	@Override
	public void toCode(MethodVisitor visitor) {
		expression.toCode(visitor);
		visitor.visitInsn(Opcodes.ATHROW);		
	}

	@Override
	public boolean canChangeFlow() {
		return true;
	}

	@Override
	public boolean canChangeLogic() {
		return expression.canChangeLogic();
	}

	@Override
	public boolean isAffectedBy(Statement stmt) {
		return expression.isAffectedBy(stmt);
	}
}