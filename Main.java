import java.sql.Connection;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		try (Connection conexao = ConexaoDB.conectar()) {
			ProdutoDAO produtoDAO = new ProdutoDAO(conexao);

			// listar todos os produtos (deve estar vazio neste ponto)
			mostrarProdutos(produtoDAO);

			// exemplo de inserção de produtos
			Produto novoProduto1 = new Produto("Notebook", 10, 1999.99, "Em estoque");
			Produto novoProduto2 = new Produto("Smartphone", 20, 1499.99, "Estoque baixo");
			Produto novoProduto3 = new Produto("Tablet", 15, 799.99, "Estoque baixo");

			produtoDAO.inserir(novoProduto1);
			produtoDAO.inserir(novoProduto2);
			produtoDAO.inserir(novoProduto3);

			// listar todos os produtos após inserção
			mostrarProdutos(produtoDAO);

			// exemplo de consulta por ID:
			Produto produtoConsultado = produtoDAO.consultarPorId(1);
			if (produtoConsultado != null) {
				System.out.println("Produto encontrado: " + produtoConsultado.getNome());
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