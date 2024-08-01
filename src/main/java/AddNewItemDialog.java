import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AddNewItemDialog extends JDialog {
    private DatabaseManager dbManager;
    private JTextField descriptionField;
    private JTextField volumeField;
    private JTextField priceField;
    private JTextField quantityField;
    private JButton addButton;

    public AddNewItemDialog(PurchaseDialog parentFrame, DatabaseManager dbManager) {
        super(parentFrame, "Adicionar Novo Item", true);
        this.dbManager = dbManager;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Configura o tamanho preferido do diálogo
        setPreferredSize(new Dimension(500, 300));

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Descrição:"), gbc);

        gbc.gridx = 1;
        descriptionField = new JTextField(20);
        add(descriptionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Volume:"), gbc);

        gbc.gridx = 1;
        volumeField = new JTextField(20);
        add(volumeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Preço:"), gbc);

        gbc.gridx = 1;
        priceField = new JTextField(20);
        add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Quantidade:"), gbc);

        gbc.gridx = 1;
        quantityField = new JTextField(20);
        add(quantityField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        addButton = new JButton("Adicionar");
        addButton.addActionListener(new AddButtonListener());
        add(addButton, gbc);

        pack();
        setLocationRelativeTo(parentFrame);
    }

    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String description = descriptionField.getText();
            String volumeText = volumeField.getText();
            String priceText = priceField.getText();
            String quantityText = quantityField.getText();

            if (description.isEmpty() || volumeText.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(AddNewItemDialog.this, "Por favor, preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int volume;
            double price;
            int quantity;

            try {
                volume = Integer.parseInt(volumeText);
                price = Double.parseDouble(priceText);
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(AddNewItemDialog.this, "Dados inválidos. Verifique o volume, preço e quantidade.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Statement stmt = dbManager.getConnection().createStatement()) {
                String sql = "INSERT INTO produtos (descricao, volume, preco, estoque) VALUES ('" +
                        description + "', " + volume + ", " + price + ", " + quantity + ")";
                stmt.executeUpdate(sql);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(AddNewItemDialog.this, "Erro ao adicionar novo item", "Erro", JOptionPane.ERROR_MESSAGE);
            }

            // Registrar a transação de compra
            try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(
                    "INSERT INTO transacoes (descricao, tipo_transacao, quantidade, valor) VALUES (?, 'compra', ?, ?)")) {
                pstmt.setString(1, description);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, quantity * price);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(AddNewItemDialog.this, "Erro ao registrar a transação.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(AddNewItemDialog.this, "Novo item adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Fecha o diálogo
        }
    }
}
