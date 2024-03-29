package org.imixs.maven.manik;

/**
 * This class AutoDeployment defines a source and target path for a single
 * autoDeployment inside the current project
 */
public  class AutoDeployment extends AbstractDeployment {
	private static final long serialVersionUID = 3235694234880934530L;
	
	private boolean unpack;
	
	public boolean isUnpack() {
		return unpack;
	}

	public void setUnpack(boolean unpack) {
		this.unpack = unpack;
	}
}
