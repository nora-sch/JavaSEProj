package fr.eni.papeterie.dal.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;


import fr.eni.papeterie.bo.Article;
import fr.eni.papeterie.bo.Ramette;
import fr.eni.papeterie.bo.Stylo;
import fr.eni.papeterie.dal.DALException;

public class ArticleDaoJdbcImpl {
	private static final String TYPE_STYLO = "STYLO";
	private static final String TYPE_RAMETTE = "RAMETTE";

	private Connection connection = null;

	static {
		// charger le driver jdbc en mémoire
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public ArticleDaoJdbcImpl() {

	}

	// connexion 
	public Connection getConnection() throws SQLException {
		String url = "jdbc:sqlserver://localhost:1433;databaseName=PAPETERIE_DB";
		connection = DriverManager.getConnection(url, "sa", "rt");
		return connection;
	}

	public void closeConnection(){
		if(connection != null){
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection=null;
		}
	}

	// méthodes
	public void insert(Article a) throws DALException  {
		//TODO verifier si un article existe déjà avec des parametres d'instance particuliers
		String sql = "INSERT INTO Articles (reference,marque,designation,prixUnitaire,qteStock,type,grammage,couleur) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, a.getReference());
			stmt.setString(2, a.getMarque());
			stmt.setString(3, a.getDesignation());
			stmt.setFloat(4, a.getPrixUnitaire());
			stmt.setInt(5, a.getQteStock());
			if (a instanceof Ramette) {
				Ramette r = (Ramette) a;
				stmt.setString(6, TYPE_RAMETTE);
				stmt.setInt(7, r.getGrammage());
				stmt.setNull(8, Types.VARCHAR);
			}
			if (a instanceof Stylo) {
				Stylo s = (Stylo) a;
				stmt.setString(6, TYPE_STYLO);
				stmt.setNull(7, Types.INTEGER);
				stmt.setString(8, s.getCouleur());
			}
			// ---------------------------
			int result = stmt.executeUpdate();
			// récuperer et set dernière id géneré à l'instance d'Article
			if (result == 1) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					a.setIdArticle(rs.getInt(1));
				}
			}

		} catch (SQLException e) {
			throw new DALException("Insert article failed - " + a, e);
		}
		finally {
			try {
				if (stmt != null) {
					stmt.close();
				}

			} catch (SQLException e) {
				throw new DALException("close failed - ", e);
			}
			closeConnection();
		}
	}

	public void update(Article a) {

	}
	public Article selectById(int idArticle) throws DALException {
		Article a = null;

		String sql = "SELECT * FROM Articles WHERE idArticle = ?";
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setInt(1, idArticle);
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				// trim pour reference et type, car dans la bdd ils sont stockés comme un nchar10
				if (TYPE_STYLO.equalsIgnoreCase(result.getString("type").trim())) {
					a =	new Stylo(result.getInt("idArticle"), result.getString("marque"), result.getString("reference").trim(),
							result.getString("designation"), result.getFloat("prixUnitaire"), result.getInt("qteStock"),
							result.getString("couleur"));
				}
				if (TYPE_RAMETTE.equalsIgnoreCase(result.getString("type").trim())) {
					a =	new Ramette(result.getInt("idArticle"), result.getString("marque"), result.getString("reference").trim(),
							result.getString("designation"), result.getFloat("prixUnitaire"), result.getInt("qteStock"),
							result.getInt("grammage"));
				}

			}
			if(a == null) {
				System.out.println("Article with id - " + idArticle + " not found ");
				throw new DALException();
				
			}
		}
		catch(SQLException e) {

			throw new DALException("Article with id - " + idArticle + " not found ", e);
		}

		finally {
			try {
				if (stmt != null) {
					stmt.close();
				}

			} catch (SQLException e) {
				throw new DALException("close failed - ", e);
			}
			closeConnection();
		}
		return a;
	}

	public List<Article> selectAll() {
		return null;
	}
	
	public void delete(Integer idArticle) throws DALException {
		String sql = "DELETE FROM Articles WHERE idArticle = ?";
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement(sql);
			//TODO if is getById than:
			stmt.setInt(1, idArticle);
			int result = stmt.executeUpdate();
			System.out.println("Number of deleted records: "+ result);
			System.out.println("Article with id: "+ idArticle+ " has been deleted");
			//TODO else syso article with this id not exist BREAK
		} catch (SQLException e) {
			throw new DALException("Delete article failed - " + idArticle, e);
		}
		finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				throw new DALException("close failed - ", e);
			}
			closeConnection();
		}
	}
}
