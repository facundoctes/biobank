package test.ualberta.med.biobank;

import gov.nih.nci.system.applicationservice.WritableApplicationService;

import org.junit.Before;

public class TestDatabase {
	protected static WritableApplicationService appService;

	@Before
	public void setUp() throws Exception {
		appService = AllTests.appService;
		if (appService == null) {
			AllTests.setUp();
			appService = AllTests.appService;
		}
	}

}
