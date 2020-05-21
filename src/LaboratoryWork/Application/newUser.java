package LaboratoryWork.Application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class newUser extends Thread {
    private static int count_users = 0;
    private final int id;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private Socket socket;
    private String user_name;

    public newUser(Socket s) {
        socket = s;
        id = count_users++;
        try {
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
            user_name = inStream.readUTF();
            Server.usersList.addLast(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(int data) {
        try {
            outStream.writeInt(data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String data) {
        try {
            outStream.writeUTF(data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateUsers() {
        for (newUser user : Server.usersList) {
            user.sendData("update");
            user.sendData(Server.usersList.size());
            for(newUser usr : Server.usersList)
                user.sendData(usr.getUserName());
        }
    }

    public int getID() { return id; }
    public String getUserName() { return user_name; }

    public void run() {
        try {
            updateUsers();
            boolean exit = false;

            while(!exit) {
                String action = inStream.readUTF();
                switch (action) {
                    case "send" -> {
                        String target = inStream.readUTF();
                        for(newUser user: Server.usersList) {
                            if(user.getUserName().equals(target)) {
                                user.sendData("request");
                                user.sendData(this.getUserName());
                            }
                        }
                        Server.serverConsole.info_area.append(">>> " + this.getUserName()
                                + " выслал запрос на конфиг. файл " + target + "\n");
                    }

                    case "answer" -> {
                        String target = inStream.readUTF();
                        String data = inStream.readUTF();
                        for(newUser user: Server.usersList) {
                            if(user.getUserName().equals(target)) {
                                user.sendData("accept");
                                user.sendData(data);
                            }
                        }
                        Server.serverConsole.info_area.append(">>> " + this.getUserName()
                                + " принял запрос " + target + "\n");
                    }

                    case "disconnect" -> {
                        exit = true;
                        Server.usersList.remove(this);
                        inStream.close();
                        outStream.close();
                        socket.close();
                        updateUsers();
                        Server.serverConsole.info_area.append(">>> " + this.getUserName() + " отключился\n");
                    }
                }
            }
        }
        catch (IOException e) {
            Server.usersList.remove(this);
            updateUsers();
            Server.serverConsole.info_area.append(">>> " + this.getUserName() + " был исключён. Причина: клиент перестал отвечать...\n");
        }

    }
}