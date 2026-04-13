# NexusPlay Gaming Platform - Java Backend

A comprehensive gaming platform backend built with Java 17, Spring Boot, and Maven, connected to a MySQL database. This application provides full CRUD operations for managing users, games, players, teams, achievements, and more.

## 🎮 Features

### Core Entities
- **Users**: User management with different roles (Admin, Coach, Player, Organization)
- **Games**: Game catalog with metadata
- **Players**: Player profiles with statistics and achievements
- **Teams**: Team management with organization support
- **Achievements**: Game achievements with player progress tracking
- **Matches**: Game matches with detailed statistics
- **Coaching**: Coaching sessions and coach management
- **Forum**: Forum posts and comments
- **Statistics**: Player and team performance statistics

### Key Features
- Complete CRUD operations for all entities
- RESTful API endpoints
- Database relationships and constraints
- Search and filtering capabilities
- Statistics and reporting
- Team invitations and management
- Achievement tracking system

## 🛠 Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Web**
- **Spring Security**
- **MySQL 8.0**
- **Maven**
- **Jakarta Validation**

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## 🚀 Getting Started

### 1. Database Setup

Create a MySQL database named `nexusplay` and import the provided SQL schema:

```sql
CREATE DATABASE nexusplay;
-- Import the SQL schema from the provided file
```

### 2. Configuration

Update the database connection in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3307/nexusplay?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and Run

```bash
# Clone the repository
git clone <repository-url>
cd nexus_java

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Users API
```
GET    /api/users                    - Get all users
GET    /api/users/{id}               - Get user by ID
GET    /api/users/username/{username} - Get user by username
GET    /api/users/email/{email}      - Get user by email
POST   /api/users                    - Create new user
PUT    /api/users/{id}               - Update user
DELETE /api/users/{id}               - Delete user
GET    /api/users/status/{status}    - Get users by status
GET    /api/users/type/{userType}    - Get users by type
GET    /api/users/search/{keyword}   - Search users
```

### Games API
```
GET    /api/games                    - Get all games
GET    /api/games/{id}               - Get game by ID
GET    /api/games/name/{name}        - Get game by name
POST   /api/games                    - Create new game
PUT    /api/games/{id}               - Update game
DELETE /api/games/{id}               - Delete game
GET    /api/games/year/{year}        - Get games by release year
GET    /api/games/search/{keyword}   - Search games
```

### Players API
```
GET    /api/players                  - Get all players
GET    /api/players/{id}             - Get player by ID
GET    /api/players/user/{userId}    - Get players by user ID
GET    /api/players/team/{teamId}    - Get players by team ID
GET    /api/players/game/{gameId}    - Get players by game ID
GET    /api/players/pro/{isPro}      - Get players by pro status
POST   /api/players                  - Create new player
PUT    /api/players/{id}             - Update player
DELETE /api/players/{id}             - Delete player
GET    /api/players/search/{keyword} - Search players
```

### Teams API
```
GET    /api/teams                    - Get all teams
GET    /api/teams/{id}               - Get team by ID
GET    /api/teams/game/{gameId}      - Get teams by game ID
GET    /api/teams/organization/{orgId} - Get teams by organization
POST   /api/teams                    - Create new team
PUT    /api/teams/{id}               - Update team
DELETE /api/teams/{id}               - Delete team
```

### Admin API (Requires ADMIN role)
```
# Dashboard
GET    /api/admin/dashboard/stats           - Get dashboard statistics
GET    /api/admin/access/check              - Check admin access

# User Management
GET    /api/admin/users                     - Get all users
PUT    /api/admin/users/{userId}/ban        - Ban a user
PUT    /api/admin/users/{userId}/unban      - Unban a user
PUT    /api/admin/users/{userId}/role/{role} - Change user role
POST   /api/admin/users/bulk-ban            - Bulk ban users

# Content Management
GET    /api/admin/content/forum-posts       - Get all forum posts
DELETE /api/admin/content/forum-posts/{id}  - Delete forum post
POST   /api/admin/content/forum-posts/bulk-delete - Bulk delete posts
GET    /api/admin/content/all               - Get all content
DELETE /api/admin/content/{id}              - Delete content

# Match Management
GET    /api/admin/matches                   - Get all matches
PUT    /api/admin/matches/{id}/result       - Update match result

# Team Management
GET    /api/admin/teams/invitations         - Get all team invitations
PUT    /api/admin/teams/invitations/{id}/approve - Approve invitation
PUT    /api/admin/teams/invitations/{id}/reject  - Reject invitation
POST   /api/admin/teams/invitations/bulk-approve - Bulk approve

# Achievement Management
GET    /api/admin/achievements/player-achievements - Get all player achievements
PUT    /api/admin/achievements/player-achievements/{id}/unlock - Unlock achievement

# Reports and Statistics
GET    /api/admin/reports/recent-users      - Get recent users
GET    /api/admin/reports/recent-matches    - Get recent matches
GET    /api/admin/reports/game-statistics   - Get game statistics

# System Management
POST   /api/admin/system/cleanup            - Cleanup old data

# Validation
GET    /api/admin/validate/user/{id}        - Validate user exists
GET    /api/admin/validate/game/{id}        - Validate game exists
GET    /api/admin/validate/player/{id}      - Validate player exists
GET    /api/admin/validate/team/{id}        - Validate team exists
```

## 🏗 Project Structure

```
src/main/java/com/nexusplay/
├── NexusPlayApplication.java        # Main application class
├── controller/                      # REST controllers
│   ├── UserController.java
│   ├── GameController.java
│   ├── PlayerController.java
│   ├── TeamController.java
│   └── AdminController.java         # Admin management endpoints
├── service/                         # Business logic
│   ├── UserService.java
│   ├── GameService.java
│   ├── PlayerService.java
│   ├── TeamService.java
│   └── AdminService.java            # Admin business logic
├── repository/                      # Data access layer
│   ├── UserRepository.java
│   ├── GameRepository.java
│   ├── PlayerRepository.java
│   ├── TeamRepository.java
│   ├── AchievementRepository.java
│   ├── CoachRepository.java
│   ├── CoachingSessionRepository.java
│   ├── ForumPostRepository.java
│   ├── GameMatchRepository.java
│   ├── StatisticRepository.java
│   ├── TeamInvitationRepository.java
│   ├── ContentRepository.java
│   └── PlayerAchievementRepository.java
├── config/                          # Configuration
│   └── AdminSecurityConfig.java     # Admin security configuration
├── dto/                             # Data Transfer Objects
│   └── AdminDashboardDTO.java       # Admin dashboard response
├── util/                            # Utility classes
│   └── AdminUtils.java              # Admin utility functions
└── entity/                          # JPA entities
    ├── User.java
    ├── Game.java
    ├── Player.java
    ├── Team.java
    ├── Achievement.java
    ├── Coach.java
    ├── CoachingSession.java
    ├── Content.java
    ├── ForumPost.java
    ├── GameMatch.java
    ├── MatchPlayer.java
    ├── Organization.java
    ├── PlayerAchievement.java
    ├── Statistic.java
    ├── TeamInvitation.java
    ├── RankHistory.java
    ├── Stream.java
    ├── Product.java
    ├── ProductPurchase.java
    ├── Conversation.java
    ├── Message.java
    ├── GuideComment.java
    ├── Like.java
    ├── Report.java
    └── Reponse.java
```

## 🔧 Configuration

### Application Properties
- Database connection settings
- Server configuration
- JPA/Hibernate settings
- Logging configuration

### Database Schema
The application uses a comprehensive database schema with the following main tables:
- `user` - User accounts and profiles
- `game` - Game information
- `player` - Player profiles and statistics
- `team` - Team information
- `achievement` - Game achievements
- `coach` - Coach profiles
- `coaching_session` - Coaching sessions
- `forum_post` - Forum posts
- `game_match` - Match data
- `statistic` - Player statistics

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

## 📝 Example Usage

### Create a New User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "userType": "REGISTERED"
  }'
```

### Get All Games
```bash
curl http://localhost:8080/api/games
```

### Create a New Player
```bash
curl -X POST http://localhost:8080/api/players \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "ProPlayer",
    "realName": "John Doe",
    "role": "Mid Laner",
    "nationality": "US",
    "game": {"id": 1}
  }'
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions, please open an issue in the repository.

## 🎨 Frontend Design

A comprehensive gaming platform UI has been designed with modern aesthetics and gaming-focused features:

### **Design Features**
- **Dark Gaming Theme**: Professional dark color scheme with vibrant accents
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile
- **Interactive Components**: Smooth animations and micro-interactions
- **Real-time Updates**: Live match scores and notifications
- **Gamification Elements**: Achievement badges, progress bars, leaderboards

### **Key UI Components**
- **Dashboard**: Live matches, top players, recent activity
- **Game Hub**: Game showcases, leaderboards, match history
- **Player Profiles**: Performance charts, achievements, statistics
- **Team Management**: Roster, schedules, analytics
- **Coaching Center**: Session booking, coach profiles, reviews
- **Forum & Community**: Discussion boards, content moderation
- **Admin Panel**: System analytics, user management, content control

### **Technology Stack**
- **HTML5/CSS3/JavaScript**: Modern web standards
- **Responsive Grid System**: Flexible layouts for all devices
- **CSS Variables**: Consistent theming and easy customization
- **Font Awesome Icons**: Professional iconography
- **Smooth Animations**: Engaging user interactions

### **View the Design**
- **HTML Prototype**: `frontend/index.html`
- **Stylesheet**: `frontend/styles.css`
- **Interactions**: `frontend/script.js`
- **Design Spec**: `GAMING_UI_DESIGN.md`

Open `frontend/index.html` in your browser to see the complete gaming platform interface!

## 🔄 Future Enhancements

- Authentication and authorization
- Real-time notifications with WebSocket
- File upload for images and avatars
- Advanced statistics and analytics dashboard
- React/TypeScript frontend implementation
- Mobile app development (React Native)
- Caching implementation with Redis
- API rate limiting and security
- Swagger/OpenAPI documentation
- Integration with gaming APIs (Riot, Steam, etc.)
