package org.openntf.nsfodp.example;

import com.ibm.xsp.library.AbstractXspLibrary;

/**
 * @since 1.0.0
 */
public class ExampleLibrary extends AbstractXspLibrary {

	@Override
	public String getLibraryId() {
	    return Activator.class.getPackage().getName() + ".library"; //$NON-NLS-1$
	}

	@Override
    public String getPluginId() {
        return Activator.getDefault().getBundle().getSymbolicName();
    }

    @Override
    public String getTagVersion() {
        return "1.0.0"; //$NON-NLS-1$
    }

    @Override
    public String[] getDependencies() {
        return new String[] {
                "com.ibm.xsp.core.library", //$NON-NLS-1$
                "com.ibm.xsp.extsn.library", //$NON-NLS-1$
                "com.ibm.xsp.domino.library", //$NON-NLS-1$
                "com.ibm.xsp.designer.library", //$NON-NLS-1$
                "com.ibm.xsp.extlib.library" //$NON-NLS-1$
        };
    }

    @Override
    public boolean isGlobalScope() {
        return false;
    }
}
