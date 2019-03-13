package org.openntf.nsfodp.eclipse.ui.resources.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openntf.nsfodp.eclipse.ui.resources.DesignElementType;

public class NewDesignElementPage extends WizardPage {
	private Text fileText;
	private ISelection selection;
	private final DesignElementType type;

	public NewDesignElementPage(ISelection selection, DesignElementType type) {
		super("wizardPage"); //$NON-NLS-1$
		this.type = type;
		setTitle(type.getLabel());
		setDescription("This wizard creates a new " + type.getLabel() + " design element");
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		Label label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(e -> dialogChanged());
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		fileText.setText("new_view.view");
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		String fileName = getFileName();

		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("view") == false) {
				updateStatus("File extension must be \"view\"");
				return;
			}
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getFileName() {
		return fileText.getText();
	}
}