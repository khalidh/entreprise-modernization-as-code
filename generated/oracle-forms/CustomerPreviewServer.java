import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.concurrent.Executors;

public final class CustomerPreviewServer {
        private static final String DATABASE_URL = environmentOrDefault(
            "CUSTOMER_DB_URL", "jdbc:oracle:thin:@//127.0.0.1:1521/FREEPDB1");
    private static final String DATABASE_USER = environmentOrDefault("CUSTOMER_DB_USER", "CUSTOMER_APP");
    private static final int PORT = 8080;
    private static final int MAX_REQUEST_BYTES = 32768;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String databasePassword;
    private final Path htmlFile;
    private final ObjectMapper json = new ObjectMapper();

    private CustomerPreviewServer(String databasePassword, Path htmlFile) {
        this.databasePassword = databasePassword;
        this.htmlFile = htmlFile;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Pass the path to customer-preview.html.");
        }
        String password = System.getenv("CUSTOMER_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("CUSTOMER_DB_PASSWORD is not set.");
        }
        if (DATABASE_URL == null || DATABASE_URL.isBlank()) {
            throw new IllegalStateException("CUSTOMER_DB_URL is not set.");
        }

        Class.forName("oracle.jdbc.OracleDriver");
        CustomerPreviewServer application = new CustomerPreviewServer(password, Path.of(args[0]));
        int initialCount = application.loadCustomers().size();
        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT), 0);
        var executor = Executors.newFixedThreadPool(4);
        server.setExecutor(executor);
        server.createContext("/", application::handleRequest);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            executor.shutdown();
        }));
        server.start();

        System.out.println("Oracle login verified for CUSTOMER_APP; clients: " + initialCount);
        System.out.println("Open http://127.0.0.1:" + PORT + "/ in your browser.");
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if ("GET".equals(method) && ("/".equals(path) || "/customer-preview.html".equals(path))) {
                if (!Files.isRegularFile(htmlFile)) {
                    sendText(exchange, 404, "Customer page not found.", "text/plain; charset=utf-8");
                    return;
                }
                sendBytes(exchange, 200, Files.readAllBytes(htmlFile), "text/html; charset=utf-8");
                return;
            }

            if ("/api/customers".equals(path)) {
                if ("GET".equals(method)) {
                    sendJson(exchange, 200, loadCustomers());
                    return;
                }
                if ("POST".equals(method)) {
                    Customer customer = parseCustomer(readJson(exchange), true);
                    insertCustomer(customer);
                    sendJson(exchange, 201, customer.toJson());
                    return;
                }
                exchange.getResponseHeaders().set("Allow", "GET, POST");
                sendText(exchange, 405, "Method not allowed.", "text/plain; charset=utf-8");
                return;
            }

            String customerPrefix = "/api/customers/";
            if (path.startsWith(customerPrefix)) {
                long customerId = parseCustomerId(path.substring(customerPrefix.length()));
                if ("PUT".equals(method)) {
                    Customer customer = parseCustomer(readJson(exchange), false);
                    if (updateCustomer(customerId, customer)) {
                        customer.customerId = customerId;
                        sendJson(exchange, 200, customer.toJson());
                    } else {
                        sendJson(exchange, 404, Map.of("error", "Customer was not found."));
                    }
                    return;
                }
                if ("DELETE".equals(method)) {
                    if (deleteCustomer(customerId)) {
                        exchange.sendResponseHeaders(204, -1);
                    } else {
                        sendJson(exchange, 404, Map.of("error", "Customer was not found."));
                    }
                    return;
                }
                exchange.getResponseHeaders().set("Allow", "PUT, DELETE");
                sendText(exchange, 405, "Method not allowed.", "text/plain; charset=utf-8");
                return;
            }

            sendText(exchange, 404, "Not found.", "text/plain; charset=utf-8");
        } catch (JsonProcessingException exception) {
            sendJson(exchange, 400, Map.of("error", "Request body must contain valid JSON."));
        } catch (IllegalArgumentException exception) {
            sendJson(exchange, 400, Map.of("error", exception.getMessage()));
        } catch (SQLException exception) {
            System.err.println("Oracle customer request failed: " + exception.getErrorCode());
            if (exception.getErrorCode() == 1) {
                sendJson(exchange, 409, Map.of("error", "Customer ID or email already exists."));
            } else {
                sendJson(exchange, 503, Map.of("error", "Oracle database request failed."));
            }
        } finally {
            exchange.close();
        }
    }

    private JsonNode readJson(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readNBytes(MAX_REQUEST_BYTES + 1);
        if (body.length > MAX_REQUEST_BYTES) {
            throw new IllegalArgumentException("Request body is too large.");
        }
        JsonNode payload = json.readTree(body);
        if (payload == null || !payload.isObject()) {
            throw new IllegalArgumentException("A JSON object is required.");
        }
        return payload;
    }

    private Customer parseCustomer(JsonNode payload, boolean requireId) {
        long customerId = 0;
        if (requireId) {
            JsonNode id = payload.get("customer_id");
            if (id == null || !id.isIntegralNumber()) {
                throw new IllegalArgumentException("Customer ID must be an integer.");
            }
            customerId = id.longValue();
            if (customerId < -9999999999L || customerId > 9999999999L) {
                throw new IllegalArgumentException("Customer ID exceeds NUMBER(10,0).");
            }
        }

        String name = textField(payload, "name", 100, true);
        String email = textField(payload, "email", 150, true);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email format is invalid.");
        }

        JsonNode activeValue = payload.get("active");
        if (activeValue != null && !activeValue.isNull() && !activeValue.isBoolean()) {
            throw new IllegalArgumentException("Active must be true or false.");
        }
        boolean active = activeValue == null || activeValue.isNull() || activeValue.booleanValue();
        return new Customer(
                customerId,
                name,
                email,
                textField(payload, "phone", 30, false),
                textField(payload, "city", 80, false),
                textField(payload, "country", 80, false),
                active);
    }

    private String textField(JsonNode payload, String field, int maxLength, boolean required) {
        JsonNode value = payload.get(field);
        String text = value == null || value.isNull() ? null : value.asText().trim();
        if (required && (text == null || text.isEmpty())) {
            throw new IllegalArgumentException(field + " is required.");
        }
        if (text != null && text.isEmpty()) {
            return null;
        }
        if (text != null && text.codePointCount(0, text.length()) > maxLength) {
            throw new IllegalArgumentException(field + " exceeds " + maxLength + " characters.");
        }
        return text;
    }

    private long parseCustomerId(String value) {
        try {
            long customerId = Long.parseLong(value);
            if (customerId < -9999999999L || customerId > 9999999999L) {
                throw new NumberFormatException();
            }
            return customerId;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Customer ID must be a NUMBER(10,0) integer.");
        }
    }

    private void insertCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO CUSTOMER_APP.CUSTOMER "
                + "(CUSTOMER_ID, NAME, EMAIL, PHONE, CITY, COUNTRY, ACTIVE) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customer.customerId);
            bindCustomer(statement, customer, 2);
            statement.executeUpdate();
        }
    }

    private boolean updateCustomer(long customerId, Customer customer) throws SQLException {
        String sql = "UPDATE CUSTOMER_APP.CUSTOMER SET NAME = ?, EMAIL = ?, PHONE = ?, "
                + "CITY = ?, COUNTRY = ?, ACTIVE = ? WHERE CUSTOMER_ID = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCustomer(statement, customer, 1);
            statement.setLong(7, customerId);
            return statement.executeUpdate() == 1;
        }
    }

    private boolean deleteCustomer(long customerId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM CUSTOMER_APP.CUSTOMER WHERE CUSTOMER_ID = ?")) {
            statement.setLong(1, customerId);
            return statement.executeUpdate() == 1;
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, databasePassword);
    }

    private void bindCustomer(PreparedStatement statement, Customer customer, int offset)
            throws SQLException {
        statement.setString(offset, customer.name);
        statement.setString(offset + 1, customer.email);
        statement.setString(offset + 2, customer.phone);
        statement.setString(offset + 3, customer.city);
        statement.setString(offset + 4, customer.country);
        statement.setString(offset + 5, customer.active ? "Y" : "N");
    }

    private List<Map<String, Object>> loadCustomers() throws SQLException {
        String query = "SELECT CUSTOMER_ID, NAME, EMAIL, PHONE, CITY, COUNTRY, ACTIVE "
                + "FROM CUSTOMER_APP.CUSTOMER ORDER BY NAME ASC";
        List<Map<String, Object>> customers = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Map<String, Object> customer = new LinkedHashMap<>();
                customer.put("customer_id", result.getLong("CUSTOMER_ID"));
                customer.put("name", result.getString("NAME"));
                customer.put("email", result.getString("EMAIL"));
                customer.put("phone", result.getString("PHONE"));
                customer.put("city", result.getString("CITY"));
                customer.put("country", result.getString("COUNTRY"));
                customer.put("active", "Y".equalsIgnoreCase(result.getString("ACTIVE")));
                customers.add(customer);
            }
        }
        return customers;
    }

    private static final class Customer {
        private long customerId;
        private final String name;
        private final String email;
        private final String phone;
        private final String city;
        private final String country;
        private final boolean active;

        private Customer(long customerId, String name, String email, String phone,
                         String city, String country, boolean active) {
            this.customerId = customerId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.city = city;
            this.country = country;
            this.active = active;
        }

        private Map<String, Object> toJson() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("customer_id", customerId);
            result.put("name", name);
            result.put("email", email);
            result.put("phone", phone);
            result.put("city", city);
            result.put("country", country);
            result.put("active", active);
            return result;
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        sendBytes(exchange, status, json.writeValueAsBytes(body), "application/json; charset=utf-8");
    }

    private void sendText(HttpExchange exchange, int status, String body, String contentType)
            throws IOException {
        sendBytes(exchange, status, body.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private void sendBytes(HttpExchange exchange, int status, byte[] body, String contentType)
            throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, body.length);
        try (var output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}