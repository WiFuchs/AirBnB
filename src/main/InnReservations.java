package main;

import database.Database;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public class InnReservations {

  private static final String JDBC_URL = "jdbc:h2:~/csc365_lab7";
  private static final String JDBC_USER = "";
  private static final String JDBC_PASSWORD = "";

  public static void main(String[] args) {
    try {
      Database.init(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    } catch (FileNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }
}