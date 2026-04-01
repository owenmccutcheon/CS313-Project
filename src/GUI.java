
import java.awt.*;
import javax.swing.*;

public class GUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // try {
            //     Socket socket = new Socket("localhost", 5000);
            //     new ChatFrame(socket);
            // } catch (IOException e) {
            //     JOptionPane.showMessageDialog(null, "Could not connect to server");
            //     e.printStackTrace();
            // }
            new ChatFrame();
        });
    }

    static class ChatFrame extends JFrame {

        private JPanel streamingPanel;
        private JTextArea textBox;
        // private Socket socket;
        // private PrintWriter out;
        // private BufferedReader in;

        public ChatFrame(/*Socket socket */) {
            // this.socket = socket;

            setTitle("Chat UI");
            setSize(1000, 700);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            setLayout(new BorderLayout());

            add(streamingHeader(), BorderLayout.NORTH);
            add(makeCenterArea(), BorderLayout.CENTER);
            add(inputTextBox(), BorderLayout.SOUTH);

            //test here to see look with x amount of video feeds
            updateStreamingLayout(0);

            setVisible(true);

            // try {
            //     out = new PrintWriter(socket.getOutputStream(), true);
            //     in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //     // Listen for incoming messages
            //     new Thread(() -> {
            //         try {
            //             String msg;
            //             while ((msg = in.readLine()) != null) {
            //                 String inMessage = msg;
            //                 SwingUtilities.invokeLater(() -> textBox.append(inMessage + "\n"));
            //             }
            //         } catch (IOException e) {
            //             e.printStackTrace();
            //         }
            //     }).start();
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
        }

        private JPanel streamingHeader() {
            streamingPanel = new JPanel();
            streamingPanel.setPreferredSize(new Dimension(0, 180));
            streamingPanel.setBackground(Color.BLACK);
            return streamingPanel;
        }

        private void updateStreamingLayout(int count) {
            streamingPanel.removeAll();

            if (count == 0) {
                streamingPanel.setLayout(new BorderLayout());
                JLabel emptyLabel = new JLabel("No active video feeds", SwingConstants.CENTER);
                emptyLabel.setForeground(Color.WHITE);
                streamingPanel.add(emptyLabel, BorderLayout.CENTER);
            } else {
                int rows = (int) Math.ceil(Math.sqrt(count));
                int cols = (int) Math.ceil((double) count / rows);

                streamingPanel.setLayout(new GridLayout(rows, cols, 5, 5));

                for (int i = 0; i < count; i++) {
                    streamingPanel.add(makeStreamingFeed("Feed " + (i + 1)));
                }
            }

            streamingPanel.revalidate();
            streamingPanel.repaint();
        }

        private JPanel makeStreamingFeed(String name) {
            JPanel feed = new JPanel(new BorderLayout());
            feed.setBackground(Color.DARK_GRAY);
            feed.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            JLabel label = new JLabel(name, SwingConstants.CENTER);
            label.setForeground(Color.WHITE);

            feed.add(label, BorderLayout.CENTER);
            return feed;
        }

        //main section (users and recieved messages)
        private JSplitPane makeCenterArea() {

            //user list
            DefaultListModel<String> userModel = new DefaultListModel<>();
            userModel.addElement("Test 1");
            userModel.addElement("Test 3");
            userModel.addElement("Test 2");

            JList<String> userList = new JList<>(userModel);
            JScrollPane userScroll = new JScrollPane(userList);

            //recieved messages area
            textBox = new JTextArea();
            textBox.setEditable(false);
            textBox.setLineWrap(true);
            textBox.setWrapStyleWord(true);

            JScrollPane messageScroll = new JScrollPane(textBox);

            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    userScroll,
                    messageScroll
            );

            splitPane.setResizeWeight(0.2);
            splitPane.setDividerLocation(0.2);

            return splitPane;
        }

        //text input
        private JPanel inputTextBox() {
            JPanel feed = new JPanel(new BorderLayout(10, 10));
            feed.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JTextField inputField = new JTextField();

            JButton sendButton = new JButton("Send");
            JButton plusButton = new JButton("+");

            sendButton.addActionListener(e -> {
                String message = inputField.getText().trim();

                if (message.isEmpty() == false) {
                    //sendMessageGUI(message);
                    inputField.setText("");
                }
            });

            plusButton.addActionListener(e -> {
                textBox.append("you clicked a button i haven't made yet");
                //sendFileGUI();
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(sendButton);
            buttonPanel.add(plusButton);

            feed.add(inputField, BorderLayout.CENTER);
            feed.add(buttonPanel, BorderLayout.EAST);

            return feed;
        }

        // private void sendMessageGUI(String message) {
        //     if (out != null) {
        //         out.println(message);
        //         out.flush();
        //         textBox.append("Me: " + message + "\n");
        //     }
        // }
    }
}
