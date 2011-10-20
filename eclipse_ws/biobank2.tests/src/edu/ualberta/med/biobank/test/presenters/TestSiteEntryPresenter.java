package edu.ualberta.med.biobank.test.presenters;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import edu.ualberta.med.biobank.common.action.Dispatcher;
import edu.ualberta.med.biobank.common.action.site.GetSiteStudyInfoAction.StudyInfo;
import edu.ualberta.med.biobank.model.ActivityStatus;
import edu.ualberta.med.biobank.mvp.PresenterModule;
import edu.ualberta.med.biobank.mvp.presenter.impl.ActivityStatusComboPresenter;
import edu.ualberta.med.biobank.mvp.presenter.impl.AddressEntryPresenter;
import edu.ualberta.med.biobank.mvp.presenter.impl.FormManagerPresenter;
import edu.ualberta.med.biobank.mvp.presenter.impl.SiteEntryPresenter;
import edu.ualberta.med.biobank.mvp.user.ui.HasSelectedValue;
import edu.ualberta.med.biobank.test.TestingDispatcher;

public class TestSiteEntryPresenter {

    private Injector injector;

    private SiteEntryPresenter.View siteEntryView;

    private AddressEntryPresenter.View addressView;

    private ActivityStatusComboPresenter.View activityStatusView;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testValidInput() {
        activityStatusView = Mockito
            .mock(ActivityStatusComboPresenter.View.class);
        addressView = Mockito.mock(AddressEntryPresenter.View.class);
        siteEntryView = Mockito.mock(SiteEntryPresenter.View.class);

        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                install(new PresenterModule());

                bind(EventBus.class).to(SimpleEventBus.class).in(
                    Singleton.class);
                bind(Dispatcher.class).to(TestingDispatcher.class).in(
                    Singleton.class);

                bind(ActivityStatusComboPresenter.View.class).toInstance(
                    activityStatusView);
                bind(AddressEntryPresenter.View.class).toInstance(addressView);
                bind(FormManagerPresenter.View.class).toInstance(
                    Mockito.mock(FormManagerPresenter.View.class));
                bind(SiteEntryPresenter.View.class).toInstance(siteEntryView);
            }

        };

        injector = Guice.createInjector(Stage.PRODUCTION, module);

        SiteEntryPresenter presenter = injector
            .getInstance(SiteEntryPresenter.class);

        HasValue<String> name = Mockito.mock(HasValue.class);
        HasValue<String> nameShort = Mockito.mock(HasValue.class);
        HasValue<String> comment = Mockito.mock(HasValue.class);
        HasValue<List<StudyInfo>> studies = Mockito.mock(HasValue.class);

        HasValue<String> nullString = Mockito.mock(HasValue.class);
        HasSelectedValue<ActivityStatus> astatus = Mockito
            .mock(HasSelectedValue.class);

        Mockito.when(name.getValue()).thenReturn("Test Site");
        Mockito.when(nameShort.getValue()).thenReturn("TS");
        Mockito.when(comment.getValue()).thenReturn("This is a comment");
        Mockito.when(nullString.getValue()).thenReturn(null);
        Mockito.when(astatus.getValue()).thenReturn(new ActivityStatus());

        Mockito.when(siteEntryView.getName()).thenReturn(name);
        Mockito.when(siteEntryView.getNameShort()).thenReturn(nameShort);
        Mockito.when(siteEntryView.getComment()).thenReturn(comment);
        Mockito.when(siteEntryView.getStudies()).thenReturn(studies);

        Mockito.when(addressView.getStreet1()).thenReturn(nullString);
        Mockito.when(addressView.getStreet2()).thenReturn(nullString);
        Mockito.when(addressView.getCity()).thenReturn(nullString);
        Mockito.when(addressView.getProvince()).thenReturn(nullString);
        Mockito.when(addressView.getPostalCode()).thenReturn(nullString);
        Mockito.when(addressView.getPhoneNumber()).thenReturn(nullString);
        Mockito.when(addressView.getFaxNumber()).thenReturn(nullString);
        Mockito.when(addressView.getCountry()).thenReturn(nullString);

        Mockito.when(activityStatusView.getActivityStatus())
            .thenReturn(astatus);

        presenter.bind();
        presenter.createSite();

        // TODO; instead of call doSave() the view's save button should be
        // pressed
        presenter.doSave();

        // TODO: test needs to listen to event buffer for site created event

    }
}