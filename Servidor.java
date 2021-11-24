// package com.alexaldr.final;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.lang.System;
import java.nio.charset.Charset;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
// import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BoxLayout;


/**
 * @author alex_e_matheus
 */
public class Servidor extends Thread {
    private static ArrayList<BufferedWriter>clientes;
    private static ServerSocket server;
    private String nome;
    private Socket con;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;
    private static JTextArea texto;
    private static JLabel lblIP;
    private static JLabel lblPorta = new JLabel(".");
    private static byte[] key = {'a','r','e','y','o','u','o','k','a','r','e','y','o','u','o','k'};

    /**
        * Método construtor
        * @param com do tipo Socket
        */
    public Servidor(Socket con){

        System.setProperty("file.encoding","UTF-8");

        this.con = con;
        try {
            in  = con.getInputStream();
            inr = new InputStreamReader(in, "UTF-8");
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*CRIPTOGRAFIA */
    //###############################################
    public static final byte[] encBytes(byte[] srcBytes, byte[] key, byte[] newIv) {
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(newIv);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(srcBytes);
            return encrypted;
        }catch(Exception e){
            System.out.println("Error while encBytes: "+ e.toString());
            // byte[] err = "ERROR".getBytes();
            return null;
        }
    }

    public static final String decryptText(String strToDecrypt)
    {
        try
        {
            //setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt.getBytes(Charset.forName("UTF-8")))), Charset.forName("UTF-8"));
            // return new String(cipher.doFinal(strToDecrypt.getBytes(Charset.forName("UTF-8"))));
        }
        catch (Exception e)
        {
            System.out.println("Error while decryptText: " + e.toString());
        }
        return null;
    }

    public static final String encryptText(String sSrc)
        {
        try{
            
            byte[] ivk = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            byte[] srcBytes = sSrc.getBytes(Charset.forName("UTF-8"));
            byte[] encrypted = encBytes(srcBytes, key, ivk);
            // return Base64.encodeToString(encrypted,Base64.DEFAULT);
            return Base64.getEncoder().encodeToString(encrypted);
        }catch(Exception e){
            System.out.println("Error while encryptText: "+ e.toString());
            return "ERROR";
        }
    }
    /* FIM DA CRIPTOGRAFIA */
    //###############################################

    /**
    * Método run
    */
    public void run(){
      try{
        String msg;
        OutputStream ou =  this.con.getOutputStream();
        Writer ouw = new OutputStreamWriter(ou, "UTF-8");
        BufferedWriter bfw = new BufferedWriter(ouw);
        clientes.add(bfw);
        nome = msg = decryptText(bfr.readLine()); //#######################################################
        System.out.println("'" + nome + "' se conectou!");

        while(!"Sair".equalsIgnoreCase(msg) && msg != null)
          {
            msg = bfr.readLine(); //#######################################
            if (msg==null) {
                continue;
            }
           msg = decryptText(msg); //#######################################
           sendToAll(bfw, msg);
           System.out.println(msg);
        //    texto.append(msg);
           }

       }catch (Exception e) {
         e.printStackTrace();

       }
    }
    
    /***
    * Método usado para enviar mensagem para todos os clients
    * @param bwSaida do tipo BufferedWriter
    * @param msg do tipo String
    * @throws IOException
    */
    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException {
        BufferedWriter bwS;
        for(BufferedWriter bw : clientes){
            bwS = (BufferedWriter)bw;
            if(!(bwSaida == bwS)){
                bw.write(encryptText(nome + " -> " + msg+" \r\n")); //#####################################################
                // bw.flush();
            }else{
                texto.append(nome + " -> " + msg+" \r\n");
                // bw.flush();
            }
            bw.flush();
        }
    }
    
    /**
     * *
     * Método main
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            //Cria os objetos necessários para instânciar o servidor
            JLabel lblMessage = new JLabel("Porta do Servidor:");
            JTextField txtPorta = new JTextField("12345");
            lblPorta.setText("Porta: " + txtPorta.getText());
            Object[] texts = {lblMessage, txtPorta};
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            clientes = new ArrayList<BufferedWriter>();
            new Thread(new Monitoramento()).start();
            // ---------------------------------
            while (true) {
                System.out.println("Aguardando conexão...");
                Socket con = server.accept();
                System.out.println("Cliente conectado...");
                Thread t = new Servidor(con);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }// Fim do método main

    // INTERFACE GRAFICA
    public static class Monitoramento extends JFrame implements Runnable {
        @Override
        public void run() {
            System.out.println("Thread's MONITORAMENTO started");
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            lblIP = new JLabel("IP: 127.0.0.1");
            texto = new JTextArea(30,50);
            texto.setEditable(false);
            texto.setBackground(new Color(240, 240, 240));
            JScrollPane scroll = new JScrollPane(texto);
            texto.setLineWrap(true);
            mainPanel.add(lblIP);
            mainPanel.add(lblPorta);
            mainPanel.add(scroll);
            setTitle("SERVIDOR");
            setContentPane(mainPanel);
            setLocationRelativeTo(null);
            setResizable(false);
            setVisible(true);
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            this.setContentPane(mainPanel);
            pack();
            setLocationRelativeTo(null);
            this.setVisible(true);
        }
    }

} //Fim da classe

