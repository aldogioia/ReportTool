import org.jdatepicker.impl.JDatePickerImpl;
import utils.DatePickerInitializer;
import utils.MessageCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportToolGui extends JFrame {
    private JDatePickerImpl startDatePicker;
    private JDatePickerImpl endDatePicker;
    private JTextField txtCpm, txtImpressionsGoal, txtCampaignName;
    private JLabel impressionsLabel;
    private final List<String> creativeList = new ArrayList<>();
    private JLabel creativeLabel;
    private final Map<String, JCheckBox> CheckBoxMap = new HashMap<>();

    public ReportToolGui() {
        setTitle("Strumento di Reportistica");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        // Data inizio
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Data inizio:"), gbc);
        gbc.gridx = 1;
        startDatePicker = DatePickerInitializer.createDatePicker();
        startDatePicker.getJFormattedTextField().addPropertyChangeListener("value", _ -> updateImpressionsLabel());
        panel.add(startDatePicker, gbc);
        y++;

        // Data fine
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Data fine:"), gbc);
        gbc.gridx = 1;
        endDatePicker = DatePickerInitializer.createDatePicker();
        endDatePicker.getJFormattedTextField().addPropertyChangeListener("value", _ -> updateImpressionsLabel());
        panel.add(endDatePicker, gbc);
        y++;

        // CPM
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("CPM:"), gbc);
        gbc.gridx = 1;
        txtCpm = new JTextField("0");
        panel.add(txtCpm, gbc);
        y++;

        // Impressions Goal
        gbc.gridx = 0; gbc.gridy = y;
        impressionsLabel = new JLabel("Impressions:");
        panel.add(impressionsLabel, gbc);
        gbc.gridx = 1;
        txtImpressionsGoal = new JTextField("0");
        panel.add(txtImpressionsGoal, gbc);
        y++;

        // Campaign Name
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Campagna:"), gbc);
        gbc.gridx = 1;
        txtCampaignName = new JTextField();
        panel.add(txtCampaignName, gbc);
        y++;

        // Creative
        creativeLabel = new JLabel("Creative: ");
        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(creativeLabel, gbc);
        y++;

        JPanel creativePanel = new JPanel(new BorderLayout());
        JTextField txtCreative = new JTextField();
        JButton btnAddCreative = new JButton("Aggiungi");
        JButton btnDeleteCreative = new JButton("Elimina");

        creativePanel.add(txtCreative, BorderLayout.CENTER);
        creativePanel.add(btnAddCreative, BorderLayout.WEST);
        creativePanel.add(btnDeleteCreative, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(creativePanel, gbc);
        y++;

        // Bottone aggiungi creative
        btnAddCreative.addActionListener(_ -> {
            String name = txtCreative.getText().trim();
            if (!name.isEmpty() && !creativeList.contains(name)) {
                creativeList.add(name);
                updateCreativeLabel();
                txtCreative.setText("");
            }
        });

        // Bottone rimuovi creative
        btnDeleteCreative.addActionListener(_ -> {
            if (!creativeList.isEmpty()) {
                creativeList.removeLast();
                updateCreativeLabel();
                txtCreative.setText("");
            }
        });


        // Schermi
        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Schermi:"), gbc);
        y++;

        JPanel screenPanel = new JPanel(new GridLayout(0, 2));
        for (Map.Entry<String, Long> entry : CSVLoader.getInstance().getDataScreen().entrySet()) {
            JCheckBox checkBox = new JCheckBox(entry.getKey());
            checkBox.setSelected(true);
            checkBox.addItemListener(_ -> updateImpressionsLabel());
            screenPanel.add(checkBox);
            CheckBoxMap.put(entry.getKey(), checkBox);
        }

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(screenPanel, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        JButton btnReport = new JButton("Genera Report");
        gbc.gridwidth = 2;
        panel.add(btnReport, gbc);

        add(panel);

        btnReport.addActionListener(this::handleReport);
    }

    private void updateCreativeLabel() {
        String joined = String.join(", ", creativeList);
        creativeLabel.setText("Creative: " + joined);
    }


    private void updateImpressionsLabel() {
        List<String> selectedScreens = CheckBoxMap.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();

        String startDateStr = startDatePicker.getJFormattedTextField().getText();
        String endDateStr = endDatePicker.getJFormattedTextField().getText();

        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            impressionsLabel.setText("Impressions:");
            return;
        }

        try {
            LocalDate start = LocalDate.parse(startDateStr);
            LocalDate end = LocalDate.parse(endDateStr);

            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            double weeks = days / 7.0;

            long totalEstimated = selectedScreens.stream()
                    .mapToLong(screen -> {
                        Long weekly = CSVLoader.getInstance().getDataScreen().getOrDefault(screen, 0L);
                        return Math.round(weekly * weeks);
                    })
                    .sum();

            impressionsLabel.setText("Impressions: " + totalEstimated);
        } catch (Exception ex) {
            impressionsLabel.setText("Impressions:");
        }
    }

    private void handleReport(ActionEvent e) {
        List<String> screens = CheckBoxMap.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();

        checkInput(screens);
    }

    private void checkInput(List<String> screens) {
        String startDate = startDatePicker.getJFormattedTextField().getText();
        String endDate = endDatePicker.getJFormattedTextField().getText();
        String cpmText = txtCpm.getText();
        String impressionsGoalText = txtImpressionsGoal.getText();
        String campaignNameText = this.txtCampaignName.getText();

        if (startDate.isEmpty() || endDate.isEmpty() || cpmText.isBlank() || impressionsGoalText.isBlank() || campaignNameText.isBlank()) {
            JOptionPane.showMessageDialog(this, "Tutti i campi devono essere compilati.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (creativeList.isEmpty()){
            JOptionPane.showMessageDialog(this, "È obbligatorio caricare le creative.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double cpm = Double.parseDouble(cpmText);
            int impressionsGoal = Integer.parseInt(impressionsGoalText);
            int impressionsEstimated = Integer.parseInt(impressionsLabel.getText().substring(13));

            if (impressionsGoal > impressionsEstimated) {
                JOptionPane.showMessageDialog(this, "L'impressions goal dev'essere inferiore o uguale rispetto alla capacità massima", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cpm <= 0 && impressionsGoal <= 0) {
                JOptionPane.showMessageDialog(this, "CPM e Impressions Goal devono essere maggiori di zero.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                if (start.isAfter(end)) {
                    JOptionPane.showMessageDialog(this, "La data di inizio non può essere dopo la data di fine.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<String> dataLines = ProjectionHandler
                        .generateData(start, end, cpm, impressionsEstimated, impressionsGoal, screens, creativeList);

                List<String> infoLines = List.of(
                        "Report Generato il: ;" + LocalDateTime.now(),
                        "Campagna: ;" + campaignNameText,
                        "CPM: ;" + cpm,
                        "Impressions Goal: ;" + impressionsGoal,
                        "Creative: ;" + String.join(";", creativeList)
                );

                String headerLine = "Date;Screen;Creative;Impressions;Spend";

                MessageCode code = ExcelHandler.writeExcel(
                        campaignNameText,
                        infoLines,
                        headerLine,
                        dataLines
                );

                if (code == MessageCode.OK) {
                    JOptionPane.showMessageDialog(this, "Report generato con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Errore nella generazione del report.", "Errore", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Impossibile convertire le date", "Errore", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Impossibile convertire cpm e impressions", "Errore", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportToolGui().setVisible(true));
    }
}

