import java.sql.Connection;
import java.util.List;

public class Main2 {
	public static void main(String[] args) {
		try (Connection conexao = ConexaoDB.conectar()) {
			ProdutoDAO produtoDAO = new ProdutoDAO(conexao);

			// listar todos os produtos após inserção
			mostrarProdutos(produtoDAO);

			// exemplo de consulta por ID:
			Produto produtoConsultado = produtoDAO.consultarPorId(1);
			if (produtoConsultado != null) {
				produtoConsultado.setNome("Laptop");
				System.out.println("Novo nome do produto: " + produtoConsultado.getNome());
				produtoDAO.atualizar(produtoConsultado);

				System.out.println("A base de dados ficou assim:");
				mostrarProdutos(produtoDAO);
			} else {
				System.out.println("Produto nao encontrado.");
			}
		} catch (Exception e) {
			System.err.println("Erro geral: " + e.getMessage());
		}
	}

	// metodo para listar os produtos
	private static void mostrarProdutos(ProdutoDAO produtoDAO) {
		List<Produto> todosProdutos = produtoDAO.listarTodos();
		if (todosProdutos.isEmpty()) {
			System.out.println("Nenhum produto encontrado.");
		} else {
			System.out.println("Lista de produtos:");
			for (Produto p : todosProdutos) {
				System.out.println(p.getId() + ": " + p.getNome() + " - " + p.getPreco());
			}
		} 
	}
}