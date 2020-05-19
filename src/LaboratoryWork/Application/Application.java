package LaboratoryWork.Application;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import LaboratoryWork.Habitat.Fish.Fish;
import LaboratoryWork.Habitat.Fish.FishArray;
import LaboratoryWork.Habitat.Habitat;
import LaboratoryWork.Habitat.Fish.Objects.GoldenFish;
import LaboratoryWork.Habitat.Status;

public class Application extends JFrame {
    Dimension screenSize = getToolkit().getScreenSize();
    private final int window_width;
    private final int window_height;
    private final String configFile = "src/LaboratoryWork/Configs/config.txt";

    private final Menu menu;
    private final StatusPanel statusPanel;
    private final TimerPanel timerPanel;
    private final ControlPanel controlPanel;

    private boolean isPausedGoldenAI = false;
    private boolean isPausedGuppiesAI = false;
    private boolean isVisible = true;
    private boolean isAllowModalInfo = true;

    private final Habitat habitat;

    public Application(int width, int height, Habitat h) {
        this.habitat = h;
        this.window_width = width;
        this.window_height = height;

        this.setTitle("Генератор рыбок");
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
        statusPanel = new StatusPanel();
        timerPanel = new TimerPanel();

        this.add(habitat, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.EAST);
        this.add(timerPanel, BorderLayout.SOUTH);
        this.add(statusPanel, BorderLayout.NORTH);
        this.setJMenuBar(menu = new Menu());

        try {
            readConfig(configFile);
        }
        catch (FileNotFoundException fnf) {
            try {
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

    private void show_hide_timer() {
        isVisible = !isVisible;
        timerPanel.setVisible(isVisible);
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
        ButtonPanel buttonPanel;
        CheckBoxPanel checkBoxPanel;
        RadioButtonPanel radioButtonPanel;
        TextFieldPanel textFieldPanel;
        ProbablyPanel probablyPanel;
        PriorityPanel priorityPanel;

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
            gbc.insets = new Insets(2,2,2,2);

            add(textFieldPanel = new TextFieldPanel(), gbc);
            gbc.gridy++;
            add(probablyPanel = new ProbablyPanel(), gbc);
            gbc.gridy++;
            add(priorityPanel = new PriorityPanel(), gbc);
            gbc.gridy++;
            add(radioButtonPanel = new RadioButtonPanel(), gbc);
            gbc.gridy++;
            add(checkBoxPanel = new CheckBoxPanel(), gbc);
            gbc.gridy++;
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

        private class TextFieldPanel extends JPanel {
            private final JTextField golden_text_field_N;
            private final JTextField golden_text_field_TTL;
            private final JTextField guppies_text_field_N;
            private final JTextField guppies_text_field_TTL;

            public TextFieldPanel() {
                setLayout(new GridBagLayout());
                setBackground(background);
                setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1;
                c.weighty = 1;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                //Период рождения
                c.gridwidth = 7;
                JLabel periodTime = new JLabel("Период между рождениями");
                periodTime.setFont(new Font("Open Sans", Font.BOLD, 14));
                add(periodTime, c);

                c.gridy++;
                c.gridwidth = 1;

                add(new JLabel("Золотых:"), c);
                c.gridx++;
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
                        JOptionPane.showMessageDialog(null,
                                "Введено неверное значение...", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });
                add(golden_text_field_N, c);
                c.gridx++;
                add(new JLabel("/сек"), c);
                c.gridx++;
                add(new JLabel(" | "), c);
                c.gridx++;

                add(new JLabel("Гуппи:"), c);
                c.gridx++;
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
                        JOptionPane.showMessageDialog(null,
                                "Введено неверное значение...", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });
                add(guppies_text_field_N, c);
                c.gridx++;
                add(new JLabel("/сек"), c);
                c.gridy++;

                //Время жизни
                c.gridx = 0;
                c.gridwidth = 7;
                JLabel liveTime = new JLabel("Время жизни");
                liveTime.setFont(new Font("Open Sans", Font.BOLD, 14));
                add(liveTime, c);

                c.gridy++;
                c.gridwidth = 1;

                add(new JLabel("Золотых:"), c);
                c.gridx++;
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
                        JOptionPane.showMessageDialog(null,
                                "Введено неверное значение...", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });
                add(golden_text_field_TTL, c);
                c.gridx++;
                add(new JLabel("сек."), c);
                c.gridx++;
                add(new JLabel(" | "), c);
                c.gridx++;

                add(new JLabel("Гуппи:"), c);
                c.gridx++;
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
                        JOptionPane.showMessageDialog(null,
                                "Введено неверное значение...", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });
                add(guppies_text_field_TTL, c);
                c.gridx++;
                add(new JLabel("сек."), c);
            }
        }

        private class PriorityPanel extends JPanel {
            private final JComboBox<String> goldenFishPriority;
            private final JComboBox<String> guppiesFishPriority;

            PriorityPanel() {
                setLayout(new GridBagLayout());
                setBackground(background);
                setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1;
                c.weighty = 1;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                String[] items = {
                    "Низкий",
                    "Нормальный",
                    "Высокий"
                };

                c.gridwidth = 4;
                JLabel priorityTitle = new JLabel("Приоритет ИИ");
                priorityTitle.setFont(new Font("Open Sans", Font.BOLD, 14));
                add(priorityTitle, c);
                c.gridy++;
                c.gridwidth = 1;

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
                c.gridx++;

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
                setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1;
                c.weighty = 1;
                c.anchor = GridBagConstraints.CENTER;
                c.insets = new Insets(2,2,2,2);

                JLabel probablyBorn = new JLabel("Вероятность рождения [%]");
                probablyBorn.setFont(new Font("Open Sans", Font.BOLD, 14));
                add(probablyBorn, c);
                c.gridy++;
                add(comboBoxPanel = new ComboBoxPanel(), c);
                c.gridy++;
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
                    info_area.append("Доступные команды:\n" +
                            "clear - очистить экран\n" +
                            "close - закрыть консоль\n" +
                            "setP1 [от 0 до 100] - Установить вероятность рождения золотой рыбки\n" +
                            "getP1 - Узнать вероятность рождения золотой рыбки\n");
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
                    info_area.append("Успешно изменено!\n");
                }
                else if (command.startsWith("getP1")) {
                    info_area.append("Вероятность рождения золотой рыбки: " + habitat.getP1() + "%\n");
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
                    info_area.append("Неизвестная команда. Узнать все доступные команды: help\n");
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
            console_menu.addMenuListener(new MenuListener() {
                @Override
                public void menuSelected(MenuEvent e) { new ConsoleDialog(); }
                @Override
                public void menuDeselected(MenuEvent e) {}
                @Override
                public void menuCanceled(MenuEvent e) {}
            });

            this.add(main_menu);
            this.add(console_menu);
        }
    }

    private class TimerPanel extends JPanel {
        JLabel text;

        TimerPanel() {
            this.setBackground(Color.GRAY);

            text = new JLabel("Время симуляции: " + habitat.getTime());
            text.setBounds(5,20, 200,20);
            text.setFont(new Font("Open Sans", Font.BOLD, 16));
            text.setVisible(true);
            text.setFocusable(false);

            this.setToolTipText("Текущее время симуляции в секундах");
            this.add(text);
            this.setVisible(true);
        }
    }

    private class StatusPanel extends JPanel {
        JLabel text;

        StatusPanel() {
            this.setBackground(Color.RED);

            text = new JLabel("Статус симуляции: " + habitat.getStatus());
            text.setBounds(5,20, 200,20);
            text.setFont(new Font("Open Sans", Font.BOLD, 16));
            text.setVisible(true);
            text.setFocusable(false);

            this.setToolTipText("Текущий статус симуляции");
            this.add(text);
            this.setVisible(true);
        }
    }

    private void writeConfig(String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        String config = controlPanel.textFieldPanel.golden_text_field_N.getText() + ",";
        config += controlPanel.textFieldPanel.guppies_text_field_N.getText() + ",";
        config += controlPanel.textFieldPanel.golden_text_field_TTL.getText() + ",";
        config += controlPanel.textFieldPanel.guppies_text_field_TTL.getText() + ",";
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
            if(!String.valueOf((char)buf).equals(","))
                line += String.valueOf((char)buf);
            else {
                switch (cur_line) {
                    case 0 -> {
                        habitat.setN1(Double.parseDouble(line));
                        controlPanel.textFieldPanel.golden_text_field_N.setText(line);
                    }
                    case 1 -> {
                        habitat.setN2(Double.parseDouble(line));
                        controlPanel.textFieldPanel.guppies_text_field_N.setText(line);
                    }
                    case 2 -> {
                        habitat.setGoldenTTL(Double.parseDouble(line));
                        controlPanel.textFieldPanel.golden_text_field_TTL.setText(line);
                    }
                    case 3 -> {
                        habitat.setGuppiesTTL(Double.parseDouble(line));
                        controlPanel.textFieldPanel.guppies_text_field_TTL.setText(line);
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
                            timerPanel.setVisible(true);
                            controlPanel.radioButtonPanel.show_timer_button.setSelected(true);
                        }
                        else {
                            isVisible = false;
                            timerPanel.setVisible(false);
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
                cur_line++;
                line = "";
            }
        }
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
                timerPanel.text.setText("Время симуляции: " + habitat.getTime());
                statusPanel.text.setText("Статус симуляции: " + habitat.getStatus());
                if(habitat.getStatus() == Status.ВЫКЛ)
                    statusPanel.setBackground(Color.RED);
                else if(habitat.getStatus() == Status.ВКЛ)
                    statusPanel.setBackground(Color.GREEN);
                else if(habitat.getStatus() == Status.ПАУЗА)
                    statusPanel.setBackground(Color.ORANGE);
            }
        }, 0, habitat.getPeriod());
    }
}