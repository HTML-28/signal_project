package com.alerts.decorator;

import com.alerts.Alert;
import com.alerts.AlertSeverity;

/**
 * Decorator that escalates the severity of an alert for prioritization.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    // Reason for priority escalation
    private String priorityReason;

    /**
     * Constructs a PriorityAlertDecorator.
     *
     * @param alert the alert to decorate
     * @param priorityReason explanation for priority escalation
     */
    public PriorityAlertDecorator(Alert alert, String priorityReason) {
        super(alert);
        this.priorityReason = priorityReason;
        escalateSeverity();
    }

    /**
     * Escalates the severity level of the alert by one step, if possible.
     */
    private void escalateSeverity() {
        switch (wrappedAlert.getSeverity()) {
            case LOW:
                setSeverity(AlertSeverity.MEDIUM);
                break;
            case MEDIUM:
                setSeverity(AlertSeverity.HIGH);
                break;
            case HIGH:
                setSeverity(AlertSeverity.CRITICAL);
                break;
            case CRITICAL:
                // Already at highest severity; do nothing
                break;
        }
    }

    @Override
    public String getMessage() {
        return "PRIORITY: " + wrappedAlert.getMessage() + " - " + priorityReason;
    }
}