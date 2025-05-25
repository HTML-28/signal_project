package com.alerts.decorator;

import java.util.Timer;
import java.util.TimerTask;
import com.alerts.Alert;

/**
 * Decorator that adds repeat notification behavior to alerts.
 * Repeats alert notifications at a fixed interval, up to a maximum count.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private long repeatIntervalMs;
    private int repeatCount;
    private int maxRepeatCount;
    private Timer timer;

    /**
     * Constructs a RepeatedAlertDecorator.
     *
     * @param alert the alert to decorate
     * @param repeatIntervalMs interval between repeats in milliseconds
     * @param maxRepeatCount maximum number of repeats
     */
    public RepeatedAlertDecorator(Alert alert, long repeatIntervalMs, int maxRepeatCount) {
        super(alert);
        this.repeatIntervalMs = repeatIntervalMs;
        this.maxRepeatCount = maxRepeatCount;
        this.repeatCount = 0;
        scheduleNextRepeat();
    }

    /**
     * Schedules the next repeat of the alert.
     */
    private void scheduleNextRepeat() {
        this.timer = new Timer(true); // Daemon thread
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                repeatCount++;
                if (repeatCount <= maxRepeatCount) {
                    System.out.println("Alert repeated: " + getMessage() + " (" + repeatCount + "/" + maxRepeatCount + ")");
                    if (repeatCount < maxRepeatCount) {
                        scheduleNextRepeat();
                    }
                }
            }
        }, repeatIntervalMs);
    }

    /**
     * Cancels all future scheduled repeats.
     */
    public void cancelRepeats() {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public String getMessage() {
        return wrappedAlert.getMessage() + " [REPEAT " + repeatCount + "/" + maxRepeatCount + "]";
    }
}