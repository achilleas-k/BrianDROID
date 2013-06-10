package org.briansimulator.briandroid.Simulations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SimulationContent {

    /**
     * An array of sample (Simulations) items.
     */
    public static List<SimItem> ITEMS = new ArrayList<SimItem>();

    /**
     * A map of sample (Simulations) items, by ID.
     */
    public static Map<String, SimItem> ITEM_MAP = new HashMap<String, SimItem>();

    static {
        // Add 3 sample items.
        addItem(new SimItem("COBA example", "COBA"));
        addItem(new SimItem("CUBA example", "CUBA"));
    }

    private static void addItem(SimItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A Simulations item representing a piece of content.
     */
    public static class SimItem {
        public String id;
        public String content;

        public SimItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
