package AbstractSyntax;

public abstract class ImportException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportException(String message) {
		super(message);
	}

	public static class NonExistentPackage extends ImportException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NonExistentPackage(Identifier id) {
			super(String.format("The package %s does not exist.\n"
					+ " at %s\n",
					id.getPackageName(),
					id.getPositionalString()));
		}
	}

	public static class NonExistentType extends ImportException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NonExistentType(Identifier id) {
			super(String.format("The type %s does not exist.\n"
					+ " at %s\n",
					id.toString(),
					id.getPositionalString()));
		}
	}

	public static class DuplicateType extends ImportException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DuplicateType(Identifier id) {
			super(String.format("The import %s is ambiguous.\n"
					+ " at %s\n",
					id.toString(),
					id.getPositionalString()));
		}
	}

	public static class Clash extends ImportException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Clash(Identifier id1, Identifier id2) {
			super(String.format("The imports %s and %s are in conflict.\n"
					+ " at %s\n",
					id1.toString(),
					id2.toString(),
					id2.getPositionalString()));
		}
	}

}
