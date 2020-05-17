package LaboratoryWork.Application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class newUser extends Thread {
    private static int count_users = 0;
    private int id;
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}