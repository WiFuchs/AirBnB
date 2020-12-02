package database;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NoDoubleBookingsTrigger implements Trigger {

    private final int room = 1;
    private final int checkin = 2;
    private final int checkout = 3;
    @Override
    public void init(Connection conn, String schemaName,
                     String triggerName, String tableName, boolean before, int type)
            throws SQLException {}

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow)
            throws SQLException {
        StringBuilder sb = new StringBuilder("select checkout from lab7_reservations where room = ? and checkout > ? and checkin < ?");
        if (oldRow != null) {
            sb.append(" and code <> ");
            sb.append(oldRow[0]);
        }
        try (PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
            stmt.setObject(1, newRow[room]);
            stmt.setObject(2, newRow[checkin]);
            stmt.setObject(3, newRow[checkout]);
            ResultSet rs = stmt.executeQuery();

            // if rs is not empty, there is a conflicting reservation
            if (rs.next()) {
                throw new SQLException("This room is booked during your stay.");
            }
        }
    }

    @Override
    public void close() throws SQLException {}

    @Override
    public void remove() throws SQLException {}
}