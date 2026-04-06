package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import dao.ProductDAO;
import model.Product;

/**
 * Minimal HTTP server (no Spring Boot) that:
 * - Serves `web/index.html` and `web/index.css`
 * - Exposes CRUD API backed by JDBC + MySQL
 *
 * Run: main.WebServer
 * Open: http://localhost:8080
 */
public class WebServer {

    private static final int PORT = 8080;
    private static final ProductDAO productDAO = new ProductDAO();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API
        server.createContext("/api/products", new ProductsApiHandler());

        // Static files
        server.createContext("/", new StaticHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Web server started on http://localhost:" + PORT);
    }

    private static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                serveFile(exchange, Paths.get("web", "index.html"), "text/html; charset=utf-8");
                return;
            }
            if (path.equals("/index.css")) {
                serveFile(exchange, Paths.get("web", "index.css"), "text/css; charset=utf-8");
                return;
            }

            sendJson(exchange, 404, "{\"error\":\"Not found\"}");
        }

        private void serveFile(HttpExchange exchange, Path filePath, String contentType) throws IOException {
            if (!Files.exists(filePath)) {
                sendJson(exchange, 500,
                        "{\"error\":\"Static file missing. Make sure web/index.html and web/index.css exist.\"}");
                return;
            }

            byte[] bytes = Files.readAllBytes(filePath);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static class ProductsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String method = exchange.getRequestMethod().toUpperCase();
            String fullPath = exchange.getRequestURI().getPath(); // ex: /api/products/5
            Integer id = parseIdIfPresent(fullPath);
            if (id != null && id < 0) {
                sendJson(exchange, 400, "{\"error\":\"Invalid product id in URL\"}");
                return;
            }

            try {
                if (id == null) {
                    handleCollection(exchange, method);
                } else {
                    handleItem(exchange, method, id);
                }
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, "{\"error\":\"" + jsonEscape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"Server error: " + jsonEscape(e.getMessage()) + "\"}");
            }
        }

        private void handleCollection(HttpExchange exchange, String method) throws IOException {
            if ("GET".equals(method)) {
                List<Product> products = productDAO.getAllProducts();
                sendJson(exchange, 200, productsToJson(products));
                return;
            }

            if ("POST".equals(method)) {
                Map<String, String> form = readForm(exchange);
                String name = require(form, "name");
                String description = form.getOrDefault("description", "");
                double price = requireDouble(form, "price");
                int quantity = requireInt(form, "quantity");

                boolean ok = productDAO.addProduct(new Product(name, description, price, quantity));
                if (!ok) {
                    sendJson(exchange, 500, "{\"error\":\"Failed to add product\"}");
                    return;
                }

                sendJson(exchange, 201, "{\"ok\":true}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }

        private void handleItem(HttpExchange exchange, String method, int id) throws IOException {
            if ("GET".equals(method)) {
                Product product = productDAO.getProductById(id);
                if (product == null) {
                    sendJson(exchange, 404, "{\"error\":\"Product not found\"}");
                    return;
                }
                sendJson(exchange, 200, productToJson(product));
                return;
            }

            if ("PUT".equals(method)) {
                Product existing = productDAO.getProductById(id);
                if (existing == null) {
                    sendJson(exchange, 404, "{\"error\":\"Product not found\"}");
                    return;
                }

                Map<String, String> form = readForm(exchange);
                String name = require(form, "name");
                String description = form.getOrDefault("description", "");
                double price = requireDouble(form, "price");
                int quantity = requireInt(form, "quantity");

                boolean ok = productDAO.updateProduct(new Product(id, name, description, price, quantity));
                if (!ok) {
                    sendJson(exchange, 500, "{\"error\":\"Failed to update product\"}");
                    return;
                }

                sendJson(exchange, 200, "{\"ok\":true}");
                return;
            }

            if ("DELETE".equals(method)) {
                boolean ok = productDAO.deleteProduct(id);
                if (!ok) {
                    sendJson(exchange, 404, "{\"error\":\"Product not found or already deleted\"}");
                    return;
                }
                sendJson(exchange, 200, "{\"ok\":true}");
                return;
            }

            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }

        private Integer parseIdIfPresent(String path) {
            // /api/products or /api/products/123
            if (path.equals("/api/products") || path.equals("/api/products/")) {
                return null;
            }

            String prefix = "/api/products/";
            if (!path.startsWith(prefix)) {
                return null;
            }

            String tail = path.substring(prefix.length()).trim();
            if (tail.isEmpty()) {
                return null;
            }

            try {
                return Integer.parseInt(tail);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        private Map<String, String> readForm(HttpExchange exchange) throws IOException {
            byte[] bodyBytes = readAllBytes(exchange.getRequestBody());
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            Map<String, String> map = new HashMap<>();
            if (body.isEmpty()) {
                return map;
            }

            for (String pair : body.split("&")) {
                int idx = pair.indexOf('=');
                String key = idx >= 0 ? pair.substring(0, idx) : pair;
                String value = idx >= 0 ? pair.substring(idx + 1) : "";

                key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                map.put(key, value);
            }

            return map;
        }

        private byte[] readAllBytes(InputStream inputStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        }

        private String require(Map<String, String> form, String key) throws IOException {
            String value = form.get(key);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing field: " + key);
            }
            return value.trim();
        }

        private int requireInt(Map<String, String> form, String key) {
            String value = form.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing field: " + key);
            }
            return Integer.parseInt(value.trim());
        }

        private double requireDouble(Map<String, String> form, String key) {
            String value = form.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing field: " + key);
            }
            return Double.parseDouble(value.trim());
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String productsToJson(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(productToJson(products.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String productToJson(Product p) {
        return "{"
                + "\"id\":" + p.getId() + ","
                + "\"name\":\"" + jsonEscape(p.getName()) + "\","
                + "\"description\":\"" + jsonEscape(p.getDescription()) + "\","
                + "\"price\":" + p.getPrice() + ","
                + "\"quantity\":" + p.getQuantity()
                + "}";
    }

    private static String jsonEscape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
