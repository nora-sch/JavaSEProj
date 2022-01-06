package fr.eni.papeterie.dal.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import fr.eni.papeterie.bo.Article;
import fr.eni.papeterie.bo.Ramette;
import fr.eni.papeterie.bo.Stylo;
import fr.eni.papeterie.dal.ArticleDAO;
import fr.eni.papeterie.dal.DALException;

public class ArticleDaoJdbcImpl implements ArticleDAO {
	private static final String TYPE_STYLO = "STYLO";
	private static final String TYPE_RAMETTE = "RAMETTE";

	// private Connection connection = null;

	// static {
	// // charger le driver jdbc en mémoire
	// try {
	// Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// }

	public ArticleDaoJdbcImpl() {

	}

	// méthodes

	public int existByRefAndPar(Article a) throws DALException {
		int exists = 0;
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		String sql = "";

		try {
			sql = "SELECT * FROM Articles WHERE reference = ?";
			con = JdbcTools.getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setString(1, a.getReference());
			result = stmt.executeQuery();
			if (result.next()) {
				if (TYPE_STYLO.equalsIgnoreCase(result.getString("type").trim())) {
					if (result.getString("couleur").equalsIgnoreCase(((Stylo) a).getCouleur())) {
						exists = result.getInt("idArticle");
					}
				}
				if (TYPE_RAMETTE.equalsIgnoreCase(result.getString("type").trim())) {
					if (((Integer) result.getInt("grammage")).toString()
							.equals(((Integer) ((Ramette) a).getGrammage()).toString())) {
						exists = result.getInt("idArticle");
					}
				}
			}
		} catch (SQLException e) {
			throw new DALException("Article not found ", e);
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return exists;
	}

	public void insert(Article a) throws DALException {
		String sql = "INSERT INTO Articles (reference,marque,designation,prixUnitaire,qteStock,type,grammage,couleur) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			// exists récupère l'id d'une article où la reference et parametres d'instance
			// sont les mêmes qu'à l'article qu'on veut insèrer
			// int exists = existByRefAndPar(a);
			// System.out.println(exists);
			// if(exists == 0) {
			con = JdbcTools.getConnection();
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
			stmt.executeUpdate();
			// récuperer et set dernière id géneré à l'instance d'Article
			result = stmt.getGeneratedKeys();
			if (result.next()) {
				a.setIdArticle(result.getInt(1));
			}
			// }else{
			// // si article est trouvé dans la bdd - seule la quantité est modifié (ajouté)
			// Article duplicat = selectById(exists);
			// updateQte(duplicat, a.getQteStock());
			// }

		} catch (SQLException e) {
			throw new DALException("Insert article failed - " + a, e);
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void update(Article a) throws DALException {
		String sql = "UPDATE Articles set reference=?, marque=?,designation=?,prixUnitaire=?,qteStock=?, grammage=?,couleur=? WHERE idArticle=?";
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = JdbcTools.getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setString(1, a.getReference());
			stmt.setString(2, a.getMarque());
			stmt.setString(3, a.getDesignation());
			stmt.setFloat(4, a.getPrixUnitaire());
			stmt.setInt(5, a.getQteStock());
			if (a instanceof Ramette) {
				Ramette r = (Ramette) a;
				stmt.setInt(6, r.getGrammage());
				stmt.setNull(7, Types.VARCHAR);
			}
			if (a instanceof Stylo) {
				Stylo s = (Stylo) a;
				stmt.setNull(6, Types.INTEGER);
				stmt.setString(7, s.getCouleur());
			}
			stmt.setInt(8, a.getIdArticle());
			// ---------------------------
			stmt.executeUpdate();
			System.out.println("article " + a.getReference() + " with id " + a.getIdArticle() + " has been updated");

		} catch (SQLException e) {
			throw new DALException("Update of article failed - " + a, e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public void updateQte(Article a, int plusQte) throws DALException {
		String sql = "UPDATE Articles set qteStock=? WHERE idArticle=?";
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = JdbcTools.getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setInt(1, (a.getQteStock() + plusQte));
			stmt.setInt(2, a.getIdArticle());
			// ---------------------------
			stmt.executeUpdate();
			System.out.println("article " + a.getReference() + " with id " + a.getIdArticle() + " has been updated");

		} catch (SQLException e) {
			throw new DALException("Update of quantity of article failed - " + a, e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Article selectById(int idArticle) throws DALException {
		Article a = null;
		String sql = "SELECT * FROM Articles WHERE idArticle = ?";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			con = JdbcTools.getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setInt(1, idArticle);
			result = stmt.executeQuery();
			if (result.next()) {
				// trim pour reference et type, car dans la bdd ils sont stockés comme un
				// nchar10
				if (TYPE_STYLO.equalsIgnoreCase(result.getString("type").trim())) {
					a = new Stylo(result.getInt("idArticle"), result.getString("marque"),
							result.getString("reference").trim(), result.getString("designation"),
							result.getFloat("prixUnitaire"), result.getInt("qteStock"), result.getString("couleur"));
				}
				if (TYPE_RAMETTE.equalsIgnoreCase(result.getString("type").trim())) {
					a = new Ramette(result.getInt("idArticle"), result.getString("marque"),
							result.getString("reference").trim(), result.getString("designation"),
							result.getFloat("prixUnitaire"), result.getInt("qteStock"), result.getInt("grammage"));
				}

			}
			if (a == null) {
				System.out.println("Article with id - " + idArticle + " not found ");
				throw new DALException();

			}
		} catch (SQLException e) {

			throw new DALException("Article with id - " + idArticle + " not found ", e);
		}

		finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return a;
	}

	public List<Article> selectAll() throws DALException {
		List<Article> articles = new ArrayList<Article>();
		Connection con = null;
		Statement stmt = null;
		ResultSet result = null;
		try {
			con = JdbcTools.getConnection();
			stmt = con.createStatement();
			result = stmt.executeQuery(
					"SELECT idArticle, reference, marque, designation, prixUnitaire, qteStock, grammage, couleur, type from Articles");
			Article a = null;
			while (result.next()) {
				if (TYPE_STYLO.equalsIgnoreCase(result.getString("type").trim())) {
					a = new Stylo(result.getInt("idArticle"), result.getString("reference").trim(),
							result.getString("marque"), result.getString("designation"),
							result.getFloat("prixUnitaire"), result.getInt("qteStock"), result.getString("couleur"));
				}
				if (TYPE_RAMETTE.equalsIgnoreCase(result.getString("type").trim())) {
					a = new Ramette(result.getInt("idArticle"), result.getString("reference").trim(),
							result.getString("marque"), result.getString("designation"),
							result.getFloat("prixUnitaire"), result.getInt("qteStock"), result.getInt("grammage"));
				}
				articles.add(a);
			}
		} catch (SQLException e) {
			throw new DALException("Selection of articles failed - " + e);
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return articles;
	}

	public void delete(int idArticle) throws DALException {
		String sql = "DELETE FROM Articles WHERE idArticle = ?";
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = JdbcTools.getConnection();
			stmt = con.prepareStatement(sql);
			// TODO if is getById than:
			stmt.setInt(1, idArticle);
			int result = stmt.executeUpdate();
			System.out.println("Number of deleted records: " + result);
			System.out.println("Article with id: " + idArticle + " has been deleted");
			// TODO else syso article with this id not exist BREAK
		} catch (SQLException e) {
			throw new DALException("Delete article failed - " + idArticle, e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void deleteAll() throws DALException {
		String sql = "TRUNCATE table Articles";
		Connection con = null;
		Statement stmt = null;
		try {
			int nbArticles = selectAll().size();
			con = JdbcTools.getConnection();
			stmt = con.createStatement();
			stmt.execute(sql);
			System.out.println("Content of Articles has been truncated");
			System.out.println(nbArticles + " articles deleted");

		} catch (SQLException e) {
			throw new DALException("Delete articles failed  ", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				JdbcTools.closeConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
