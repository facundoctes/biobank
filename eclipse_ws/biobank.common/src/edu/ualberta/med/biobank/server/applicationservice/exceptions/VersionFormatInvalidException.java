package edu.ualberta.med.biobank.server.applicationservice.exceptions;

import edu.ualberta.med.biobank.i18n.SS;

public class VersionFormatInvalidException extends BiobankServerException {

    private static final long serialVersionUID = 1L;

    public VersionFormatInvalidException() {
        super();
    }

    public VersionFormatInvalidException(Throwable cause) {
        super(cause);
    }

    @SuppressWarnings("nls")
    @Override
    public String getMessage() {
        return SS.tr("The version string is formatted incorrectly.").toString();
    }

}
