package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Product;
import util.DBConnection;

/**
 * DAO (Data Access Object) for Product table.
 *
 * Uses PreparedStatement for all queries.
 */
public class ProductDAO {

    // SQL (as given in requirement)
    private static final String INSERT_SQL =
            "INSERT INTO products(name, description, price, quantity) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL =
            "SELECT * FROM products";
    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM products WHERE id = ?";
    private static final String UPDATE_SQL =
            "UPDATE products SET name=?, description=?, price=?, quantity=? WHERE id=?";
    private static final String DELETE_SQL =
            "DELETE FROM products WHERE id=?";

    public boolean addProduct(Product product) {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("DB Error (addProduct): " + e.getMessage());
            return false;
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapRowToProduct(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("DB Error (getAllProducts): " + e.getMessage());
        }

        return products;
    }

    public Product getProductById(int id) {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToProduct(resultSet);
                }
            }
        } catch (SQLException e) {
            System.out.println("DB Error (getProductById): " + e.getMessage());
        }

        return null;
    }

    public boolean updateProduct(Product product) {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());
            statement.setInt(5, product.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("DB Error (updateProduct): " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("DB Error (deleteProduct): " + e.getMessage());
            return false;
        }
    }

    private Product mapRowToProduct(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        double price = resultSet.getDouble("price");
        int quantity = resultSet.getInt("quantity");

        return new Product(id, name, description, price, quantity);
    }
}
