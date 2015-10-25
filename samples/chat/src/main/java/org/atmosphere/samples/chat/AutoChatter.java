package org.atmosphere.samples.chat;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.Universe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 10/25/15
 * Time: 2:24 PM
 */
public class AutoChatter {
    private static final Logger logger = LoggerFactory.getLogger(Chat.class);
    static int counter = 0;
    static final Object lock = new Object();
    static boolean started = false;
    static Thread thread;

    public static void stopAutoChatter() {
        synchronized (lock) {
            if (started) {
                thread.interrupt();
            }
            started = false;
        }

    }

    public static void startAutoChatter() {
        synchronized (lock) {

            if (!started) {
                logger.info("Starting auto chatter");
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        doAutochat();
                    }
                };
                thread = new Thread(r);
                thread.start();
                started = true;
                logger.info("Started auto chatter");
            }
            counter = 0;
        }
    }

    private static void doAutochat() {
        try {
            logger.info("Auto chat loop starting soon..");
            //  10 seconds before it zips by
            Thread.sleep(5000);
            while (true) {
                logger.info("Auto chat loop " + counter);
                Thread.sleep(20);
                Broadcaster caster = Universe.broadcasterFactory().lookup("/chat");
                if (caster != null) {
                    logger.info("Chatted");
                    caster.broadcast(new Message("autochat", "" + counter));
                } else {
                    logger.info("Did not chat");
                    return;
                }
                ++counter;
            }
        } catch (InterruptedException e) {
            logger.info("Stopping auto chat loop");
        }
    }
}
