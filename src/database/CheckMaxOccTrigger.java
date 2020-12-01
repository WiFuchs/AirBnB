package database;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckMaxOccTrigger implements Trigger {

    private final int room = 1;
    private final int adults = 7;
    private final int kids = 8;
    @Override
    public void init(Connection conn, String schemaName,
                     String triggerName, String tableName, boolean before, int type)
            throws SQLException {}

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "select maxOcc from lab7_rooms where roomCode = ?;")
        ) {
            stmt.setObject(1, newRow[room]);
            ResultSet rs = stmt.executeQuery();

            // if rs is not empty, there is a conflicting reservation
            if (!rs.next()) {
                throw new SQLException("Room does not exist: "+newRow[room]);
            } else {
                int maxOcc = rs.getInt("maxOcc");
                if (maxOcc < (int)newRow[adults] + (int)newRow[kids]) {
                    throw new SQLException("Too many occupants, "+newRow[room]+" can only fit "+maxOcc);
                }
            }
        }
    }

    @Override
    public void close() throws SQLException {}

    @Override
    public void remove() throws SQLException {}
}