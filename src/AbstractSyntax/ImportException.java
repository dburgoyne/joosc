package AbstractSyntax;

import Utilities.StringUtils;

public abstract class ImportException extends Exception {
	
	public static class DuplicateTypeDefinition extends ImportException {

		private static final long serialVersionUID = -673645760020688692L;

		public DuplicateTypeDefinition(TypeDecl type1, TypeDecl type2) {
			super(String.format("The type %s is defined multiple times.\n"
					+ " at %s\n"
					+ "First defined at %s\n",
					type1.getCanonicalName(),
					type1.getPositionalString(),
					type2.getPositionalString()));
		}

	}

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
	
	public static class PackagePrefix extends ImportException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PackagePrefix(Identifier id, TypeDecl type) {
			super(String.format("The package %s is a prefix of the type %s.\n"
					+ " at %s\n",
					id.toString(),
					type.getCanonicalName(),
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
