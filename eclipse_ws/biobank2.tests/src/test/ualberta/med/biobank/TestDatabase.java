package test.ualberta.med.biobank;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;

public class TestDatabase {
    protected static WritableApplicationService appService;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    private static final int ALPHABET_LEN = ALPHABET.length();

    protected Random r;

    private static final List<String> IGNORE_RETURN_TYPES = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("java.lang.Class");
            add("java.lang.Object");
            add("java.util.Set");
            add("java.util.List");
            add("java.util.Collection");
        }
    };

    private class GetterInfo {
        Method getMethod;
        Method setMethod;
    }

    @Before
    public void setUp() throws Exception {
        r = new Random();
        appService = AllTests.appService;
        if (appService == null) {
            AllTests.setUp();
            appService = AllTests.appService;
        }
    }

    public Collection<GetterInfo> getGettersAndSetters(ModelWrapper<?> w) {
        HashMap<String, GetterInfo> map = new HashMap<String, GetterInfo>();
        Method[] methods = w.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().startsWith("get")
                && !method.getName().equals("getClass")
                && !IGNORE_RETURN_TYPES.contains(method.getReturnType()
                    .getName())
                && !method.getReturnType().getName().startsWith(
                    "edu.ualberta.med.biobank.common.wrappers")) {
                GetterInfo getterInfo = new GetterInfo();
                getterInfo.getMethod = method;
                map.put(method.getName(), getterInfo);
            }
        }

        for (Method method : methods) {
            if (method.getName().startsWith("set")
                && !method.getName().equals("setClass")) {
                String setterName = method.getName();
                String getterName = "g"
                    + setterName.substring(1, setterName.length());
                GetterInfo getterInfo = map.get(getterName);
                if (getterInfo == null) {
                    System.out.println("no getter found for "
                        + w.getClass().getName() + "." + setterName + "()");
                    continue;
                }
                getterInfo.setMethod = method;
            }
        }
        return map.values();
    }

    public void testGettersAndSetters(ModelWrapper<?> w)
        throws BiobankCheckException, Exception {
        Collection<GetterInfo> gettersInfoList = getGettersAndSetters(w);
        for (GetterInfo getterInfo : gettersInfoList) {
            if (getterInfo.setMethod == null) {
                System.out.println("no setter found for "
                    + w.getClass().getName() + "."
                    + getterInfo.getMethod.getName() + "()");
                continue;
            }

            String getReturnType = getterInfo.getMethod.getReturnType()
                .getName();

            for (int i = 0; i < 5; ++i) {
                Object parameter = null;

                if (getReturnType.equals("java.lang.Boolean")) {
                    parameter = new Boolean(r.nextBoolean());
                } else if (getReturnType.equals("java.lang.Integer")) {
                    parameter = new Integer(r.nextInt());
                } else if (getReturnType.equals("java.lang.Double")) {
                    parameter = new Double(r.nextDouble());
                } else if (getReturnType.equals("java.lang.String")) {
                    String str = new String();
                    for (int j = 0, n = r.nextInt(32); j < n; ++j) {
                        int begin = r.nextInt(ALPHABET_LEN - 1);
                        str += ALPHABET.substring(begin, begin + 1);
                    }
                    parameter = str;
                } else if (getReturnType.equals("java.util.Date")) {
                    String dateStr = String
                        .format("%04-%02-%02 %02:%02", 2000 + r.nextInt(40), r
                            .nextInt(12) + 1, r.nextInt(30) + 1,
                            r.nextInt(24) + 1, r.nextInt(60) + 1);
                    Date date = DateFormatter.parse(
                        DateFormatter.dateFormatter, dateStr);
                    parameter = date;
                } else {
                    throw new Exception("return type " + getReturnType
                        + " for method " + getterInfo.getMethod.getName()
                        + " for class " + w.getClass().getName()
                        + " not implemented");
                }

                getterInfo.setMethod.invoke(w, parameter);
                w.persist();
                w.reload();
                Object getResult = getterInfo.getMethod.invoke(w);

                Assert.assertEquals(w.getClass().getName() + "."
                    + getterInfo.getMethod.getName() + "()", parameter,
                    getResult);
            }
        }

    }

    protected ContainerTypeWrapper newContainerType(SiteWrapper site,
        String name, String nameShort, Integer labelingScheme,
        Integer rowCapacity, Integer colCapacity, boolean isTopLevel) {
        ContainerTypeWrapper ct = new ContainerTypeWrapper(appService);
        ct.setSite(site);
        ct.setName(name);
        ct.setNameShort(nameShort);
        ct.setChildLabelingScheme(labelingScheme);
        if (rowCapacity != null)
            ct.setRowCapacity(rowCapacity);
        if (colCapacity != null)
            ct.setColCapacity(colCapacity);
        ct.setTopLevel(isTopLevel);
        return ct;
    }

    protected ContainerTypeWrapper addContainerType(SiteWrapper site,
        String name, String nameShort, Integer labelingScheme,
        Integer rowCapacity, Integer colCapacity, boolean isTopLevel)
        throws BiobankCheckException, Exception {
        ContainerTypeWrapper container = newContainerType(site, name,
            nameShort, labelingScheme, rowCapacity, colCapacity, isTopLevel);
        container.persist();
        return container;
    }

    protected ContainerWrapper newContainer(String barcode, SiteWrapper site,
        ContainerTypeWrapper type) throws Exception {
        ContainerWrapper container;

        container = new ContainerWrapper(appService);
        container.setProductBarcode(barcode);
        if (site != null) {
            container.setSite(site);
        }
        container.setContainerType(type);
        return container;
    }

    protected ContainerWrapper addContainer(String barcode, SiteWrapper site,
        ContainerTypeWrapper type) throws Exception {
        ContainerWrapper container = newContainer(barcode, site, type);
        container.persist();
        return container;
    }
}
