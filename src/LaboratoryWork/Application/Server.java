package LaboratoryWork.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    public static LinkedList<newUser> usersList = new LinkedList<>();
    static ServerConsole serverConsole;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        try {
            int port = 2000;
            serverConsole = new ServerConsole();
            ServerSocket serverSocket = new ServerSocket(port);

            Thread serverAccepter = new Thread(() -> {
                while(true) {
                    try {
                        Socket socket = serverSocket.accept();
                        newUser nUser = new newUser(socket);
                        nUser.start();
                        serverConsole.info_area.append(">>> " + nUser.getUserName() + " подключился\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            serverAccepter.setDaemon(true);
            serverAccepter.setName("Server Accepter");
            serverAccepter.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ServerConsole extends JDialog {
        final JTextArea info_area;
        private final JTextField input_area;
        private final int dialog_window_width = 500;
        private final int dialog_window_height = 300;

        public ServerConsole() {
            this.setTitle("Консоль сервера");
            this.setIconImage(new ImageIcon("src/LaboratoryWork/Assets/users_icon.png").getImage());
            this.setBackground(Color.white);
            this.setForeground(Color.DARK_GRAY);
            this.setBounds(50, 50, dialog_window_width, dialog_window_height);
            this.setLayout(new BorderLayout());
            this.setModal(false);
            this.setResizable(true);
            this.setAlwaysOnTop(true);
            this.setFocusable(true);

            info_area = new JTextArea(">>> Сервер запущен\n");
            info_area.setEditable(false);
            info_area.setFont(new Font("Open Sans", Font.ITALIC, 12));
            add(info_area, BorderLayout.CENTER);

            input_area = new JTextField();
            input_area.setPreferredSize(new Dimension(dialog_window_width, 25));
            input_area.setRequestFocusEnabled(true);
            info_area.setLineWrap(true);
            info_area.setWrapStyleWord(true);
            input_area.addActionListener(actionEvent -> {
                String command = input_area.getText();
                info_area.append(command + "\n");
                switch (command) {
                    case "help" -> info_area.append(">>> Доступные команды:\n" +
                            ">>> clear - очистить экран\n" +
                            ">>> close - закрыть консоль\n" +
                            ">>> users - узнать текущих пользователей в сети\n");
                    case "clear" -> info_area.setText("");
                    case "close" -> dispose();
                    case "users" -> {
                        info_area.append(">>> Список подключенных пользователей:\n");
                        if(usersList.size() == 0)
                            info_area.append("Пусто\n");
                        else
                            for(newUser user : usersList)
                                info_area.append("Имя: " + user.getUserName() + "    id: " + user.getID() + "\n");
                    }
                    case "" -> {}
                    default -> info_area.append(">>> Неизвестная команда. Узнать все доступные команды: help\n");
                }
                input_area.setText("");
            });

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    dispose();

                }
            });

            this.add(new JScrollPane(info_area), BorderLayout.CENTER);
            this.add(input_area, BorderLayout.SOUTH);
            this.setVisible(true);
        }
    }
}
