package AbstractSyntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CodeGeneration.AsmWriter;
import CodeGeneration.Frame;
import Exceptions.CodeGenerationException;
import Exceptions.ImportException;
import Exceptions.NameConflictException;
import Exceptions.NameLinkingException;
import Exceptions.ReachabilityException;
import Exceptions.TypeCheckingException;
import Exceptions.TypeLinkingException;
import Parser.ParseTree;
import Types.MemberSet;
import Types.PrimitiveType;
import Types.Type;
import Utilities.Cons;
import Utilities.ObjectUtils;
import Utilities.Predicate;

public class TypeDecl extends ASTNode
		implements EnvironmentDecl, Type, Identifier.Interpretation {
	
	public enum Kind {
		CLASS,
		INTERFACE
	};
	
	protected List<Modifier> modifiers;
	protected TypeDecl.Kind kind;
	protected Identifier name;
	
	protected TypeDecl superclass;
	protected List<TypeDecl> interfaces;
	protected Identifier superclassName;
	protected List<Identifier> interfacesNames;
	
	protected List<Constructor> constructors;
	protected List<Field> fields;
	protected List<Method> methods;
	
	protected MemberSet memberSet;
	
	protected List<TypeDecl> allTypes;
	
	// Back reference to the parent Classfile node.
	protected Classfile parent;
	
	public TypeDecl.Kind getKind() {
		return this.kind;
	}
	
	// Type IDs: must be assigned before being accessed. 
	private int tid = 0;
	public void setTypeID(int tid) {
		assert this.tid == 0;
		assert tid > 0;
		this.tid = tid;
	}
	@Override public int getTypeID() {
		assert this.tid > 0;
		return this.tid;
	}
	
	public String toString() {
		String toReturn = "";
		if (parent.packageName != null) {
			toReturn += parent.packageName.toString() + ".";
		}
		toReturn += name.toString();
		return toReturn;
	}
	
	public Identifier getName() {
		return this.name;
	}
	
	public boolean isAbstract() {
		return this.modifiers.contains(Modifier.ABSTRACT);
	}
	
	public boolean isClass() {
		return this.kind == Kind.CLASS;
	}
	
	public boolean isInterface() {
		return this.kind == Kind.INTERFACE;
	}
	
	public String getCanonicalName() {
		Identifier pkName = this.getPackageName();
		return (pkName == null ? "" : pkName + ".") + this.getName();
	}
	
	/** Can be null! */
	public Identifier getPackageName() {
		return this.parent.packageName;
	}
	
	public TypeDecl(ParseTree tree, Classfile parent) {
		super(tree);
		assert(tree.getSymbol().equals("TypeDeclaration"));
		this.parent = parent;
		
		this.interfaces = new ArrayList<TypeDecl>();
		this.interfacesNames = new ArrayList<Identifier>();
        this.constructors = new ArrayList<Constructor>();
        this.fields = new ArrayList<Field>();
        this.methods = new ArrayList<Method>();
		
		String firstChildName = tree.getChildren()[0].getSymbol();
		if (firstChildName.equals("ClassDeclaration")) {
			tree = tree.getChildren()[0];
			this.kind = Kind.CLASS;
			this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
			this.name = new Identifier(tree.getChildren()[2]);
			
			switch (tree.numChildren()) {
			case 4:
				extractClassBodyDecls(tree.getChildren()[3]);
				break;
			case 5:
				if (tree.getChildren()[3].getSymbol().equals("Super")) {
					extractSuper(tree.getChildren()[3]);
				} else if (tree.getChildren()[3].getSymbol().equals("Interfaces")) {
					extractInterfaces(tree.getChildren()[3]);
				}
				extractClassBodyDecls(tree.getChildren()[4]);
				break;
			case 6:
				extractSuper(tree.getChildren()[3]);
				extractInterfaces(tree.getChildren()[4]);
				extractClassBodyDecls(tree.getChildren()[5]);
				break;
			}
			
			
		} else if (firstChildName.equals("InterfaceDeclaration")) {
			tree = tree.getChildren()[0];
			this.kind = Kind.INTERFACE;
			this.modifiers = Modifier.extractModifiers(tree.getChildren()[0]);
			this.name = new Identifier(tree.getChildren()[2]);
			
			switch (tree.numChildren()) {
			case 4:
				extractInterfaceMemberDecls(tree.getChildren()[3]);
				break;
			case 5:
				extractExtendsInterfaces(tree.getChildren()[3]);
				extractInterfaceMemberDecls(tree.getChildren()[4]);
				break;
			}
		} else {
			return;
		}
	}

	// Extracts constructors, fields and methods from a ClassBody node.
	private void extractClassBodyDecls(ParseTree tree) {
		assert(tree.getSymbol().equals("ClassBody"));
		tree = tree.getChildren()[1];
		while (tree.getChildren().length > 1) {
			extractClassBodyDecl(tree.getChildren()[1]);
			tree = tree.getChildren()[0];
		}
		extractClassBodyDecl(tree.getChildren()[0]);
	}

	
	// Extracts a constructor, field or method from a ClassBodyDeclaration node.
	private void extractClassBodyDecl(ParseTree tree) {
		assert(tree.getSymbol().equals("ClassBodyDeclaration"));
		ParseTree firstChild = tree.getChildren()[0];
		String firstChildName = firstChild.getSymbol();
		if (firstChildName.equals("ClassMemberDeclaration")) {
			ParseTree grandchild = firstChild.getChildren()[0];
			String grandchildName= grandchild.getSymbol();
			if (grandchildName.equals("FieldDeclaration")) {
				Field field = new Field(grandchild, this);
				this.fields.add(0, field);
			} else if (grandchildName.equals("MethodDeclaration")) {
				Method method = new Method(grandchild, false);
				this.methods.add(0, method);
			} else {
				return;
			}
		} else if (firstChildName.equals("ConstructorDeclaration")) {
			Constructor constructor = new Constructor(firstChild, this);
			this.constructors.add(0, constructor);
		}
	}
	
	// Extracts method declarations from an InterfaceBody node.
	private void extractInterfaceMemberDecls(ParseTree tree) {
		assert(tree.getSymbol().equals("InterfaceBody"));
		
		if (tree.numChildren() == 3) {
			tree = tree.getChildren()[1];
			while (tree.getChildren().length == 2) {
				extractInterfaceMemberDecl(tree.getChildren()[1]);
				tree = tree.getChildren()[0];
			}
			extractInterfaceMemberDecl(tree.getChildren()[0]);
		}
	}
	
	// Extracts a method declaration from an InterfaceMemberDeclaration node.
	private void extractInterfaceMemberDecl(ParseTree tree) {
		assert(tree.getSymbol().equals("InterfaceMemberDeclaration"));
		
		ParseTree firstChild = tree.getChildren()[0];
		String firstChildName = firstChild.getSymbol();
		if (firstChildName.equals("AbstractMethodDeclaration")) {
			Method method = new Method(firstChild, true);
			this.methods.add(method);
		} else {
			return;
		}
	}
	
	// Extracts extended class name from a Super node.
	private void extractSuper(ParseTree tree) {
		assert(tree.getSymbol().equals("Super"));
		this.superclassName = new Identifier(tree.getChildren()[1]);
	}
	
	// Extracts implemented interface names from an Interfaces node.
	private void extractInterfaces(ParseTree tree) {
		assert(tree.getSymbol().equals("Interfaces"));
		tree = tree.getChildren()[1];
		while (tree.getChildren().length == 3) {
			Identifier identifier = new Identifier(tree.getChildren()[2]);
			this.interfacesNames.add(identifier);
			tree = tree.getChildren()[0];
		}
		Identifier identifier = new Identifier(tree.getChildren()[0]);
		this.interfacesNames.add(identifier);
	}
	
	// Extracts extended interface names from an ExtendsInterfaces node.
	private void extractExtendsInterfaces(ParseTree tree) {
		assert(tree.getSymbol().equals("ExtendsInterfaces"));
		while (tree.getChildren().length == 3) {
			Identifier identifier = new Identifier(tree.getChildren()[2]);
			this.interfacesNames.add(identifier);
			tree = tree.getChildren()[0];
		}
		Identifier identifier = new Identifier(tree.getChildren()[1]);
		this.interfacesNames.add(identifier);
	}
	
	protected void checkNameConflicts(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException {
		final Identifier packageName = this.getPackageName();
		final Identifier typeName = this.getName();
		Cons<EnvironmentDecl> conflicts = Cons.filter(parentEnvironment,
				new Predicate<EnvironmentDecl>() {
					public boolean test(EnvironmentDecl decl) {
						if (!(decl instanceof TypeDecl)) return false;
						TypeDecl type = (TypeDecl)decl;
						return ObjectUtils.equals(type.getPackageName(), packageName)
						    && ObjectUtils.equals(type.getName(), typeName)
						    && type != TypeDecl.this; // (<- do not want actual self!)
					}
			});
		
		if(conflicts != null) {
			// Give up.
			throw new NameConflictException((TypeDecl)conflicts.head, this);
		}
	}
	
	public void buildEnvironment(Cons<EnvironmentDecl> parentEnvironment) throws NameConflictException, ImportException {
		// Make sure our canonical name is not already taken.
		checkNameConflicts(parentEnvironment);
		
		if (Cons.contains(parentEnvironment, this)) {
			this.environment = parentEnvironment;
		} else {
			this.environment = new Cons<EnvironmentDecl>(this, parentEnvironment);
			assert false;
		}
		
		// For each field, stick its exported symbol in our environment, 
		// then build the environments of all fields when done.
		for (Field field : fields) {
			EnvironmentDecl export = field.exportEnvironmentDecls();
			assert(export != null);
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		Cons<Field> consFields = Cons.fromList(fields);
		while (consFields != null) {
			Field field = consFields.head;
			field.buildEnvironment(this.environment);
			consFields = consFields.tail;
			field.followingFields = consFields;
		}

		
		// Add all symbols exported by methods and constructors to our
		// environment before building the environments for these nodes.
		for (Constructor constructor : constructors) {
			EnvironmentDecl export = constructor.exportEnvironmentDecls();
			assert(export != null);
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		for (Method method : methods) {
			EnvironmentDecl export = method.exportEnvironmentDecls();
			assert(export != null);
			this.environment = new Cons<EnvironmentDecl>(export, this.environment);
		}
		for (Constructor constructor : constructors) {
			constructor.buildEnvironment(this.environment);
		}
		for (Method method : methods) {
			method.buildEnvironment(this.environment);
		}
		
		// Set the superclass to java.lang.Object if no superclass was defined.
		if (this != Program.javaLangObject) {
			this.superclass = Program.javaLangObject;
		}
	}
	
	public EnvironmentDecl exportEnvironmentDecls() {
		return this;
	}

	@Override public void linkTypes(Cons<TypeDecl> allTypes) throws TypeLinkingException {
		
		this.allTypes = Cons.toList(allTypes);
		
		// Hierarchy decls:
		
		if (this.superclassName != null) {
			Type type = this.superclassName.resolveType(allTypes, this.environment);
			if (!(type instanceof TypeDecl)) {
				throw new TypeLinkingException.NotRefType(type, 
						this.superclassName.getPositionalString());
			}
			this.superclass = (TypeDecl)type;
			
			if (this.superclass.kind == Kind.INTERFACE) {
				throw new TypeLinkingException.BadSupertype(this, this.superclass);
			}
			
			if (this.superclass.modifiers.contains(Modifier.FINAL)) {
				throw new TypeLinkingException.ExtendsFinal(this, this.superclass);
			}
		}
		
		for (Identifier iface : this.interfacesNames) {
			Type type = iface.resolveType(allTypes, this.environment);
			if (!(type instanceof TypeDecl)) {
				throw new TypeLinkingException.NotRefType(type, 
							iface.getPositionalString());
			}
			
			this.interfaces.add((TypeDecl)type);
			
			if (((TypeDecl)type).kind == Kind.CLASS) {
				throw new TypeLinkingException.BadSupertype(this, (TypeDecl)type);
			}
			
			if (this.interfaces.indexOf(type) < this.interfaces.size() - 1) {
				throw new TypeLinkingException.AlreadyInherits(this, (TypeDecl)type);
			}
		}
		
		// Class body decls: 
		
		for (Constructor ctor : constructors) {
			ctor.linkTypes(allTypes);
		}
		for (Field f : fields) {
			f.linkTypes(allTypes);
		}
		for (Method m : methods) {
			m.linkTypes(allTypes);
		}
		
	}

	@Override public void linkNames(TypeDecl curType, boolean staticCtx, EnvironmentDecl curDecl, Local curLocal, boolean lValue) throws NameLinkingException {
		for (Field f : fields) {
			f.linkNames(this, f.modifiers.contains(Modifier.STATIC), curDecl, curLocal, false);
		}
		for (Constructor ctor : constructors) {
			ctor.linkNames(this, false, curDecl, curLocal, false);
		}
		for (Method m : methods) {
			m.linkNames(this, m.isStatic(), curDecl, curLocal, false);
		}
	}
	
	public List<TypeDecl> getDirectSupertypes() {
		List<TypeDecl> list = new ArrayList<TypeDecl>();
		
		if (superclass == null) {
			// Only for java.lang.Object; leave the list empty.
		} else {
			list.add(this.superclass);
		}
		
		for (TypeDecl iface : this.interfaces) {
			list.add(iface);
		}
		
		return list;
	}

	
	public void buildMemberSet() throws MemberSet.Exception {
		this.memberSet = new MemberSet(this);
		if (this.superclass != null) {
			this.memberSet.inheritClass(this.superclass.memberSet);
		}
		for (TypeDecl iface : this.interfaces) {
			this.memberSet.inheritInterface(iface.memberSet);
		}
		
		for (Field field : this.fields) {
			this.memberSet.declareField(field);
		}
		for (Method method : this.methods) {
			this.memberSet.declareMethod(method);
		}
		for (Constructor ctor : this.constructors) {
			this.memberSet.declareConstructor(ctor);
		}
		this.memberSet.validate();
	}
	
	public boolean hasZeroArgumentCtor() {
		for (Constructor ctor : this.constructors) {
			if (ctor.parameters.isEmpty()) return true;
		}
		return false;
	}
	
	@Override public boolean canBeCastAs(Type t) {
		return t.canBeAssignedTo(this) || this.canBeAssignedTo(t);
	}
	
	@Override public boolean canBeAssignedTo(Type t) {
		// Either t == this, or t is a superclass of this.
		return (t instanceof TypeDecl) && this.isSubtypeOf((TypeDecl)t);
	}
	
	public boolean isSubtypeOf(TypeDecl t) {
		return (t.equals(this) || this.memberSet.getStrictSupertypes().contains(t));
	}
	 public int f = 117 + (f=2) + 2;

	@Override public void checkTypes() throws TypeCheckingException {
		
		// A constructor in a class other than java.lang.Object implicitly calls the
		// zero-argument constructor of its superclass. Check that this zero-argument
		// constructor exists. 
		if (!this.constructors.isEmpty() && this.superclass != null) {
			if (!this.superclass.hasZeroArgumentCtor()) {
				throw new TypeCheckingException.MissingDefaultCtor(this);
			}
		}
		
		for (Constructor ctor : this.constructors) {
			ctor.checkTypes();
		}
		for (Field field : this.fields) {
			field.checkTypes();
		}
		for (Method method : this.methods) {
			method.checkTypes();
		}
	}
	
	@Override public void checkReachability(boolean canLeavePrevious) throws ReachabilityException {
		for (Constructor ctor : this.constructors) {
			ctor.checkReachability(true);
		}
		for (Field field : this.fields) {
			field.checkReachability(true);
		}
		for (Method method : this.methods) {
			method.checkReachability(true);
			
			if (method.isStatic()
					&& method.type == PrimitiveType.INT
					&& method.getName().getSingleComponent().equals("test")
					&& method.parameters.isEmpty()
					&& Program.staticIntTest == null) {
				Program.staticIntTest = method;
			}
		}
	}
	
	// ---------- Code generation ----------
	
	@Override
	public void generateCode(AsmWriter writer, Frame frame) throws CodeGenerationException {
		
		writer.pushComment("Type %s", this.getCanonicalName());
		writer.verbatimln("section .text");
		
		writer.comment("VTables");
		this.generateVTables(writer);
		
		writer.comment("Subtype table");
		this.generateSubtypeTable(writer);
		
		writer.comment("Static field initializers");
		this.generateFieldInitializers(writer, true);
		writer.comment("Non-static field initializers");
		this.generateFieldInitializers(writer, false);
		
		writer.comment("Constructors");
		for (Constructor constructor : constructors) {
			constructor.generateCode(writer, frame);
		}
		
		writer.comment("Methods");
		for (Method method : methods) {
			method.generateCode(writer, frame);
		}
		
		// Only Static fields should generate any code.
		writer.comment("Static Fields");
		writer.verbatimln("section .data");
		for (Field field : fields) {
			field.generateCode(writer, frame);
		}
		
		writer.popComment();
	}
	
	public void generateVTables(AsmWriter writer) {
		
		for (Map.Entry<TypeDecl, Method[]> entry : this.allVtables.entrySet()) {
			TypeDecl t = entry.getKey();
			Method[] vtable = entry.getValue();
			writer.pushComment("V_(%s, %s)", t.getCanonicalName(), this.getCanonicalName());
			String label = this.getVtableLabelFor(t);
			writer.verbatimfn("global %s", label);
			writer.label(label);
			writer.justDefinedGlobal(label);
			
			for (int i = 0; i < vtable.length; i++) {
				String implLbl = vtable[i].getImplementationLabel();
				writer.line("dd %s", implLbl);
				writer.justUsedGlobal(implLbl);
			}
			writer.popComment();
		}
	}
	
	public void generateSubtypeTable(AsmWriter writer) {
		String tableLabel = this.getSubtypeTableLabel();
		writer.verbatimfn("global %s", tableLabel);
		writer.label(tableLabel);
		writer.justDefinedGlobal(tableLabel);
		
		for (int i = 0; i < this.subtypeTableEntries.length; i++) {
			String entryLabel = this.subtypeTableEntries[i];
			if (entryLabel == null 
					&& i == 0
					&& (this == Program.javaLangObject
					 || this == Program.javaLangCloneable
					 || this == Program.javaIoSerializable)) {
				entryLabel = Program.javaLangObject.getVtableLabelFor(Program.javaLangObject);
			}
			writer.instr("dd", entryLabel == null ? 0 : entryLabel);
			if (entryLabel != null) {
				writer.justUsedGlobal(entryLabel);
			}
		}
	}
	
	public String getDefaultConstructorLabel() {
		return Utilities.Label.generateLabel("ctor", this.getCanonicalName(), null, null);
	}
	
	public String getInitializerLabel(boolean isStatic) {
		return Utilities.Label.generateLabel(isStatic ? "si" : "ii", this.getCanonicalName(), null, null);
	}
	
	public void generateFieldInitializers(AsmWriter writer, boolean isStatic) throws CodeGenerationException {

		String label = this.getInitializerLabel(isStatic);
		if (isStatic) {
			writer.verbatimfn("global %s", label);
			writer.label(label);
			writer.justDefinedGlobal(label);
		} else {
			writer.label(label);
		}
		
		// New top-level frame.
		Frame frame = new Frame();
		frame.declare(new ArrayList<Formal>(), isStatic);
		frame.enter(writer);
		
		// Call supertype's zero-argument constructor before non-static field initialization.
		if (!isStatic && this.superclass != null) {
			String superCtorLbl = this.superclass.getDefaultConstructorLabel();
			writer.instr("push", "dword " + frame.derefThis());
			writer.instr("call", superCtorLbl);
			writer.justUsedGlobal(superCtorLbl);
		}
		
		for (Field f : this.fields) {
			if (f.initializer != null) {
				if (f.isStatic() && isStatic) {
					// Evaluate the initializer and stick eax in the field
					f.initializer.generateCode(writer, frame);
					writer.instr("mov", "[" + f.getStaticLabel() + "]", "eax");
				} else if (!f.isStatic() && !isStatic) {
					// Evaluate the initializer and stick eax in the field
					f.initializer.generateCode(writer, frame);
					writer.instr("mov", "ebx", frame.derefThis());
					writer.instr("mov", "[ebx+" + f.byteOffset + "]", "eax");
				}
			}
		}

		frame.leave(writer);
		writer.instr("ret", isStatic ? 0 : 4);
	}
	
	// All non-static fields present in a concrete object of this type,
	// sorted in object layout order. 
	private Field[] allInstanceFields = null;
	protected Field[] getAllInstanceFields() {
		assert this.allInstanceFields != null;
		return this.allInstanceFields;
	}
	
	// All non-static methods callable on an object of this type,
	// sorted in the order in which they appear in this type's Vtable.
	private Method[] allInstanceMethods = null;
	protected Method[] getAllInstanceMethods() {
		assert this.allInstanceMethods != null;
		return this.allInstanceMethods;
	}
	
	// Maps a type T to a Vtable. If this <: T, maps to V_(T, this). Else, null.
	private HashMap<TypeDecl, Method[]> allVtables = null;
	private String[] subtypeTableEntries;

	protected Method[] getVtableFor(TypeDecl t) {
		assert(allVtables != null);
		assert(allVtables.containsKey(t) == this.isSubtypeOf(t));
		return allVtables.get(t);
	}
	
	protected String getVtableLabelFor(TypeDecl t) {
		return Utilities.Label.generateLabel("vt", t.getCanonicalName(), this.getCanonicalName(), null);
	}
	
	protected String getSubtypeTableLabel() {
		return Utilities.Label.generateLabel("st", this.getCanonicalName(), null, null);
	}

	public void buildSchema() {
		// Fields.
		Field[] parentInstanceFields = this.superclass == null 
				? new Field[0] 
				: this.superclass.getAllInstanceFields();
		allInstanceFields = Arrays.copyOf(parentInstanceFields,
										  parentInstanceFields.length + this.fields.size());
		for (int i = 0; i < this.fields.size(); i++) {
			this.fields.get(i).byteOffset = 4 + (parentInstanceFields.length + i) * 4;
			allInstanceFields[parentInstanceFields.length + i] = this.fields.get(i);
		}
		
		// Vtables.
		this.allInstanceMethods = Cons.toList(this.memberSet.getInstanceMethods()).toArray(new Method[0]);
		this.allVtables = new HashMap<TypeDecl, Method[]>(); 
		
		for (TypeDecl supertype : this.memberSet.getStrictSupertypes()) {
			Method[] superVTable = (Method[])supertype.getAllInstanceMethods().clone();
			for (int i = 0; i < superVTable.length; i++) {
				Method m1 = superVTable[i];
				for (int j = 0; j < this.allInstanceMethods.length; j++) {
					Method m2 = this.allInstanceMethods[j];
					if (new Method.SameSignaturePredicate().test(m1, m2)) {
						superVTable[i] = m2;
						break;
					}
				}
			}
			this.allVtables.put(supertype, superVTable);
		}
		
		// Add this type's methods to the map.
		this.allVtables.put(this, this.allInstanceMethods);
		
		// Subtype table.
		this.subtypeTableEntries = new String[this.allTypes.size() + 1];
		for (TypeDecl t : this.allTypes) {
			String label = t.isSubtypeOf(this) ? t.getVtableLabelFor(this) : null;
			subtypeTableEntries[t.getTypeID()] = label;
		}
	}
	
	public int sizeOf() {
		return (this.allInstanceFields.length + 1) * 4;
	}
}
