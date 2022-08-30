package org.imixs.maven.manik;

/**
 * This class AbstractDeployment is the base clase which defines a source and target path for a single
 * deployment unit inside the current project
 */
public abstract class AbstractDeployment implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String source;
	private String target;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
