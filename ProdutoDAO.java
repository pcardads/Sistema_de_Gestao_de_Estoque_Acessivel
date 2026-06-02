import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
	private final Connection CONEXAO_DB;

	// construtor que inicializa a conexao com o banco de dados
	public ProdutoDAO(Connection conexao) {
		this.CONEXAO_DB = conexao;
	}

	// metodo para inserir produto no banco de dados
	public void inserir(Produto produto) {
		String sql = "INSERT INTO produtos (nome_produto, quantidade, preco, status) VALUES (?, ?, ?, ?)";
		// Solicitamos as chaves geradas automaticamente
		try (PreparedStatement stmt = CONEXAO_DB.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, produto.getNome());
			stmt.setInt(2, produto.getQuantidade());
			stmt.setDouble(3, produto.getPreco());
			stmt.setString(4, produto.getStatus());
			stmt.executeUpdate();

			// Otimização: Recuperar o ID gerado para evitar fazer um SELECT a toda a tabela
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					produto.setId(generatedKeys.getInt(1)); // Define o ID real no objeto
				}
			}
		} catch (SQLException e) {
			System.err.println("Erro ao inserir produto: " + e.getMessage());
		}
	}

	// metodo para excluir todos os produtos do banco de dados
	public void excluirTodos() {
		String sql = "DELETE FROM produtos";
		try (PreparedStatement stmt = CONEXAO_DB.prepareStatement(sql)) {
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Erro ao excluir todos os produtos: " + e.getMessage());
		}
	}

	public Produto consultarPorId(int id) {
		String sql = "SELECT * FROM produtos WHERE id_produto = ?";
		try (PreparedStatement stmt = CONEXAO_DB.prepareStatement(sql)) {
			stmt.setInt(1, id); // definindo o paramentro do id antes de executar a consultar
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Produto produto = new Produto();
					produto.setId(rs.getInt("id_produto"));
					produto.setNome(rs.getString("nome_produto"));
					produto.setQuantidade(rs.getInt("quantidade"));
					produto.setPreco(rs.getDouble("preco"));
					produto.setStatus(rs.getString("status"));
					return produto;
				}
			}
		} catch (SQLException e) {
			System.out.println("Erro ao consultar produto por ID: " + e.getMessage());
		}
		return null;
	}

	// metodo para atualizar informacoes do produto no banco de dados
	public void atualizar(Produto produto) {
		String sql = "UPDATE produtos SET nome_produto = ?, quantidade = ?, preco = ?, status = ? WHERE id_produto = ?";
		try (PreparedStatement stmt = CONEXAO_DB.prepareStatement(sql)) {
			stmt.setString(1, produto.getNome());
			stmt.setInt(2, produto.getQuantidade());
			stmt.setDouble(3, produto.getPreco());
			stmt.setString(4, produto.getStatus());
			stmt.setInt(5, produto.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Erro ao atualizar produto: " + e.getMessage());
		}
	}

	// metodo para excluir um produto especifico
	public void excluir(int id) {
		String sql = "DELETE FROM produtos WHERE id_produto = ?";
		try (PreparedStatement stmt = CONEXAO_DB.prepareStatement(sql)) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Erro ao excluir produto: " + e.getMessage());
		}
	}

	// metodo para listar todos os produtos do banco de dados
	public List<Produto> listarTodos() {
		List<Produto> produtos = new ArrayList<>();
		String sql = "SELECT * FROM produtos";
		try (PreparedStatement stmt = CONEXAO_DB.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				Produto produto = new Produto();
				produto.setId(rs.getInt("id_produto"));
				produto.setNome(rs.getString("nome_produto"));
				produto.setQuantidade(rs.getInt("quantidade"));
				produto.setPreco(rs.getDouble("preco"));
				produto.setStatus(rs.getString("status"));
				produtos.add(produto);
			}
		} catch (SQLException e) {
			System.err.println("Erro ao listar produtos: " + e.getMessage());
		}
		return produtos;
	}
}