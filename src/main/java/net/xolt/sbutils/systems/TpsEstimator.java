package net.xolt.sbutils.systems;

import net.xolt.sbutils.SbUtils;

import java.util.LinkedList;
import java.util.List;

public class TpsEstimator {
    private static final int SAMPLE_SIZE = 10;
    private final LinkedList<Long> timeSetAt;

    private double tickRate;
    private double cappedTickRate;

    public TpsEstimator() {
        this.timeSetAt = new LinkedList<>();
        reset();
    }

    public void onSetTime() {
        timeSetAt.offer(System.currentTimeMillis());
        if (timeSetAt.size() > SAMPLE_SIZE) {
            timeSetAt.poll();
        }
        tickRate = calcTickRate(timeSetAt);
        cappedTickRate = Math.min(tickRate, 20.0);
    }

    public void onDisconnect() {
        reset();
    }

    public double getTickRate() {
        return tickRate;
    }

    public double getCappedTickRate() {
        return cappedTickRate;
    }

    private void reset() {
        timeSetAt.clear();
        tickRate = 20.0;
        cappedTickRate = 20.0;
    }

    private static double calcTickRate(List<Long> times) {
        if (times.size() < 2)
            return 10;

        int total = 0;
        for (int i = 0; i < times.size() - 1; i++) {
            total += (int)(Math.abs(times.get(i + 1) - times.get(i)));
        }
        double totalSecs = total / 1000.0;
        double avgDelay = totalSecs / (times.size() - 1);
        return 20.0 / avgDelay;
    }
}
