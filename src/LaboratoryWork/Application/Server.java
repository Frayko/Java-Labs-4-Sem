package LaboratoryWork.Application;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class Server {
    public static LinkedList<newUser> usersList = new LinkedList<>();
    public static void main(String[] args) {

    }

    private class ServerConsole extends JDialog {
        JTextArea info_area;
        JTextField input_area;
        int dialog_window_width = 500;
        int dialog_window_height = 300;

        private ServerConsole() {
            this.setTitle("Консоль");
            this.setIconImage(new ImageIcon("src/LaboratoryWork/Assets/console_icon.png").getImage());
            this.setBackground(Color.white);
            this.setForeground(Color.DARK_GRAY);
            this.setBounds(50, 50, dialog_window_width, dialog_window_height);
            this.setLayout(new BorderLayout());
            this.setModal(false);
            this.setAlwaysOnTop(true);
            this.setResizable(false);
            this.setFocusable(true);

            info_area = new JTextArea(">>> Сервер запущен");
            info_area.setEditable(false);
            info_area.setFont(new Font("Open Sans", Font.ITALIC, 12));
            add(info_area, BorderLayout.CENTER);

            input_area = new JTextField();
            input_area.setPreferredSize(new Dimension(dialog_window_width, 25));
            input_area.setRequestFocusEnabled(true);
            input_area.addActionListener(actionEvent -> {
                String command = input_area.getText();
                info_area.append(command + "\n");
                if (command.startsWith("help")) {
                    info_area.append(">>> Доступные команды:\n" +
                            "clear - очистить экран\n" +
                            "close - закрыть консоль\n" +
                            "users - узнать текущих пользователей в сети\n");
                } else if (command.startsWith("clear")) {
                    info_area.setText("");
                } else if (command.startsWith("users")) {
                    info_area.append(getName());
                } else if (command.startsWith("close")) {
                    this.dispose();
                } else {
                    info_area.append(">>> Неизвестная команда. Узнать все доступные команды: help\n");
                }
                input_area.setText("");
            });
            this.add(input_area, BorderLayout.SOUTH);
        }
    }
}
