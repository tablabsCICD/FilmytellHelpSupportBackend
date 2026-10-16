package com.filmytell.support.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum TicketStatus {
    NEW,
    AI_ANALYSIS,
    AI_RESOLVED,
    ASSIGNED,
    IN_PROGRESS,
    PENDING_USER,
    RESOLVED,
    CLOSED;

    public boolean canTransitionTo(TicketStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case NEW -> EnumSet.of(AI_ANALYSIS, ASSIGNED, IN_PROGRESS, CLOSED).contains(target);
            case AI_ANALYSIS -> EnumSet.of(AI_RESOLVED, ASSIGNED, IN_PROGRESS, CLOSED).contains(target);
            case AI_RESOLVED -> EnumSet.of(CLOSED, ASSIGNED, IN_PROGRESS).contains(target);
            case ASSIGNED -> EnumSet.of(IN_PROGRESS, PENDING_USER, CLOSED).contains(target);
            case IN_PROGRESS -> EnumSet.of(PENDING_USER, RESOLVED, ASSIGNED, CLOSED).contains(target);
            case PENDING_USER -> EnumSet.of(IN_PROGRESS, RESOLVED, CLOSED).contains(target);
            case RESOLVED -> EnumSet.of(CLOSED, IN_PROGRESS).contains(target); // Reopening allowed
            case CLOSED -> false; // Closed is terminal unless admin overrides
        };
    }

    public Set<TicketStatus> getAllowedTransitions() {
        return switch (this) {
            case NEW -> EnumSet.of(AI_ANALYSIS, ASSIGNED, IN_PROGRESS, CLOSED);
            case AI_ANALYSIS -> EnumSet.of(AI_RESOLVED, ASSIGNED, IN_PROGRESS, CLOSED);
            case AI_RESOLVED -> EnumSet.of(CLOSED, ASSIGNED, IN_PROGRESS);
            case ASSIGNED -> EnumSet.of(IN_PROGRESS, PENDING_USER, CLOSED);
            case IN_PROGRESS -> EnumSet.of(PENDING_USER, RESOLVED, ASSIGNED, CLOSED);
            case PENDING_USER -> EnumSet.of(IN_PROGRESS, RESOLVED, CLOSED);
            case RESOLVED -> EnumSet.of(CLOSED, IN_PROGRESS);
            case CLOSED -> Collections.emptySet();
        };
    }
}
