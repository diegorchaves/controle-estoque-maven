import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/db-estoque";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";

    private Connection connection;

    public DatabaseManager() {
        // Tenta estabelecer uma conexão com o banco de dados ao criar a instância
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão com o banco de dados estabelecida com sucesso.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Falha ao conectar ao banco de dados.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexão com o banco de dados fechada.");
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Erro ao fechar a conexão com o banco de dados.");
            }
        }
    }

    // Método para executar uma consulta simples (exemplo)
    public void executeQuery(String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            System.out.println("Consulta executada: " + sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
