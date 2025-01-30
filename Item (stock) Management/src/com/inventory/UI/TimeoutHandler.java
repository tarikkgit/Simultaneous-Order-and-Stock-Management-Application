package com.inventory.UI;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeoutHandler {
    private static final Logger logger = Logger.getLogger(TimeoutHandler.class.getName());
    private Timer timer;
    private final long timeoutPeriod;
    private boolean isCompleted;

    public TimeoutHandler(long timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
        this.isCompleted = false;
    }

    public void startTimer(int customerId, int orderId) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isCompleted) {
                    handleTimeout(customerId, orderId);
                }
            }
        }, timeoutPeriod);
    }

    public void completeTask() {
        isCompleted = true;
        if (timer != null) {
            timer.cancel();
        }
    }

    private void handleTimeout(int customerId, int orderId) {
        logger.log(Level.WARNING, "Order {0} for customer {1} timed out", new Object[]{orderId, customerId});
        // Burada işlem iptali ve loglama işlemlerini gerçekleştirebiliriz
    }
}
