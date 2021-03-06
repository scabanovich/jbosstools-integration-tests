/*******************************************************************************
 * Copyright (c) 2007-2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.easymport.ui.bot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.jboss.reddeer.core.handler.ShellHandler;
import org.jboss.reddeer.eclipse.core.resources.Project;
import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
import org.jboss.reddeer.eclipse.ui.problems.Problem;
import org.jboss.reddeer.eclipse.ui.problems.ProblemsView;
import org.jboss.reddeer.eclipse.ui.problems.ProblemsView.ProblemType;
import org.jboss.reddeer.eclipse.ui.views.log.LogMessage;
import org.jboss.reddeer.eclipse.ui.views.log.LogView;
import org.jboss.tools.easymport.reddeer.wizard.ImportedProject;
import org.jboss.tools.easymport.reddeer.wizard.ProjectProposal;
import org.jboss.tools.easymport.reddeer.wizard.SmartImportRootWizardPage;
import org.jboss.tools.easymport.reddeer.wizard.SmartImportWizard;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class ProjectTestTemplate {

	@After
	public void cleanup() {
		ShellHandler.getInstance().closeAllNonWorbenchShells();
		for (Project p : new ProjectExplorer().getProjects()) {
			p.delete(false);
		}
		// empty error log

		LogView logView = new LogView();
		logView.open();
		logView.deleteLog();
	}
	
	@BeforeClass
	public static void setupClass(){
		LogView logView = new LogView();
		logView.open();
		logView.deleteLog();
	}

	@Test
	public void testImport() {
		SmartImportWizard easymportWizard = new SmartImportWizard();
		easymportWizard.open();
		SmartImportRootWizardPage selectImportRootWizardPage = new SmartImportRootWizardPage();
		String path = getProjectPath().getAbsolutePath();
		selectImportRootWizardPage.selectDirectory(path);
		selectImportRootWizardPage.setSearchForNestedProjects(true);
		selectImportRootWizardPage.setDetectAndConfigureNatures(true);
		
		//check proposals
		List<ProjectProposal> allProjectProposals = selectImportRootWizardPage.getAllProjectProposals();
		List<ProjectProposal> expectedProposals = getExpectedProposals();
		assertEquals(expectedProposals.size(), allProjectProposals.size());
		for (ProjectProposal projectProposal : allProjectProposals) {
			if (!expectedProposals.contains(projectProposal)) {
				fail("Expected proposals: " + expectedProposals.toString() + ", actual proposals: "
						+ allProjectProposals.toString());
			}
		}
		easymportWizard.finish();
		
		//check imported project
		checkErrorLog();
		checkProblemsView();
		
		cehckImportedProject();
	}

	private void checkErrorLog() {
		LogView logView = new LogView();
		logView.open();
		List<LogMessage> errorMessages = logView.getErrorMessages();
		assertEquals("There are errors in error log: "+errorMessages, 0, errorMessages.size());
	}

	private void checkProblemsView() {
		ProblemsView problemsView = new ProblemsView();
		problemsView.open();
		List<Problem> problems = problemsView.getProblems(ProblemType.ERROR);
		assertEquals("There should be no errors in imported project", 0, problems.size());

	}

	abstract File getProjectPath();

	abstract List<ProjectProposal> getExpectedProposals();

	abstract List<ImportedProject> getExpectedImportedProjects();
	
	
	/**
	 * Checks whether the project was imported correctly.
	 * 
	 */
	abstract void cehckImportedProject();

}
