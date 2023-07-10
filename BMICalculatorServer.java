import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BMICalculatorServer {
    private static final int PORT = 1234;
    private static final String DB_URL = "jdbc:sqlite:database.db";

    private ServerSocket serverSocket;
    private Connection connection;

    public BMICalculatorServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server socket initialized and listening on port " + PORT);
        } catch (IOException e) {
            System.out.println("Failed to initialize server socket: " + e.getMessage());
        }
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to the SQLite database");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket);
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    private void handleClientRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String height = in.readLine();
            String weight = in.readLine();

            double bmi = calculateBMI(height, weight);
            saveMeasurementToDatabase(height, weight, bmi);

            String bmiCategory = getBMICategory(bmi);
            out.println(bmi + " (" + bmiCategory + ")");

            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error handling client request: " + e.getMessage());
        }
    }

    private double calculateBMI(String height, String weight) {
        double heightInCm = Double.parseDouble(height);
        double weightInKg = Double.parseDouble(weight);
        double heightInMeters = heightInCm / 100.0;

        return weightInKg / (heightInMeters * heightInMeters);
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 25) {
            return "Healthy Weight";
        } else if (bmi >= 25 && bmi < 30) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    private void saveMeasurementToDatabase(String height, String weight, double bmi) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO measurements (height, weight, bmi) VALUES (?, ?, ?)");
            statement.setString(1, height);
            statement.setString(2, weight);
            statement.setDouble(3, bmi);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to save measurement to the database: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Disconnected from the database");
            }
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Server socket closed");
            }
        } catch (IOException | SQLException e) {
            System.out.println("Error closing server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        BMICalculatorServer server = new BMICalculatorServer();
        server.start();
        server.stop();
    }
}
//javac -cp ".:sqlite-jdbc-3.42.0.0.jar" BMICalculatorServer.java
//java -cp ".:sqlite-jdbc-3.42.0.0.jar" BMICalculatorServer
