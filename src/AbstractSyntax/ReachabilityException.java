package AbstractSyntax;

public class ReachabilityException extends Exception {
	
	private static final long serialVersionUID = -1111335513985684886L;

	public ReachabilityException(String msg) {
		super(msg);
	}

	public static class UnreachableStatement extends ReachabilityException {

		private static final long serialVersionUID = 3920771292762975134L;

		public UnreachableStatement(BlockStatement stmnt) {
			super(String.format("Unreachable statement.\n"
					+ " at %s\n",
					stmnt.getPositionalString()));
		}
	}
	
	public static class MayNotReturn extends ReachabilityException {

		private static final long serialVersionUID = 8457161163202210657L;

		public MayNotReturn(Method method) {
			super(String.format("The method %s may not return in all executions.\n"
					+ " at %s\n",
					method.getName().toString(),
					method.getPositionalString()));
		}
	}
}
