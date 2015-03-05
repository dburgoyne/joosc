package AbstractSyntax;

import java.util.List;

import Utilities.StringUtils;

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
					StringUtils.join(id.getPackageName(), "."),
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

		public Clash(Identifier id, TypeDecl decl) {
			super(String.format("The import %s clashes with the already-defined type %s.\n"
					+ " at %s\n",
					id.toString(),
					decl.getName().toString(),
					id.getPositionalString()));
		}
	}

}
