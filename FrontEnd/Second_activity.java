package com.example.ourmessenger;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Second_activity extends AppCompatActivity {
    private static final long serialversionUID = -1L;
    TextView resultTextView;
    TextView infoTextView;
    TextView results_header;
    TextView results_text;
    TextView newMessages;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button7;
    Button button8;
    Button button_return;
    Button infoButton;
    Button results_enter_chat;
    Button chat_return;
    EditText infoEditText;
    View resultLayout;
    View infoLayout;
    View options;
    View chatLayout;
    int opt;
    int sec_opt=1;
    private ProfileName user;
    private ObjectOutputStream out=null;
    private ObjectInputStream in=null;
    private Socket requestSocket = null;
    private volatile HashSet<String> topicsWithNewMessages=new HashSet<String>();
    private HashMap<String,ArrayList<String>> responsibleBrokers;

    private String port0 = "5000";
    private String port1 = "5001";
    private String port2 = "5002";

    private String ip0 = "192.168.1.7";
    private String ip1 = "192.168.1.7";
    private String ip2 = "192.168.1.7";
    private ArrayList<Pair<String,String>> hardcodedBrokerInfo=new ArrayList<Pair<String,String>>();
    public static ArrayList<String> brokerForStories =new ArrayList<String>(){{add("192.168.1.7"); add("5000");}};
    private RecyclerView recyclerView;


    protected ArrayList<String> conversationNames= new ArrayList<String>();
    protected HashMap<String,ArrayList<MultimediaFile>> myHistory=new HashMap<String,ArrayList<MultimediaFile>>();
    String onoma_user;
    String active_topic;
    String temp_topicName;
    String temp_path;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_second);
       // resultTextView=(TextView) findViewById(R.id.enteredAppTextView);

        Bundle extras = getIntent().getExtras();
        onoma_user=extras.getString("userName");
        user=new ProfileName(extras.getString("userName"));

        LayoutInflater inflater=getLayoutInflater();
        options=inflater.inflate(R.layout.activity_second,null);
        setContentView(options);
        resultLayout=inflater.inflate(R.layout.results,null);
        infoLayout=inflater.inflate(R.layout.provide_info,null);
        chatLayout=inflater.inflate(R.layout.chat,null);


        recyclerView=chatLayout.findViewById(R.id.recyclerView);
        chat_return=chatLayout.findViewById(R.id.chat_return);


        resultTextView=(TextView) findViewById(R.id.enteredAppTextView);
        button1=(Button) findViewById(R.id.button1);
        button2=(Button) findViewById(R.id.button2);
        button3=(Button) findViewById(R.id.button3);
        button4=(Button) findViewById(R.id.button4);
        button5=(Button) findViewById(R.id.button5);
        button6=(Button) findViewById(R.id.button6);
        button7=(Button) findViewById(R.id.button7);
        button8=(Button) findViewById(R.id.button8);
        newMessages=(TextView)findViewById(R.id.newMessages);



        results_header=(TextView) resultLayout.findViewById(R.id.results_header);
        results_text=(TextView) resultLayout.findViewById(R.id.results_text);
        button_return=(Button) resultLayout.findViewById(R.id.return_results);
        results_enter_chat=(Button) resultLayout.findViewById(R.id.enter_chat);

        infoTextView=(TextView) infoLayout.findViewById(R.id.info_TextView);
        infoEditText=(EditText) infoLayout.findViewById(R.id.info_EditText);
        infoButton=(Button) infoLayout.findViewById(R.id.info_button);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onStart(){
        super.onStart();
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        File folder = new File(Environment.getExternalStorageDirectory() +"/Download/ourMessenger");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        resultTextView.setText("Welcome to ourMessenger " +onoma_user);

        Consumer cc=new Consumer(user);
        cc.start();
        ConnectThread t=new ConnectThread(onoma_user);
        t.start();

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FindNames fn=new FindNames();
                fn.execute();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(infoLayout);
                infoTextView.setText("Enter topic's name you would like to subscribe to");
                opt=2;
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(infoLayout);
                infoTextView.setText("Enter topic's name you would like to get the latest messages");
                opt=3;
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(infoLayout);
                infoTextView.setText("Enter topic's name you would like to publish a message to");
                opt=4;
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(infoLayout);
                infoTextView.setText("Enter topic's name whose users you want to search");
                opt=5;
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(infoLayout);
                infoTextView.setText("Enter topic's name you would like to unsubscribe from");
                opt=6;
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String input=infoEditText.getText().toString();
                infoEditText.setText("");
                switch(opt) {
                    case 2:
                        new Subscribe().execute(input);
                        break;
                    case 3:
                        active_topic=input;
                        new GetLatestMessages().execute(input);
                        break;
                    case 4:
                        switch(sec_opt){
                            case 1:
                                infoTextView.setText("Enter the path of the message you would like to publish");
                                temp_topicName=input;
                                sec_opt=2;
                                break;
                            case 2:
                                infoTextView.setText("Enter how you would like the message to be named");
                                temp_path=input;
                                sec_opt=3;
                                break;
                            case 3:
                                new PublishMessage().execute(temp_topicName,temp_path,input);
                                sec_opt=1;
                                break;

                        }
                        break;

                    case 5:
                        new Search().execute(input);
                        break;
                    case 6:
                        new Unsubscribe().execute(input);
                        break;
                }

            }
        });

        button_return.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(options);
                synchronized (topicsWithNewMessages) {
                    if(topicsWithNewMessages.isEmpty()){
                        newMessages.setText("You have no new messages");
                    }
                    else{
                        String s="You have new messages in these topics:";
                        for (String topic:topicsWithNewMessages){
                            s+=topic+" ";
                        }
                        newMessages.setText(s);
                    }
                }
                results_enter_chat.setVisibility(View.INVISIBLE);
            }
        });

        results_enter_chat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //mpes sto chat kai exe se mia metavliti to current topic na to peraseis
                setAdapter(active_topic);
                setContentView(chatLayout);
            }
        });

        chat_return.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(options);
                synchronized (topicsWithNewMessages) {
                    if(topicsWithNewMessages.isEmpty()){
                        newMessages.setText("You have no new messages");
                    }
                    else{
                        String s="You have new messages in these topics:";
                        for (String topic:topicsWithNewMessages){
                            s+=topic+" ";
                        }
                        newMessages.setText(s);
                    }
                }
                results_enter_chat.setVisibility(View.INVISIBLE);
            }
        });


    }
    //sdcard/Download/ourMessenger/skilivid.mp4

    private void setAdapter(String topic){
        recyclerAdapter adapter=new recyclerAdapter(myHistory.get(topic));
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }

    private class Consumer extends Thread{
        public ProfileName user;

        public void run() {
            StayNotified notifiedBroker0=new StayNotified(user,"192.168.1.7",5000);
            StayNotified notifiedBroker1=new StayNotified(user,"192.168.1.7",5001);
            StayNotified notifiedBroker2=new StayNotified(user,"192.168.1.7",5002);
            notifiedBroker0.start();
            notifiedBroker1.start();
            notifiedBroker2.start();
        }

        public Consumer(ProfileName user){
            this.user=user;
        }


        private class StayNotified extends Thread{
            private ProfileName user;
            private String Ip;
            private int port;
            public StayNotified(ProfileName user,String Ip,int port){
                this.user=user;
                this.Ip=Ip;
                this.port=port;
            }

            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            Socket requestSocket = null;

            public void run(){
                try {
                    requestSocket=new Socket(Ip,port);
                    out=new ObjectOutputStream(requestSocket.getOutputStream());
                    in =new ObjectInputStream(requestSocket.getInputStream());
                    out.writeObject("sub");
                    out.writeObject(user);

                    //perimene na mathaineis se poia topic erxontai kainourgia minimata
                    while(true){
                        String topicWithNewMessage=(String)in.readObject();
                        System.out.println("Got a message");
                        synchronized (topicsWithNewMessages){
                            topicsWithNewMessages.add(topicWithNewMessage);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        in.close();
                        out.close();
                        requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }

        }
    }

    private class ConnectThread extends Thread{

        public ConnectThread(String username){
            //this.userName=username;
        }
        public void run() {
            try {
                hardcodedBrokerInfo.add(new Pair<String,String> (ip0,port0)); hardcodedBrokerInfo.add(new Pair<String,String> (ip1,port1)); hardcodedBrokerInfo.add(new Pair<String,String> (ip2,port2));
                Random rand = new Random(System.currentTimeMillis());
                int random_int = rand.nextInt(3);
                requestSocket = new Socket(hardcodedBrokerInfo.get(random_int).getElement0(), Integer.parseInt(hardcodedBrokerInfo.get(random_int).getElement1()));
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                int choice=0;
                out.writeObject("pub");
                out.flush();
                out.writeInt(choice);
                out.flush();
                responsibleBrokers =(HashMap<String,ArrayList<String>>)in.readObject();
                disconnect();
                for(String conv:responsibleBrokers.keySet()){
                    //System.out.println(conv);
                    conversationNames.add(conv);
                    myHistory.put(conv,new ArrayList<MultimediaFile>());

                }
            }
            catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }


        }
        private void disconnect(){
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class FindNames extends AsyncTask<Integer,Void,ArrayList<String>> {
        ProgressDialog progressDialog;

        protected ArrayList<String> doInBackground(Integer... args){

            return conversationNames;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            Toast.makeText(Second_activity.this, "Finding names", Toast.LENGTH_LONG).show();
            //setContentView(R.layout.results);
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            //ui thread
            // execution of result of Long time consuming operation
           // progressDialog.dismiss();
            setContentView(resultLayout);
            String names="";
            for (String name:result){
                names+=name+"\n";
            }
            results_header.setText("The available converstations are:");
            results_text.setText(names);

            Log.d("MY_TAG","TEST");
        }

    }

    private class Subscribe extends AsyncTask<String,Void,String> {
        //SUBSCRIBE TO TOPIC
        ProgressDialog progressDialog;


        protected String doInBackground(String... args){
            String topic=args[0];
            try {
                if (conversationNames.contains(topic)) {
                    if (user.isSubscribedToConversation(topic)) {
                        return "You are already subscribed to " + topic;
                       // break;
                    }
                    connectToResponsibleBroker(topic);
                    out.writeObject("pub");
                    out.flush();
                    out.writeInt(2);
                    out.flush();
                    out.writeObject(topic);
                    out.flush();
                    out.writeObject(user);
                    out.flush();
                    //System.out.println("Sent registration request to broker for topic");
                    //Thread.sleep(5000);
                    Boolean success = in.readBoolean();
                    disconnect();
                    if (success == true) {
                        user.updateSubscribedConversations(topic, 0);
                        return "Succesful registration to:" + topic;
                        //enimerwse to subscribedConversation tou profileName
                    } else {
                        return "Registration failed";
                    }
                } else {
                    return "Conversation name provided doesn't exist";
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
           return "";
        }

        @Override
        protected void onProgressUpdate(Void... param) {
            Toast.makeText(Second_activity.this, "Subscribing to Topic", Toast.LENGTH_LONG).show();
            //setContentView(R.layout.results);
        }

        @Override
        protected void onPostExecute(String result) {
            //ui thread
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            setContentView(resultLayout);
            results_header.setText(result);
            results_text.setText("");


        }

    }

    private class Unsubscribe extends AsyncTask<String,Void,String> {
        //SUBSCRIBE TO TOPIC
        ProgressDialog progressDialog;


        protected String doInBackground(String... args){
            String topic=args[0];
            try {
                //UNSUBSCRIBE FROM TOPIC
                System.out.println("Provide Topic's name:");
                //topic = sc.next();
                if (conversationNames.contains(topic)) {
                    if (!user.getSubscribedConversations().containsKey(topic)) {
                        return "You aren't subscribed to " + topic;
                    }
                    connectToResponsibleBroker(topic);
                    out.writeObject("pub");
                    out.flush();
                    out.writeInt(6);
                    out.flush();
                    out.writeObject(topic);
                    out.flush();
                    out.writeObject(user);
                    out.flush();
                    //System.out.println("Contacting Broker");
                    Boolean success = in.readBoolean();
                    disconnect();
                    if (success == true) {
                        user.unsubscribeFromConversation(topic);
                        synchronized (topicsWithNewMessages) {
                            if (topicsWithNewMessages.contains(topic)) {
                                topicsWithNewMessages.remove(topic);
                            }
                        }
                        myHistory.put(topic,new ArrayList<MultimediaFile>());
                        return "Successfully unsubscribed from topic :" + topic;

                    } else {
                        return "Unsubscription from topic " + topic + " failed";
                    }
                } else {
                        return "Conversation name provided doesn't exist";
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Void... param) {
            Toast.makeText(Second_activity.this, "Unsubscribing from topic", Toast.LENGTH_LONG).show();
            //setContentView(R.layout.results);
        }

        @Override
        protected void onPostExecute(String result) {
            //ui thread
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            setContentView(resultLayout);
            results_header.setText(result);
            results_text.setText("");



        }


    }

    private class Search extends AsyncTask<String,Void,String[]> {
        //SEARCH USER IN A TOPIC
        ProgressDialog progressDialog;


        protected String[] doInBackground(String... args){
            String topic=args[0];
            String result;
            String[] results =new String[2];
            results[1]="";
            try {
                //System.out.println("Provide Topic's name:");
                if (conversationNames.contains(topic)) {
                    if (!user.isSubscribedToConversation(topic)) {
                        results[0]= "You aren't subscribed to " + topic;
                        return results;
                    }
                    connectToResponsibleBroker(topic);
                    out.writeObject("pub");
                    out.flush();
                    out.writeInt(5);
                    out.flush();
                    out.writeObject(topic);
                    out.flush();
                    out.writeObject(user);
                    out.flush();
                    //System.out.println("Getting subscribed users from Broker about topic " + topic);
                    ArrayList<String> subs = (ArrayList<String>) in.readObject();
                    results[0]="Users in topic "+topic+":";
                    String temp="";
                    for (String sub : subs) {
                        //System.out.print(sub + " ");
                        results[1]+=sub+"\n";
                    }
                    disconnect();
                    return results;

                } else {
                    results[0]= "Conversation name provided doesn't exist";
                    return results;
                }
            }catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            Toast.makeText(Second_activity.this, "Searching users ", Toast.LENGTH_LONG).show();
            //setContentView(R.layout.results);
        }

        @Override
        protected void onPostExecute(String... results) {
            //ui thread
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            setContentView(resultLayout);
            results_header.setText(results[0]);
            results_text.setText(results[1]);


        }

    }

    private class GetLatestMessages extends AsyncTask<String,Void,String[]> {
        //Get Latest Messages from a topic
        ProgressDialog progressDialog;


        @RequiresApi(api = Build.VERSION_CODES.O)
        protected String[] doInBackground(String... args){
            String topic=args[0];
            String[] results =new String[3];
            results[1]="";
            results[2]="access";
            try {
                if (conversationNames.contains(topic)) {
                    if (!user.getSubscribedConversations().containsKey(topic)) {
                        results[0]="You aren't subscribed to " + topic + " so you can't read messages from there unless you subscribe";
                        results[2]="no";
                        return results;

                    }
                    connectToResponsibleBroker(topic);
                    System.out.println("Contacting Broker for latest messages");
                    out.writeObject("pub");
                    out.flush();
                    out.writeInt(3);
                    out.flush();
                    out.writeObject(topic);
                    out.flush();

                    //pare to offset tou topic apo to profileName kai kanto out.writeInt();
                    out.writeInt(user.getSubscribedConversations().get(topic));
                    out.flush();

                    if (!in.readBoolean()) {
                        results[0]="There are no new messages for topic " + topic; //if there are no new messages
                        return results;
                    }

                    ArrayList<MultimediaFile> chunksReceived = new ArrayList<MultimediaFile>();
                    List<MultimediaFile> fileInChunks = new ArrayList<MultimediaFile>();

                    int numberOfChunks = in.readInt();
                    //System.out.println("Number of chunks being received" + numberOfChunks);
                    for (int i = 1; i <= numberOfChunks; i++) {
                        MultimediaFile chunk = (MultimediaFile) in.readObject();//diavase to chunk kai apothikeuse to kapou
                        chunksReceived.add(chunk);
                    }
                    //System.out.println("Received Latest messages");
                    //System.out.println("Downloading latest messages to your device at " + Paths.get(".").toAbsolutePath().normalize().toString() + "\\" + user.getName() + "'s Folder");
                    Map<String, List<MultimediaFile>> groupedFiles = chunksReceived.stream().collect(Collectors.groupingBy(MultimediaFile::getMessageId));
                    for (String messageId : groupedFiles.keySet()) {
                        fileInChunks = groupedFiles.get(messageId);
                        Collections.sort(fileInChunks, Comparator.comparing(MultimediaFile::getChunkNumber));   //sort me vasi to chunk number
                        MultimediaFile composedFile = MultimediaFile.composeFile(fileInChunks);
                        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                        composedFile.DownloadFile(baseDir+ "/Download/ourMessenger/");
                        //composedFile.DownloadFile(Paths.get(".").toAbsolutePath().normalize().toString() + "\\" + user.getName() + "'s Folder\\"); //save sto correct path
                        System.out.println("Downloaded '" + composedFile.getMultimediaFileName() + "' by " + composedFile.getProfileName());
                        results[1]+=composedFile.getMultimediaFileName() + " by " + composedFile.getProfileName() + "\n";
                        myHistory.get(topic).add(composedFile);
                    }


                    int newOffset = in.readInt();
                    user.updateSubscribedConversations(topic, newOffset);
                    synchronized (topicsWithNewMessages) {
                        if (topicsWithNewMessages.contains(topic)) {
                            topicsWithNewMessages.remove(topic);
                        }
                    }
                    disconnect();
                    results[0]="Downloaded the following files to your device at '/sdcard/Download/ourMessenger/':";
                    return results;
                } else {
                    results[0]="Conversation name provided doesn't exist";
                    results[2]="no";
                    return results;
                }
            }catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            Toast.makeText(Second_activity.this, "Getting latest messages ", Toast.LENGTH_LONG).show();
            //setContentView(R.layout.results);
        }

        @Override
        protected void onPostExecute(String... results) {
            //ui thread
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            setContentView(resultLayout);
            results_header.setText(results[0]);
            results_text.setText(results[1]);
            if (results[2].equals("access")) {
                results_enter_chat.setVisibility(View.VISIBLE);
            }


        }

    }

    private class PublishMessage extends AsyncTask<String,Void,String> {
        //PUBLISH A MESSAGE
        ProgressDialog progressDialog;


        @RequiresApi(api = Build.VERSION_CODES.O)
        protected String doInBackground(String... args){
            String topic=args[0];
            String path=args[1];
            String messageName=args[2];
            String result;
            //results[1]="";///sdcard/Download/android_test_files/mqdefault.jpg
            try {
                if (conversationNames.contains(topic)) {
                    //check an einai subscribed se auti ti sinomilia
                    if (!user.getSubscribedConversations().containsKey(topic)) {
                        return "You aren't subscribed to " + topic + " so you can't post there unless you subscribe";
                    }
                    String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                    path=baseDir+path;
                    System.out.println(path);

                    if (!(new File(path)).isFile()) {
                        return "File path provided is invalid(remember to type the full path AND name of the file at the end";
                    }

                    MultimediaFile publisherFile = makeFile(path,messageName);
                    List<MultimediaFile> chunks = publisherFile.makeFileIntoChunks();

                    connectToResponsibleBroker(topic);
                    out.writeObject("pub");
                    out.flush();
                    out.writeInt(4);
                    out.flush();
                    out.writeObject(topic);
                    out.flush();

                    System.out.println("Sending File to Broker...");
                    publisherFile.setLengthInByteChunks(chunks.size());
                    out.writeInt(chunks.size());
                    out.flush();
                    for (MultimediaFile chunk : chunks) {
                        out.writeObject(chunk);
                        out.flush();
                    }
                    boolean answer = in.readBoolean();
                    //System.out.println(answer);
                    disconnect();
                    if (answer) {     //diavase true an stalthike epitixws
                        user.updateUserVideoFilesMap(topic, publisherFile);
                        return "File successfully published";
                    } else {
                        return "File publication failed.";
                    }
                    //disconnect();
                } else {
                    return "Conversation name provided doesn't exist";
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            Toast.makeText(Second_activity.this, "Publishing file ", Toast.LENGTH_LONG).show();
            //setContentView(R.layout.results);
        }

        @Override
        protected void onPostExecute(String result) {
            //ui thread
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            setContentView(resultLayout);
            results_header.setText(result);
        }

    }


    private ArrayList<String> findResponsibleBroker(String topic){
        return responsibleBrokers.get(topic);
    }

    private void connectToResponsibleBroker(String topic){
        try {
            String ip =findResponsibleBroker(topic).get(0);
            String port=findResponsibleBroker(topic).get(1);
            requestSocket = new Socket(ip,Integer.parseInt(port));
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void connect(String ip,String port){
        try {
            requestSocket = new Socket(ip,Integer.parseInt(port));   //sindesi se tixaio host
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect(){
        try {
            in.close();
            out.close();
            requestSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private MultimediaFile makeFile(String path,String mulitmediaFileName){// Scanner sc, String path

        String dateCreated = "0";
        String fileLength = "0";
        String framerate = "0";
        String frameWidth = "0";
        String frameHeight = "0";

        MultimediaFile publisherFile = new MultimediaFile(mulitmediaFileName, user.getName(), dateCreated, fileLength, framerate, frameWidth, frameHeight, path, new Date());
        return publisherFile;

    }

    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(Second_activity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Second_activity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(Second_activity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(Second_activity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(Second_activity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Second_activity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(Second_activity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class Pair<K, V> {

        private final K element0;
        private final V element1;

        public <K, V> Pair<K, V> createPair(K element0, V element1) {
            return new Pair<K, V>(element0, element1);
        }

        public Pair(K element0, V element1) {
            this.element0 = element0;
            this.element1 = element1;
        }

        public K getElement0() {
            return element0;
        }

        public V getElement1() {
            return element1;
        }
    ///sdcard/Download/ourMessenger/skiliii.jpg
    }

}
