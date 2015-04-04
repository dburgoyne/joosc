package CodeGeneration;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;

import AbstractSyntax.TypeDecl;
import Compiler.Compiler;

// Convenience wrapper around PrintWriter
public class AsmWriter {

	protected PrintWriter writer;
	protected TypeDecl typeDecl;
	protected final String INDENT = "  ";
	
	public AsmWriter(TypeDecl typeDecl) throws FileNotFoundException {
		this.typeDecl = typeDecl;
		String outputFileName = typeDecl.getCanonicalName().toString() + ".s";
		writer = new PrintWriter(Compiler.OUTPUT_DIR + "/" + outputFileName);
	}
	
	public void close() {
		writer.close();
	}
	
	private LinkedList<String> comments = new LinkedList<String>();
	
	// Print the "beginning" of this comment and increase indentation.
	public void pushComment(String fmt, Object... args) {
		String comment = String.format(fmt, args);
		writer.println();
		for (String line : comment.split("\\r?\\n"))
			this.comment(">>> %s", line);
		comments.addLast(comment);
	}
	
	// Pop last pushed comment, print its "end", decrease indent.
	public void popComment() {
		String comment = comments.removeLast();
		assert comment != null;
		for (String line : comment.split("\\r?\\n"))
			this.comment("<<< %s", line);
		writer.println();
	}
	
	// print a comment at the current indentation level.
	public void comment(String fmt, Object... args) {
		int indent = this.getIndentLevel();
		for (String line
				: String.format(fmt, args).split("\\r?\\n")) {
			writer.print("; ");
			for (int i = 0; i < indent-1; i++) {
				writer.print(INDENT);
			}
			writer.println(line);
		}
	}
	
	// print an instruction at the current indentation level.
	public void instr(Object operator, Object... operands) {
		int indent = this.getIndentLevel();
		for (int i = 0; i < indent; i++) {
			writer.print(INDENT);
		}
		writer.print(operator);
		for (int i = 0; i < operands.length; i++) {
			if (i == 0) {
				writer.print('\t');
			}
			writer.print(operands[i]);
			if (i != operands.length - 1) {
				writer.print(", ");
			}
		}
		writer.println();
	}
	
	// print some formatted text at the current indentation 
	// level, followed by a newline.
	public void line(String fmt, Object... args) {
		int indent = this.getIndentLevel();
		for (String line
				: String.format(fmt, args).split("\\r?\\n")) {
			for (int i = 0; i < indent; i++) {
				writer.print(INDENT);
			}
			writer.println(line);
		}
	}
	
	// introduce a label.
	public void label(String label) {
		writer.print(label);
		writer.println(": ");
	}
	
	// Verbatim printing methods:
	public void verbatim(Object x) {
		writer.print(x);
	}
	public void verbatimln(Object x) {
		writer.println(x);
	}
	public void verbatimln() {
		writer.println();
	}
	public void verbatimf(String fmt, Object... args) {
		writer.printf(fmt, args);
	}
	public void verbatimfn(String fmt, Object... args) {
		verbatimf(fmt, args);
		verbatimln();
	}
	
	private int getIndentLevel() {
		return this.comments.size();
	}
}
