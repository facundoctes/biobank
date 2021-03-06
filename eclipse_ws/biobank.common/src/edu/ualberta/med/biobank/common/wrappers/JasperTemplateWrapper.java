package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.base.JasperTemplateBaseWrapper;
import edu.ualberta.med.biobank.model.JasperTemplate;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class JasperTemplateWrapper extends JasperTemplateBaseWrapper {

    public JasperTemplateWrapper(WritableApplicationService appService,
        JasperTemplate wrappedObject) {
        super(appService, wrappedObject);
    }

    public JasperTemplateWrapper(WritableApplicationService appService) {
        super(appService);
    }

    private static final String TEMPLATES_QRY = "from " //$NON-NLS-1$
        + JasperTemplate.class.getName();

    @Deprecated
    public static List<JasperTemplateWrapper> getAllTemplates(
        WritableApplicationService appService) throws ApplicationException {
        StringBuilder qry = new StringBuilder(TEMPLATES_QRY);

        HQLCriteria criteria = new HQLCriteria(qry.toString());
        List<JasperTemplate> templates = appService.query(criteria);
        List<JasperTemplateWrapper> wrappers =
            new ArrayList<JasperTemplateWrapper>();
        for (JasperTemplate t : templates) {
            wrappers.add(new JasperTemplateWrapper(appService, t));
        }
        return wrappers;
    }

    private static final String TEMPLATE_NAMES_QRY = "select name from " //$NON-NLS-1$
        + JasperTemplate.class.getName();

    public static List<String> getTemplateNames(
        WritableApplicationService appService) throws ApplicationException {
        StringBuilder qry = new StringBuilder(TEMPLATE_NAMES_QRY);

        HQLCriteria criteria = new HQLCriteria(qry.toString());
        return appService.query(criteria);
    }

    private static final String TEMPLATE_BY_NAME_QRY = "from " //$NON-NLS-1$
        + JasperTemplate.class.getName() + " where name=?"; //$NON-NLS-1$

    @Deprecated
    public static JasperTemplateWrapper getTemplateByName(
        WritableApplicationService appService, String name)
        throws ApplicationException {
        StringBuilder qry = new StringBuilder(TEMPLATE_BY_NAME_QRY);
        List<Object> qryParms = new ArrayList<Object>();
        qryParms.add(name);

        HQLCriteria criteria = new HQLCriteria(qry.toString(), qryParms);
        List<JasperTemplate> templates = appService.query(criteria);
        return new JasperTemplateWrapper(appService, templates.get(0));
    }

}
