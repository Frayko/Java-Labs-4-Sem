package LaboratoryWork.Application;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.net.Socket;

import LaboratoryWork.Habitat.Fish.Fish;
import LaboratoryWork.Habitat.Fish.FishArray;
import LaboratoryWork.Habitat.Habitat;
import LaboratoryWork.Habitat.Fish.Objects.GoldenFish;
import LaboratoryWork.Habitat.Status;

public class Application extends JFrame {
    Dimension screenSize = getToolkit().getScreenSize();
    private final int window_width;
    private final int window_height;
    private String userName;
    private final String configFile;
    private final String directory;

    private Socket socket;
    private final String host = "localhost";
    private final int port = 2000;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private static final LinkedList<String> usersList = new LinkedList<>();

    private final Menu menu;
    private final ControlPanel controlPanel;

    private boolean isPausedGoldenAI = false;
    private boolean isPausedGuppiesAI = false;
    private boolean isVisible = true;
    private boolean isAllowModalInfo = true;
    private boolean isConnected = false;

    private final Habitat habitat;

    public Application(int width, int height, Habitat h) {
        this.habitat = h;
        this.window_width = width;
        this.window_height = height;

        new userDialog();
        this.setTitle("Генератор рыбок (Текущий пользователь: " + userName + ")");
        this.setIconImage(new ImageIcon("src/LaboratoryWork/Assets/window_icon.png").getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setBounds(screenSize.width/2 - window_width/2,
                       screenSize.height/2 - window_height/2,
                          window_width, window_height);
        this.setBackground(Color.white);
        this.setLocationRelativeTo(null);
        this.setFocusable(true);
        this.setResizable(false);

        controlPanel = new ControlPanel();

        this.add(habitat, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.EAST);
        this.setJMenuBar(menu = new Menu());

        directory = "src/LaboratoryWork/Configs/" + userName;
        configFile = directory + "/config.txt";

        try {
            readConfig(configFile);
        }
        catch (FileNotFoundException fnf) {
            try {
                new File(directory).mkdir();
                FileOutputStream fos = new FileOutputStream(configFile);
                fos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_B -> {
                        if(habitat.getStatus() == Status.ВЫКЛ)
                            start();
                    }
                    case KeyEvent.VK_E -> {
                        if(habitat.getStatus() != Status.ВЫКЛ) {
                            if (isAllowModalInfo)
                                new ModalInfoDialog();
                            else
                                stop();
                        }
                    }
                    case KeyEvent.VK_T -> show_hide_timer();
                    case KeyEvent.VK_P -> {
                        if(habitat.getStatus() != Status.ВЫКЛ)
                            resume();
                    }
                    case KeyEvent.VK_ESCAPE -> {
                        try {
                            writeConfig(configFile);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        System.exit(0);
                    }
                }
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    writeConfig(configFile);
                } catch (IOException ioException) {
                    System.out.println("Ошибка записи конфигурационного файла!\n");
                    ioException.printStackTrace();
                }
            }
        });
        this.setVisible(true);
    }

    private void start() {
        if(habitat.getStatus() == Status.ВЫКЛ) {
            habitat.begin_simulation();
            controlPanel.buttonPanel.start_button.setEnabled(false);
            menu.start_item.setEnabled(false);
            controlPanel.buttonPanel.stop_button.setEnabled(true);
            menu.stop_item.setEnabled(true);
            controlPanel.buttonPanel.pause_button.setEnabled(true);
            menu.pause_item.setEnabled(true);

            controlPanel.buttonPanel.goldenAI_button.setEnabled(true);
            controlPanel.buttonPanel.guppiesAI_button.setEnabled(true);

            runTimer();
        }
    }

    private void pause() {
        habitat.pause_simulation();
        controlPanel.buttonPanel.pause_button.setText("Продолжить");
        menu.pause_item.setText("Продолжить");
    }

    private void continue_() {
        habitat.continue_simulation();
        update_status_goldenAI();
        update_status_guppiesAI();
        controlPanel.buttonPanel.pause_button.setText("Пауза");
        menu.pause_item.setText("Пауза");
    }

    private void resume() {
        if(habitat.getStatus() == Status.ВКЛ)
            pause();
        else if(habitat.getStatus() == Status.ПАУЗА) {
            continue_();
        }
    }

    private void stop() {
        if(habitat.getStatus() != Status.ВЫКЛ) {
            habitat.end_simulation();
            controlPanel.buttonPanel.start_button.setEnabled(true);
            menu.start_item.setEnabled(true);
            controlPanel.buttonPanel.stop_button.setEnabled(false);
            menu.stop_item.setEnabled(false);
            controlPanel.buttonPanel.pause_button.setEnabled(false);
            menu.pause_item.setEnabled(false);

            controlPanel.buttonPanel.goldenAI_button.setEnabled(false);
            controlPanel.buttonPanel.guppiesAI_button.setEnabled(false);

            isPausedGoldenAI = false;
            isPausedGuppiesAI = false;
            controlPanel.buttonPanel.goldenAI_button.setText("Заморозить ИИ (З)");
            controlPanel.buttonPanel.guppiesAI_button.setText("Заморозить ИИ (Г)");

            controlPanel.buttonPanel.pause_button.setText("Пауза");
            menu.pause_item.setText("Пауза");
        }
    }

    private void update_status_goldenAI() {
        if(isPausedGoldenAI) {
            habitat.pause_goldenAI();
            controlPanel.buttonPanel.goldenAI_button.setText("Разморозить ИИ (З)");
        }
        else {
            habitat.resume_goldenAI();
            controlPanel.buttonPanel.goldenAI_button.setText("Заморозить ИИ (З)");
        }
    }

    private void update_status_guppiesAI() {
        if(isPausedGuppiesAI) {
            habitat.pause_guppiesAI();
            controlPanel.buttonPanel.guppiesAI_button.setText("Разморозить ИИ (Г)");
        }
        else {
            habitat.resume_guppiesAI();
            controlPanel.buttonPanel.guppiesAI_button.setText("Заморозить ИИ (Г)");
        }
    }

    private void connect() throws IOException {
        socket = new Socket(host, port);
        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());
        isConnected = true;
        controlPanel.networkPanel.send_button.setEnabled(true);
        controlPanel.networkPanel.clients.setEnabled(true);
        controlPanel.networkPanel.action_button.setText("Отключиться");
    }

    private void disconnect() throws IOException {
        socket.close();
        inStream.close();
        outStream.close();
    }

    private void show_hide_timer() {
        isVisible = !isVisible;
        controlPanel.timerPanel.setVisible(isVisible);
        if(isVisible) {
            controlPanel.radioButtonPanel.show_timer_button.setSelected(true);
            controlPanel.radioButtonPanel.hide_timer_button.setSelected(false);
        }
        else {
            controlPanel.radioButtonPanel.hide_timer_button.setSelected(true);
            controlPanel.radioButtonPanel.show_timer_button.setSelected(false);
        }
    }

    private class ControlPanel extends JPanel {
        TimerPanel timerPanel;
        ButtonPanel buttonPanel;
        CheckBoxPanel checkBoxPanel;
        RadioButtonPanel radioButtonPanel;
        PeriodPanel periodPanel;
        LivePanel livePanel;
        ProbablyPanel probablyPanel;
        PriorityPanel priorityPanel;
        NetworkPanel networkPanel;

        Color background = Color.PINK;

        public ControlPanel() {
            setLayout(new GridBagLayout());
            setBackground(background);
            setPreferredSize(new Dimension(280, getHeight()));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.insets = new Insets(-7,0,0,0);

            add(timerPanel = new TimerPanel(), gbc);
            gbc.gridy++;
            gbc.insets = new Insets(2,2,2,2);

            add(periodPanel = new PeriodPanel(), gbc);
            gbc.gridy++;
            add(livePanel = new LivePanel(), gbc);
            gbc.gridy++;
            add(probablyPanel = new ProbablyPanel(), gbc);
            gbc.gridy++;
            add(priorityPanel = new PriorityPanel(), gbc);
            gbc.gridy++;
            add(networkPanel = new NetworkPanel(), gbc);
            gbc.gridy++;
            add(radioButtonPanel = new RadioButtonPanel(), gbc);
            gbc.gridy++;
            add(checkBoxPanel = new CheckBoxPanel(), gbc);
            gbc.gridy++;
            add(buttonPanel = new ButtonPanel(), gbc);
        }

        private class ButtonPanel extends JPanel {
            private final JButton start_button;
            private final JButton stop_button;
            private final JButton pause_button;
            private final JButton show_objects;
            private final JButton goldenAI_button;
            private final JButton guppiesAI_button;

            ButtonPanel() {
                setLayout(new GridLayout(3,2));
                setBackground(background);

                Dimension button_size = new Dimension(135,30);

                start_button = new JButton("Начать");
                start_button.setPreferredSize(button_size);
                start_button.setBackground(Color.lightGray);
                start_button.setEnabled(true);
                start_button.addActionListener(actionEvent -> {
                    start();
                    Application.this.requestFocus(true);
                });
                start_button.setToolTipText("Запуск симуляции");

                stop_button = new JButton("Закончить");
                stop_button.setPreferredSize(button_size);
                stop_button.setBackground(Color.lightGray);
                stop_button.setEnabled(false);
                stop_button.addActionListener(actionEvent -> {
                    if(isAllowModalInfo)
                        new ModalInfoDialog();
                    else
                        stop();
                    Application.this.requestFocus(true);
                });
                stop_button.setToolTipText("Остановка симуляции");

                show_objects = new JButton("Текущие объекты");
                show_objects.setPreferredSize(button_size);
                show_objects.setBackground(Color.lightGray);
                show_objects.setEnabled(true);
                show_objects.addActionListener(actionEvent -> {
                    if(habitat.getStatus() == Status.ВКЛ)
                        pause();

                    StringBuilder buf = new StringBuilder();

                    for(Fish fish : FishArray.getFishArray().getList()) {
                        for(Map.Entry<Integer, Double> entry : FishArray.getFishArray().getMap().entrySet()) {
                            if(entry.getKey() == fish.hashCode()) {
                                buf.append("Тип рыбы: "); buf.append(fish instanceof GoldenFish ? "Золотая" : "Гуппи    ");
                                buf.append("   Время рождения(сек): "); buf.append(entry.getValue() / 10.);
                                buf.append("   id рыбы: "); buf.append(entry.getKey());
                                buf.append("\n");
                            }
                        }
                    }

                    String[] options = { "ОК" };
                    JOptionPane.showOptionDialog(
                            null,
                            buf.toString(),
                            "Текущие объекты",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[0]
                    );
                    resume();
                    Application.this.requestFocus(true);
                });
                show_objects.requestFocus(false);
                show_objects.setToolTipText("Показать все объекты");

                pause_button = new JButton("Пауза");
                pause_button.setPreferredSize(button_size);
                pause_button.setBackground(Color.lightGray);
                pause_button.setEnabled(false);
                pause_button.addActionListener(actionEvent -> {
                    resume();
                    Application.this.requestFocus(true);
                });
                pause_button.setToolTipText("Пауза");

                goldenAI_button = new JButton("Заморозить ИИ (З)");
                goldenAI_button.setPreferredSize(button_size);
                goldenAI_button.setBackground(Color.lightGray);
                goldenAI_button.setEnabled(false);
                goldenAI_button.addActionListener(actionEvent -> {
                    isPausedGoldenAI = !isPausedGoldenAI;
                    update_status_goldenAI();
                    Application.this.requestFocus(true);
                });
                goldenAI_button.setToolTipText("ВКЛ/ВЫКЛ ИИ золотой рыбки");

                guppiesAI_button = new JButton("Заморозить ИИ (Г)");
                guppiesAI_button.setPreferredSize(button_size);
                guppiesAI_button.setBackground(Color.lightGray);
                guppiesAI_button.setEnabled(false);
                guppiesAI_button.addActionListener(actionEvent -> {
                    isPausedGuppiesAI = !isPausedGuppiesAI;
                    update_status_guppiesAI();
                    Application.this.requestFocus(true);
                });
                guppiesAI_button.setToolTipText("ВКЛ/ВЫКЛ ИИ рыбки гуппи");

                add(start_button);
                add(stop_button);
                add(show_objects);
                add(pause_button);
                add(goldenAI_button);
                add(guppiesAI_button);
            }
        }

        private class CheckBoxPanel extends JPanel {
            JCheckBox modal_info_checkbox;

            CheckBoxPanel() {
                setLayout(new GridLayout(1,1));
                setBackground(background);

                modal_info_checkbox = new JCheckBox("Показывать информацию после остановки");
                modal_info_checkbox.setSelected(true);
                modal_info_checkbox.setBackground(background);
                modal_info_checkbox.addActionListener(actionEvent -> {
                    isAllowModalInfo = !isAllowModalInfo;
                    Application.this.requestFocus(true);
                });
                add(modal_info_checkbox);
                modal_info_checkbox.setToolTipText("Включить/Выключить отображение информации");
            }
        }

        private class RadioButtonPanel extends JPanel {
            private final JRadioButton show_timer_button;
            private final JRadioButton hide_timer_button;

            RadioButtonPanel() {
                setLayout(new GridLayout(2,1));
                setBackground(background);

                show_timer_button = new JRadioButton("Показывать время симуляции");
                show_timer_button.setBackground(background);
                show_timer_button.addActionListener(actionEvent -> {
                    isVisible = true;
                    timerPanel.setVisible(true);
                    Application.this.requestFocus(true);
                });
                add(show_timer_button);

                hide_timer_button = new JRadioButton("Скрывать время симуляции");
                hide_timer_button.setBackground(background);
                hide_timer_button.addActionListener(actionEvent -> {
                    isVisible = false;
                    timerPanel.setVisible(false);
                    Application.this.requestFocus(true);
                });
                add(hide_timer_button);

                ButtonGroup timer_buttons = new ButtonGroup();
                timer_buttons.add(show_timer_button);
                timer_buttons.add(hide_timer_button);
                timer_buttons.setSelected(show_timer_button.getModel(), true);
            }
        }

        private class PeriodPanel extends JPanel {
            private final JTextField golden_text_field_N;
            private final JTextField guppies_text_field_N;

            public PeriodPanel() {
                setLayout(new GridBagLayout());
                setBackground(background);
                setPreferredSize(new Dimension(270, 50));

                TitledBorder border = new TitledBorder("Период рождения");
                border.setTitleFont(new Font("Open Sans", Font.BOLD, 14));
                border.setBorder(BorderFactory.createLineBorder(Color.black, 2));
                setBorder(border);

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1;
                c.weighty = 1;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                add(new JLabel("Золотых:"), c);
                c.gridx++;
                c.ipadx = 30;
                golden_text_field_N = new JTextField(Double.toString(habitat.getN1()));
                golden_text_field_N.setHorizontalAlignment(JTextField.RIGHT);
                golden_text_field_N.setPreferredSize(new Dimension(30, 20));
                golden_text_field_N.addActionListener(actionEvent -> {
                    try {
                        habitat.setN1(Double.parseDouble(golden_text_field_N.getText()));
                        Application.this.requestFocus(true);
                        if(habitat.getN1() < 0) {
                            throw new Exception();
                        }
                        golden_text_field_N.setText(Double.toString(habitat.getN1()));
                    }
                    catch (Exception e) {
                        habitat.setN1(0.5);
                        golden_text_field_N.setText(Double.toString(habitat.getN1()));
                        new ErrorDialog("Введено неверное значение...");
                    }
                });
                add(golden_text_field_N, c);
                c.gridx++;
                c.ipadx = 0;
                add(new JLabel("/сек"), c);
                c.gridx++;
                add(new JLabel("   "), c);
                c.gridx++;

                add(new JLabel("Гуппи:"), c);
                c.gridx++;
                c.ipadx = 30;
                guppies_text_field_N = new JTextField(Double.toString(habitat.getN2()));
                guppies_text_field_N.setHorizontalAlignment(JTextField.RIGHT);
                guppies_text_field_N.setPreferredSize(new Dimension(30, 20));
                guppies_text_field_N.addActionListener(actionEvent -> {
                    try {
                        habitat.setN2(Double.parseDouble(guppies_text_field_N.getText()));
                        Application.this.requestFocus(true);
                        if(habitat.getN2() < 0) {
                            throw new Exception();
                        }
                        guppies_text_field_N.setText(Double.toString(habitat.getN2()));
                    }
                    catch (Exception e) {
                        habitat.setN1(0.3);
                        guppies_text_field_N.setText(Double.toString(habitat.getN1()));
                        new ErrorDialog("Введено неверное значение...");
                    }
                });
                add(guppies_text_field_N, c);
                c.gridx++;
                c.ipadx = 0;
                add(new JLabel("/сек"), c);
            }
        }

        private class LivePanel extends JPanel {
            private final JTextField golden_text_field_TTL;
            private final JTextField guppies_text_field_TTL;

            public LivePanel() {
                setLayout(new GridBagLayout());
                setBackground(background);
                setPreferredSize(new Dimension(270, 50));

                TitledBorder border = new TitledBorder("Время жизни");
                border.setTitleFont(new Font("Open Sans", Font.BOLD, 14));
                border.setBorder(BorderFactory.createLineBorder(Color.black, 2));
                setBorder(border);

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1;
                c.weighty = 1;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                add(new JLabel("Золотых:"), c);
                c.gridx++;
                c.ipadx = 30;
                golden_text_field_TTL = new JTextField(Double.toString(habitat.getGoldenTTL()));
                golden_text_field_TTL.setHorizontalAlignment(JTextField.RIGHT);
                golden_text_field_TTL.setPreferredSize(new Dimension(30, 20));
                golden_text_field_TTL.addActionListener(actionEvent -> {
                    try {
                        habitat.setGoldenTTL(Double.parseDouble(golden_text_field_TTL.getText()));
                        Application.this.requestFocus(true);
                        if(habitat.getGoldenTTL() < 0) {
                            throw new Exception();
                        }
                        golden_text_field_TTL.setText(Double.toString(habitat.getGoldenTTL()));
                    }
                    catch (Exception e) {
                        habitat.setGoldenTTL(1.0);
                        golden_text_field_TTL.setText(Double.toString(habitat.getGoldenTTL()));
                        new ErrorDialog("Введено неверное значение...");
                    }
                });
                add(golden_text_field_TTL, c);
                c.gridx++;
                c.ipadx = 0;
                add(new JLabel("сек."), c);
                c.gridx++;
                add(new JLabel("   "), c);
                c.gridx++;

                add(new JLabel("Гуппи:"), c);
                c.gridx++;
                c.ipadx = 30;
                guppies_text_field_TTL = new JTextField(Double.toString(habitat.getGuppiesTTL()));
                guppies_text_field_TTL.setHorizontalAlignment(JTextField.RIGHT);
                guppies_text_field_TTL.setPreferredSize(new Dimension(30, 20));
                guppies_text_field_TTL.addActionListener(actionEvent -> {
                    try {
                        habitat.setGuppiesTTL(Double.parseDouble(guppies_text_field_TTL.getText()));
                        Application.this.requestFocus(true);
                        if(habitat.getGuppiesTTL() < 0) {
                            throw new Exception();
                        }
                        guppies_text_field_TTL.setText(Double.toString(habitat.getGuppiesTTL()));
                    }
                    catch (Exception e) {
                        habitat.setGuppiesTTL(2.0);
                        guppies_text_field_TTL.setText(Double.toString(habitat.getGuppiesTTL()));
                        new ErrorDialog("Введено неверное значение...");
                    }
                });
                add(guppies_text_field_TTL, c);
                c.gridx++;
                c.ipadx = 0;
                add(new JLabel("сек."), c);
            }
        }

        private class PriorityPanel extends JPanel {
            private final JComboBox<String> goldenFishPriority;
            private final JComboBox<String> guppiesFishPriority;

            PriorityPanel() {
                setLayout(new GridBagLayout());
                setBackground(background);
                setPreferredSize(new Dimension(270, 80));

                TitledBorder border = new TitledBorder("Приоритет ИИ");
                border.setTitleFont(new Font("Open Sans", Font.BOLD, 14));
                border.setBorder(BorderFactory.createLineBorder(Color.black, 2));
                setBorder(border);

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                String[] items = {
                    "Низкий",
                    "Нормальный",
                    "Высокий"
                };

                add(new JLabel("Золотых"), c);
                c.gridx++;
                goldenFishPriority = new JComboBox<>(items);
                if(habitat.getGoldenFishAIPriority() == 5) {
                    goldenFishPriority.setSelectedItem("Нормальный");
                }
                else if(habitat.getGoldenFishAIPriority() == 1) {
                    goldenFishPriority.setSelectedItem("Низкий");
                }
                else if(habitat.getGoldenFishAIPriority() == 10) {
                    goldenFishPriority.setSelectedItem("Высокий");
                }
                goldenFishPriority.addActionListener(actionEvent -> {
                    if(goldenFishPriority.getSelectedItem() == "Нормальный") {
                        habitat.setGoldenFishAIPriority(5);
                        Application.this.requestFocus(true);
                    }
                    else if (goldenFishPriority.getSelectedItem() == "Низкий") {
                        habitat.setGoldenFishAIPriority(1);
                        Application.this.requestFocus(true);
                    }
                    else if (goldenFishPriority.getSelectedItem() == "Высокий") {
                        habitat.setGoldenFishAIPriority(10);
                        Application.this.requestFocus(true);
                    }
                });
                add(goldenFishPriority, c);
                c.gridy++;
                c.gridx = 0;

                add(new JLabel("Гуппи"), c);
                c.gridx++;
                guppiesFishPriority = new JComboBox<>(items);
                if(habitat.getGuppiesFishAIPriority() == 5) {
                    guppiesFishPriority.setSelectedItem("Нормальный");
                }
                else if(habitat.getGuppiesFishAIPriority() == 1) {
                    guppiesFishPriority.setSelectedItem("Низкий");
                }
                else if(habitat.getGuppiesFishAIPriority() == 10) {
                    guppiesFishPriority.setSelectedItem("Высокий");
                }
                guppiesFishPriority.addActionListener(actionEvent -> {
                    if(guppiesFishPriority.getSelectedItem() == "Нормальный") {
                        habitat.setGuppiesFishAIPriority(5);
                        Application.this.requestFocus(true);
                    }
                    else if (guppiesFishPriority.getSelectedItem() == "Низкий") {
                        habitat.setGuppiesFishAIPriority(1);
                        Application.this.requestFocus(true);
                    }
                    else if (guppiesFishPriority.getSelectedItem() == "Высокий") {
                        habitat.setGuppiesFishAIPriority(10);
                        Application.this.requestFocus(true);
                    }
                });
                add(guppiesFishPriority, c);
            }
        }

        private class ProbablyPanel extends JPanel {
            ComboBoxPanel comboBoxPanel;
            SliderPanel sliderPanel;

            ProbablyPanel() {
                setLayout(new GridBagLayout());
                setBackground(background);
                setPreferredSize(new Dimension(270, 110));

                TitledBorder border = new TitledBorder("Вероятнось рождения");
                border.setTitleFont(new Font("Open Sans", Font.BOLD, 14));
                border.setBorder(BorderFactory.createLineBorder(Color.black, 2));
                setBorder(border);

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                add(comboBoxPanel = new ComboBoxPanel(), c);
                c.gridy++;
                c.ipadx = 150;
                add(sliderPanel = new SliderPanel(), c);
            }
        }

        private class ComboBoxPanel extends JPanel {
            private final JComboBox<String> golden_combo_box;

            public ComboBoxPanel() {
                setBackground(background);

                add(new JLabel("Золотых: "));

                golden_combo_box = new JComboBox<>(items);
                golden_combo_box.setSelectedItem(Integer.toString(habitat.getP1()));
                golden_combo_box.addActionListener(actionEvent -> {
                    habitat.setP1(Integer.parseInt(items[golden_combo_box.getSelectedIndex()]));
                    Application.this.requestFocus(true);
                });
                add(golden_combo_box);
            }

            String[] items = {
                    "0",
                    "10",
                    "20",
                    "30",
                    "40",
                    "50",
                    "60",
                    "70",
                    "80",
                    "90",
                    "100"
            };
        }

        private class SliderPanel extends JPanel {
            private final JSlider guppies_slider;

            public SliderPanel() {
                setBackground(background);

                add(new JLabel("Гуппи: "));

                guppies_slider = new JSlider(0,100, habitat.getP2());
                guppies_slider.setBackground(background);
                guppies_slider.setPreferredSize(new Dimension(170, 50));
                guppies_slider.setFont(new Font("Times New Roman", Font.PLAIN, 9));
                guppies_slider.setMajorTickSpacing(10);
                guppies_slider.setPaintLabels(true);
                guppies_slider.setName("Гуппи");
                guppies_slider.setPaintTicks(true);
                guppies_slider.addChangeListener(actionEvent -> {
                    habitat.setP2(guppies_slider.getValue());
                    Application.this.requestFocus(true);
                });
                add(guppies_slider);
            }
        }

        private class NetworkPanel extends JPanel {
            private final JButton action_button;
            private final JButton send_button;
            private final JComboBox<String> clients;
            private ClientUpdater clientUpdater;

            NetworkPanel() {
                setLayout(new GridBagLayout());
                setBackground(background);
                setPreferredSize(new Dimension(270, 90));

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                TitledBorder border = new TitledBorder("Сервер");
                border.setTitleFont(new Font("Open Sans", Font.BOLD, 14));
                border.setBorder(BorderFactory.createLineBorder(Color.black, 2));
                setBorder(border);

                clients = new JComboBox<>();
                clients.setPreferredSize(new Dimension(30, 20));
                clients.setFocusable(false);
                clients.setEnabled(false);

                send_button = new JButton("Запросить конфиг. файл");
                send_button.setEnabled(false);
                send_button.addActionListener(actionEvent -> {
                    try {
                        String target = clients.getSelectedItem().toString();
                        outStream.writeUTF("send");
                        outStream.writeUTF(target);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                action_button = new JButton("Подключиться");
                action_button.addActionListener(actionEvent -> {
                    try {
                        if (!isConnected) {
                            connect();
                            outStream.writeUTF(userName);
                            clientUpdater = new ClientUpdater();
                        } else {
                            outStream.writeUTF("disconnect");
                            clientUpdater.interrupt();
                            disconnect();
                        }
                    }
                    catch (IOException e) {
                        if(!isConnected) {
                            new ErrorDialog("Попытка подключения не удалась!\nПроверьте работу сервера...\n");
                        }
                        else {
                            new ErrorDialog("Принудительное отключение");
                        }
                    }
                });

                c.gridwidth = 2;
                c.ipadx = 50;
                add(action_button, c);
                c.gridy++;
                c.gridwidth = 1;
                add(clients, c);
                c.gridx++;
                c.gridwidth = 1;
                c.ipadx = 5;
                add(send_button, c);
            }

            public void setConnection (boolean status) { isConnected = status; }
        }
    }

    private class ClientUpdater extends Thread {
        public ClientUpdater() {
            Thread clientUpdater = new Thread(() -> {
                try {
                    while(true) {
                        String action = inStream.readUTF();
                        switch (action) {
                            case "update" -> {
                                usersList.clear();
                                controlPanel.networkPanel.clients.removeAllItems();
                                int size = inStream.readInt();
                                for(int i = 0; i < size; i++) {
                                    usersList.addLast(inStream.readUTF());
                                    controlPanel.networkPanel.clients.addItem(usersList.get(i));
                                }

                                if(size > 0)
                                    controlPanel.networkPanel.clients.setSelectedItem(0);
                            }

                            case "request" -> {
                                String bufFile = directory + "/buf.txt";
                                new File(bufFile).deleteOnExit();
                                writeConfig(bufFile);
                                String target = inStream.readUTF();
                                outStream.writeUTF("answer");
                                outStream.writeUTF(target);
                                outStream.writeUTF(bufFile);
                            }

                            case "accept" -> {
                                String data = inStream.readUTF();
                                readConfig(data);
                            }
                        }
                    }
                }
                catch (IOException e) {
                    controlPanel.networkPanel.send_button.setEnabled(false);
                    controlPanel.networkPanel.clients.removeAllItems();
                    controlPanel.networkPanel.clients.setEnabled(false);
                    controlPanel.networkPanel.action_button.setText("Подключиться");
                    controlPanel.networkPanel.setConnection(false);
                }
            });

            clientUpdater.setDaemon(true);
            clientUpdater.setName("Client updater");
            clientUpdater.start();
        }
    }

    private class ModalInfoDialog extends JDialog {
        JTextArea info_area;

        private ModalInfoDialog () {
            int dialog_window_width = 250;
            int dialog_window_height = 220;

            pause();

            this.setTitle("Результаты симуляции");
            this.setIconImage(new ImageIcon("src/LaboratoryWork/Assets/attention.png").getImage());
            this.setBackground(Color.white);
            this.setForeground(Color.DARK_GRAY);
            this.setBounds(screenSize.width/2 - dialog_window_width/2,
                           screenSize.height/2 - dialog_window_height/2,
                              dialog_window_width, dialog_window_height);
            this.setLayout(new GridLayout(2,1));
            this.setResizable(false);

            info_area = new JTextArea(habitat.getMetrics());
            info_area.setEditable(false);
            info_area.setBackground(this.getBackground());
            info_area.setForeground(this.getForeground());
            info_area.setFont(new Font("Open Sans", Font.ITALIC,14));
            info_area.setPreferredSize(new Dimension(250,110));

            Dimension button_size = new Dimension(100,45);

            JButton ok_button = new JButton("ОК");
            ok_button.setPreferredSize(button_size);
            ok_button.setBackground(Color.white);
            ok_button.setForeground(this.getForeground());
            ok_button.setToolTipText("Прекратить симуляцию");

            JButton cancel_button = new JButton("Отмена");
            cancel_button.setPreferredSize(button_size);
            cancel_button.setBackground(Color.white);
            cancel_button.setForeground(this.getForeground());
            cancel_button.setToolTipText("Продолжить симуляцию");

            JLabel question = new JLabel("Прекратить симуляцию?");
            question.setBackground(this.getBackground());
            question.setForeground(this.getForeground());
            question.setFont(new Font("Open Sans", Font.BOLD,18));

            JPanel panel_buttons = new JPanel();
            panel_buttons.setBackground(this.getBackground());
            panel_buttons.add(question);
            panel_buttons.add(ok_button);
            panel_buttons.add(cancel_button);

            ok_button.addActionListener(actionEvent -> { stop(); dispose(); });
            cancel_button.addActionListener(actionEvent -> { continue_(); dispose(); });

            this.setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ENTER -> {
                            stop();
                            dispose();
                        }
                        case KeyEvent.VK_ESCAPE -> {
                            continue_();
                            dispose();
                        }
                    }
                }
            });

            this.setModalityType(ModalityType.APPLICATION_MODAL);

            this.add(info_area);
            this.add(panel_buttons);
            this.setVisible(true);
        }
    }

    private class ConsoleDialog extends JDialog {
        JTextArea info_area;
        JTextField input_area;
        int dialog_window_width = 500;
        int dialog_window_height = 300;

        private ConsoleDialog () {
            this.setTitle("Консоль");
            this.setIconImage(new ImageIcon("src/LaboratoryWork/Assets/console_icon.png").getImage());
            this.setBackground(Color.white);
            this.setForeground(Color.DARK_GRAY);
            this.setBounds(screenSize.width/2 - dialog_window_width/2,
                    screenSize.height/2 - dialog_window_height/2,
                    dialog_window_width, dialog_window_height);
            this.setLayout(new BorderLayout());
            this.setModal(false);
            this.setAlwaysOnTop(true);
            this.setResizable(false);
            this.setFocusable(true);

            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream();

            try {
                pis.connect(pos);
            } catch (IOException e) {
                e.printStackTrace();
            }

            info_area = new JTextArea("Для того, чтобы узнать все доступные команды напишите: help\n");
            info_area.setFont(new Font("Open Sans", Font.ITALIC, 12));
            info_area.setEditable(false);
            this.add(info_area, BorderLayout.CENTER);

            input_area = new JTextField();
            input_area.setPreferredSize(new Dimension(dialog_window_width, 25));
            input_area.setRequestFocusEnabled(true);
            input_area.addActionListener(actionEvent -> {
                String command = input_area.getText();
                info_area.append(command + "\n");
                if (command.startsWith("help")) {
                    info_area.append("> Доступные команды:\n" +
                            "> clear - очистить экран\n" +
                            "> close - закрыть консоль\n" +
                            "> setP1 [от 0 до 100] - Установить вероятность рождения золотой рыбки\n" +
                            "> getP1 - Узнать вероятность рождения золотой рыбки\n");
                }
                else if (command.startsWith("clear")) {
                    info_area.setText("");
                }
                else if (command.startsWith("setP1 ")) {
                    int P1 = Integer.parseInt(command.substring(command.indexOf(" ") + 1));
                    if(P1 > 100)
                        P1 = 100;
                    else if (P1 < 0)
                        P1 = 0;
                    try {
                        pos.write((byte) P1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    info_area.append("> Успешно изменено!\n");
                }
                else if (command.startsWith("getP1")) {
                    info_area.append("> Вероятность рождения золотой рыбки: " + habitat.getP1() + "%\n");
                }
                else if (command.startsWith("close")) {
                    try {
                        pis.close();
                        pos.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.dispose();
                }
                else {
                    info_area.append("> Неизвестная команда. Узнать все доступные команды: help\n");
                }
                input_area.setText("");
            });
            this.add(input_area, BorderLayout.SOUTH);

            Thread consoleIO = new Thread(() -> {
                while(true) {
                    try {
                        byte[] in = new byte[1];
                        if(pis.available() > 0) {
                            pis.read(in);
                            int buf = in[0];
                            habitat.setP1(buf);
                            controlPanel.probablyPanel.comboBoxPanel.golden_combo_box.setSelectedItem(Integer.toString(buf));
                            Thread.sleep(10);
                        }
                    } catch (IOException | InterruptedException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    try {
                        pis.close();
                        pos.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });

            consoleIO.setName("Console I/O daemon");
            consoleIO.setDaemon(true);
            consoleIO.start();
            this.add(new JScrollPane(info_area), BorderLayout.CENTER);
            this.setVisible(true);
        }
    }

    private class userDialog extends JDialog {
        private final int WINDOW_WIDTH = 300;
        private final int WINDOW_HEIGHT = 120;

        public userDialog() {
            setTitle("Регистриция нового пользователя");
            setIconImage(new ImageIcon("src/LaboratoryWork/Assets/user_add_icon.png").getImage());
            setModal(true);
            setAlwaysOnTop(true);
            setResizable(false);
            setBounds(screenSize.width/2 - WINDOW_WIDTH/2,
                    screenSize.height/2 - WINDOW_HEIGHT/2,
                    WINDOW_WIDTH, WINDOW_HEIGHT);
            setLayout(new BorderLayout());

            JTextArea text_area = new JTextArea("Имя пользователя");
            text_area.setFont(new Font("Open Sans", Font.BOLD, 16));
            text_area.setEditable(false);

            JTextField name_area = new JTextField();
            name_area.setFont(new Font("Open Sans", Font.ITALIC, 16));
            name_area.setPreferredSize(new Dimension(WINDOW_WIDTH, 40));
            name_area.addActionListener(actionEvent -> {
                if(name_area.getText().equals(""))
                    userName = "admin";
                else {
                    userName = name_area.getText();
                }
                dispose();
            });

            JButton ok_button = new JButton("ОК");
            ok_button.addActionListener(actionEvent -> {
                if(name_area.getText().equals(""))
                    userName = "admin";
                else {
                    userName = name_area.getText();
                }
                dispose();
            });

            add(text_area, BorderLayout.NORTH);
            add(name_area, BorderLayout.CENTER);
            add(ok_button, BorderLayout.SOUTH);
            setVisible(true);
        }
    }

    private class ErrorDialog extends JDialog {
        private final int WINDOW_WIDTH = 300;
        private final int WINDOW_HEIGHT = 150;
        public ErrorDialog(String msg) {
            setTitle("Ошибка");
            setIconImage(new ImageIcon("src/LaboratoryWork/Assets/warning.png").getImage());
            setModal(true);
            setAlwaysOnTop(true);
            setResizable(false);
            setLayout(new BorderLayout());
            setBounds(screenSize.width/2 - WINDOW_WIDTH/2,
                    screenSize.height/2 - WINDOW_HEIGHT/2,
                    WINDOW_WIDTH, WINDOW_HEIGHT);

            JTextArea info_error = new JTextArea();
            info_error.setText(msg);
            info_error.setEditable(false);
            info_error.setLineWrap(true);
            info_error.setWrapStyleWord(true);
            info_error.setFont(new Font("Open Sans", Font.ITALIC, 14));
            info_error.setBackground(Color.lightGray);
            add(info_error, BorderLayout.CENTER);

            JButton ok_button = new JButton("ОК");
            ok_button.setPreferredSize(new Dimension(40,20));
            ok_button.addActionListener(actionEvent -> dispose());
            add(ok_button, BorderLayout.SOUTH);

            add(new JScrollPane(info_error), BorderLayout.CENTER);
            setVisible(true);
        }
    }

    private class Menu extends JMenuBar {
        JMenuItem start_item;
        JMenuItem stop_item;
        JMenuItem pause_item;
        JMenuItem save_item;
        JMenuItem load_item;

        public Menu() {
            JMenu main_menu = new JMenu("Главное меню");

            start_item = new JMenuItem("Начать");
            start_item.setEnabled(true);
            start_item.addActionListener(actionEvent -> start());
            start_item.setToolTipText("Запуск симуляции");
            main_menu.add(start_item);

            stop_item = new JMenuItem("Закончить");
            stop_item.setEnabled(false);
            stop_item.addActionListener(actionEvent -> {
                if(isAllowModalInfo)
                    new ModalInfoDialog();
                else
                    stop();
            });
            stop_item.setToolTipText("Окончание симуляции");
            main_menu.add(stop_item);

            pause_item = new JMenuItem("Пауза");
            pause_item.setEnabled(false);
            pause_item.addActionListener(actionEvent -> resume());
            pause_item.setToolTipText("Пауза");
            main_menu.add(pause_item);

            save_item = new JMenuItem("Сохранить");
            save_item.setEnabled(true);
            save_item.addActionListener(actionEvent -> {
                if(habitat.getStatus() == Status.ВКЛ)
                    pause();

                JFileChooser fc = new JFileChooser();
                fc.showSaveDialog(null);
                File file = fc.getSelectedFile();
                saveObjects(file);
            });
            save_item.setToolTipText("Сохранить всех рыбок");
            main_menu.add(save_item);

            load_item = new JMenuItem("Загрузить");
            load_item.setEnabled(true);
            load_item.addActionListener(actionEvent -> {
                if(habitat.getStatus() == Status.ВКЛ)
                    pause();

                JFileChooser fc = new JFileChooser();
                fc.showOpenDialog(null);
                File file = fc.getSelectedFile();
                loadObjects(file);
            });
            load_item.setToolTipText("Загрузить всех рыбок");
            main_menu.add(load_item);

            JMenuItem exit_item = new JMenuItem("Выход");
            exit_item.addActionListener(actionEvent -> {
                try {
                    writeConfig(configFile);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                System.exit(0);
            });
            exit_item.setToolTipText("Закрыть программу");
            main_menu.add(exit_item);


            JMenu console_menu = new JMenu("Консоль");
            JMenuItem console_start_item = new JMenuItem("Запуск");
            console_start_item.addActionListener(actionEvent -> new ConsoleDialog());
            console_menu.add(console_start_item);

            this.add(main_menu);
            this.add(console_menu);
        }
    }

    private class TimerPanel extends JPanel {
        JLabel text;

        TimerPanel() {
            setLayout(new FlowLayout());
            this.setBackground(Color.RED);
            setPreferredSize(new Dimension(280, 35));

            text = new JLabel("Время симуляции: " + habitat.getTime() + " [" + habitat.getStatus() + "]");
            text.setFont(new Font("Open Sans", Font.BOLD, 16));
            text.setVisible(true);
            text.setFocusable(false);

            this.setToolTipText("Текущее время симуляции в секундах");
            this.add(text, FlowLayout.LEFT);
            this.setVisible(true);
        }
    }

    private void writeConfig(String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        String config = controlPanel.periodPanel.golden_text_field_N.getText() + ",";
        config += controlPanel.periodPanel.guppies_text_field_N.getText() + ",";
        config += controlPanel.livePanel.golden_text_field_TTL.getText() + ",";
        config += controlPanel.livePanel.guppies_text_field_TTL.getText() + ",";
        config += controlPanel.probablyPanel.comboBoxPanel.golden_combo_box.getSelectedItem() + ",";
        config += controlPanel.probablyPanel.sliderPanel.guppies_slider.getValue() + ",";
        config += controlPanel.priorityPanel.goldenFishPriority.getSelectedIndex() + ",";
        config += controlPanel.priorityPanel.guppiesFishPriority.getSelectedIndex() + ",";
        config += isVisible ? "1," : "0,";
        config += controlPanel.checkBoxPanel.modal_info_checkbox.isSelected() ? "1," : "0,";
        fos.write(config.getBytes());
        fos.close();
    }

    private void readConfig(String fileName) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        int buf;
        int cur_line = 0;
        String line = "";
        while ((buf = bis.read()) != -1) {
            if(!String.valueOf((char) buf).equals(","))
                line += String.valueOf((char)buf);
            else {
                switch (cur_line) {
                    case 0 -> {
                        habitat.setN1(Double.parseDouble(line));
                        controlPanel.periodPanel.golden_text_field_N.setText(line);
                    }
                    case 1 -> {
                        habitat.setN2(Double.parseDouble(line));
                        controlPanel.periodPanel.guppies_text_field_N.setText(line);
                    }
                    case 2 -> {
                        habitat.setGoldenTTL(Double.parseDouble(line));
                        controlPanel.livePanel.golden_text_field_TTL.setText(line);
                    }
                    case 3 -> {
                        habitat.setGuppiesTTL(Double.parseDouble(line));
                        controlPanel.livePanel.guppies_text_field_TTL.setText(line);
                    }
                    case 4 -> {
                       habitat.setP1(Integer.parseInt(line));
                       controlPanel.probablyPanel.comboBoxPanel.golden_combo_box.setSelectedItem(line);
                    }
                    case 5 -> {
                        habitat.setP2(Integer.parseInt(line));
                        controlPanel.probablyPanel.sliderPanel.guppies_slider.setValue(Integer.parseInt(line));
                    }
                    case 6 -> {
                        habitat.setGoldenFishAIPriority(Integer.parseInt(line) == 0 ? 1 : 5 * Integer.parseInt(line));
                        controlPanel.priorityPanel.goldenFishPriority.setSelectedIndex(Integer.parseInt(line));
                    }
                    case 7 -> {
                        habitat.setGuppiesFishAIPriority(Integer.parseInt(line) == 0 ? 1 : 5 * Integer.parseInt(line));
                        controlPanel.priorityPanel.guppiesFishPriority.setSelectedIndex(Integer.parseInt(line));
                    }
                    case 8 -> {
                        if(Integer.parseInt(line) == 1) {
                            isVisible = true;
                            controlPanel.timerPanel.setVisible(true);
                            controlPanel.radioButtonPanel.show_timer_button.setSelected(true);
                        }
                        else {
                            isVisible = false;
                            controlPanel.timerPanel.setVisible(false);
                            controlPanel.radioButtonPanel.hide_timer_button.setSelected(true);
                        }
                    }
                    case 9 -> {
                        if(Integer.parseInt(line) == 1) {
                            isAllowModalInfo = true;
                            controlPanel.checkBoxPanel.modal_info_checkbox.setSelected(true);
                        }
                        else {
                            isAllowModalInfo = false;
                            controlPanel.checkBoxPanel.modal_info_checkbox.setSelected(false);
                        }
                    }
                }
                line = "";
                cur_line++;
            }
        }
        bis.close();
    }

    private void saveObjects(File file) {
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file, false));
            oos.writeObject(FishArray.getFishArray().getList());
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadObjects(File file) {
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            FishArray.getFishArray().removeAllFishes();
            FishArray.getFishArray().setFishes((LinkedList<Fish>) ois.readObject(), habitat.getTime() * 10.);
            habitat.repaint();
            ois.close();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void runTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                controlPanel.timerPanel.text.setText("Время симуляции: " + habitat.getTime() + " [" + habitat.getStatus() + "]");
                if(habitat.getStatus() == Status.ВЫКЛ)
                    controlPanel.timerPanel.setBackground(Color.RED);
                else if(habitat.getStatus() == Status.ВКЛ)
                    controlPanel.timerPanel.setBackground(Color.GREEN);
                else if(habitat.getStatus() == Status.ПАУЗА)
                    controlPanel.timerPanel.setBackground(Color.ORANGE);
            }
        }, 0, habitat.getPeriod());
    }
}