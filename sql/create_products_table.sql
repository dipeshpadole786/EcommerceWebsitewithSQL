-- Create database (run once)
CREATE DATABASE IF NOT EXISTS ecommerce;
USE ecommerce;

-- Create "products" table
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DOUBLE NOT NULL,
    quantity INT NOT NULL
);

