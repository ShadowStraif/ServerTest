package com.example.servertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView connectionList;
    private ArrayList<String> connections ;
    private ArrayAdapter<String> adapter;
    private Thread serverThread = null;
    private ServerSocket serverSocket;
    public static final int PORT = 6000;
    private boolean serverFlag;
    private Handler updateConversationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionList = findViewById(R.id.list);
        connections = new ArrayList<>();
        //адаптер
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, connections);
        connectionList.setAdapter(adapter);

        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new Server());
        this.serverThread.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Server implements Runnable{
        @Override
        public void run() {
            serverFlag = true;
            Socket socket = null;

            try {
                    serverSocket = new ServerSocket(PORT);

            } catch (IOException e) {
                e.printStackTrace();
            }
//&& !serverFlag
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();


                       ClientCommunication clientThread = new ClientCommunication(socket);
                       new Thread(clientThread).start();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class ClientCommunication implements Runnable{
        private Socket clientSocket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private String ip = null  ;
        public ClientCommunication(Socket clientSocket) {

            this.clientSocket = clientSocket;
            try {
                this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.writer = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
                while (!clientSocket.isClosed()) {
          //  if (!clientSocket.isClosed()) {
           // while (!Thread.currentThread().isInterrupted()) {
                try {
                    ip = String.valueOf(clientSocket.getLocalAddress());
                    String read = reader.readLine();
                    if(read==null)
                    {
                        if(adapter.getCount()!=0)
                        {
                            updateConversationHandler.post(new deleteUI(ip));
                        }
                        break;
                    }

                    writer.write(read);
                    writer.newLine();
                    writer.flush();

                    updateConversationHandler.post(new updateUI(ip));


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
           /* else
            {
                if(adapter.getCount()!=0)
                {
                    updateConversationHandler.post(new deleteUI(ip));
                }

            }*/

        }
    }


    class updateUI implements Runnable{
        private String foundIP;

        public updateUI(String strip) {
            foundIP = strip;

        }

        @Override
        public void run() {
            adapter.add(foundIP);
        }
    }

    class deleteUI implements Runnable{
        private String foundIP;

        public deleteUI(String strip) {
            foundIP = strip;

        }

        @Override
        public void run() {
            int cnt = adapter.getCount();
            for(int i=0;i<=cnt ;i++) {
                adapter.remove(foundIP);
                adapter.notifyDataSetChanged();
            }
          //  adapter.clear();

        }
    }
}