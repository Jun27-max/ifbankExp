package ifbank.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ifbank.modelo.Pessoa;

public class PessoaDAO {
	private IFBankDataSource dataSource = new IFBankDataSource();
	
	public Pessoa findByNumeroConta(int numeroConta) throws DataAccessException {
		Pessoa pessoa = null;
		
		try (Connection conn = dataSource.getConnection();
				PreparedStatement ps = conn
						.prepareStatement("select p.nome, p.id from pessoa p inner join conta c on c.titular = p.id "
								+ "where c.numero = ?;")) {
			ps.setInt(1, numeroConta);

			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					pessoa = new Pessoa();
					pessoa.setId(rs.getInt("id"));
					pessoa.setNome(rs.getString("nome"));					
				}
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
		
		return pessoa;
	}
}
