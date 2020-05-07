package LaboratoryWork.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.Timer;
import java.util.TimerTask;

import LaboratoryWork.Habitat.Habitat;
import LaboratoryWork.Habitat.Status;

public class Application extends JFrame {
    private final int window_width;
    private final int window_height;

    private final JButton start_button;
    private final JButton stop_button;
    private final JButton pause_button;

    private final JRadioButton show_timer_radio_button;
    private final JRadioButton hide_timer_radio_button;

    private final ModalInfoDialog modalInfoDialog;
    private final Menu menu;
    private final StatusPanel statusPanel;
    private final TimerPanel timerPanel;

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
        this.setBounds(50, 50, window_width, window_height);
        this.setBackground(Color.white);
        this.setLocationRelativeTo(null);
        this.setFocusable(true);
        this.setResizable(true);

        JPanel scene = new JPanel(new BorderLayout());

        JPanel control_panel = new JPanel(new GridLayout());
        Dimension control_panel_size = new Dimension(240, getHeight());
        control_panel.setPreferredSize(control_panel_size);
        control_panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        control_panel.setBackground(Color.PINK);

        modalInfoDialog = new ModalInfoDialog();

        this.setJMenuBar(menu = new Menu());

        statusPanel = new StatusPanel();
        timerPanel = new TimerPanel();

        scene.add(habitat, BorderLayout.CENTER);
        scene.add(control_panel, BorderLayout.EAST);
        scene.add(timerPanel, BorderLayout.SOUTH);
        scene.add(statusPanel, BorderLayout.NORTH);
        this.add(scene);

        this.setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_B -> start();
                    case KeyEvent.VK_E -> stop();
                    case KeyEvent.VK_T -> show_hide_timer();
                    case KeyEvent.VK_P -> pause_continue();
                    case KeyEvent.VK_ESCAPE -> System.exit(0);
                }
            }
        });


        //КНОПКИ
        Dimension button_size = new Dimension(180,30);

        start_button = new JButton("Начать");
        start_button.setPreferredSize(button_size);
        start_button.setBackground(Color.lightGray);
        start_button.setEnabled(true);
        start_button.addActionListener(actionEvent -> {
            start();
            requestFocus(false);
        });
        start_button.setToolTipText("Запуск симуляции");
        control_panel.add(start_button);

        stop_button = new JButton("Посмотреть информацию");
        stop_button.setPreferredSize(button_size);
        stop_button.setBackground(Color.lightGray);
        stop_button.setEnabled(false);
        stop_button.addActionListener(actionEvent -> {
            stop();
            requestFocus(false);
        });
        stop_button.setToolTipText("Остановка симуляции");
        control_panel.add(stop_button);

        pause_button = new JButton("Приостановить");
        pause_button.setPreferredSize(button_size);
        pause_button.setBackground(Color.lightGray);
        pause_button.setEnabled(false);
        pause_button.addActionListener(actionEvent -> {
            pause_continue();
            requestFocus(false);
        });
        pause_button.setToolTipText("Пауза");
        control_panel.add(pause_button);


        //ЧЕКБОКС
        JCheckBox modal_info = new JCheckBox("Показывать информацию");
        modal_info.setSelected(true);
        modal_info.setContentAreaFilled(false);
        modal_info.setBackground(control_panel.getBackground());
        modal_info.addActionListener(actionEvent -> {
            isAllowModalInfo = !isAllowModalInfo;
            if(isAllowModalInfo) {
                stop_button.setText("Посмотреть информацию");
                menu.stop_item.setText("Посмотреть информацию");
            }
            else {
                stop_button.setText("Остановить");
                menu.stop_item.setText("Остановить");
            }
            requestFocus(false);
        });
        modal_info.setToolTipText("Включить/Выключить отображение информации");
        control_panel.add(modal_info);


        //РАДИО-КНОПКА
        JPanel timer_radio_button_panel = new JPanel(new GridLayout(2,1));
        timer_radio_button_panel.setBackground(control_panel.getBackground());
        control_panel.add(timer_radio_button_panel);

        show_timer_radio_button = new JRadioButton("Показывать время симуляции");
        show_timer_radio_button.setBackground(control_panel.getBackground());
        show_timer_radio_button.addActionListener(actionEvent -> {
            isVisible = true;
            timerPanel.setVisible(true);
            requestFocus(false);
        });
        timer_radio_button_panel.add(show_timer_radio_button);

        hide_timer_radio_button = new JRadioButton("Скрывать время симуляции");
        hide_timer_radio_button.setBackground(control_panel.getBackground());
        hide_timer_radio_button.addActionListener(actionEvent -> {
            isVisible = false;
            timerPanel.setVisible(false);
            requestFocus(false);
        });
        timer_radio_button_panel.add(hide_timer_radio_button);

        ButtonGroup timer_radio_buttons = new ButtonGroup();
        timer_radio_buttons.add(show_timer_radio_button);
        timer_radio_buttons.add(hide_timer_radio_button);
        timer_radio_buttons.setSelected(show_timer_radio_button.getModel(), true);


        //ПЕРИОД РОЖДЕНИЯ
        JPanel period_panel = new JPanel(new GridLayout(2,1));
        period_panel.setBackground(control_panel.getBackground());
        control_panel.add(period_panel);

        JLabel period_title = new JLabel("Период рождения");
        period_title.setPreferredSize(new Dimension(237, 30));
        period_title.setToolTipText("Время в секундах / 10");
        period_panel.add(period_title);

        JPanel period_edit_panel = new JPanel();
        period_edit_panel.setLayout(new BoxLayout(period_edit_panel, BoxLayout.X_AXIS));
        period_edit_panel.setBackground(control_panel.getBackground());
        period_panel.add(period_edit_panel);

        JPanel golden_period_panel = new JPanel();
        golden_period_panel.setBackground(control_panel.getBackground());
        period_edit_panel.add(golden_period_panel);

        JLabel golden_label = new JLabel("Золотой:");
        golden_period_panel.add(golden_label);

        JTextField golden_text_field = new JTextField(Integer.toString(habitat.getN1()));
        golden_text_field.setPreferredSize(new Dimension(30, 20));
        golden_text_field.addActionListener(actionEvent -> {
            try {
                habitat.setN1(Integer.parseInt(golden_text_field.getText()));
                requestFocus(false);
                if(habitat.getN1() < 0) {
                    throw new Exception();
                }
            }
            catch (Exception e) {
                habitat.setN1(5);
                golden_text_field.setText(Integer.toString(habitat.getN1()));
                JOptionPane.showMessageDialog(null,
                        "Введено неверное значение...", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        golden_period_panel.add(golden_text_field);

        JPanel guppies_period_panel = new JPanel();
        guppies_period_panel.setBackground(control_panel.getBackground());
        period_edit_panel.add(guppies_period_panel);

        JLabel period_guppies_label = new JLabel("Гуппи:");
        guppies_period_panel.add(period_guppies_label);

        JTextField guppies_text_field = new JTextField(Integer.toString(habitat.getN2()));
        guppies_text_field.setPreferredSize(new Dimension(30, 20));
        guppies_text_field.addActionListener(actionEvent -> {
            try {
                habitat.setN2(Integer.parseInt(guppies_text_field.getText()));
                requestFocus(false);
                if(habitat.getN2() < 0) {
                    throw new Exception();
                }
            }
            catch (Exception e) {
                habitat.setN1(3);
                guppies_text_field.setText(Integer.toString(habitat.getN1()));
                JOptionPane.showMessageDialog(null,
                        "Введено неверное значение...", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        guppies_period_panel.add(guppies_text_field);


        //ВЕРОЯТНОСТЬ РОЖДЕНИЯ
        JPanel probably_panel = new JPanel(new GridLayout(3,1));
        probably_panel.setBackground(control_panel.getBackground());
        control_panel.add(probably_panel);

        String[] items = {
                "0.0",
                "0.1",
                "0.2",
                "0.3",
                "0.4",
                "0.5",
                "0.6",
                "0.7",
                "0.8",
                "0.9",
                "1.0"
        };

        JLabel probably_title = new JLabel("Вероятность рождения");
        probably_title.setPreferredSize(new Dimension(200, 30));
        probably_title.setToolTipText("Вероятность от 0 до 1. В случае с Гуппи делим цифры на слайдере на 100");
        probably_panel.add(probably_title);


        //ВЕРОЯТНОСТЬ РОЖДЕНИЯ ЗОЛОТЫХ РЫБОК
        JPanel probably_golden_panel = new JPanel();
        probably_golden_panel.setLayout(new BoxLayout(probably_golden_panel, BoxLayout.X_AXIS));
        probably_golden_panel.setBackground(control_panel.getBackground());
        probably_panel.add(probably_golden_panel);

        JLabel probably_golden_label = new JLabel("Золотых: ");
        probably_golden_panel.add(probably_golden_label);

        JComboBox<String> probably_golden_combo_box = new JComboBox<>(items);
        probably_golden_combo_box.setSelectedItem(Double.toString(habitat.getP1()));
        probably_golden_combo_box.addActionListener(actionEvent -> {
            habitat.setP1(Double.parseDouble(items[probably_golden_combo_box.getSelectedIndex()]));
            requestFocus(false);
        });
        probably_golden_panel.add(probably_golden_combo_box);

        //ВЕРОЯТНОСТЬ РОЖДЕНИЯ РЫБОК ГУППИ
        JPanel probably_guppies_panel = new JPanel();
        probably_guppies_panel.setLayout(new BoxLayout(probably_guppies_panel, BoxLayout.X_AXIS));
        probably_guppies_panel.setBackground(control_panel.getBackground());
        probably_panel.add(probably_guppies_panel);

        JLabel probably_guppies_label = new JLabel("Гуппи: ");
        probably_guppies_panel.add(probably_guppies_label);


        JSlider probably_guppies_slider = new JSlider(0,100);
        probably_guppies_slider.setBackground(control_panel.getBackground());
        double probably_guppies_value = habitat.getP2() * 100.;
        probably_guppies_slider.setValue((int) probably_guppies_value);
        probably_guppies_slider.setMajorTickSpacing(10);
        probably_guppies_slider.setPaintLabels(true);
        probably_guppies_slider.setPaintTicks(true);
        probably_guppies_slider.addChangeListener(actionEvent -> {
            habitat.setP2(probably_guppies_slider.getValue() / 100.);
            requestFocus(false);
        });
        probably_guppies_panel.add(probably_guppies_slider);

        this.setVisible(true);
    }

    private void start() {
        if(habitat.getStatus() == Status.ВЫКЛ) {
            habitat.begin_simulation();
            start_button.setEnabled(false);
            menu.start_item.setEnabled(false);
            stop_button.setEnabled(true);
            menu.stop_item.setEnabled(true);
            pause_button.setEnabled(true);
            menu.pause_item.setEnabled(true);
            runTimer();
        }
    }

    private void pause() {
        habitat.pause_simulation();
        pause_button.setText("Продолжить");
        menu.pause_item.setText("Продолжить");
    }

    private void continue_() {
        habitat.continue_simulation();
        pause_button.setText("Приостановить");
        menu.pause_item.setText("Приостановить");
    }

    private void pause_continue() {
        if(habitat.getStatus() == Status.ВКЛ)
            pause();
        else if(habitat.getStatus() == Status.ПАУЗА) {
            continue_();
            modalInfoDialog.dispose();
        }
    }

    private void stop() {
        if(habitat.getStatus() != Status.ВЫКЛ) {
            if (isAllowModalInfo) {
                modalInfoDialog.update_metrics();
                modalInfoDialog.setVisible(true);
                modalInfoDialog.setAlwaysOnTop(true);
            } else {
                modalInfoDialog.dispose();
                stopped();
            }
        }
    }

    private void stopped() {
        pause_button.setText("Приостановить");
        habitat.end_simulation();
        start_button.setEnabled(true);
        menu.start_item.setEnabled(true);
        stop_button.setEnabled(false);
        menu.stop_item.setEnabled(false);
        pause_button.setEnabled(false);
        menu.pause_item.setEnabled(false);
    }

    private void show_hide_timer() {
        isVisible = !isVisible;
        timerPanel.setVisible(isVisible);
        if(isVisible) {
            show_timer_radio_button.setSelected(true);
            hide_timer_radio_button.setSelected(false);
        }
        else {
            hide_timer_radio_button.setSelected(true);
            show_timer_radio_button.setSelected(false);
        }
    }

    private class ModalInfoDialog extends JDialog {
        JTextArea info_area;

        private ModalInfoDialog () {
            int dialog_window_width = 250;
            int dialog_window_height = 220;

            this.setTitle("Результаты симуляции");
            this.setIconImage(new ImageIcon("src/LaboratoryWork/Assets/attention.png").getImage());
            this.setBackground(Color.LIGHT_GRAY);
            this.setForeground(Color.DARK_GRAY);
            this.setBounds(window_width/2 + dialog_window_width/4,
                        window_height/2 - dialog_window_height/4,
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

            ok_button.addActionListener(actionEvent -> { stopped(); dispose(); });
            cancel_button.addActionListener(actionEvent -> { continue_(); dispose(); });

            this.setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ENTER -> {
                            stopped();
                            dispose();
                        }
                        case KeyEvent.VK_ESCAPE -> {
                            continue_();
                            dispose();
                        }
                    }
                }
            });

            this.add(info_area);
            this.add(panel_buttons);
            this.setVisible(false);
        }

        public void update_metrics() {
            info_area.setText(habitat.getMetrics());
            pause();
        }
    }

    private class Menu extends JMenuBar {
        JMenuItem start_item;
        JMenuItem stop_item;
        JMenuItem pause_item;

        public Menu() {
            JMenu main_menu = new JMenu("Главное меню");

            start_item = new JMenuItem("Начать");
            start_item.setEnabled(true);
            start_item.addActionListener(actionEvent -> start());
            start_item.setToolTipText("Запуск симуляции");
            main_menu.add(start_item);

            stop_item = new JMenuItem("Посмотреть информацию");
            stop_item.setEnabled(false);
            stop_item.addActionListener(actionEvent -> stop());
            stop_item.setToolTipText("Остановка симуляции");
            main_menu.add(stop_item);

            pause_item = new JMenuItem("Приостановить");
            pause_item.setEnabled(false);
            pause_item.addActionListener(actionEvent -> pause_continue());
            pause_item.setToolTipText("Пауза");
            main_menu.add(pause_item);

            this.add(main_menu);
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