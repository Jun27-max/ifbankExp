package ifbank.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ifbank.modelo.Transferencia;

public class TransferenciaDAO {
	private IFBankDataSource dataSource = new IFBankDataSource();
	
	public void update(Transferencia transferencia) throws DataAccessException {
		try (Connection conn = dataSource.getConnection()) {
			conn.setAutoCommit(false);
			
			try (
					PreparedStatement saque = conn
							.prepareStatement("UPDATE conta SET saldo = (saldo - ?) WHERE numero = ?;");
					PreparedStatement deposito = conn
							.prepareStatement("UPDATE conta SET saldo = (saldo + ?) WHERE numero = ?;");
					PreparedStatement movimentacao = conn
							.prepareStatement("INSERT INTO movimentacao (origem, destino, valor) VALUES (?, ?, ?);");) {
				
			
				saque.setDouble(1, transferencia.getValor());
				saque.setInt(2, transferencia.getContaOrigem());
				saque.executeUpdate();
				
				deposito.setDouble(1, transferencia.getValor());
				deposito.setInt(2, transferencia.getContaDestino());
				deposito.executeUpdate();
				
				movimentacao.setInt(1, transferencia.getContaOrigem());
				movimentacao.setInt(2, transferencia.getContaDestino());
				movimentacao.setDouble(3, transferencia.getValor());
				movimentacao.executeUpdate();				
				
				conn.commit();

			} catch(SQLException e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}
}
