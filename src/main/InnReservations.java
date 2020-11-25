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
      boolean exit = false;
      while (!exit) {
        System.out.println(MENU);
        try {
          switch (in.nextInt()) {
            case 1:
              roomsAndRates();
              break;
            case 2:
              reservations();
              break;
            case 3:
              reservationChange();
              break;
            case 4:
              reservationCancellation();
              break;
            case 5:
              revenueSummary();
              break;
            case 6:
              exit = true;
              break;
            default:
              throw new InputMismatchException();
          }
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