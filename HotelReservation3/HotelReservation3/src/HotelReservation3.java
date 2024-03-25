import java.sql.*;
import java.util.*;

class Room {
    private final int roomNumber;
    private boolean isAvailable;

    public Room(int roomNumber) {
        this.roomNumber = roomNumber;
        this.isAvailable = true;
    }

    public Room(int roomNumber, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.isAvailable = isAvailable;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailability(boolean availability) {
        isAvailable = availability;
    }
}

class Reservation {
    private String guestName;
    private int roomNumber;
    private int numOfDays;

    public Reservation(String guestName, int roomNumber, int numOfDays) {
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.numOfDays = numOfDays;
    }

    public String getGuestName() {
        return guestName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getNumOfDays() {
        return numOfDays;
    }
}

 class Hotel {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_reservation";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "W7301@jqir#";

    private List<Room> rooms;
    private List<Reservation> reservations;

    public Hotel(int numRooms) throws SQLException {
    rooms = new ArrayList<>();
    reservations = new ArrayList<>();

    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM rooms");

        while (resultSet.next()) {
            int roomNumber = resultSet.getInt("number");
            boolean isAvailable = resultSet.getBoolean("is_available");
            rooms.add(new Room(roomNumber, isAvailable));
        }

        if (rooms.size() < numRooms) {
            for (int i = rooms.size() + 1; i <= numRooms; i++) {
                rooms.add(new Room(i));
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO rooms (number, is_available) VALUES (?, ?)")) {
                for (Room room : rooms.subList(rooms.size() - numRooms, rooms.size())) {
                    preparedStatement.setInt(1, room.getRoomNumber());
                    preparedStatement.setBoolean(2, true);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }
}
    public void displayAvailableRooms() {
        System.out.println("Available Rooms:");
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.println("Room Number: " + room.getRoomNumber());
            }
        }
    }

    private void saveReservationsToDatabase() throws SQLException {
    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO reservations (guest_name, room_number, num_of_days, reservation_date) VALUES (?, ?, ?, ?)");

        for (Reservation reservation : reservations) {
            preparedStatement.setString(1, reservation.getGuestName());
            preparedStatement.setInt(2, reservation.getRoomNumber());
            preparedStatement.setInt(3, reservation.getNumOfDays());
            preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            preparedStatement.addBatch();
        }

        preparedStatement.executeBatch();
    }
}
    
    public boolean makeReservation(String guestName, int roomNumber, int numOfDays) {
    Room room = findRoom(roomNumber);
    if (room != null && room.isAvailable()) {
        room.setAvailability(false);
        Reservation reservation = new Reservation(guestName, roomNumber, numOfDays);
        reservations.add(reservation);

        try {
            saveReservationsToDatabase();
            System.out.println("Reservation made successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving reservation to the database: " + e.getMessage());
            room.setAvailability(true);
            reservations.remove(reservation);
            return false;
        }
    } else {
        System.out.println("Room is not available for reservation.");
        return false;
    }
}

    private Room findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }
}

public class HotelReservation3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Hotel Reservation System!");

        Hotel hotel;
        try {
            hotel = new Hotel(10); // Creating a hotel with 10 rooms
        } catch (SQLException e) {
            System.out.println("Error initializing hotel: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Display Available Rooms");
            System.out.println("2. Make a Reservation");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    hotel.displayAvailableRooms();
                    break;
                case 2:
                    System.out.print("Enter your name: ");
                    String name = scanner.next();
                    System.out.print("Enter desired room number: ");
                    int roomNumber = scanner.nextInt();
                    System.out.print("Enter number of days for reservation: ");
                    int numOfDays = scanner.nextInt();
                    hotel.makeReservation(name, roomNumber, numOfDays);
                    break;
                case 3:
                    System.out.println("Thank you for using Hotel Reservation System. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
}
