import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class DAS{
    static private int MIN_MTU = 576;
    static private int MAX_IP_HEADER_SIZE = 60;
    static private int UDP_HEADER_SIZE = 8;
    static public int MAX_DATAGRAM_SIZE = MIN_MTU - MAX_IP_HEADER_SIZE - UDP_HEADER_SIZE;

    private DatagramSocket socket;
    private int applicationValue;
    private List<Integer> values = new ArrayList<Integer>();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("The number of parameters is incorrect.");
            System.exit(1);
        }
        int port;
        int number;
        try{
            port = Integer.parseInt(args[0]);
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("The parameters must be integers.");
            System.exit(1);
            return;
        }

        DAS das = new DAS();
        try{
            das.listen(port, number);
        }catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void listen(int port, int number) throws IOException {
        try {
            socket = new DatagramSocket(port);
            System.out.println("Listening on port " + port + " in Master mode");
            startMasterMode(number);
        } catch (SocketException  e) {
            System.out.println("Port " + port + " is already in use. Switching to Slave mode.");
            startSlaveMode(number);
        }
    }

    public void startMasterMode(int number) throws IOException {

    }

    public void startSlaveMode(int number) throws IOException {
        applicationValue = number;
        values.add(number);
        System.out.println("Value: " + number);
        byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagram);
        new Thread(() -> {
            int n = Integer.parseInt(new String(datagram.getData(), 0, datagram.getLength()));
            handleN(n, datagram.getAddress(), datagram.getPort());
        }).start();
    }

    public void handleN(int n, InetAddress adress, int port){
        switch (n){
            case -1:{
                System.out.println(n);
                broadcast(n, adress, port);
                socket.close();
                System.exit(0);
            }
            break;
            case 0:{
                int avg = calculateAverage();
                System.out.println(avg);
                broadcast(avg, adress, port);
            }
            break;
            default:{
                System.out.println(n);
                values.add(n);
            }
        }
    }

    public int calculateAverage(){
        int avg = 0, sum = 0, count = 0;
        for(int value : values){
            if(value != 0){
                sum += value;
                count++;
            }
        }
        if(sum != 0 || count != 0){
            avg = sum / count;
        }
        return avg;
    }
    public void broadcast(int n, InetAddress address, int port) {
        try {
            byte[] nBuff = String.valueOf(n).getBytes();
            DatagramPacket datagram = new DatagramPacket(nBuff, nBuff.length, address, port);
            socket.send(datagram);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

