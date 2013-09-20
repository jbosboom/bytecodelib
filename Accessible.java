package edu.mit.streamjit.util.bytecode;

/**
 * An element to which access control kinds can be applied.
 * @author Jeffrey Bosboom <jbosboom@csail.mit.edu>
 * @since 4/3/2013
 */
public interface Accessible {
	public Access getAccess();
	public void setAccess(Access access);
}
