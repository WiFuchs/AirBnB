package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Database {
  private static final String CREATE_TABLE = "inputs/h2creation.sql";
  private static final String POPULATE_TABLE = "inputs/h2populate.sql";
  private static final String NO_DOUBLE_BOOKINGS =
    "CREATE TRIGGER no_double_bookings\n" +
    "BEFORE INSERT\n" +
    "ON lab7_reservations\n" +
    "for each row\n" +
    "begin\n" +
    "    declare conflicting integer;\n" +
    "    select 1\n" +
    "    into conflicting\n" +
    "    from lab7_reservations\n" +
    "    where Room = new.Room\n" +
    "        and (\n" +
    "            (CheckOut > new.CheckIn and CheckOut <= new.Checkout)\n" +
    "            or (CheckIn >= new.CheckIn  and CheckIn < new.Checkout)\n" +
    "            );\n" +
    "    if conflicting then\n" +
    "        SIGNAL SQLSTATE '45000'\n" +
    "        SET MESSAGE_TEXT = 'Double Booking';\n" +
    "    end if;\n" +
    "end;";
  private static final String CHECK_ROOM_CAPACITY =
    "CREATE  TRIGGER check_room_capacity\n" +
    "BEFORE INSERT\n" +
    "ON lab7_reservations\n" +
    "for each row\n" +
    "begin\n" +
    "    declare maximumOcc integer;\n" +
    "    select maxOcc\n" +
    "    into maximumOcc\n" +
    "    from lab7_rooms\n" +
    "    where RoomCode = new.Room;\n" +
    "    if maximumOcc < (new.Adults + new.Kids) then\n" +
    "        SIGNAL SQLSTATE '45000'\n" +
    "        SET MESSAGE_TEXT = 'Maximum Room Occupancy Exceeded';\n" +
    "    end if;\n" +
    "end;";
  private Database() {}

  public static void init(String url, String user, String password) throws FileNotFoundException, SQLException {
    runFile(CREATE_TABLE, url, user, password);
//    createTrigger(NO_DOUBLE_BOOKINGS, url, user, password);
//    createTrigger(CHECK_ROOM_CAPACITY, url, user, password);
    runFile(POPULATE_TABLE, url, user, password);
  }

  private static void runFile(String file, String url, String user, String password) throws FileNotFoundException, SQLException {
    try (Scanner scanner = new Scanner(new File(file));
         Connection conn = DriverManager.getConnection(url, user, password)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if(line.isEmpty()) {
          continue;
        }
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(line);
        }
      }
    }
  }

  private static void createTrigger(String sql, String url, String user, String password) throws SQLException {
    try (Connection conn = DriverManager.getConnection(url, user, password)) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute(sql);
      }
    }
  }
}