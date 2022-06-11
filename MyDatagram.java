package sj;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MyDatagram {
    public static final int command = 0;
    public static final int response = 1;
    public static final int data = 2;
    public static final int acknowledge = 3;

    public int PACKETTYPE;
    public int SEQ;
    public int LENGTH;
    public byte[] CHECKSUM;
    public byte[] DATA;

    public MyDatagram(String data, int PACKETTYPE){
        this.DATA = data.getBytes();
        this.PACKETTYPE = PACKETTYPE;
        this.LENGTH = this.DATA.length;
    }

    public MyDatagram(){
        super();
    }

    public String printString() {
        return "MyPacket{" +
                "PACKETTYPE=" + PACKETTYPE +
                ", seq=" + SEQ +
                ", length=" + LENGTH +
                ", checksum=" + Arrays.toString(CHECKSUM) +
                ", data=" + Arrays.toString(DATA) +
                '}';
    }

    public byte[] sendBytes() throws IOException {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        dous.writeInt(SEQ);
        dous.writeInt(PACKETTYPE);
        dous.writeInt(LENGTH);
        dous.write(DATA);
        dous.write(CHECKSUM);
        dous.flush();
        byte[] bmd = bous.toByteArray();
        bous.close();
        dous.close();
        return bmd;
    }

    public void bytesTomd(byte[] bmd) throws IOException {
        ByteArrayInputStream bins = new ByteArrayInputStream(bmd);
        DataInputStream dins = new DataInputStream(bins);
        SEQ = dins.readInt();
        PACKETTYPE = dins.readInt();
        LENGTH = dins.readInt();
        DATA = new byte[LENGTH];
        CHECKSUM = new byte[16];
        dins.read(DATA);
        dins.read(CHECKSUM);
        bins.close();
        dins.close();
    }

    public byte[] calculateCHECKSUM() throws IOException {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        dous.writeInt(SEQ);
        dous.writeInt(PACKETTYPE);
        dous.writeInt(LENGTH);
        dous.write(DATA);
        dous.flush();
        String mdwithoutCHECKSUM = new String(bous.toByteArray());
        bous.close();
        dous.close();
        if(mdwithoutCHECKSUM == null || mdwithoutCHECKSUM.length() == 0) {
            return null;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(mdwithoutCHECKSUM.getBytes());
            byte[] byteArray = md5.digest();

            char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'a','b','c','d','e','f'};
            // A byte corresponds to two hexadecimal numbers, so the length is the byte array multiplied by 2
            char[] charArray = new char[byteArray.length * 2];
            int index = 0;
            for (byte b : byteArray) {
                charArray[index++] = hexDigits[b>>>4 & 0xf];
                charArray[index++] = hexDigits[b & 0xf];
            }
            byte[] md5_32 = new String(charArray).getBytes();
            byte[] md5_16 = Arrays.copyOfRange(md5_32,8,24);
            return md5_16;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean compareCHECKSUM(byte[] otherchecksum) throws IOException {
        String local = new String(CHECKSUM);
        String calculated = new String(otherchecksum);
//        System.out.println("Local cks:"+local);
//        System.out.println("cks to compare:"+calculated);
        System.out.println("checksumCorrect?:"+Arrays.equals(CHECKSUM,otherchecksum));
        return Arrays.equals(CHECKSUM,otherchecksum);
//        return true;
    }

}
