import org.jdatepicker.impl.JDatePickerImpl;
import utils.DatePickerInitializer;
import utils.MessageCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportToolGui extends JFrame {
    private JDatePickerImpl startDatePicker;
    private JDatePickerImpl endDatePicker;
    private JTextField txtCpm, txtImpressionsGoal, txtCampaignName;
    private final Map<String, JCheckBox> CheckBoxMap = new HashMap<>();

    public ReportToolGui() {
        setTitle("Strumento di Reportistica");
        setSize(900, 550);
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
        panel.add(startDatePicker, gbc);
        y++;

        // Data fine
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Data fine:"), gbc);
        gbc.gridx = 1;
        endDatePicker = DatePickerInitializer.createDatePicker();
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
        panel.add(new JLabel("Impressions:"), gbc);
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

        // Schermi
        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Schermi:"), gbc);
        y++;

        JPanel screenPanel = new JPanel(new GridLayout(0, 2));
        for (Map.Entry<String, Integer> entry : CSVLoader.getInstance().getDataScreen().entrySet()) {
            JCheckBox checkBox = new JCheckBox(entry.getKey());
            checkBox.setSelected(true);
            screenPanel.add(checkBox);
            CheckBoxMap.put(entry.getKey(), checkBox);
        }

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(screenPanel, gbc);
        y++;

        // Bottoni
        JButton btnLoad = new JButton("Carica Creative");
        JButton btnReport = new JButton("Genera Report");

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(btnLoad, gbc);
        gbc.gridx = 1;
        panel.add(btnReport, gbc);

        add(panel);

        btnLoad.addActionListener(this::handleLoad);
        btnReport.addActionListener(this::handleReport);
    }

    private void handleLoad(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                CSVLoader.getInstance().loadCreatives(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Creative caricate con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore nel caricamento dei dati: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
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

        if (CSVLoader.getInstance().getCreatives().isEmpty()){
            JOptionPane.showMessageDialog(this, "È obbligatorio caricare le creative.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double cpm = Double.parseDouble(cpmText);
            int impressionsGoal = Integer.parseInt(impressionsGoalText);

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
                        .generateData(start, end, cpm, impressionsGoal, screens);

                List<String> infoLines = List.of(
                        "Report Generato il: ;" + LocalDate.now(),
                        "Campagna: ;" + campaignNameText,
                        "CPM: ;" + cpm,
                        "Impressions Goal: ;" + impressionsGoal,
                        "Schermi: ;" + String.join(";", screens),
                        "Creative: ;" + String.join(";", CSVLoader.getInstance().getCreatives())
                );

                String headerLine = "Date;Screen;Creative;Impressions;Spend";

                MessageCode code = ExcelHandler.writeExcelWithInfo(
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
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportToolGui().setVisible(true));
    }
}

