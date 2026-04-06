package main;

import java.util.List;
import java.util.Scanner;

import dao.ProductDAO;
import model.Product;

/**
 * Simple menu-driven CRUD application (no Spring Boot).
 *
 * Note:
 * - Make sure MySQL is running and you created the DB/table (see README + SQL file).
 * - Add MySQL JDBC driver (mysql-connector-j) jar into /lib and refresh VS Code Java
 *   dependencies if needed.
 */
public class App {

    private static final ProductDAO productDAO = new ProductDAO();

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMenu();

                int choice = readInt(scanner, "Enter choice: ");
                System.out.println();

                switch (choice) {
                    case 1:
                        addProduct(scanner);
                        break;
                    case 2:
                        viewAllProducts();
                        break;
                    case 3:
                        viewProductById(scanner);
                        break;
                    case 4:
                        updateProduct(scanner);
                        break;
                    case 5:
                        deleteProduct(scanner);
                        break;
                    case 6:
                        System.out.println("Exiting... Bye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please select 1 to 6.");
                }

                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println("===== Product CRUD (JDBC + MySQL) =====");
        System.out.println("1. Add Product");
        System.out.println("2. View All Products");
        System.out.println("3. View Product by ID");
        System.out.println("4. Update Product");
        System.out.println("5. Delete Product");
        System.out.println("6. Exit");
    }

    private static void addProduct(Scanner scanner) {
        System.out.println("---- Add Product ----");
        String name = readLine(scanner, "Name: ");
        String description = readLine(scanner, "Description: ");
        double price = readDouble(scanner, "Price: ");
        int quantity = readInt(scanner, "Quantity: ");

        Product product = new Product(name, description, price, quantity);
        boolean added = productDAO.addProduct(product);

        System.out.println(added ? "Product added successfully." : "Failed to add product.");
    }

    private static void viewAllProducts() {
        System.out.println("---- All Products ----");
        List<Product> products = productDAO.getAllProducts();

        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        for (Product product : products) {
            System.out.println(product);
        }
    }

    private static void viewProductById(Scanner scanner) {
        System.out.println("---- View Product by ID ----");
        int id = readInt(scanner, "Enter product id: ");

        Product product = productDAO.getProductById(id);
        System.out.println(product == null ? "Product not found." : product.toString());
    }

    private static void updateProduct(Scanner scanner) {
        System.out.println("---- Update Product ----");
        int id = readInt(scanner, "Enter product id to update: ");

        Product existing = productDAO.getProductById(id);
        if (existing == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("Current: " + existing);

        String name = readLine(scanner, "New name: ");
        String description = readLine(scanner, "New description: ");
        double price = readDouble(scanner, "New price: ");
        int quantity = readInt(scanner, "New quantity: ");

        Product updatedProduct = new Product(id, name, description, price, quantity);
        boolean updated = productDAO.updateProduct(updatedProduct);

        System.out.println(updated ? "Product updated successfully." : "Failed to update product.");
    }

    private static void deleteProduct(Scanner scanner) {
        System.out.println("---- Delete Product ----");
        int id = readInt(scanner, "Enter product id to delete: ");

        boolean deleted = productDAO.deleteProduct(id);
        System.out.println(deleted ? "Product deleted successfully." : "Failed to delete product.");
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (example: 199.99).");
            }
        }
    }

    private static String readLine(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field cannot be empty.");
        }
    }
}
