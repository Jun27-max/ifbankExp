package ifbank.console;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Scanner;

public class TransferApp {
	private static final Locale BRAZIL = new Locale("pt", "br");

	private Connection conn;

	private static final String URL_TEMPLATE = "jdbc:mysql://localhost/%s?user=%s&password=%s";

	private Connection getConnection(String dbName, String username, String password) throws SQLException {
		String url = String.format(URL_TEMPLATE, dbName, username, password);
		return DriverManager.getConnection(url);
	}

	private void connectDatabase() throws SQLException {
		conn = getConnection("financas", "junior", "jun@");
		conn.setAutoCommit(false);
		System.out.println("Database: connected");
	}

	private void releaseConnection() {
		if (conn != null) {
			try {
				conn.close();
				System.out.println("Database connection released.");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void execute(String[] args) throws SQLException {
		int contaOrigem = 0;
		int contaDestino = 0;
		double valor = 0;
		char keepRunning = 'S';

		connectDatabase();

		Scanner sc = new Scanner(System.in);
		sc.useLocale(BRAZIL);

		while (keepRunning == 'S') {
			System.out.println("Transferencias on-line");

			System.out.print("Conta de origem: ");
			contaOrigem = sc.nextInt();

			System.out.print("Conta de destino: ");
			contaDestino = sc.nextInt();

			System.out.print("Valor (formato: 0,00): ");
			valor = sc.nextDouble();

			String titularOrigem = mostrarTitular(contaOrigem);
			if (titularOrigem == null) {
				System.out.println("Conta não encontrada");
				continue;
			}

			String titularDestino = mostrarTitular(contaDestino);
			if (titularDestino == null) {
				System.out.println("Conta não encontrada");
				continue;
			}

			System.out.println();
			System.out.println("== Transferindo ==");
			System.out.printf("%s -> %s\n", titularOrigem, titularDestino);
			System.out.println("Valor: " + String.format(BRAZIL, "%.2f", valor));

			try (PreparedStatement saque = conn
					.prepareStatement("UPDATE conta SET saldo = (saldo - ?) WHERE numero = ?;");
					PreparedStatement deposito = conn
							.prepareStatement("UPDATE conta SET saldo = (saldo + ?) WHERE numero = ?;");
					PreparedStatement movimentacao = conn
							.prepareStatement("insert into movimentacao (origem, destino, valor) values (?,?,?);");) {

				saque.setDouble(1, valor);
				saque.setInt(2, contaOrigem);
				saque.executeUpdate();

				deposito.setDouble(1, valor);
				deposito.setInt(2, contaDestino);
				deposito.executeUpdate();

				movimentacao.setInt(1, contaOrigem);
				movimentacao.setInt(2, contaDestino);
				movimentacao.setDouble(3, valor);
				movimentacao.executeUpdate();

				conn.commit();

			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}

			System.out.print("Continuar [S/N]? ");
			keepRunning = sc.next().toUpperCase().charAt(0);
		}

		releaseConnection();

		sc.close();

		System.out.println("-- fim --");
	}

	private String mostrarTitular(int numeroConta) throws SQLException {

		String nome = null;

		try (PreparedStatement ps = conn.prepareStatement(
				"select p.nome from pessoa p inner join conta c on c.titular = p.id where c.numero = ?;")) {
			ps.setInt(1, numeroConta);

			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					nome = rs.getString("nome");
				}
			}
		}
		return nome;

	}

	public static void main(String[] args) {
		try {
			new TransferApp().execute(args);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
