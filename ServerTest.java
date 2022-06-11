package sj;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ServerTest {

//static ArrayList<Integer> listport = new ArrayList<>();
static ArrayList<String>  listname = new ArrayList<>();
static ArrayList<String> listdate = new ArrayList<>();
static ArrayList<InetSocketAddress> listClient = new ArrayList<>();
static ArrayList<Integer> normaltypes = new ArrayList<>();
MultiValueMap<InetSocketAddress, Integer> membersOnServer = new LinkedMultiValueMap<>();
static InetSocketAddress currentMember = null;
private DatagramSocket datagramSocket;
public ServerTest(DatagramSocket datagramSocket){
        this.datagramSocket = datagramSocket;
    }

public static String path="C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\";
private static String filenameTemp;

public static final int command = 0;
public static final int response = 1;
public static final int data = 2;
public static final int acknowledge = 3;

public static final int sendingseq = 0;
public static final int receivingseq = 1;
public static final int loginstate = 2;
public static final int uploadstate = 3;

public MyDatagram getSEQforsending(MyDatagram md){
    if(md.PACKETTYPE==acknowledge){
        md.SEQ = membersOnServer.getValue(currentMember,receivingseq);
    }else {
        md.SEQ = membersOnServer.getValue(currentMember,sendingseq);
        membersOnServer.set(currentMember,sendingseq, md.SEQ+1);
    }
    return md;
}

public void sendaMD(MyDatagram md, InetSocketAddress currentMember) throws IOException {
//        md = getSEQforsending(md);
        md.CHECKSUM = md.calculateCHECKSUM();
        byte[] message = md.sendBytes();
        DatagramPacket datagramPacket = new DatagramPacket(message,message.length,currentMember.getAddress(),currentMember.getPort());
        datagramSocket.send(datagramPacket);
}

public void ACK(MyDatagram md,InetSocketAddress currentMember) throws IOException {
    System.out.println("Sending acknowledgement for message whose number(SEQ) is "+md.SEQ);
    MyDatagram ack = new MyDatagram("Message"+md.SEQ+"has been received.",acknowledge);
    md = getSEQforsending(ack);
    sendaMD(ack,currentMember);
}

public MyDatagram getMDfromClient(){
    byte[] datagram = new byte[548];
    MyDatagram md = new MyDatagram();
    md.PACKETTYPE = -999;
    DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length);
    try {
        datagramSocket.receive(datagramPacket);
        datagram = datagramPacket.getData();
        md.bytesTomd(datagram);
        currentMember = new InetSocketAddress(datagramPacket.getAddress(),datagramPacket.getPort());
//        System.out.println("typebefore: "+md.PACKETTYPE);
        if(!(membersOnServer.containsKey(currentMember))){//found a new member online
            membersOnServer.add(currentMember,0);//local sending sequence number
            membersOnServer.add(currentMember,0);//receiving sequence number
            membersOnServer.add(currentMember,0);//login state(0/1)
            membersOnServer.add(currentMember,0);//upload state(0/1)
        }
        if(md.PACKETTYPE!=-999){
            boolean compareCHECKSUM = md.compareCHECKSUM(md.calculateCHECKSUM());
            if(compareCHECKSUM == true){
                if(normaltypes.contains(md.PACKETTYPE)){
                    if(md.SEQ>membersOnServer.getValue(currentMember,receivingseq)||md.SEQ==0){//new md coming from currentMember
                            membersOnServer.set(currentMember,receivingseq, md.SEQ);//change the receiving sequence number
                        ACK(md,currentMember);
                        System.out.println("received new datagram with type:"+md.PACKETTYPE);
                    }else{//this is not a new md
                        md.PACKETTYPE = -999;//throw this md
                        System.out.println("Old datagram thrown.");
                    }
                }else if(md.PACKETTYPE==3){//this is an ACK
//                    System.out.println("ACK thrown.");//no need to reply to an ACK
                }else{//weird PACKETTYPE
                    md.PACKETTYPE = -999;//throw this md
                    System.out.println("Weird type thrown.");
                }
            }else{//checksum failed
                md.PACKETTYPE = -999;//throw this md
                System.out.println("Checksum failed.");
            }
            InetAddress inetAddress =currentMember.getAddress(); // IPv4, IPv6?获取ip 地址和端口号
            InetSocketAddress inetSocketAddress = currentMember;
            int currentport = currentMember.getPort();// get portnumber from dataparkage
        }else{
            System.out.println("nothing received");
        }
    }catch (Exception e){

    }
    return md;
}

public void deliverandWaiting(MyDatagram md0) throws IOException {
    int round;
    md0 = getSEQforsending(md0);
    for (round = 1; round <= 6; round++) {
        System.out.println("Sending datagram to Client, time:" + round);
        sendaMD(md0,currentMember);
        MyDatagram md1 = getMDfromClient();
        if (md1.PACKETTYPE == acknowledge && md1.SEQ == membersOnServer.getValue(currentMember, sendingseq)-1) {
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
        deliverandWaiting(mdnum);
        int tep_num = 0;
        while (num_package > 0) {
            System.out.println("send greeting information to client" + tep_num+1);
            int left_length = Bigfile_length - (tep_num * size);
            int cur_length = 0;
            if (left_length < size) {
                cur_length = left_length;
            } else {
                cur_length = size;
            }
            byte[] bpart = Arrays.copyOfRange(Bigfile,tep_num*size,tep_num*size+cur_length);
            MyDatagram mdpart = new MyDatagram(new String(bpart),data);
            deliverandWaiting(mdpart);
            num_package--;
            tep_num++;

        }
    }




public static void creatTxtFile(String name) throws IOException {
    boolean flag = false;
    filenameTemp = path + name + ".txt";
    File filename = new File(filenameTemp);
        filename.createNewFile();
        flag = true;
}

//write file
    public static void writeTxtFile(String newStr) throws IOException {
      boolean flag = false;

        String filein = newStr + "\r\n";
        String temp = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileOutputStream fos = null;
        PrintWriter pw = null;

        try {

            File file = new File(filenameTemp);

            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // save
            for (int j = 1; (temp = br.readLine()) != null; j++) {
                buf = buf.append(temp);
                // System.getProperty("line.separator")
                // “\n”
                buf = buf.append(System.getProperty("line.separator"));
            }
            buf.append(filein);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            flag = true;
        } catch (IOException e1) {
            //TODO Auto-generated catch block
            throw e1;
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
       
    }






//private int port = 8888;
//private ServerSocket serverSocket;
//private static String fileName="C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\test.txt";
//
//    public ServerTest() throws IOException {
//        ServerSocket serverSocket = new ServerSocket(port);
//        System.out.println("，，，，，，");
//    }
//
//    private void receieveFile(String filePath) {
//        while (true) {
//            try {
//                Socket socket = null;
//
//                socket = serverSocket.accept();
//                System.out.println("，，，，");
//
//                DataInputStream dis = new DataInputStream(socket.getInputStream());
//                DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath));
//
//                byte[] buf = new byte[1027 * 9];
//                int len = 0;
//
//                while ((len = dis.read(buf)) != -1) {
//                    dos.write(buf, 0, len);
//                }
//                dos.flush();
//
//                System.out.println("，，，，");
//                dis.close();
//                dos.close();
//
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//        }
//
//    }
//







    public void Receivethensend() throws IOException {

            try{
               while(true) {
                   //accept "login" from Client;
//                   System.out.println("START");
                   MyDatagram md = getMDfromClient();


                   //print the commends from client
                   if (normaltypes.contains(md.PACKETTYPE)) {
                       String MessagefromClient = new String(md.DATA);
                       System.out.println("Message from Client  " + MessagefromClient);
                       System.out.println(currentMember.getAddress() + "  " + currentMember.getPort());


                       //Check the authentication command, verify "login", "country name" + subsequent "list" + "upload" request command
                       Pattern check_login = Pattern.compile("^login\\s(?<name>[A-Z]+[a-z]*)\\s(?<date>\\d{4})$");
                       Pattern check_list = Pattern.compile("^list$");
                       Pattern check_upload = Pattern.compile("^upload$");
                       Pattern check_download = Pattern.compile("^download\\s(?<name>[A-Z]+[a-z]*)$");
                       //Perform regular matching to check whether the input command is correct
                       Matcher matcher = check_login.matcher(MessagefromClient);
                       Matcher matcher_list = check_list.matcher(MessagefromClient);
                       Matcher macher_upload = check_upload.matcher(MessagefromClient);
                       Matcher matcher_download = check_download.matcher(MessagefromClient);


                       if (matcher.find()) {//"login" correct
                           String currentName = matcher.group("name");
                           String currentDate = matcher.group("date");
//                        String currentPort = matcher.group("port");
//                        listport.add(currentPort);


                           if (listClient.contains(currentMember)) {//In the state of the same ip address, if you request to log in to multiple countries, it will show failure
                               String IPinf = "003, ur IP has been login in";
                               System.out.println(IPinf);
                               //Create a package and send a reply message to the client
                               MyDatagram mdinf = new MyDatagram(IPinf, response);
                               deliverandWaiting(mdinf);

                           } else {
                               if (listname.contains(currentName)) {//
                                   String IPname = "001, this Country has been login in";
                                   System.out.println(IPname);
                                   System.out.println("Response to Client...");
                                   MyDatagram mdipn = new MyDatagram(IPname, response);
                                   deliverandWaiting(mdipn);
                                   //
                               } else {//legal!
                                   listname.add(currentName);
                                   listdate.add(currentDate);
                                   listClient.add(currentMember);
                                   System.out.println("A client has successfully logged in.");
                                   System.out.println("Client address and port: " + currentMember.getAddress() + "  " + currentMember.getPort());
                                   String IPpass = "000, Successfully logged in! Welcome!";
                                   System.out.println(IPpass);
                                   System.out.println("Response to Client...");
                                   MyDatagram mdps = new MyDatagram(IPpass, response);
                                   deliverandWaiting(mdps);
                                   //回复client端，是合法登录
                               }
                           }
                            int num_c = listClient.size();
                           System.out.println("Current clients online:");
                           for (int i=0;i<num_c;i++){
                               System.out.println(listname.get(i)+" "+listClient.get(i).getAddress()+" "+listClient.get(i).getPort()+" "+listdate.get(i));
                           }

                       } else if (matcher_list.find()) {//check server's"list"
//                        Scanner order_list = new Scanner(System.in);
//                        String a = order_list.nextLine();
//                        if(a.equals("list")) {
                           if (listClient.contains(currentMember)) {
                               System.out.println("Has received a list command.");
                               String reponse_list = "007 Sending a list of clients:";
                               System.out.println(reponse_list);
                               System.out.println("Sending 007 notification...");
                               MyDatagram mdrl = new MyDatagram(reponse_list, response);
                               deliverandWaiting(mdrl);
                               creatTxtFile("Countrieslist");
                               //Create a packet, the server creates a list of connected countries
                               StringBuilder stringBuilder0 = new StringBuilder();
                               for (int i = 0; i < listname.size(); i++) {
                                   String list = "New Year Country: " + listname.get(i) + " IP: " + listClient.get(i).getAddress() + "Port: " + listClient.get(i).getPort() + "Date: " + listdate.get(i);
                                   stringBuilder0.append(list + "\r\n");
                               }
                               String path1 = "C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\Countrieslist.txt";
                               BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path1)));
                               wr.write(stringBuilder0.toString());
                               wr.close();


                               String file_list = path + "Countrieslist.txt";
                               File file = new File(file_list);
                               FileReader fileReader = new FileReader(file);
                               BufferedReader bufferedReader = new BufferedReader(fileReader);
                               StringBuilder stringBuilder = new StringBuilder();
                               String temp = null;
                               while ((temp = bufferedReader.readLine()) != null) {
                                   stringBuilder.append(temp + "\r\n");

                               }

                               MyDatagram mdlist = new MyDatagram(stringBuilder.toString(), data);
                               deliverandWaiting(mdlist);
                               fileReader.close();
                               bufferedReader.close();
                           } else {
                               String response_checklist = "009,Authentication failed, please log in";
                               System.out.println(response_checklist);
                               MyDatagram nologin = new MyDatagram(response_checklist, response);
                               deliverandWaiting(nologin);
                           }


                       } else if (macher_upload.find()) {
                           if (listClient.contains(currentMember)) {
                               String response_upload = "008 Waiting for a file to be uploaded…";
                               System.out.println(response_upload);
                               System.out.println("Response to Client...  ");
                               MyDatagram mdru = new MyDatagram(response_upload, response);
                               deliverandWaiting(mdru);

//                    System.out.println("printing"+data);
                               int index = listClient.indexOf(currentMember);
                               String name = listname.get(index);
                               StringBuilder stringBuilder = new StringBuilder();
                               String path = new String("C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\CSconnection\\Server\\");
                               stringBuilder.append(path);
                               stringBuilder.append(name);
                               String dirpath = stringBuilder.toString();
                               System.out.println(dirpath);
                               File file = new File(dirpath);
                               boolean isDirCreated = file.mkdir();
                               stringBuilder.append("\\greeting.txt");
                               File filename = new File(stringBuilder.toString());
                               filename.createNewFile();

//                            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stringBuilder.toString())));
//                            wr.write(data);
//                            wr.close();
//                            System.out.println("Successfully file written.\n");

                               int round;
                               MyDatagram mdpknum = new MyDatagram();
                               for (round = 1; round <= 6; round++) {
                                   System.out.println("getting package numbers from Client,trying times:" + round);
                                   mdpknum = getMDfromClient();
                                   if (mdpknum.PACKETTYPE == data) {
                                       System.out.println("Has received number package");
                                       break;
                                   }
                               }
                               String sdata = new String(mdpknum.DATA);
                               char[] chars = sdata.toCharArray();
                               //char[] newchar ={'q'};
                               ArrayList<Character> newchar = new ArrayList<Character>();
                               int len = 0;
                               for (int i = 0; i < chars.length; i++) {
                                   if ((chars[i] >= '0') && (chars[i] <= '9')) {
                                       newchar.add(chars[i]);
                                   } else {
                                       len = i;
                                       break;
                                   }
                               }
                               System.out.println(newchar.size());
                               StringBuilder charBuilder = new StringBuilder();
                               for (Character character : newchar) {
                                   charBuilder.append(character);
                               }
                               sdata = new String(charBuilder.toString());
                               int d = Integer.parseInt(sdata);
                               int tep_num_s = 0;
//                            String greeting_r = "C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\CSconnection\\Server\\greetingUK\\greeting.txt";
                               BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true)));

                               MyDatagram mddata = new MyDatagram();
                               while (d > 0) {

                                   System.out.println("receiving greeting information from client" + (tep_num_s + 1));
                                   //114514
                                   for (round = 1; round <= 6; round++) {
                                       System.out.println("getting package " + (tep_num_s + 1) + " from Client,trying times:" + round);
                                       mddata = getMDfromClient();
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
                               membersOnServer.set(currentMember, uploadstate, 1);//change upload state


                           } else {
                               String response_checklist = "009,Authentication failed, please log in";
                               System.out.println(response_checklist);
                               MyDatagram nologin = new MyDatagram(response_checklist, response);
                               deliverandWaiting(nologin);
                           }


                       } else if (matcher_download.find()) {
                           if (listClient.contains(currentMember)) {
                               String countryname = matcher_download.group("name");
                               if (listname.contains(countryname)) {
                                   int index = listname.indexOf(countryname);
                                   InetSocketAddress memberRequired = listClient.get(index);
                                   if (membersOnServer.getValue(memberRequired, uploadstate) == 1) {//found the file required, permit download
                                       String dpass = "010 Sending file for download...";
                                       System.out.println(dpass);
                                       MyDatagram mddp = new MyDatagram(dpass, response);
                                       deliverandWaiting(mddp);
                                       byte[] buffer_se = new byte[1024];
                                       StringBuilder sbpath = new StringBuilder("C:\\Users\\JJ\\Desktop\\SURREY21CS\\NW\\CSconnection\\Server\\");
                                       sbpath.append(countryname + "\\greeting.txt");
                                       BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sbpath.toString())));
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
                                   } else {//this country hasn't uploaded data yet
                                       String dfail = "011 The file you required doesn't exist!";
                                       System.out.println(dfail);
                                       MyDatagram mddf = new MyDatagram(dfail, response);
                                       deliverandWaiting(mddf);
                                   }
                               } else {//no such country online
                                   String dfail = "011 The file you required doesn't exist!";
                                   System.out.println(dfail);
                                   MyDatagram mddf = new MyDatagram(dfail, response);
                                   deliverandWaiting(mddf);
                               }
                           } else {
                               String response_checklist = "009,Authentication failed, please log in";
                               System.out.println(response_checklist);
                               MyDatagram nologin = new MyDatagram(response_checklist, response);
                               deliverandWaiting(nologin);
                           }
                       } else {

                           String IPinv = "002，error, Invalid Command!";
                           System.out.println(IPinv);
                           MyDatagram md0 = new MyDatagram(IPinv, response);
                           System.out.println("Response to Client...  ");
                           deliverandWaiting(md0);

                       }


//                        Scanner order_list = new Scanner(System.in);
//                        String a = order_list.nextLine();
//                   if(a.equals("list")) {
//                       System.out.println("当前在线的国家：");
//                       creatTxtFile("Countrieslist");
//                       for (int i = 0; i < listname.size(); i++) {
//                           String list = "新年国家：" + listname.get(i) + " IP：" + listClient.get(i).getAddress() + " 端口：" + listClient.get(i).getPort() + " 日期" + listdate.get(i);
//
//                           writeTxtFile(list);
//                           System.out.println(list);
//                       }
//
//
//                   }


                   }
               }
        }catch (IOException e){
                e.printStackTrace();

            }
//        //功能键：list
//        Scanner list = new Scanner(System.in);
//
//        if (list.equals("list")){
//            if(listname.contains("China")&&listClient.contains(inetSocketAddress)) {
//                System.out.println("当前在线的国家：");
//                for (int i = 0; i < listname.size(); i++) {
//                    System.out.println("新年国家：" + listname.get(i) + " IP：" + listClient.get(i).getAddress() + " 端口：" + listClient.get(i).getPort() + " 日期" + listdate.get(i));
//                }
//            }else{
//                System.out.println("004 Authentication failed. Please log in.");
//            }
//        }





    }





    public static void main(String[]args) throws IOException {
        for(int i=0;i<=2;i++){
            normaltypes.add(i);//only command,response and data need to be replied by an ACK
        }
        DatagramSocket datagramSocket = new DatagramSocket(1234); // listening package
        datagramSocket.setSoTimeout(6000);
        ServerTest serverTest= new ServerTest(datagramSocket);
        serverTest.Receivethensend();




//        try {
//            new ServerTest().receieveFile(fileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }

}
