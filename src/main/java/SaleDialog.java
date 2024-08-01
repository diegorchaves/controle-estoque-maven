import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SaleDialog extends JDialog {
    private DatabaseManager dbManager;
    private JComboBox<String> itemComboBox;
    private JTextField quantityField;
    private JLabel volumeLabel;
    private JLabel priceLabel;
    private JLabel stockLabel;
    private JButton sellButton;

    public SaleDialog(JFrame parentFrame, DatabaseManager dbManager) {
        super();
        this.dbManager = dbManager;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        setPreferredSize(new Dimension(400, 300));

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Item:"), gbc);

        gbc.gridx = 1;
        itemComboBox = new JComboBox<>();
        itemComboBox.addActionListener(new ItemSelectionListener());
        add(itemComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Volume:"), gbc);

        gbc.gridx = 1;
        volumeLabel = new JLabel();
        add(volumeLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Preço:"), gbc);

        gbc.gridx = 1;
        priceLabel = new JLabel();
        add(priceLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Estoque:"), gbc);

        gbc.gridx = 1;
        stockLabel = new JLabel();
        add(stockLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Quantidade:"), gbc);

        gbc.gridx = 1;
        quantityField = new JTextField(10);
        add(quantityField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        sellButton = new JButton("Vender");
        sellButton.addActionListener(new SellButtonListener());
        add(sellButton, gbc);

        loadItems();

        pack();
        setLocationRelativeTo(parentFrame);
    }

    private void loadItems() {
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT descricao FROM produtos")) {

            itemComboBox.removeAllItems();

            while (rs.next()) {
                itemComboBox.addItem(rs.getString("descricao"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar os itens", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItemDetails(String itemDescription) {
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(
                "SELECT volume, preco, estoque FROM produtos WHERE descricao = ?")) {
            pstmt.setString(1, itemDescription);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double volume = rs.getDouble("volume");
                    double price = rs.getDouble("preco");
                    int stock = rs.getInt("estoque");

                    volumeLabel.setText(String.valueOf(volume));
                    priceLabel.setText(String.format("%.2f", price));
                    stockLabel.setText(String.valueOf(stock));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar as informações do item", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ItemSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedItem = (String) itemComboBox.getSelectedItem();
            if (selectedItem != null) {
                updateItemDetails(selectedItem);
            }
        }
    }

    private class SellButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String itemDescription = (String) itemComboBox.getSelectedItem();
            String quantityText = quantityField.getText();

            if (itemDescription == null || quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(SaleDialog.this, "Por favor, preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(SaleDialog.this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(
                    "SELECT preco, estoque FROM produtos WHERE descricao = ?")) {
                pstmt.setString(1, itemDescription);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double price = rs.getDouble("preco");
                        int stock = rs.getInt("estoque");

                        if (quantity > stock) {
                            JOptionPane.showMessageDialog(SaleDialog.this, "Quantidade vendida não pode ser maior que o estoque disponível.", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Atualiza o estoque
                        try (PreparedStatement updatePstmt = dbManager.getConnection().prepareStatement(
                                "UPDATE produtos SET estoque = estoque - ? WHERE descricao = ?")) {
                            updatePstmt.setInt(1, quantity);
                            updatePstmt.setString(2, itemDescription);
                            updatePstmt.executeUpdate();
                        }

                        // Registra a transação
                        try (PreparedStatement insertPstmt = dbManager.getConnection().prepareStatement(
                                "INSERT INTO transacoes (descricao, tipo_transacao, quantidade, valor) VALUES (?, 'venda', ?, ?)")) {
                            insertPstmt.setString(1, itemDescription);
                            insertPstmt.setInt(2, quantity);
                            insertPstmt.setDouble(3, quantity * price);
                            insertPstmt.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(SaleDialog.this, "Venda realizada com sucesso! Total a pagar: " + (quantity * price), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Fecha o diálogo
                    } else {
                        JOptionPane.showMessageDialog(SaleDialog.this, "Item não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(SaleDialog.this, "Erro ao processar a venda.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
