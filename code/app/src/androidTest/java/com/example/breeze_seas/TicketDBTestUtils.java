package com.example.breeze_seas;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Test-only helper that restores {@link TicketDB} to its seeded demo state.
 *
 * <p>The ticket DB is a mutable singleton, so instrumented tests need a deterministic reset
 * point between runs without widening production API surface purely for tests.
 */
public final class TicketDBTestUtils {

    private TicketDBTestUtils() {
    }

    public static void resetDemoData() {
        try {
            TicketDB ticketDb = TicketDB.getInstance();

            clearListField(ticketDb, "listeners");
            clearListField(ticketDb, "activeTickets");
            clearListField(ticketDb, "attendingTickets");
            clearListField(ticketDb, "pastTickets");

            Field useDemoData = TicketDB.class.getDeclaredField("useDemoData");
            useDemoData.setAccessible(true);
            useDemoData.setBoolean(ticketDb, true);

            Method seedDemoData = TicketDB.class.getDeclaredMethod("seedDemoData");
            seedDemoData.setAccessible(true);
            seedDemoData.invoke(ticketDb);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to reset TicketDB demo data for tests", e);
        }
    }

    private static void clearListField(TicketDB ticketDb, String fieldName)
            throws ReflectiveOperationException {
        Field field = TicketDB.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        List<?> list = (List<?>) field.get(ticketDb);
        list.clear();
    }
}
