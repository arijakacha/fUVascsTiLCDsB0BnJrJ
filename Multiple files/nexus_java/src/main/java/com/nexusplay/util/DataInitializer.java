package com.nexusplay.util;

import com.nexusplay.dao.GameDAO;
import com.nexusplay.dao.ProductDAO;
import com.nexusplay.dao.UserDAO;
import com.nexusplay.entity.Game;
import com.nexusplay.entity.Product;
import com.nexusplay.entity.User;

import java.util.List;

public class DataInitializer {
    
    private static final GameDAO gameDAO = new GameDAO();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final UserDAO userDAO = new UserDAO();
    
    public static void initializeSampleData() {
        initializeSampleUsers();
        initializeSampleGames();
        initializeSampleProducts();
    }
    
    private static void initializeSampleUsers() {
        // Check if "hedi" user already exists, if not create it
        boolean hediExists = false;
        try {
            User existingUser = userDAO.findByUsername("hedi");
            if (existingUser != null) {
                hediExists = true;
            }
        } catch (Exception e) {
            System.out.println("Error checking existing users: " + e.getMessage());
        }
        
        if (!hediExists) {
            // Create hedi user
            User hedi = createUser("hedi", "hedi@nexusplay.com", "123456", User.UserType.ADMIN, User.UserStatus.ACTIVE);
            try {
                userDAO.save(hedi);
                System.out.println("✅ Created user: hedi");
            } catch (Exception e) {
                System.out.println("❌ Failed to create user hedi: " + e.getMessage());
            }
        } else {
            System.out.println("✅ User hedi already exists");
        }
        
        // Only create other sample users if database is empty
        try {
            List<User> existingUsers = userDAO.findAll();
            if (existingUsers != null && existingUsers.size() <= 1) {
                User[] sampleUsers = {
                    createUser("admin", "admin@nexusplay.com", "admin123", User.UserType.ADMIN, User.UserStatus.ACTIVE),
                    createUser("player1", "player1@nexusplay.com", "player123", User.UserType.REGISTERED, User.UserStatus.ACTIVE),
                    createUser("coach1", "coach1@nexusplay.com", "coach123", User.UserType.COACH, User.UserStatus.ACTIVE),
                    createUser("org1", "org1@nexusplay.com", "org123", User.UserType.ORGANIZATION, User.UserStatus.ACTIVE),
                    createUser("visitor1", "visitor1@nexusplay.com", "visitor123", User.UserType.VISITOR, User.UserStatus.ACTIVE)
                };
                
                for (User user : sampleUsers) {
                    userDAO.save(user);
                }
            }
        } catch (Exception e) {
            System.out.println("Error creating sample users: " + e.getMessage());
        }
    }
    
    private static User createUser(String username, String email, String password, User.UserType userType, User.UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setUserType(userType);
        user.setStatus(status);
        user.setHasPlayer(false);
        user.setCreatedAt(java.time.LocalDateTime.now());
        return user;
    }
    
    private static void initializeSampleGames() {
        // Check if games already exist
        if (gameDAO.findAll() != null && !gameDAO.findAll().isEmpty()) {
            return;
        }
        
        // Professional sample games with images
        Game[] sampleGames = {
            createGame("League of Legends", "https://upload.wikimedia.org/wikipedia/commons/d/d8/League_of_Legends_2019_vector.svg", 
                "League of Legends is a team-based strategy game where two teams of five powerful champions face off to destroy the other's base.", 
                (short) 2009),
            createGame("Valorant", "https://upload.wikimedia.org/wikipedia/commons/f/fc/Valorant_logo_-_pink_color_version.svg", 
                "Valorant is a 5v5 tactical shooter where character abilities create unique opportunities for gunplay.", 
                (short) 2020),
            createGame("Counter-Strike 2", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/CS2_logo.svg/1200px-CS2_logo.svg.png", 
                "Counter-Strike 2 is a tactical shooter featuring updated graphics and gameplay mechanics.", 
                (short) 2023),
            createGame("Dota 2", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Dota_2_logo.svg/1200px-Dota_2_logo.svg.png", 
                "Dota 2 is a multiplayer online battle arena where two teams of five players compete to destroy the other's Ancient.", 
                (short) 2013),
            createGame("Fortnite", "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Fortnite_logo.svg/1200px-Fortnite_logo.svg.png", 
                "Fortnite is a battle royale game where 100 players fight to be the last one standing.", 
                (short) 2017),
            createGame("Minecraft", "https://upload.wikimedia.org/wikipedia/en/thumb/3/33/Minecraft_logo.svg/1200px-Minecraft_logo.svg.png", 
                "Minecraft is a sandbox video game where players can build with a variety of different blocks in a 3D procedurally generated world.", 
                (short) 2011),
            createGame("Apex Legends", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/86/Apex_Legends_logo.svg/1200px-Apex_Legends_logo.svg.png", 
                "Apex Legends is a free-to-play battle royale game where legendary competitors battle for glory, fame, and fortune.", 
                (short) 2019),
            createGame("Overwatch 2", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Overwatch_2_logo.svg/1200px-Overwatch_2_logo.svg.png", 
                "Overwatch 2 is a team-based action game where a cast of unique heroes battle across the globe.", 
                (short) 2022),
            createGame("Call of Duty: Warzone", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Call_of_Duty_Warzone_logo.svg/1200px-Call_of_Duty_Warzone_logo.svg.png", 
                "Call of Duty: Warzone is a free-to-play battle royale game featuring massive combat zones.", 
                (short) 2020),
            createGame("Rocket League", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/Rocket_League_logo.svg/1200px-Rocket_League_logo.svg.png", 
                "Rocket League is a high-powered hybrid of arcade-style soccer and vehicular mayhem.", 
                (short) 2015)
        };
        
        for (Game game : sampleGames) {
            gameDAO.save(game);
        }
    }
    
    private static Game createGame(String name, String logo, String description, short releaseYear) {
        Game game = new Game();
        game.setName(name);
        game.setLogo(logo);
        game.setDescription(description);
        game.setReleaseYear(releaseYear);
        game.setCreatedAt(java.time.LocalDateTime.now());
        return game;
    }
    
    private static void initializeSampleProducts() {
        // Check if products already exist
        if (productDAO.findAll() != null && !productDAO.findAll().isEmpty()) {
            return;
        }
        
        // Professional sample gaming products with images
        Product[] sampleProducts = {
            createProduct("Logitech G Pro X Wireless", "Headset", "Professional gaming headset with 7.1 surround sound", "https://m.media-amazon.com/images/I/71tZwZ+8DZL._AC_SL1500_.jpg", 199.99, 50),
            createProduct("Razer DeathAdder V3 Pro", "Mouse", "High-performance wireless gaming mouse with optical sensor", "https://m.media-amazon.com/images/I/61jL70BGV1L._AC_SL1500_.jpg", 159.99, 75),
            createProduct("SteelSeries Apex Pro TKL", "Keyboard", "Mechanical gaming keyboard with OLED display", "https://m.media-amazon.com/images/I/71+4o2VrXcL._AC_SL1500_.jpg", 179.99, 30),
            createProduct("ASUS ROG Swift PG279Q", "Monitor", "27-inch 144Hz IPS gaming monitor", "https://m.media-amazon.com/images/I/71A-9GqX3JL._AC_SL1500_.jpg", 549.99, 20),
            createProduct("Xbox Elite Wireless Controller", "Controller", "Premium wireless controller for Xbox and PC", "https://m.media-amazon.com/images/I/61kIY3TQ5SL._AC_SL1500_.jpg", 179.99, 40),
            createProduct("Herman Miller Embody", "Gaming Chair", "Ergonomic gaming chair for long gaming sessions", "https://m.media-amazon.com/images/I/71tZwZ+8DZL._AC_SL1500_.jpg", 1295.00, 15),
            createProduct("Razer Gigantus V2", "Mousepad", "Large gaming mousepad with anti-slip rubber base", "https://m.media-amazon.com/images/I/71tZwZ+8DZL._AC_SL1500_.jpg", 49.99, 100),
            createProduct("Logitech C920 Pro Webcam", "Webcam", "HD webcam with stereo audio for streaming", "https://m.media-amazon.com/images/I/71tZwZ+8DZL._AC_SL1500_.jpg", 79.99, 60),
            createProduct("Shure SM7B", "Microphone", "Professional dynamic microphone for broadcasting", "https://m.media-amazon.com/images/I/71tZwZ+8DZL._AC_SL1500_.jpg", 399.00, 25),
            createProduct("Logitech Z623", "Speaker", "THX-certified 2.1 speaker system for immersive audio", "https://m.media-amazon.com/images/I/71tZwZ+8DZL._AC_SL1500_.jpg", 149.99, 35)
        };
        
        for (Product product : sampleProducts) {
            productDAO.save(product);
        }
    }
    
    private static Product createProduct(String name, String type, String description, String imagePath, double price, int quantity) {
        Product product = new Product();
        product.setName(name);
        product.setType(type);
        product.setDescription(description);
        product.setImagePath(imagePath);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCreatedAt(java.time.LocalDateTime.now());
        return product;
    }
}
