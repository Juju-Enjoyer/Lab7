/*package Server;

import Command.CollectionManager.CollectionManager;

import Command.CommandList.Save.Save;
import Command.CommandProcessor.Command;
import Command.CommandProcessor.SoundPlayer;
import Command.ListCommand;
import Exceptions.IllegalKeyException;
import Exceptions.IllegalValueException;
import Exceptions.NoSuchCommandException;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;

public class ServerMain {
    //TODO перенести тримап с командами в отдельный класс done
    //TODO разделить сервер на модули и классы
    ListCommand commandsList = new ListCommand();
    CollectionManager cm = new CollectionManager(commandsList.getCommands());
   private DatagramSocket datagramSocket;

    PostgreSQL postgreSQL;
    public ServerMain(DatagramSocket datagramSocket) throws SQLException, ClassNotFoundException {
        this.datagramSocket = datagramSocket;
    }
    private byte[] buffer = new byte[65507];
    private static Logger logger = Logger.getLogger(ServerMain.class.getName());
    boolean result = true;
    public void execute(String username,String password) throws IOException, NoSuchCommandException, IllegalValueException, IllegalKeyException {

        DatagramPacket datagramPacket;
        InetAddress inetAddress;
        try {
            this.postgreSQL = new PostgreSQL(username,password);
            cm.setPostgreSQL(this.postgreSQL);
            cm.setFlats();
            *//*cm.checkWorkFile();*//*
        }catch (NoSuchElementException | SQLException | ClassNotFoundException e) {
            SoundPlayer.playSound("Pud_ability_hook_miss_01_ru.wav",2500);
            System.out.println("Они убили Кени");
            result = false;}
        int port;

        Runnable consoleReader = () -> {
            Scanner scan = new Scanner(new InputStreamReader(System.in));
            while (true) {
                try {
                    String commandWithOutArgs = "";
                    String args = "";

                    String[] commandLine = scan.nextLine().split(" ");
                    commandWithOutArgs = commandLine[0];
                    if(commandWithOutArgs.toUpperCase().equals("SAVE")){
                        save("");
                    }
                    else {
                        throw new NoSuchCommandException();
                    }



                } catch (NoSuchElementException e){
                    try {
                        save("");
                    } catch (NoSuchCommandException ex) {
                        throw new RuntimeException(ex);
                    } catch (IllegalValueException ex) {
                        throw new RuntimeException(ex);
                    } catch (IllegalKeyException ex) {
                        throw new RuntimeException(ex);
                    }
                    logger.warning("Отключение сервера: cntl+D");
                    System.exit(0);
                } catch (NoSuchCommandException e) {
                    logger.warning("на сервере реализованна только команда save");
                } catch (IllegalValueException e) {
                    logger.warning("что-то не так с аргументом save");
                } catch (IllegalKeyException e) {
                    logger.warning("что-то не так с аргументом save");
                }
            }
        };

        new Thread(consoleReader).start();




        while (result){
            try {

                buffer = new byte[65507];
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                inetAddress = datagramPacket.getAddress();
                port = datagramPacket.getPort();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {

                buffer = datagramPacket.getData();
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
                Command command = (Command)in.readObject();
                command.setCm(cm);



                
                String result = command.execute(command.getArgument());
                logger.info(command.getName());
                String str = command.getName()+" execute"+"\n"+result;
                cm.historyFiller(command.getName());
                sendMessage(str, inetAddress,port);





            } catch (IOException e) {
                e.printStackTrace();
                save("");
            } catch (ClassNotFoundException | NoSuchCommandException | IllegalValueException | IllegalKeyException e) {
    sendMessage(e.getMessage(),inetAddress,port);
            e.printStackTrace();}
            catch (NoSuchElementException e){
                e.printStackTrace();
            }
        }
    }
    public void save(String args) throws NoSuchCommandException, IllegalValueException, IllegalKeyException {
        Save save = new Save(cm);
        logger.info(save.execute(args));
    }

    public void sendMessage (String thing, InetAddress inetAddress, int port) throws IOException {
        byte[] data = new byte[65507];
        data = thing.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress,port);
        datagramSocket.send(datagramPacket);
    }

    public static void main(String[] args) throws IOException, NoSuchCommandException, IllegalValueException, IllegalKeyException, SQLException, ClassNotFoundException {
        DatagramSocket socket = new DatagramSocket(1237);
        ServerMain server = new ServerMain(socket);
        System.out.println("Я сервер");
        server.execute(args[0],args[1]);
    }
}*/

package Server;

import Command.CollectionManager.CollectionManager;
import Command.CommandList.Save.Save;
import Command.CommandProcessor.Command;
import Command.CommandProcessor.SoundPlayer;
import Command.ListCommand;
import Exceptions.IllegalKeyException;
import Exceptions.IllegalValueException;
import Exceptions.NoSuchCommandException;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerMain {
    //TODO перенести тримап с командами в отдельный класс done
    //TODO разделить сервер на модули и классы
    ListCommand commandsList = new ListCommand();
    CollectionManager cm = new CollectionManager(commandsList.getCommands());
    private DatagramSocket datagramSocket;

    PostgreSQL postgreSQL;

    public ServerMain(DatagramSocket datagramSocket) throws SQLException, ClassNotFoundException {
        this.datagramSocket = datagramSocket;
    }

    private byte[] buffer = new byte[65507];
    private static Logger logger = Logger.getLogger(ServerMain.class.getName());
    boolean result = true;

    public void execute(String username, String password) throws IOException, NoSuchCommandException, IllegalValueException, IllegalKeyException {

        try {
            this.postgreSQL = new PostgreSQL(username, password);
            cm.setPostgreSQL(this.postgreSQL);
            cm.setFlats();
            /*cm.checkWorkFile();*/
        } catch (NoSuchElementException | SQLException | ClassNotFoundException e) {
            SoundPlayer.playSound("Pud_ability_hook_miss_01_ru.wav", 2500);
            System.out.println("Они убили Кени");
            result = false;
        }

        ExecutorService requestReadPool = Executors.newCachedThreadPool();
        ExecutorService requestProcessingPool = Executors.newCachedThreadPool();
        ExecutorService responseSendPool = Executors.newCachedThreadPool();

        Runnable consoleReader = () -> {
            Scanner scan = new Scanner(new InputStreamReader(System.in));
            while (true) {
                try {
                    String commandWithOutArgs = "";
                    String args = "";

                    String[] commandLine = scan.nextLine().split(" ");
                    commandWithOutArgs = commandLine[0];
                    if (commandWithOutArgs.toUpperCase().equals("SAVE")) {
                        save("");
                    } else {
                        throw new NoSuchCommandException();
                    }

                } catch (NoSuchElementException e) {
                    try {
                        save("");
                    } catch (NoSuchCommandException ex) {
                        ex.printStackTrace();
                    } catch (IllegalValueException ex) {
                        ex.printStackTrace();
                    } catch (IllegalKeyException ex) {
                        ex.printStackTrace();
                    }
                    logger.warning("Отключение сервера: cntl+D");
                    System.exit(0);
                } catch (NoSuchCommandException e) {
                    logger.warning("на сервере реализованна только команда save");
                } catch (IllegalValueException e) {
                    logger.warning("что-то не так с аргументом save");
                } catch (IllegalKeyException e) {
                    logger.warning("что-то не так с аргументом save");
                }
            }
        };

        new Thread(consoleReader).start();

        while (result) {
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            InetAddress inetAddress = datagramPacket.getAddress();
            int port = datagramPacket.getPort();

            Runnable requestReadTask = () -> {
                try {
                    buffer = datagramPacket.getData();
                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
                    Command command = (Command) in.readObject();
                    command.setCm(cm);

                    responseSendPool.submit(() -> {
                        String result;
                        try {
                            result = command.execute(command.getArgument());
                            logger.info(command.getName());
                            String str = command.getName() + " execute" + "\n" + result;
                            cm.historyFiller(command.getName());
                            sendMessage(str, inetAddress, port);
                        } catch (IOException | NoSuchCommandException | IllegalValueException | IllegalKeyException e) {
                            e.printStackTrace();
                            try {
                                sendMessage(e.getMessage(), inetAddress, port);
                            } catch (IOException ex) {
                                e.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    });
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    try {
                        save("");
                    } catch (NoSuchCommandException ex) {

                    } catch (IllegalValueException ex) {

                    } catch (IllegalKeyException ex) {

                    }
                }
            };

            requestReadPool.submit(requestReadTask);
        }

        requestReadPool.shutdown();
        requestProcessingPool.shutdown();
        responseSendPool.shutdown();
    }

    public void save(String args) throws NoSuchCommandException, IllegalValueException, IllegalKeyException {
        Save save = new Save(cm);
        logger.info(save.execute(args));
    }

    public void sendMessage(String thing, InetAddress inetAddress, int port) throws IOException {
        byte[] data = new byte[65507];
        data = thing.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, port);

        Runnable responseSendTask = () -> {
            try {
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ExecutorService responseSendPool = Executors.newCachedThreadPool();
        responseSendPool.submit(responseSendTask);
        responseSendPool.shutdown();
    }

    public static void main(String[] args) throws IOException, NoSuchCommandException, IllegalValueException, IllegalKeyException, SQLException, ClassNotFoundException {
        DatagramSocket socket = new DatagramSocket(1237);
        ServerMain server = new ServerMain(socket);
        System.out.println("Я сервер");
        server.execute(args[0], args[1]);
    }
}