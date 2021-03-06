package main;

import database.Database;
import org.h2.store.Data;

import java.io.FileNotFoundException;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

public class InnReservations {

    private static final String JDBC_URL = "jdbc:h2:~/csc365_lab7";
    private static final String JDBC_USER = "";
    private static final String JDBC_PASSWORD = "";
    private static final String MENU =
        "\nMain Menu"
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
        String[] headers = {"RoomCode", "RoomName", "Beds", "BedType", "MaxOcc",
            "BasePrice", "Decor", "NextAvailable", "NextReservation"};
        String bar = String.format("%-127s%n", "-").replace(" ", "-");
        String format = "%-8s | %-25s | %-5s | %-8s | %-7s | %-10s | %-11s | %-14s | %-15s%n";
        String header = String.format(format, (Object[]) headers) + bar;
        String query =
          "WITH nearestPastCheckin AS (\n" +
            "    SELECT room, MAX(checkin) AS nearestCheckin\n" +
            "    FROM lab7_reservations\n" +
            "    WHERE checkin <= CURDATE()\n" +
            "    GROUP BY room\n" +
            "), soonestDayAfterReservation AS (\n" +
            "    SELECT r.room, DATEADD(DAY, 1, checkout) AS dayAfterReservation\n" +
            "    FROM lab7_reservations AS r\n" +
            "    JOIN nearestPastCheckin AS n ON r.room = n.room \n" +
            "    WHERE nearestCheckin <= checkin AND\n" +
            "        DATEADD(DAY, 1, checkout) NOT IN (\n" +
            "            SELECT checkin FROM lab7_reservations\n" +
            "        )\n" +
            "), nextReservations AS (\n" +
            "    SELECT room, checkin\n" +
            "    FROM lab7_reservations\n" +
            "    WHERE CURDATE() <= checkin\n" +
            ") \n" +
            "SELECT roomcode, roomname, beds, bedtype, maxOcc, basePrice, decor, \n" +
            "    CASE WHEN CURDATE() >= IFNULL(MIN(s.dayAfterReservation), CURDATE())\n" +
            "        THEN NULL ELSE MIN(s.dayAfterReservation) END AS nextAvailable, \n" +
            "    MIN(n.checkin) AS nextReservation\n" +
            "FROM lab7_rooms AS r \n" +
            "    LEFT JOIN soonestDayAfterReservation AS s ON r.roomcode = s.room\n" +
            "    LEFT JOIN nextReservations AS n ON r.roomcode = n.room\n" +
            "GROUP BY r.roomcode\n" +
            "ORDER BY r.roomname";

        System.out.print("Rooms and Rates\n" + header);
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    Date nextAvailable = rs.getDate("nextAvailable");
                    Date nextReservation = rs.getDate("nextReservation");
                    System.out.printf(format,
                      rs.getString("roomcode"), rs.getString("roomname"),
                      rs.getString("beds"), rs.getString("bedtype"),
                      rs.getString("maxOcc"), rs.getString("basePrice"),
                      rs.getString("decor"),
                      nextAvailable == null ? "Today" : nextAvailable.toLocalDate(),
                      nextReservation == null ? "None" : nextReservation.toLocalDate());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void reservations() {
        System.out.println("Make a new reservation");
        Scanner in = new Scanner(System.in);
        System.out.println("What is your first name?");
        String fname = in.nextLine().toUpperCase();
        System.out.println("What is your last name?");
        String lname = in.nextLine().toUpperCase();
        System.out.println("What is the room code of the room you want to reserve?");
        String room = in.nextLine().toUpperCase();
        System.out.println("When do you want to check in?");
        Date checkin = java.sql.Date.valueOf(in.nextLine());
        System.out.println("When do you want to check out?");
        Date checkout = java.sql.Date.valueOf(in.nextLine());
        System.out.println("How many children are in your party?");
        int children = in.nextInt();
        System.out.println("How many adults are in your party?");
        int adults = in.nextInt();
        System.out.println("Checking availability for "+room+" between "+checkin+" and "+checkout+" for "+children+" children and "+adults+" adults");

        try (Connection conn = DriverManager.getConnection(JDBC_URL,
                JDBC_USER,
                JDBC_PASSWORD)) {
            String insertSql = "INSERT INTO lab7_reservations (code, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids)" +
                    "VALUES ((select max(code) + 1 from lab7_reservations), ?, ?, ?, (select basePrice from lab7_rooms where roomcode = ?), ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, room);
                pstmt.setDate(2, checkin);
                pstmt.setDate(3, checkout);
                pstmt.setString(4, room);
                pstmt.setString(5, lname);
                pstmt.setString(6, fname);
                pstmt.setInt(7, adults);
                pstmt.setInt(8, children);

                if (pstmt.executeUpdate() > 0) {
                    String confirmSql = "SELECT FirstName, LastName, RoomCode, RoomName, bedType, CheckIn, CheckOut, Adults, Kids, Rate " +
                                        "FROM lab7_reservations " +
                                        "JOIN lab7_rooms ON Room = RoomCode " +
                                        "WHERE CheckIn = ? and CheckOut = ? and room = ?";
                    try (PreparedStatement confirmPstmt = conn.prepareStatement(confirmSql)) {
                        confirmPstmt.setDate(1, checkin);
                        confirmPstmt.setDate(2, checkout);
                        confirmPstmt.setString(3, room);
                        ResultSet rs = confirmPstmt.executeQuery();
                        if (rs.next()) {
                            String cFname = rs.getString("FirstName");
                            String cLname = rs.getString("LastName");
                            String cRoomCode = rs.getString("RoomCode");
                            String cRoomName = rs.getString("RoomName");
                            String cBedType = rs.getString("BedType");
                            LocalDate cCheckIn = rs.getDate("CheckIn").toLocalDate();
                            LocalDate cCheckOut = rs.getDate("CheckOut").toLocalDate();
                            int cAdults = rs.getInt("Adults");
                            int cKids = rs.getInt("Kids");
                            int rate = rs.getInt("Rate");
                            long totalDays = DAYS.between(cCheckIn, cCheckOut);
                            Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
                                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;
                            long weekendDays = Stream.iterate(cCheckIn, date -> date.plusDays(1)).limit(totalDays)
                                    .filter(isWeekend).count();
                            long weekdays = totalDays - weekendDays;
                            double totalCharge = rate * weekdays + (rate * 1.1) * weekendDays;
                            System.out.println("Confirmation:\n" +
                                    cFname + " " + cLname + "\n" +
                                    cRoomName + " (" + cRoomCode + "), " + cBedType + " bed\n" +
                                    cAdults + " Adults, " + cKids + " Kids\n" +
                                    "Total Cost: " + totalCharge + "$");
                        }
                    }

                }

            }
        } catch (SQLException e) {
            System.out.println("Could not create reservation: " + e.getMessage().split(";")[0]);
        }
    }

    private static void reservationChange() {
        System.out.println("Change A Reservation");
        Scanner in = new Scanner(System.in);
        System.out.println("What is the code of the reservation that you want to change?");
        int reservation = Integer.parseInt(in.nextLine());
        System.out.println("New value for first name?");
        String fname = in.nextLine().toUpperCase();
        System.out.println("New value for last name?");
        String lname = in.nextLine().toUpperCase();
        System.out.println("New value for check in?");
        String sCheckin = in.nextLine();
        System.out.println("New value for check out?");
        String sCheckout = in.nextLine();
        System.out.println("New value for number of children in your party?");
        String sChildren = in.nextLine();
        System.out.println("New value for number of adults in your party?");
        String sAdults = in.nextLine();

        List<Object> values = new ArrayList<Object>();
        StringBuilder querySql = new StringBuilder("UPDATE lab7_reservations SET ");
        StringJoiner sj = new StringJoiner(", ");
        if (!fname.toUpperCase().equals("NO CHANGE")) {
            sj.add("FirstName = ?");
            values.add(fname);
        }
        if (!lname.toUpperCase().equals("NO CHANGE")) {
            sj.add("LastName = ?");
            values.add(lname);
        }
        if (!sCheckin.toUpperCase().equals("NO CHANGE")) {
            sj.add("CheckIn = ?");
            values.add(java.sql.Date.valueOf(sCheckin));
        }
        if (!sCheckout.toUpperCase().equals("NO CHANGE")) {
            sj.add("CheckOut = ?");
            values.add(java.sql.Date.valueOf(sCheckout));
        }
        if (!sChildren.toUpperCase().equals("NO CHANGE")) {
            sj.add("Kids = ?");
            values.add(Integer.valueOf(sChildren));
        }
        if (!sAdults.toUpperCase().equals("NO CHANGE")) {
            sj.add("Adults = ?");
            values.add(Integer.valueOf(sAdults));
        }
        querySql.append(sj.toString());
        querySql.append(" WHERE CODE = ?");

        try (Connection conn = DriverManager.getConnection(JDBC_URL,
                JDBC_USER,
                JDBC_PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(querySql.toString())) {
                for (int i = 0; i<values.size(); i++) {
                    Object cur = values.get(i);
                    if (cur instanceof Integer) {
                        pstmt.setInt(i+1, (int) cur);
                    }
                    else if (cur instanceof String) {
                        pstmt.setString(i+1, (String) cur);
                    } else if (cur instanceof Date) {
                        pstmt.setDate(i+1, (Date) cur);
                    }
                }
                pstmt.setInt(values.size()+1, reservation);
                long changeCount = pstmt.executeUpdate();
                System.out.println("changed "+changeCount);
            }

        } catch (SQLException e) {
            System.out.println("Could not update reservation: " + e.getMessage().split(";")[0]);
        }

    }

    private static void reservationCancellation() {
        Scanner in = new Scanner(System.in);
        System.out.println("Cancel a Reservation");
        System.out.print("Please enter your reservation number to cancel: ");
        int code = in.nextInt();
        String strCode = "" + code;
        try {
            // Look up reservation in DB
            try (Connection conn = DriverManager.getConnection(JDBC_URL,
                    JDBC_USER,
                    JDBC_PASSWORD)) {
                // Step 2: Construct SQL statement
                String selectSql = "SELECT CODE FROM lab7_reservations WHERE CODE = ?";

                // Step 4: Send SQL statement to DBMS
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                    pstmt.setInt(1, code);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        System.out.println("Could not find reservation " + strCode);
                        return;
                    }

                    System.out.println("Found reservation " + strCode);
                    System.out.print("Are you sure your would like to cancel? (y/Y): ");
                    char cancel = in.next().charAt(0);
                    System.out.println(cancel);
                    if (cancel == 'y' || cancel == 'Y') {
                        // Delete reservation from DB
                        rs.deleteRow();
                        System.out.println("Reservation " + strCode + " successfully canceled.");
                    } else {
                        System.out.println("Reservation " + strCode + " was not canceled.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not cancel reservation " + strCode);
        }
    }

    private static void revenueSummary() {
        System.out.println("Revenue Summary");
        try {
            // Look up reservation in DB
            try (Connection conn = DriverManager.getConnection(JDBC_URL,
                    JDBC_USER,
                    JDBC_PASSWORD)) {
                // Step 2: Construct SQL statement
                String selectSql = "WITH reservation_price AS (\n" +
                        "    SELECT *, DATEDIFF('DAY', CheckIn, CheckOut) * Rate Cost\n" +
                        "    FROM lab7_rooms Room\n" +
                        "             JOIN lab7_reservations R ON R.Room = Room.RoomCode\n" +
                        "), monthly_total AS (\n" +
                        "    SELECT RoomCode, RoomName, MONTH(CheckOut) Mth, SUM(Cost) MonthTot\n" +
                        "    FROM reservation_price\n" +
                        "    GROUP BY MONTH(CheckOut), RoomCode\n" +
                        ")\n" +
                        "select *\n" +
                        "from monthly_total\n" +
                        "ORDER BY RoomCode, Mth";

                // Step 4: Send SQL statement to DBMS
                try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                     ResultSet rs = stmt.executeQuery(selectSql)) {
                    //Print Header
                    System.out.format("%-5s | %-25s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s %n",
                            "CODE", "Name", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Total");
                    System.out.println(String.format("%200s", '-').replace(" ", "-"));
                    float[] totalTotals = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                    float totalTotal = 0;
                    while (rs.next()) {
                        // Loop through each room type
                        String currCode = rs.getString("RoomCode");
                        String code1;
                        String room = rs.getString("RoomName");
                        float total = 0;
                        float[] months = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                        while (true) {
                            code1 = rs.getString("RoomCode");
                            if (!currCode.equals(code1)) {
                                code1 = currCode;
                                rs.previous();
                                break;
                            }
                            int Mth = rs.getInt("Mth");
                            float monthTot = rs.getFloat("MonthTot");
                            // Set monthly totals
                            months[Mth - 1] = monthTot;
                            total += monthTot;

                            //Update totals
                            totalTotals[Mth - 1] += monthTot;
                            totalTotal += monthTot;
                            if (!rs.next()) break;
                        }
                        System.out.format("%-5s | %-25s %s %n", code1, room, totalsString(months, total));
                    }
                    System.out.println();
                    System.out.format("%-33s %s %n", "TOTAL PER MONTH", totalsString(totalTotals, totalTotal));
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load reservations.");
        }
    }

    private static String totalsString(float[] totalArr, float total) {
        StringBuilder totals = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            String totMonth = String.format("$%.2f", totalArr[i]);
            String padded = String.format("| %-10s ", totMonth);
            totals.append(padded);
        }
        String totMonth = String.format("$%.2f", total);
        return totals + String.format("| %-10s ", totMonth);
    }
}