package edu.ualberta.med.biobank.widgets;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.BioBankPlugin;

//import edu.ualberta.med.biobank.common.formatters.DateFormatter;

/**
 * Wrapper around Nebula's CDateTime widget.
 * 
 * HISTORY
 * 
 * Previously used gface Date Picker combo (http://gface.sourceforge.net/) but
 * it did not display well on Windows and Linux.
 * 
 * Attempted to use SWT DateTime but at the time it did not support null dates
 * and null times.
 */
public class DateTimeWidget extends BiobankWidget {

    private CDateTime dateEntry;

    private Button dateButton;

    private DateTime timeEntry;

    /**
     * Show date and time widget
     */
    public DateTimeWidget(Composite parent, int style, Date date) {
        this(parent, style, date, -1);
    }

    /**
     * Allow date to be null. it typeShown == SWT.DATE, show only date; if
     * typeShown == SWT.TIME, show only time otherwise show both of them
     */
    public DateTimeWidget(Composite parent, int style, Date date, int typeShown) {
        super(parent, style);

        GridLayout layout = new GridLayout(3, false);
        layout.horizontalSpacing = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        setLayout(layout);

        setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        if (typeShown != SWT.TIME) {
            dateEntry = new CDateTime(this, CDT.BORDER);
            dateEntry.setPattern("yyyy-MM-dd");
            Point size = dateEntry.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            GridData gd = new GridData();
            gd.widthHint = size.x + 10;
            gd.heightHint = size.y;
            dateEntry.setLayoutData(gd);

            dateButton = new Button(this, SWT.NONE);
            dateButton.setImage(BioBankPlugin.getDefault().getImageRegistry()
                .get(BioBankPlugin.IMG_CALENDAR));
            dateButton.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseUp(MouseEvent e) {
                    final Shell dialog = new Shell(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell(), SWT.DIALOG_TRIM);
                    dialog.setLayout(new GridLayout(3, false));

                    final DateTime calendar = new DateTime(dialog, SWT.CALENDAR
                        | SWT.BORDER);
                    if (dateEntry.getSelection() != null) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(dateEntry.getSelection());
                        calendar.setDate(c.get(Calendar.YEAR), c
                            .get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                    }
                    new Label(dialog, SWT.BORDER);
                    calendar.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseDoubleClick(MouseEvent e) {
                            // not perfect... a double click can close widget
                            // even
                            // if its on a month or year (or on nothing at all)
                            Calendar c = Calendar.getInstance();
                            c.set(Calendar.DAY_OF_MONTH, calendar.getDay());
                            c.set(Calendar.MONTH, calendar.getMonth());
                            c.set(Calendar.YEAR, calendar.getYear());
                            dateEntry.setSelection(c.getTime());
                            dialog.close();
                        }
                    });
                    dialog.pack();
                    dialog.open();

                }

            });
        }
        if (typeShown != SWT.DATE) {
            timeEntry = new DateTime(this, SWT.BORDER | SWT.TIME | SWT.SHORT);
            timeEntry.setTime(0, 0, 0);
        }
        if (date != null) {
            setDate(date);
        }
    }

    public String getText() {
        return getDate().toString();
    }

    public Date getDate() {
        Calendar cal = new GregorianCalendar();
        if (dateEntry != null && dateEntry.getSelection() != null) {
            cal.setTime(dateEntry.getSelection());
            cal.set(Calendar.HOUR, timeEntry.getHours());
            cal.set(Calendar.MINUTE, timeEntry.getMinutes());
            return cal.getTime();
        }
        return null;
    }

    public void setDate(Date date) {
        if (date == null)
            return;

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);

        if (timeEntry != null) {
            timeEntry.setTime(cal.get(Calendar.HOUR_OF_DAY), cal
                .get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }

        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        if (dateEntry != null) {
            dateEntry.setSelection(date);
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        if (dateEntry != null) {
            dateEntry.addSelectionListener(listener);
        }
        if (timeEntry != null) {
            timeEntry.addSelectionListener(listener);
        }
    }

    public void removeSelectionListener(SelectionListener listener) {
        if (dateEntry != null) {
            dateEntry.removeSelectionListener(listener);
        }
        if (timeEntry != null) {
            timeEntry.removeSelectionListener(listener);
        }
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        if (dateEntry != null) {
            dateEntry.addListener(eventType, listener);
        }
        if (timeEntry != null) {
            timeEntry.addListener(eventType, listener);
        }
    }

    @Override
    public void removeListener(int eventType, Listener listener) {
        if (dateEntry != null) {
            dateEntry.removeListener(eventType, listener);
        }
        if (timeEntry != null) {
            timeEntry.removeListener(eventType, listener);
        }
    }
}
