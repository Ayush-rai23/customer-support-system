package com.support.backend.enums;

/** Direction of a ticket message relative to the support system. */
public enum MessageDirection {
    /** From the customer into the system (email in / seeded conversation). */
    INBOUND,
    /** From the system out to the customer (admin or AI reply). */
    OUTBOUND
}
