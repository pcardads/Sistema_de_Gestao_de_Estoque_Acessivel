import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

public class ProdutoGUI extends Application {
	private ProdutoDAO produtoDAO;
	private ObservableList<Produto> produtos;
	private TableView<Produto> tableView;
	private TextField nomeInput, quantidadeInput, precoInput;
	private ComboBox<String> statusComboBox;
	private Connection conexaoDB;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage palco) {
		conexaoDB = ConexaoDB.conectar();
		produtoDAO = new ProdutoDAO(conexaoDB); // inicializa o DAO 
		// Inicializa a lista vazia para a interface carregar imediatamente
		produtos = FXCollections.observableArrayList();

		// OTIMIZAÇÃO DE PERFORMANCE: Carregamento assíncrono em Background Thread
		Thread threadCarregamento = new Thread(() -> {
			// 1. Vai à base de dados num processo paralelo (não bloqueia a interface)
			List<Produto> dadosDoBanco = produtoDAO.listarTodos();

			// 2. Devolve os dados à Thread principal da interface (JavaFX Application Thread) de forma segura
			javafx.application.Platform.runLater(() -> {
				produtos.setAll(dadosDoBanco);
			});
		});
		threadCarregamento.setDaemon(true); // Garante que a thread morre se a aplicação fechar
		threadCarregamento.start();

		palco.setTitle("Gerenciamento de Estoque de Produtos");

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10, 10, 10, 10)); // distancia entre o conteudo e as bordas em pixels
		vbox.setSpacing(10);

		HBox nomeProdutoBox = new HBox();
		nomeProdutoBox.setSpacing(10);
		Label nomeLabel = new Label("Produto: ");
		nomeInput = new TextField();
		nomeProdutoBox.getChildren().addAll(nomeLabel, nomeInput);

		HBox quantidadeBox = new HBox();
		quantidadeBox.setSpacing(10);
		Label quantidadeLabel = new Label("Quantidade: ");
		quantidadeInput = new TextField();
		quantidadeBox.getChildren().addAll(quantidadeLabel, quantidadeInput);

		HBox precoBox = new HBox();
		precoBox.setSpacing(10);
		Label precoLabel = new Label("Preco: ");
		precoInput = new TextField();
		precoBox.getChildren().addAll(precoLabel, precoInput);

		HBox statusBox = new HBox();
		statusBox.setSpacing(10);
		Label statusLabel = new Label("Status:");
		statusComboBox = new ComboBox<>();
		statusComboBox.getItems().addAll("Estoque Normal", "Estoque Baixo");
		statusBox.getChildren().addAll(statusLabel, statusComboBox);

		Button addButton = new Button("Adicionar");
		addButton.setOnAction(e -> {
			try {
				// 1. Validação de campos vazios (Evita falhas silenciosas)
				if (nomeInput.getText().trim().isEmpty() ||
						quantidadeInput.getText().trim().isEmpty() ||
						precoInput.getText().trim().isEmpty() ||
						statusComboBox.getValue() == null) {

					mostrarAlerta("Erro de Validação", "Por favor, preencha todos os campos obrigatórios.", Alert.AlertType.WARNING);
					return; // Interrompe a execução antes de tentar gravar
				}

				// 2. Conversão segura de dados
				String preco = precoInput.getText().replace(',', '.');
				int quantidade = Integer.parseInt(quantidadeInput.getText());
				double precoFormatado = Double.parseDouble(preco);

				// 3. Gravação
				Produto produto = new Produto(nomeInput.getText(), quantidade, precoFormatado, statusComboBox.getValue());
				produtoDAO.inserir(produto);
				// OTIMIZAÇÃO DE PERFORMANCE: Adicionamos apenas o novo item à lista em memória.
				// A base de dados já não é sobrecarregada com leituras massivas.
				produtos.add(produto);
				limparCampos();

				// Feedback positivo opcional
				mostrarAlerta("Sucesso", "Produto adicionado com sucesso!", Alert.AlertType.INFORMATION);

			} catch (NumberFormatException ex) {
				// Captura o erro de conversão e avisa o utilizador de forma clara
				mostrarAlerta("Erro de Formatação", "A quantidade e o preço devem ser números válidos.", Alert.AlertType.ERROR);
			}
		});

		Button updateButton = new Button("Atualizar");
		updateButton.setOnAction(e -> {
			try {
				Produto selectedProduto = tableView.getSelectionModel().getSelectedItem(); // obtém o produto selecionado

				// Verifica se há algum produto selecionado na tabela
				if (selectedProduto == null) {
					mostrarAlerta("Aviso", "Selecione um produto na tabela para atualizar.", Alert.AlertType.WARNING);
					return;
				}

				// Validação de campos vazios
				if (nomeInput.getText().trim().isEmpty() ||
						quantidadeInput.getText().trim().isEmpty() ||
						precoInput.getText().trim().isEmpty() ||
						statusComboBox.getValue() == null) {

					mostrarAlerta("Erro de Validação", "Por favor, preencha todos os campos obrigatórios.", Alert.AlertType.WARNING);
					return;
				}

				// Atualização e conversão segura
				selectedProduto.setNome(nomeInput.getText());
				selectedProduto.setQuantidade(Integer.parseInt(quantidadeInput.getText()));
				String preco = precoInput.getText().replace(',', '.');
				selectedProduto.setPreco(Double.parseDouble(preco));
				selectedProduto.setStatus(statusComboBox.getValue());

				produtoDAO.atualizar(selectedProduto);
				produtos.setAll(produtoDAO.listarTodos());
				limparCampos();

				mostrarAlerta("Sucesso", "Produto atualizado com sucesso!", Alert.AlertType.INFORMATION);

			} catch (NumberFormatException ex) {
				mostrarAlerta("Erro de Formatação", "A quantidade e o preço devem ser números válidos.", Alert.AlertType.ERROR);
			}
		});

		Button deleteButton = new Button("Excluir");
		deleteButton.setOnAction(e -> {
			Produto selectedProduto = tableView.getSelectionModel().getSelectedItem();

			if (selectedProduto != null) {
				produtoDAO.excluir(selectedProduto.getId()); // apaga na base de dados

				// OTIMIZAÇÃO DE PERFORMANCE: Remove apenas o item da lista em memória
				// em vez de recarregar toda a base de dados novamente
				produtos.remove(selectedProduto);

				limparCampos();
				mostrarAlerta("Sucesso", "Produto excluído com sucesso!", Alert.AlertType.INFORMATION);
			} else {
				mostrarAlerta("Aviso", "Selecione um produto na tabela para excluir.", Alert.AlertType.WARNING);
			}
		});

		Button clearButton = new Button("Limpar");
		clearButton.setOnAction(e -> limparCampos());

		tableView = configurarTabela();

		HBox buttonBox = new HBox();
		buttonBox.setSpacing(10);
		buttonBox.getChildren().addAll(addButton, updateButton, deleteButton, clearButton); // adiciona os botoes ao hbox

		vbox.getChildren().addAll(nomeProdutoBox, quantidadeBox, precoBox, statusBox, buttonBox, tableView);

		Scene scene = new Scene(vbox, 800, 600);
		scene.getStylesheets().add("styles-produtos.css");
		palco.setScene(scene);
		palco.show();
	}

	/**
	 * o método stop é automaticamente chamado quando a aplicação JavaFX é encerrada
	 */

	@Override
	public void stop() {
		try {
			conexaoDB.close();
		} catch (SQLException e) {
			System.err.println("Erro ao fechar conexao." + e.getMessage());
		}
	}

	/**
	 * Limpa os campos de entrada do formulario
	 * Chamado apos adicionar, atualizar ou excluir um produto
	 * Garante que os campos de entrada estejam prontos para uma nova entrada
	 */

	private void limparCampos() {
		nomeInput.clear();
		quantidadeInput.clear();
		precoInput.clear();
		statusComboBox.setValue(null);
	}

	/**
	 * Cria uma coluna para a TableView
	 * @param title o título da coluna que será exibido no cabeçalho
	 * @param property propriedade do objeto Produto que esta coluna deve exibir
	 * @return a coluna configurada para a TableView
	 */

	private TableColumn<Produto, String> criarColuna(String title, String property) {
		TableColumn<Produto, String> col = new TableColumn<>(title);
		col.setCellValueFactory(new PropertyValueFactory<>(property)); // define a propriedade da coluna
		return col;
	}

	/**
	 * Exibe um alerta visual para o utilizador.
	 * Ajuda a cumprir os requisitos de acessibilidade e tratamento de erros.
	 */
	private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
		Alert alerta = new Alert(tipo);
		alerta.setTitle(titulo);
		alerta.setHeaderText(null); // Remove o cabeçalho extra para um visual mais limpo
		alerta.setContentText(mensagem);
		alerta.showAndWait();
	}

	/**
	 * Isola a lógica de criação e configuração da tabela.
	 * Aplicação de Clean Code (Extract Method) para reduzir a complexidade do método principal.
	 */
	private TableView<Produto> configurarTabela() {
		TableView<Produto> tabela = new TableView<>();
		tabela.setItems(produtos);
		tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		List<TableColumn<Produto, ?>> columns = List.of(
				criarColuna("ID", "id"),
				criarColuna("Produto", "nome"),
				criarColuna("Quantidade", "quantidade"),
				criarColuna("Preço", "preco"),
				criarColuna("Status", "status")
		);
		tabela.getColumns().addAll(columns);

		// Listener para preencher os inputs quando clicar em uma linha
		tabela.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				nomeInput.setText(newSelection.getNome());
				quantidadeInput.setText(String.valueOf(newSelection.getQuantidade()));
				precoInput.setText(String.valueOf(newSelection.getPreco()));
				statusComboBox.setValue(newSelection.getStatus());
			}
		});

		return tabela;
	}
}
