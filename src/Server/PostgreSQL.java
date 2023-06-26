package Server;
import PossibleClassInCollection.Flat.*;

import java.math.BigInteger;
import java.net.PasswordAuthentication;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Hashtable;

public class PostgreSQL {
    static final String DB_URL = "jdbc:postgresql://localhost:1234/studs";
    static final String USER = "s368938";
    static final String PASS = "5sG4j6tOx0XjbQMc";
    private String username;
    private String password;
public PostgreSQL(){}
    public PostgreSQL(String username, String password){
    this.username=username;
    this.password=password;
    }

    public Hashtable<Long, Flat> getFlatsFromBd() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Flat flat;
        House house;
        User user;
        Hashtable<Long, Flat> flats = new Hashtable<>();
        try {
            Connection connection = DriverManager.getConnection(DB_URL,USER, PASS);
            String getFlats = "select * from flats";
            PreparedStatement statementFlats = connection.prepareStatement(getFlats);
            ResultSet resultSetFlats = statementFlats.executeQuery();
            while (resultSetFlats.next()) {
                    long id = resultSetFlats.getLong("id");
                    String name = resultSetFlats.getString("name");
                    long x = resultSetFlats.getLong("x");
                    int y = resultSetFlats.getInt("y");
                ZonedDateTime dateTime = resultSetFlats.getObject("creationdate", OffsetDateTime.class)
                        .atZoneSameInstant(ZoneId.systemDefault());
                    long area = resultSetFlats.getLong("area");
                    int numberofrooms = resultSetFlats.getInt("numberofrooms");
                    Furnish furnish = Furnish.chekerScript(resultSetFlats.getString("furnish"));
                    View view = View.chekerScript(resultSetFlats.getString("view"));
                    Transport transport = Transport.chekerScript(resultSetFlats.getString("transport"));
                    int house_id = resultSetFlats.getInt("house_id");
                    int users_id = resultSetFlats.getInt("users_id");
                    flat = new Flat();
                    flat.setId(id);
                    flat.setName(name);
                    Coordinates coordinates = new Coordinates(x,y);
                    flat.setCoordinates(coordinates);
                    flat.setCreationDate(dateTime);
                    flat.setArea(area);
                    flat.setNumberOfRooms(numberofrooms);
                    flat.setFurnish(furnish);
                    flat.setView(view);
                    flat.setTransport(transport);


                    String getHouse = "select * from house where id = ?";
                    PreparedStatement statementHouse = connection.prepareStatement(getHouse);
                    statementHouse.setInt(1, house_id);
                    ResultSet resultHouse = statementHouse.executeQuery();
                    while (resultHouse.next()){
                        String nameHouse = resultHouse.getString("name");
                        int year = resultHouse.getInt("year");
                        int numberoofloors = resultHouse.getInt(4);
                        int numberofflatsonfloor = resultHouse.getInt("numberofflatsonfloor");
                        int numberoflifts = resultHouse.getInt("numberoflifts");
                        house = new House(nameHouse,year,numberoofloors,numberofflatsonfloor,numberoflifts);
                        flat.setHouse(house);
                    }
                    String getUsers = "select * from users where id = ?";
                    PreparedStatement statementUsers = connection.prepareStatement(getUsers);
                    statementUsers.setInt(1, users_id);
                    ResultSet resultUsers = statementUsers.executeQuery();
                    while (resultUsers.next()){
                            String nameUser = resultUsers.getString("username");
                            String password_hash = resultUsers.getString("password_hash");
                            user = new User(nameUser, password_hash);
                        flat.setUser(user);
                    }
                flats.put(flat.getId(), flat);
            }
            return flats;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }

    }
    public void saveBD(Hashtable<Long,Flat> flats){
        try{
            int house_id = 0;
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL,USER,PASS);

            PreparedStatement deleteWorkerStatement = connection.prepareStatement("DELETE FROM flats");
            deleteWorkerStatement.executeUpdate();
            PreparedStatement deleteOrgStatement = connection.prepareStatement("DELETE FROM house");
            deleteOrgStatement.executeUpdate();

            for (Flat flat : flats.values()) {
                String sqlInsertHouse = "insert into house(name,year,numberoffloors,numberofflatsonfloor,numberoflifts) values(?,?,?,?,?)";
                PreparedStatement statementHouse = connection.prepareStatement(sqlInsertHouse,Statement.RETURN_GENERATED_KEYS);
                statementHouse.setString(1,flat.getHouse().getName());

                statementHouse.setInt(2,flat.getHouse().getYear());

                statementHouse.setInt(3,flat.getHouse().getNumberOfFloors());

                statementHouse.setInt(4,flat.getHouse().getNumberOfFlatsOnFloor());

                statementHouse.setInt(5,flat.getHouse().getNumberOfLifts());

                statementHouse.executeUpdate();
                ResultSet resultHouse = statementHouse.getGeneratedKeys();
                if (resultHouse.next()) {
                    house_id = resultHouse.getInt(1);
                }
                String sqlInsertFlat ="insert into flats(users_id,name,x,y,creationdate,area,numberofrooms,furnish,view,transport,house_id) values(?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement statementFlat = connection.prepareStatement(sqlInsertFlat,Statement.RETURN_GENERATED_KEYS);
                statementFlat.setInt(1,getUserId(flat.getUser()));


                statementFlat.setString(2,flat.getName());

                statementFlat.setLong(3, flat.getCoordinates().getX());

                statementFlat.setInt(4,flat.getCoordinates().getY());
                Timestamp timestamp = Timestamp.from(flat.getCreationDate().toInstant());

                statementFlat.setTimestamp(5, timestamp);

                statementFlat.setLong(6,flat.getArea());

                statementFlat.setInt(7,flat.getNumberOfRooms());

                statementFlat.setString(8,flat.getFurnish().name());

                statementFlat.setString(9,flat.getView().name());

                statementFlat.setString(10,flat.getTransport().name());

                statementFlat.setInt(11,house_id);

                statementFlat.executeUpdate();
                ResultSet resultFlat = statementFlat.getGeneratedKeys();
                if (resultFlat.next()) {
                long flatId = resultFlat.getLong(2);
                flat.setId(flatId);
                System.out.println("Success");
            }
                                }




        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
    public int getUserId(User user) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUserName());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                return id;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public String checkUser(User receivedUser) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            String sqlUser = "SELECT * FROM users WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlUser);
            preparedStatement.setString(1, receivedUser.getUserName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String passwordHash = resultSet.getString("password_hash");
                    PreparedStatement passwordStatement = connection.prepareStatement("SELECT password_hash FROM users where username = ?");
                    passwordStatement.setString(1, receivedUser.getUserName());
                    try (ResultSet passwordResultSet = passwordStatement.executeQuery()) {
                        if (passwordResultSet.next()) {
                            String receivedHash = receivedUser.getPassword();
                            if (passwordHash.equals(hashPassword(receivedHash))) {
                                return "Вход выполнен";
                            } else {
                                return "Неверный пароль.";
                            }
                        } else {
                            return "Ошибка в получении пароля.";
                        }
                    }
                } else {
                    return "Такого пользователя нет в системе.";
                }
            }
        } catch (ClassNotFoundException e){
            System.err.println(e.getMessage() + "aboba");
            return "Ошибка при авторизации";
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Ошибка при авторизации";
        }
    }
    public String registerUser(User newUser) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, newUser.getUserName());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return "Пользователь с таким именем уже существует";
                    }
                }
            }

            String sqlInsert = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {
                preparedStatement.setString(1, newUser.getUserName());
                preparedStatement.setString(2, hashPassword(newUser.getPassword()));
                int rowsInserted = preparedStatement.executeUpdate();
                return "Успешная регистрация. Воспользуйтесь командой login, чтобы авторизоваться";
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return "Ошибка регистрации";
    }


    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] bytes = md.digest(password.getBytes());
            BigInteger integers = new BigInteger(1, bytes);
            String newPassword = integers.toString(16);
            while (newPassword.length() < 32) {
                newPassword = "0" + newPassword;
            }
            return newPassword;
        } catch (NoSuchAlgorithmException exception) {
            System.err.println(exception.getMessage());
            System.err.println("Алгоритм хеширования пароля не найден!");
            throw new IllegalStateException(exception);
        }
    }









    /*public static void main(String[] argv) {
        System.out.println("Testing connection to PostgreSQL JDBC");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
            return;
        }

        System.out.println("PostgreSQL JDBC Driver successfully connected");
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DB_URL,USER, PASS);
    }catch (SQLException e ){
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }
        if (connection != null) {
            System.out.println("You successfully connected to database now");
        } else {
            System.out.println("Failed to make connection to database");
        }

    }*/
}