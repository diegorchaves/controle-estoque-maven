import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class StockInquiryDialog extends JDialog {
    private DatabaseManager dbManager;
    private JTable stockTable;
    private DefaultTableModel tableModel;

    public StockInquiryDialog(MenuButtonPanel parentFrame, DatabaseManager dbManager) {
        super();
        this.dbManager = dbManager;
        setLayout(new BorderLayout());

        // Configura o tamanho preferido do diálogo
        setPreferredSize(new Dimension(800, 600));

        // Cria o modelo da tabela
        String[] columnNames = {"Descrição", "Volume", "Preço", "Estoque"};
        tableModel = new DefaultTableModel(columnNames, 0);
        stockTable = new JTable(tableModel);

        // Adiciona a tabela em um JScrollPane
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Adiciona o scrollPane à janela
        add(scrollPane, BorderLayout.CENTER);

        // Carrega os dados na tabela
        loadStockData();

        pack();
        setLocationRelativeTo(parentFrame);
    }

    private void loadStockData() {
        String query = "SELECT descricao, volume, preco, estoque FROM produtos";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Limpa o modelo da tabela
            tableModel.setRowCount(0);

            // Adiciona os dados à tabela
            while (rs.next()) {
                String description = rs.getString("descricao");
                int volume = rs.getInt("volume");
                double price = rs.getDouble("preco");
                int stock = rs.getInt("estoque");
                tableModel.addRow(new Object[]{description, volume, price, stock});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar os dados de estoque", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
