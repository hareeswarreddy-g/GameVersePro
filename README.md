# GameVersePro

Minimal Java + JavaFX skeleton for a game purchasing system (demo).

Prerequisites
- JDK 17+
- Maven
- MySQL server (create user and set password as configured in `db.properties`)

Quick start

1. Edit `db.properties` with your DB credentials.
2. Create the schema in MySQL:

```sql
-- from project root
mysql -u root -p < sql/schema.sql
```

3. Download dependencies and run the JavaFX app:

Manual (no Maven)

1. Download JavaFX SDK (matching your JDK version) from https://openjfx.io and extract it. Place the SDK's `lib` folder at `lib\javafx\lib` inside the project.
2. Download the MySQL Connector/J JAR (e.g. `mysql-connector-java-8.x.x.jar`) from https://dev.mysql.com/downloads/connector/j/ and copy it to `lib\mysql-connector-java.jar`.
3. Run the included `run.bat` from the project root to compile and launch the app:

```powershell
.\run.bat
```

If you prefer Maven, use the commands below (Maven must be installed):

```bash
mvn clean compile
mvn javafx:run
```

Files added
- `pom.xml` - Maven build with JavaFX and MySQL deps
- `db.properties` - DB connection defaults
- `src/MainApp.java` - minimal JavaFX UI to test DB connection
- `sql/schema.sql` - initial database schema
