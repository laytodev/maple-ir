package org.rsdeob.stdlib.cfg.stat;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.rsdeob.stdlib.cfg.expr.Expression;
import org.rsdeob.stdlib.cfg.util.TabbedStringWriter;
import org.rsdeob.stdlib.cfg.util.TypeUtils;

public class ReturnStatement extends Statement {

	private Type type;
	private Expression expression;

	public ReturnStatement() {
		this(Type.VOID_TYPE, null);
	}

	public ReturnStatement(Type type, Expression expression) {
		this.type = type;
		setExpression(expression);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
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
		if (expression != null) {
			printer.print("return ");
			expression.toString(printer);
			printer.print(';');
		} else {
			printer.print("return;");
		}
	}

	@Override
	public void toCode(MethodVisitor visitor) {
		if (type != Type.VOID_TYPE) {
			expression.toCode(visitor);
			if (TypeUtils.isPrimitive(type)) {
				int[] cast = TypeUtils.getPrimitiveCastOpcodes(expression.getType(), type); // widen
				for (int i = 0; i < cast.length; i++)
					visitor.visitInsn(cast[i]);
			}
			visitor.visitInsn(TypeUtils.getReturnOpcode(type));
		} else {
			visitor.visitInsn(Opcodes.RETURN);
		}
	}

	@Override
	public boolean canChangeFlow() {
		return true;
	}

	@Override
	public boolean canChangeLogic() {
		return expression != null && expression.canChangeLogic();
	}

	@Override
	public boolean isAffectedBy(Statement stmt) {
		return expression != null && expression.isAffectedBy(stmt);
	}
}