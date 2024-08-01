import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class MenuButtonPanel extends JPanel {
    private final DatabaseManager dbManager;

    public MenuButtonPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;

        // Configuração do layout do painel
        setLayout(new GridLayout(2, 2, 10, 10)); // 2 linhas, 2 colunas, espaçamento de 10px

        // Adiciona os botões ao painel com ícones
        add(createButtonWithIcon("Venda", "/images/venda.png", "Venda"));
        add(createButtonWithIcon("Compra", "/images/compra.png", "Compra"));
        add(createButtonWithIcon("Estoque", "/images/estoque.png", "Estoque"));
        add(createButtonWithIcon("Relatório", "/images/relatorio.png", "Relatório"));
    }

    private JButton createButtonWithIcon(String text, String iconPath, String command) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);

        // Carregar ícone a partir dos recursos
        URL iconUrl = getClass().getResource(iconPath);
        if (iconUrl != null) {
            button.setIcon(resizeIcon(new ImageIcon(iconUrl), 400, 400));

            // Listener para redimensionar o ícone quando o tamanho do botão mudar
            button.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    int width = button.getWidth();
                    int height = button.getHeight();
                    button.setIcon(resizeIcon(new ImageIcon(iconUrl), width / 2, height / 2));
                }
            });
        } else {
            System.err.println("Ícone não encontrado: " + iconPath);
        }

        // Adiciona o ActionListener ao botão
        button.setActionCommand(command);
        button.addActionListener(new ButtonClickListener());

        return button;
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    // Classe interna que implementa ActionListener para lidar com cliques de botões
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "Venda":
                    // Exemplo de uso do dbManager para executar uma consulta
                    new SaleDialog((JFrame) SwingUtilities.getWindowAncestor(MenuButtonPanel.this), dbManager).setVisible(true);
                    break;
                case "Compra":
                    new PurchaseDialog(MenuButtonPanel.this, dbManager).setVisible(true);
                    break;
                case "Estoque":
                    new StockInquiryDialog(MenuButtonPanel.this, dbManager).setVisible(true);
                    break;
                case "Relatório":
                    new ReportDialog(MenuButtonPanel.this, dbManager).setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Comando desconhecido!");
                    break;
            }
        }
    }
}
