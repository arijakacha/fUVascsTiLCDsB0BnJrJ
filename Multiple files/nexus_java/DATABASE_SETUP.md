# 🗄️ Database Setup Guide - NexusPlay

## 🎯 Current Configuration

The application is configured to connect to your MySQL `nexusplay` database with these settings:

```properties       










# Connection Details
Host: 127.0.0.1
Port: 3307
Database: nexusplay
Username: root
Password: (empty)
```
## 🔧 Troubleshooting Steps

## 1. Verify MySQL is Running








Check if MySQL is running on port 3307:

```bash
# Windows (Command Prompt)
netstat -an | findstr :3307

# Windows (PowerShell)
Get-NetTCPConnection -LocalPort 3307

# Alternative: Check MySQL Service
net start | findstr MySQL
```

### 2. Check Database Exists

Connect to MySQL and verify the `nexusplay` database exists:

```sql
-- Connect to MySQL
mysql -u root -p -P 3307

-- List databases
SHOW DATABASES;

-- Create nexusplay database if it doesn't exist
CREATE DATABASE IF NOT EXISTS nexusplay;
```

### 3. Update Connection Settings

If your MySQL setup is different, update `application.properties`:

```properties
# Different Port (default MySQL port is 3306)
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/nexusplay?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

# Different Username/Password
spring.datasource.username=your_username
spring.datasource.password=your_password

# Different Host (if MySQL is on another machine)
spring.datasource.url=jdbc:mysql://your-mysql-host:3307/nexusplay?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

## 🚀 Quick Setup Options

### Option 1: Use Default MySQL Port (3306)

If MySQL is running on the default port:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/nexusplay?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

### Option 2: Use XAMPP/WAMP MySQL

If you're using XAMPP or WAMP:

```properties
# XAMPP typically uses port 3306
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/nexusplay?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```

### Option 3: Create Database with Schema

If you need to create the database with your schema:

```sql
-- Create database
CREATE DATABASE nexusplay;

-- Use the database
USE nexusplay;

-- Import your SQL schema file
SOURCE path/to/your/schema.sql;
```

## 🔍 Common Issues & Solutions

### Issue: "Connection Refused"
**Solution**: MySQL is not running or wrong port
- Start MySQL service
- Check the correct port (3306 or 3307)
- Update the port in `application.properties`

### Issue: "Access Denied"
**Solution**: Wrong username/password
- Verify MySQL credentials
- Update username/password in `application.properties`

### Issue: "Database doesn't exist"
**Solution**: Create the nexusplay database
```sql
CREATE DATABASE nexusplay;
```

### Issue: "SSL Connection Error"
**Solution**: The URL already has `useSSL=false`, but if you still get SSL errors:
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3307/nexusplay?useSSL=false&allowPublicKeyRetrieval=false&serverTimezone=UTC
```

## 🎮 After Setup

Once MySQL is configured correctly:

1. **Restart the application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Verify connection**: The application should start without database errors

3. **Test API**: Visit `http://localhost:8081/api/users` to test the connection

## 📋 Verification Checklist

- [ ] MySQL server is running
- [ ] Port is correct (3306 or 3307)
- [ ] Database `nexusplay` exists
- [ ] Username/password are correct
- [ ] Application starts without errors
- [ ] API endpoints respond correctly

## 🆘 Need Help?

If you're still having issues:

1. Check the exact error message in the application logs
2. Verify you can connect to MySQL with a MySQL client
3. Ensure your SQL schema has been imported into the `nexusplay` database
4. Try connecting with a simpler URL first to isolate the issue

The application is ready to use once the database connection is established! 🚀
