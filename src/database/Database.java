package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Database {
  private static final String CONFIG_FILE = "inputs/h2creation.sql";

  private Database() {}

  public static void init(String url, String user, String password) throws FileNotFoundException, SQLException {
    try (Scanner scanner = new Scanner(new File(CONFIG_FILE));
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
}