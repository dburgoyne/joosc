package CodeGeneration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import AbstractSyntax.Formal;
import AbstractSyntax.Local;

public class Frame {
	
	protected Frame parent;
	protected int bytesAllocated = 0;
	protected Map<Formal, Integer> formalOffsets = new HashMap<Formal, Integer>();
	protected Map<Local, Integer> localOffsets = new HashMap<Local, Integer>();
	protected Integer thisOffset;
	
	public Frame() { }
	
	public Frame(Frame parent) {
		this.parent = parent;
	}
	
	public int lookup(Local l) {
		return localOffsets.containsKey(l)
			 ? localOffsets.get(l)
			 : parent.lookup(l) + parent.bytesAllocated + 4;
	}
	
	public int lookup(Formal f) {
		return formalOffsets.containsKey(f)
			 ? formalOffsets.get(f)
			 : parent.lookup(f) + parent.bytesAllocated + 4;
	}
	
	public int lookupThis() {
		return thisOffset != null
			 ? thisOffset
			 : parent.lookupThis() + parent.bytesAllocated + 4;
	}
	
	public String deref(Local l) {
		int offset = this.lookup(l);
		return offset >= 0
			 ? "[ebp + " + offset + "]"
			 : "[ebp - " + (-offset) + "]";		
	}
	
	public String deref(Formal f) {
		int offset = this.lookup(f);
		return offset >= 0
			 ? "[ebp + " + offset + "]"
			 : "[ebp - " + (-offset) + "]";	
	}
	
	public String derefThis() {
		int offset = this.lookupThis();
		return offset >= 0
			 ? "[ebp + " + offset + "]"
			 : "[ebp - " + (-offset) + "]";		
	}
	
	public void enter(AsmWriter writer) {
		writer.pushComment("Frame of size %s", this.bytesAllocated);
		writer.instr("enter", this.bytesAllocated, 0);
	}
	
	public void leave(AsmWriter writer) {
		writer.instr("leave");
		writer.popComment();
	}
	
	public void declare(Local l) {
		this.bytesAllocated += 4;
		localOffsets.put(l, (-1)*this.bytesAllocated);
	}
	
	public void declare(List<Formal> formals, boolean isStatic) {
		for (int i = 0; i < formals.size(); i++) {
			formalOffsets.put(formals.get(i), (1 - i + formals.size()) * 4);
		}
		if (!isStatic) {
			// Skip over the saved ebp, saved eip, and all the arguments.
			thisOffset = (2 + formals.size()) * 4;
		}
	}
}
