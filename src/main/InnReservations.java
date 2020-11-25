package main;

import database.Database;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class InnReservations {

  private static final String JDBC_URL = "jdbc:h2:~/csc365_lab7";
  private static final String JDBC_USER = "";
  private static final String JDBC_PASSWORD = "";
  private static final String MENU =
    "Main Menu"
    + "\n1: FR1 view all rooms and rates"
    + "\n2: FR2 make a new reservation"
    + "\n3: FR3 change a reservation"
    + "\n4: FR4 cancel a reservation"
    + "\n5: FR5 view revenue summary"
    + "\n6: exit"
    + "\nSelect an option, or hold to speak with a representative";

  public static void main(String[] args) {
    try {
      Database.init(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    } catch (FileNotFoundException | SQLException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    mainMenu();
  }

  private static void mainMenu() {
    try (Scanner in = new Scanner(System.in)) {
      while (true) {
        System.out.println(MENU);
        try {
          int c = in.nextInt();
          if (c == 1) roomsAndRates();
          else if (c == 2) reservations();
          else if (c == 3) reservationChange();
          else if (c == 4) reservationCancellation();
          else if (c == 5) revenueSummary();
          else if (c == 6) break;
          else throw new InputMismatchException();
        } catch (InputMismatchException e) {
          in.nextLine();
          System.out.println("You must input an integer from 1 to 6!");
        }
      }
    }
  }

  private static void roomsAndRates() {
    System.out.println("roomsAndRates");
  }
  private static void reservations() {
    System.out.println("reservations");
  }
  private static void reservationChange() {
    System.out.println("reservationChange");
  }
  private static void reservationCancellation() {
    System.out.println("reservationCancellation");
  }
  private static void revenueSummary() {
    System.out.println("revenueSummary");
  }
}