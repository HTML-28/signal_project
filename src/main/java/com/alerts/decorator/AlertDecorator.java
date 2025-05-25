package com.alerts.decorator;

import com.alerts.Alert;
import com.alerts.AlertSeverity;
import com.alerts.AlertType;

/**
 * Abstract base decorator for the Alert class.
 * Allows extension of Alert functionality via composition.
 */
public abstract class AlertDecorator extends Alert {

    // The Alert instance being decorated
    protected Alert wrappedAlert;

    /**
     * Constructs an AlertDecorator wrapping the specified alert.
     *
     * @param alert the alert to decorate
     */
    public AlertDecorator(Alert alert) {
        super(alert.getPatientId(), alert.getType(), alert.getMessage(),
              alert.getTimestamp(), alert.getSeverity());
        this.wrappedAlert = alert;
    }

    // Delegate all Alert methods to the wrapped Alert

    @Override
    public int getPatientId() {
        return wrappedAlert.getPatientId();
    }

    @Override
    public AlertType getType() {
        return wrappedAlert.getType();
    }

    @Override
    public String getMessage() {
        return wrappedAlert.getMessage();
    }

    @Override
    public long getTimestamp() {
        return wrappedAlert.getTimestamp();
    }

    @Override
    public AlertSeverity getSeverity() {
        return wrappedAlert.getSeverity();
    }

    @Override
    public void setSeverity(AlertSeverity severity) {
        wrappedAlert.setSeverity(severity);
        super.setSeverity(severity);
    }
}