import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class BMICalculatorClient extends JFrame {
    private JTextField heightField;
    private JTextField weightField;
    private JButton calculateButton;
    private JTextArea displayArea;

    private final String serverAddress = "localhost";
    private final int serverPort = 1234;

    public BMICalculatorClient() {
        super("BMI Calculator");
        initializeGUI();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setSize(300, 200);
    }

    private void initializeGUI() {
        heightField = new JTextField(10);
        weightField = new JTextField(10);
        calculateButton = new JButton("Calculate BMI");
        displayArea = new JTextArea(5, 20);
        displayArea.setEditable(false);

        calculateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String height = heightField.getText();
                String weight = weightField.getText();
                sendMeasurementToServer(height, weight);
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Height (cm): "));
        panel.add(heightField);
        panel.add(new JLabel("Weight (kg): "));
        panel.add(weightField);
        panel.add(calculateButton);
        this.add(panel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    private void sendMeasurementToServer(String height, String weight) {
        try {
            Client client = new Client(serverAddress, serverPort);
            client.sendMeasurement(height, weight, new ResponseHandler() {
                public void handleSuccessResponse(String response) {
                    displayArea.setText("BMI: " + response);
                }

                public void handleErrorResponse() {
                    displayArea.setText("Error occurred while calculating BMI");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BMICalculatorClient();
            }
        });
    }

    private interface ResponseHandler {
        void handleSuccessResponse(String response);

        void handleErrorResponse();
    }

    private class Client {
        private String serverAddress;
        private int serverPort;

        public Client(String serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        public void sendMeasurement(String height, String weight, ResponseHandler responseHandler) {
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(height);
                out.println(weight);
                String response = in.readLine();

                if (response.equals("Error")) {
                    responseHandler.handleErrorResponse();
                } else {
                    responseHandler.handleSuccessResponse(response);
                }

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
