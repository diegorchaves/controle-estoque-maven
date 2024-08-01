import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PurchaseDialog extends JDialog {
    private DatabaseManager dbManager;
    private JComboBox<String> itemComboBox;
    private JTextField quantityField;
    private JButton purchaseButton;
    private JButton addItemButton;

    public PurchaseDialog(MenuButtonPanel parentFrame, DatabaseManager dbManager) {
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
        add(itemComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Quantidade:"), gbc);

        gbc.gridx = 1;
        quantityField = new JTextField(10);
        add(quantityField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        purchaseButton = new JButton("Comprar");
        purchaseButton.addActionListener(new PurchaseButtonListener());
        add(purchaseButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        addItemButton = new JButton("Adicionar Novo Item");
        addItemButton.addActionListener(new AddItemButtonListener());
        add(addItemButton, gbc);

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

    private class PurchaseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String itemDescription = (String) itemComboBox.getSelectedItem();
            String quantityText = quantityField.getText();

            if (itemDescription == null || quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(PurchaseDialog.this, "Por favor, preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(PurchaseDialog.this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Statement stmt = dbManager.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT preco, estoque FROM produtos WHERE descricao = '" + itemDescription + "'")) {

                if (rs.next()) {
                    double price = rs.getDouble("preco");

                    // Atualiza o estoque
                    try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(
                            "UPDATE produtos SET estoque = estoque + ? WHERE descricao = ?")) {
                        pstmt.setInt(1, quantity);
                        pstmt.setString(2, itemDescription);
                        pstmt.executeUpdate();
                    }

                    // Registra a transação
                    try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(
                            "INSERT INTO transacoes (descricao, tipo_transacao, quantidade, valor) VALUES (?, 'compra', ?, ?)")) {
                        pstmt.setString(1, itemDescription);
                        pstmt.setInt(2, quantity);
                        pstmt.setDouble(3, quantity * price);
                        pstmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(PurchaseDialog.this, "Compra realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Fecha o diálogo
                } else {
                    JOptionPane.showMessageDialog(PurchaseDialog.this, "Item não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(PurchaseDialog.this, "Erro ao processar a compra.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class AddItemButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new AddNewItemDialog(PurchaseDialog.this, dbManager).setVisible(true);
        }
    }
}
