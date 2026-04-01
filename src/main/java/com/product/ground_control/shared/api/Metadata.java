package com.product.ground_control.shared.api;

import java.util.Map;

/**
 * Value Object representing metadata associated with an evaluation or event.
 * Wraps a Map with defensive copying to ensure true immutability.
 */
public class Metadata {

    private final Map<String, Object> values;

    /**
     * Private constructor to enforce factory method usage.
     * Uses defensive copying to ensure immutability.
     *
     * @param values the metadata map
     */
    private Metadata(Map<String, Object> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * Factory method to create Metadata from a map of values.
     * Returns empty metadata if the input is null.
     *
     * @param values the metadata map
     * @return a new Metadata instance
     */
    public static Metadata of(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }
        return new Metadata(values);
    }

    /**
     * Factory method to create empty Metadata.
     *
     * @return an empty Metadata instance
     */
    public static Metadata empty() {
        return new Metadata(Map.of());
    }

    /**
     * Returns the underlying map of values.
     * The returned map is immutable.
     *
     * @return the metadata values
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Checks if this Metadata is empty.
     *
     * @return true if there are no values, false otherwise
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }
}
