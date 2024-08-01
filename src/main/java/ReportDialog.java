import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class ReportDialog extends JDialog {
    private DatabaseManager dbManager;
    private JTable reportTable;
    private DefaultTableModel tableModel;

    public ReportDialog(MenuButtonPanel parentFrame, DatabaseManager dbManager) {
        super();
        this.dbManager = dbManager;
        setLayout(new BorderLayout());

        setPreferredSize(new Dimension(800, 600));

        // Cria o modelo da tabela
        String[] columnNames = {"Descrição", "Tipo", "Quantidade", "Valor", "Data"};
        tableModel = new DefaultTableModel(columnNames, 0);
        reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // Carrega os dados na tabela
        loadReportData();

        pack();
        setLocationRelativeTo(parentFrame);
    }

    private void loadReportData() {
        String query = "SELECT descricao, tipo_transacao, quantidade, valor, data FROM transacoes";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                String description = rs.getString("descricao");
                String type = rs.getString("tipo_transacao");
                int quantity = rs.getInt("quantidade");
                double value = rs.getDouble("valor");
                java.sql.Timestamp timestamp = rs.getTimestamp("data");
                String date = timestamp.toLocalDateTime().toString(); // Formato de data e hora

                tableModel.addRow(new Object[]{description, type, quantity, value, date});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar os dados de relatórios", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
