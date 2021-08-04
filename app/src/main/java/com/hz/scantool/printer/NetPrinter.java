package com.hz.scantool.printer;

import android.util.Log;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;

public class NetPrinter {

    public static int NET_PRINTER_PORT = 9100;
    public static int NET_SEND_TIMEOUT = 1000;
    public static int NET_RECEIVE_TIMEOUT = 5000;
    public Socket socket;
    public boolean isOpen = false;
    public String command = "";
    public byte[] outBytes;

    PrinterCmd printerCmd = new PrinterCmd();

    //连接网络打印机
    public boolean openPrinter(String ipaddress){
        if(socket == null){
            try{
                SocketAddress socketAddress = new InetSocketAddress(ipaddress,NET_PRINTER_PORT);
                socket = new Socket();
                socket.connect(socketAddress,NET_RECEIVE_TIMEOUT);
                isOpen = true;
            }catch (Exception e){
                e.printStackTrace();
                isOpen = false;
            }
        }else{
            try{
                socket.close();
                SocketAddress socketAddress = new InetSocketAddress(ipaddress,NET_PRINTER_PORT);
                socket = new Socket();
                socket.connect(socketAddress,NET_RECEIVE_TIMEOUT);
                isOpen = true;
            }catch (Exception e){
                e.printStackTrace();
                isOpen = false;
            }
        }

        return isOpen;
    }

    //断开网络打印机
    public void closePrinter(){
        try{
            socket.close();
            socket= null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //初始化打印机
    public void setPrinter(){
        try{
            command = printerCmd.setPrinterPos();
            OutputStream outputStream = socket.getOutputStream();
            outBytes = command.getBytes(Charset.forName("ASCII"));
            outputStream.write(outBytes);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
