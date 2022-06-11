package sj;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class ClientTest1   {
    public static final int command = 0;
    public static final int response = 1;
    public static final int data = 2;
    public static final int acknowledge = 3;
    static ArrayList<Integer> normaltypes = new ArrayList<>();
    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
//    private byte[] buffer = new byte[1024];
    private String host = "localhost";
    private int port = 8888;
    private Socket socket;
    private static String fileName="C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\test.txt";

    static Scanner upload = new Scanner(System.in);
    static int SEQ_receive = 0;
    static int SEQ_send = 0;

    public ClientTest1(DatagramSocket datagramSocket, InetAddress inetAddress){
        this.datagramSocket= datagramSocket;
        this.inetAddress = inetAddress;
    }

    public ClientTest1() throws IOException {
        socket = new Socket(host, port);
    }

    //encyption: signature
    public static String signature(String inStr) throws Exception {
        MessageDigest SHA = null;
        try {
            SHA = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        byte[] byteArray = inStr.getBytes("UTF-8");
        byte[] md5Bytes = SHA.digest(byteArray);
        StringBuffer value = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                value.append("0");
            }
            value.append(Integer.toHexString(val));
        }
        return value.toString();
    }







    private void sendFile(String filePath) {

        while (true) {

            try {
                File file = new File(filePath);
                System.out.println("File length: " + file.length() + "bytes");
                DataInputStream dis = new DataInputStream(new FileInputStream(filePath));
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                byte[] buf = new byte[1024 * 9];
                int len = 0;
                while ((len = dis.read(buf)) != -1) {
                    dos.write(buf, 0, len);

                }
                dos.flush();
                System.out.println("end of upload");

                dis.close();
                dos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    //Image encryption BASE64, just an idea
// public static String img_Base64Str(String imgFile) {
//     InputStream inputStream = null;
//     byte[] imgdata = null;
//     try {
//         inputStream = new FileInputStream(imgFile);//
//         imgdata = new byte[inputStream.available()];
//         inputStream.read(imgdata);
//         inputStream.close();
//     } catch (IOException e) {
//         e.printStackTrace();
//     }
//     // encryption
//     BASE64Encoder encoder = new BASE64Encoder();
//     return encoder.encode(imgdata);
// }

    public MyDatagram getSEQforsending(MyDatagram md){
        if(md.PACKETTYPE==acknowledge){
            md.SEQ = SEQ_receive;
        }else {
            md.SEQ = SEQ_send;
            SEQ_send++;
        }
        return md;
    }
    public void sendaMD(MyDatagram md) throws IOException {
//        md = getSEQforsending(md);
        md.CHECKSUM = md.calculateCHECKSUM();
        byte[] message = md.sendBytes();
        InetAddress dd = InetAddress.getByName("Localhost");
        DatagramPacket datagramPacket = new DatagramPacket(message,message.length,dd,1234);
        datagramSocket.send(datagramPacket);
    }
    public void ACK(MyDatagram md) throws IOException {
        System.out.println("Sending acknowledgement for message whose number(SEQ) is "+md.SEQ);
        MyDatagram ack = new MyDatagram("Message"+md.SEQ+"has been received.",acknowledge);
        getSEQforsending(ack);
        sendaMD(ack);
    }

    public MyDatagram getMDfromServer(){
        byte[] datagram = new byte[548];
        MyDatagram md = new MyDatagram();
        md.PACKETTYPE = -999;
        DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length);
        try {
            datagramSocket.receive(datagramPacket);
            datagram = datagramPacket.getData();
            md.bytesTomd(datagram);
            if(md.PACKETTYPE!=-999){
                boolean compareCHECKSUM = md.compareCHECKSUM(md.calculateCHECKSUM());
                if(compareCHECKSUM == true){
                    if(normaltypes.contains(md.PACKETTYPE)){
                        if(md.SEQ>SEQ_receive||md.SEQ==0){//new md coming from currentMember
                            SEQ_receive= md.SEQ;//change the receiving sequence number
                            ACK(md);
                        }else{//this is not a new md
                            System.out.println("old data thrown");
                            md.PACKETTYPE = -999;//throw this md
                        }
                    }else if(md.PACKETTYPE==3){//this is an ACK
//                        System.out.println("ack thrown");//no need to reply to an ACK
                    }else{//weird PACKETTYPE
                        System.out.println("weird type");
                        md.PACKETTYPE = -999;//throw this md
                    }
                }else{//checksum failed
                    System.out.println("checksum failed");
                    md.PACKETTYPE = -999;//throw this md
                }
            }else{
                System.out.println("no datagram received.");
            }
        }catch (Exception e){

        }
        return md;
    }

    public void deliverandWaiting(MyDatagram md0,boolean islost) throws IOException {
        int round;
        getSEQforsending(md0);
        for (round = 1; round <= 6; round++) {
            System.out.println("Sending datagram to Server, time:" + round);
            if(islost==false) {
                sendaMD(md0);
            }
            islost=false;
            MyDatagram md1 = getMDfromServer();
            System.out.println("deliverandwaiting get a packet with type:"+md1.PACKETTYPE);
//            System.out.println("md1.SEQ=="+md1.SEQ);
//            System.out.println("SEQ_send=="+SEQ_send);
            if (md1.PACKETTYPE == acknowledge && md1.SEQ == SEQ_send-1) {
                System.out.println("Has received ACK for this datagram.");
                break;
            }
        }
    }
    public void divideandDeliver(byte[] Bigfile) throws IOException {
        int Bigfile_length = Bigfile.length;
        int size = 520;
        int num_package = Bigfile_length / size + 1;
        String s = String.valueOf(num_package);
        System.out.println("There is "+s+" packets");
        MyDatagram mdnum = new MyDatagram(s,data);
        deliverandWaiting(mdnum,false);
        int tep_num = 0;
        while (num_package > 0) {
            System.out.println("send greeting information to server" + tep_num+1);
            int left_length = Bigfile_length - (tep_num * size);
            int cur_length = 0;
            if (left_length < size) {
                cur_length = left_length;
            } else {
                cur_length = size;
            }
            byte[] bpart = Arrays.copyOfRange(Bigfile,tep_num*size,tep_num*size+cur_length);
            MyDatagram mdpart = new MyDatagram(new String(bpart),data);
            deliverandWaiting(mdpart,false);
            num_package--;
            tep_num++;

        }
    }
    public void SendthenReceive(){


            while(true) {
                //发送命令请求
                try{
                System.out.println("Please input datagram to server:");
                Scanner scanner = new Scanner(System.in);
                String messagetoserver = scanner.nextLine();
                boolean islost = false;
//                if (messagetoserver.equals("list")){
//                    islost=true;
//                }//test 10
                MyDatagram mts = new MyDatagram(messagetoserver, command);
                deliverandWaiting(mts,islost);
                //接收命令
                int round;
                MyDatagram mdr = new MyDatagram();
                for (round = 1; round <= 6; round++) {
                    System.out.println("getting response from Server,trying times:" + round);
                    mdr = getMDfromServer();
                    if (mdr.PACKETTYPE == response) {
                        System.out.println("Has received response package");
                        break;
                    }
                }
                if(mdr.PACKETTYPE==response){
                String messagefromserver = new String(mdr.DATA);
                System.out.println(messagefromserver);

                //接收"list"创建命令，在本地创建list.txt文件
                    if (messagefromserver.equals("007 Sending a list of clients:")) {
                        System.out.println("getting list package from server");
                        MyDatagram mdlist = new MyDatagram();
                        for (round = 1; round <= 6; round++) {
                            System.out.println("getting list from Server,trying times:" + round);
                            mdlist = getMDfromServer();
                            if (mdlist.PACKETTYPE == data) {
                                System.out.println("Has received list package");
                                break;
                            }
                        }
                        if(mdlist.PACKETTYPE==data) {
                            String sdata = new String(mdlist.DATA);
//                    System.out.println("printing"+sdata);
                            String Client = new String("C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\CSconnection\\Client\\list.txt");
                            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Client)));
                            wr.write(sdata);
                            wr.close();
                            System.out.println("Successfully file written.\n");
                        }
                    } else if (messagefromserver.equals("008 Waiting for a file to be uploaded…")) {

                        byte[] buffer_se = new byte[1024];
                        String filepath = new String("C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\CSconnection\\Client\\upload\\upload_to_server.txt");
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
                        String tmp = "";
                        StringBuilder stringBuilder = new StringBuilder();
                        while ((tmp = bufferedReader.readLine()) != null) {
                            stringBuilder.append(tmp + "\r\n");
                        }
                        bufferedReader.close();
                        String file = stringBuilder.toString();
                        byte[] Bigfile = new byte[65536];
                        Bigfile = file.getBytes(StandardCharsets.UTF_8);
                        divideandDeliver(Bigfile);


//                    buffer = greetingtoserver.getBytes();
//                    DatagramPacket greetingpacket = new DatagramPacket(buffer, buffer.length, inetAddress, 1234);
//                    datagramSocket.send(greetingpacket);


                    }else if(messagefromserver.equals("010 Sending file for download...")){
                        MyDatagram mdpknum = new MyDatagram();
                        for (round = 1; round <= 6; round++){
                            System.out.println("getting package numbers from Server,trying times:"+round);
                            mdpknum = getMDfromServer();
                            if(mdpknum.PACKETTYPE==data){
                                System.out.println("Has received number package");
                                break;
                            }
                        }
                        String sdata = new String(mdpknum.DATA);
                        char[] chars = sdata.toCharArray();
                        //char[] newchar ={'q'};
                        ArrayList<Character> newchar = new ArrayList<Character>();
                        int len = 0;
                        for(int i = 0; i<chars.length; i++){
                            if((chars[i]>='0')&&(chars[i]<='9')){
                                newchar.add(chars[i]);
                            }else{
                                len = i;
                                break;
                            }
                        }
                        System.out.println(newchar.size());
                        StringBuilder charBuilder = new StringBuilder();
                        for(Character character:newchar){
                            charBuilder.append(character);
                        }
                        sdata = new String(charBuilder.toString());
                        int d = Integer.parseInt(sdata);
                        int tep_num_s = 0;
                        String filepath = new String("C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\CSconnection\\Client\\download\\download_from_server.txt");
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath,true)));

                        MyDatagram mddata = new MyDatagram();
                        while(d>0){

                            System.out.println("receiving greeting information from Server"+(tep_num_s+1));
                            //114514
                            for (round = 1; round <= 6; round++) {
                                System.out.println("getting package "+(tep_num_s+1)+" from Client,trying times:" + round);
                                mddata = getMDfromServer();
                                if (mddata.PACKETTYPE == data) {
                                    System.out.println("Has received data package");
                                    break;
                                }
                            }
                            String data_re = new String(mddata.DATA);
                            bufferedWriter.write(data_re);


                            d--;
                            tep_num_s++;

                        }
                        bufferedWriter.close();
                    }
                }else {
                    continue;
                }

                }catch (IOException e){
//                    e.printStackTrace();
                }
            }
        }







    public static void main(String[] args) throws IOException {
        for(int i=0;i<=2;i++){
            normaltypes.add(i);//only command,response and data need to be replied by an ACK
        }
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(6000);
        InetAddress inetAddress = InetAddress.getByName("localhost");
        ClientTest1 clientTest = new ClientTest1(datagramSocket,inetAddress);

        clientTest.SendthenReceive();

    }
}
