package org.example;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Metrics {
    private final Map<String, Long> counters = new HashMap<>();
    private final Map<String, Long> timers = new HashMap<>();
    private final Map<String, Long> results = new LinkedHashMap<>();



    public void start(String name) { 
        timers.put(name, System.nanoTime()); 
    }


    public void stop(String name) {
        long endTime = System.nanoTime();
        if (timers.containsKey(name)) {
            results.put(name, endTime - timers.get(name));
        }
    }


    public void increment(String counter) { 
        increment(counter, 1); 
    }


    public void increment(String counter, long amount) {
        counters.put(counter, counters.getOrDefault(counter, 0L) + amount);
    }


    public void printReport() {
        System.out.println("Counters:");
        for (Map.Entry<String, Long> entry : counters.entrySet()) {
            System.out.printf("  %-25s: %d\n", entry.getKey(), entry.getValue());
        }
        System.out.println("\nTimings:");
        for (Map.Entry<String, Long> entry : results.entrySet()) {
            System.out.printf("  %-25s: %.4f ms\n", entry.getKey(),
                    TimeUnit.NANOSECONDS.toMicros(entry.getValue()) / 1000.0);
        }
    }
}
