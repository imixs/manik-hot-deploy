package org.imixs.maven.manik;

/**
 * This class HotDeployment defines a source and target path for a single
 * hotdeployment unit inside the current project
 */
public class HotDeployment extends AbstractDeployment {
	private static final long serialVersionUID = 4219324571205568934L;
	
	private String objectPath;
	
	/**
	 * Default constructor
	 */
	public HotDeployment() {
		super();
	}

	public HotDeployment(String source, String target) {
		super();
		this.source = source;
		this.target = target;
	}

	public String getObjectPath() {
		return objectPath;
	}

	public void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}

}
