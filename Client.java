package com.example.ourmessenger;

import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.net.*;
import java.io.File;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.Random;

public class Client extends Thread {
    static String port0 = "5000";
    static String port1 = "5001";
    static String port2 = "5002";

    static String ip0 = "localhost";
    static String ip1 = "localhost";
    static String ip2 = "localhost";
    public static ArrayList<Pair<String,String>> hardcodedBrokerInfo=new ArrayList<Pair<String,String>>();
    public static volatile HashSet<String> topicsWithNewMessages=new HashSet<String>();
    static ObjectOutputStream out = null;
    static ObjectInputStream in = null;
    static Socket requestSocket = null;
    private static HashMap<String,ArrayList<String>> responsibleBrokers;
    public static ArrayList<String> conversationNames= new ArrayList<String>();
    public static ProfileName user;
    public static volatile ArrayList<String> subscribers = new ArrayList<String>();
    public static ArrayList<String> broker=new ArrayList<String>();
    public static HashMap<String,ArrayList<MultimediaFile>> myHistory=new HashMap<String,ArrayList<MultimediaFile>>();
    public static ArrayList<String> brokerForStories =new ArrayList<String>(){{add("localhost"); add("5000");}};
    private static Consumer sub;

    Client() { }

    public void run() {

        try {
            hardcodedBrokerInfo.add(new Pair<String,String> (ip0,port0)); hardcodedBrokerInfo.add(new Pair<String,String> (ip1,port1)); hardcodedBrokerInfo.add(new Pair<String,String> (ip2,port2));
            Scanner sc= new Scanner(System.in);
            System.out.println("Welcome to ourMessenger");
            System.out.println("What would you like your profilename to be?");
            String profileName=sc.next();
            user = new ProfileName(profileName);

            sub=new Consumer(user);
            sub.start();


            System.out.println("Connecting to random broker");
            Random rand=new Random(System.currentTimeMillis());
            int random_int=rand.nextInt(3);
            requestSocket = new Socket(hardcodedBrokerInfo.get(random_int).getElement0(), Integer.parseInt(hardcodedBrokerInfo.get(random_int).getElement1()));  //sindesi se tixaio broker
            System.out.println("Connected to broker with ip and port:"+hardcodedBrokerInfo.get(random_int).getElement0()+" "+hardcodedBrokerInfo.get(random_int).getElement1());
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
                conversationNames.add(conv);
                myHistory.put(conv,new ArrayList<MultimediaFile>());

            }
            File f1=new File(user.getName()+"'s Folder");
            f1.mkdir();
            System.out.println("Got info from broker");
            while (choice!=9) {
                synchronized (topicsWithNewMessages) {
                    if(topicsWithNewMessages.isEmpty()){
                        System.out.print("You have no new messages");
                    }
                    else{
                        System.out.print("You have new messages in these topics:");
                        for (String topic:topicsWithNewMessages){
                            System.out.print(topic+" ");
                        }
                    }
                    System.out.println("");
                }
                System.out.println("What would you like to do:\n" +
                        "1)Find about all coversation names \n" +
                        "2)Subscribe to a Topic \n" +
                        "3)Get latest messages from a topic \n" +
                        "4)Publish a message to A topic \n" +
                        "5)Get users of a topic\n" +
                        "6)Unsubscribe from a topic\n" +
                        "7)Upload a story\n"  +
                        "8)Watch a story\n" +
                        "9)Close app");
                String input=sc.next();
                try {
                    choice = Integer.parseInt(input);
                    handleChoice(choice,sc);

                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number from the options available");
                }

            }
            System.exit(0);
        } catch (UnknownHostException unknownHost) {
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

    public static void main(String args[]) {
        new Client().start();

    }
    public static void connect(String ip,String port){
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

    public static void connectToResponsibleBroker(String topic){
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

    public static void disconnect(){
        try {
            in.close();
            out.close();
            requestSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MultimediaFile makeFile(Scanner sc,String path){
        System.out.println("Provide how you want the message to be named:");
        String mulitmediaFileName = sc.next();

        System.out.println("Do you wanna provide the details of the file?Answer with 'yes' or 'no'");
        String userAnswer = sc.next();
        while(!(userAnswer.equals("yes")||userAnswer.equals("no"))){
            System.out.println("Please answer with 'yes' or 'no'");
            userAnswer=sc.next();
        }
        String dateCreated = "0";
        String fileLength = "0";
        String framerate = "0";
        String frameWidth = "0";
        String frameHeight = "0";
        if (userAnswer.equals("yes")) {
            System.out.println("Provide file's date:");
            dateCreated = sc.next();

            System.out.println("Provide file's length:");
            fileLength = sc.next();

            System.out.println("Provide file's framerate:");
            framerate = sc.next();

            System.out.println("Provide file's frameWidth:");
            frameWidth = sc.next();

            System.out.println("Provide file's frameHeight:");
            frameHeight = sc.next();
        }

        MultimediaFile publisherFile = new MultimediaFile(mulitmediaFileName, user.getName(), dateCreated, fileLength, framerate, frameWidth, frameHeight, path, new Date());
        return publisherFile;
    }

    public static ArrayList<String> findResponsibleBroker(String topic){
        return responsibleBrokers.get(topic);
    }


    public static void handleChoice(int choice, Scanner sc){
        try {
            String topic;
        switch(choice){

            case 1:
                //GET CONVERSATION NAMES
                System.out.println("Printing conversation names:");
                for(String conversation:conversationNames){
                    System.out.print(conversation+" ");
                }
                System.out.println("");
                break;
            case 2:
                //SUBSCRIBE TO TOPIC
                System.out.println("Provide Topic's name:");
                topic = sc.next();
                if (conversationNames.contains(topic)){
                    if(user.isSubscribedToConversation(topic)){
                        System.out.println("You are already subscribed to "+topic);
                        break;
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
                    System.out.println("Sent registration request to broker for topic");
                    Boolean success=in.readBoolean();
                    if(success==true){
                        System.out.println("Succesful registration to:"+topic);
                        user.updateSubscribedConversations(topic,0);
                        //enimerwse to subscribedConversation tou profileName
                    } else{
                        System.out.println("Registration failed");
                    }
                    disconnect();
                }
                else{
                    System.out.println("Conversation name provided doesn't exist");
                }
                break;
            case 3:
                //GET LATEST MESSAGES FROM A TOPIC
                System.out.println("Provide Topic's name:");
                topic = sc.next();
                if (conversationNames.contains(topic)) {
                    if (!user.getSubscribedConversations().containsKey(topic)) {
                        System.out.println("You aren't subscribed to " + topic + " so you can't read messages from there unless you subscribe");
                        break;
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
                        System.out.println("There are no new messages for topic " + topic); //if there are no new messages
                        break;
                    }

                    ArrayList<MultimediaFile> chunksReceived = new ArrayList<MultimediaFile>();
                    List<MultimediaFile> fileInChunks = new ArrayList<MultimediaFile>();

                    int numberOfChunks = in.readInt();
                    System.out.println("Number of chunks being received" + numberOfChunks);
                    for (int i = 1; i <= numberOfChunks; i++) {
                        MultimediaFile chunk = (MultimediaFile) in.readObject();//diavase to chunk kai apothikeuse to kapou
                        chunksReceived.add(chunk);
                    }
                    System.out.println("Received Latest messages");
                    System.out.println("Downloading latest messages to your device at "+ Paths.get(".").toAbsolutePath().normalize().toString() +"\\"+ user.getName() + "'s Folder");
                    Map<String, List<MultimediaFile>> groupedFiles = chunksReceived.stream().collect(Collectors.groupingBy(MultimediaFile::getMessageId));
                    for (String messageId : groupedFiles.keySet()) {
                        fileInChunks = groupedFiles.get(messageId);
                        Collections.sort(fileInChunks, Comparator.comparing(MultimediaFile::getChunkNumber));   //sort me vasi to chunk number
                        MultimediaFile composedFile = MultimediaFile.composeFile(fileInChunks);
                        composedFile.DownloadFile(Paths.get(".").toAbsolutePath().normalize().toString() +"\\"+ user.getName() + "'s Folder\\");
                        System.out.println("Downloaded '" + composedFile.getMultimediaFileName() + "' by " + composedFile.getProfileName());
                        myHistory.get(topic).add(composedFile);
                    }


                    int newOffset = in.readInt();
                    user.updateSubscribedConversations(topic, newOffset);
                    synchronized (topicsWithNewMessages){
                        if(topicsWithNewMessages.contains(topic)){
                            topicsWithNewMessages.remove(topic);
                        }
                    }
                    disconnect();
                }
                else{
                    System.out.println("Conversation name provided doesn't exist");
                }

                break;
            case 4:
                //PUBLISH A MESSAGE
                System.out.println("Provide Topic's name:");
                topic = sc.next();

                if (conversationNames.contains(topic)) {
                    //check an einai subscribed se auti ti sinomilia
                    if (!user.getSubscribedConversations().containsKey(topic)) {
                        System.out.println("You aren't subscribed to " + topic + " so you can't post there unless you subscribe");
                        break;
                    }
                    System.out.println("Provide path to file and name of actual file");

                    //checkare oti iparxei to arxeiooo se auto to path
                    String path = sc.next();

                    if (!(new File(path)).isFile()) {
                        System.out.println("File path provided is invalid(remember to type the full path AND name of the file at the end");
                        break;
                    }

                    MultimediaFile publisherFile = makeFile(sc,path);
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
                    System.out.println(answer);
                    if (answer) {     //diavase true an stalthike epitixws
                        System.out.println("File successfully published");
                        user.updateUserVideoFilesMap(topic, publisherFile);
                    } else {
                        System.out.println("File publication failed.");
                    }


                    disconnect();
                }
                else{
                    System.out.println("Conversation name provided doesn't exist");
                }
                break;
            case 5:
                //SEARCH USERS IN A TOPIC
                System.out.println("Provide Topic's name:");
                topic = sc.next();
                if (conversationNames.contains(topic)) {
                    if (!user.isSubscribedToConversation(topic)) {
                        System.out.println("You aren't subscribed to " + topic);
                        break;
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
                    System.out.println("Getting subscribed users from Broker about topic "+topic);
                    ArrayList<String> subs = (ArrayList<String>) in.readObject();
                    System.out.print("Users:");
                    for (String sub:subs){
                        System.out.print(sub+" ");
                    }
                    System.out.println("");
                    disconnect();

                }
                else{
                    System.out.println("Conversation name provided doesn't exist");
                }
                break;
            case 6:
                //UNSUBSCRIBE FROM TOPIC
                System.out.println("Provide Topic's name:");
                topic = sc.next();
                if (conversationNames.contains(topic)) {
                    if (!user.getSubscribedConversations().containsKey(topic)) {
                        System.out.println("You aren't subscribed to " + topic);
                        break;
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
                    System.out.println("Contacting Broker");
                    if(in.readBoolean()){
                        System.out.println("Successfully unsubscribed from topic "+topic);
                        user.unsubscribeFromConversation(topic);
                        synchronized (topicsWithNewMessages){
                            if(topicsWithNewMessages.contains(topic)){
                                topicsWithNewMessages.remove(topic);
                            }
                        }
                    }
                    else{
                        System.out.println("Unsubscription from topic "+topic+ " failed");
                    }
                    disconnect();
                }
                else{
                    System.out.println("Conversation name provided doesn't exist");
                }

                break;
            case 7:
                //UPLOAD STORY
                System.out.println("Provide path to file and name of actual file");
                String path = sc.next();

                if (!(new File(path)).isFile()) {
                    System.out.println("File path provided is invalid(remember to type the full path AND name of the file at the end");
                    break;
                }

                MultimediaFile publisherFile=makeFile(sc,path);
                List<MultimediaFile> chunks = publisherFile.makeFileIntoChunks();

                //kane connect ston ipefthino broker gia ta stories
                connect(brokerForStories.get(0),brokerForStories.get(1));
                out.writeObject("pub");
                out.flush();

                out.writeInt(7);
                out.flush();
                out.writeObject(user);
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
                System.out.println(answer);
                if (answer) {     //diavase true an stalthike epitixws
                    System.out.println("Story successfully uploaded");
                } else {
                    System.out.println("Story upload failed.");
                }


                disconnect();
                break;
            case 8:
                //WATCH STORY
                connect(brokerForStories.get(0),brokerForStories.get(1));

                out.writeObject("pub");
                out.flush();

                out.writeInt(8);
                out.flush();
                System.out.println("Getting user names with active stories from broker");
                ArrayList<String> userWithStories=(ArrayList<String>) in.readObject();
                if(userWithStories.isEmpty()){
                    System.out.println("There are no active stories");
                    break;
                }
                System.out.print("Users with active stories:");
                for(String user:userWithStories){
                    System.out.print(user+" ");
                }
                System.out.println("");

                System.out.println("Select the name of the user whose stories you want to watch:");
                String storyUser=sc.next();
                while(!userWithStories.contains(storyUser)){
                    System.out.println("Please select a user from the list above");
                    storyUser=sc.next();
                }

                out.writeObject(storyUser);  //edw einai o user tou opoiou theloume na diavasoume ta stories oxi o dikos mas user
                out.flush();
                answer=in.readBoolean();
                if (!answer) {
                    System.out.println("User " + storyUser + " hasn't got any active stories (they probably just expired)"); //if there are no new messages
                    break;
                }

                ArrayList<MultimediaFile> chunksReceived = new ArrayList<MultimediaFile>();
                List<MultimediaFile> fileInChunks = new ArrayList<MultimediaFile>();

                int numberOfChunks = in.readInt();
                System.out.println("Number of chunks being received" + numberOfChunks);
                for (int i = 1; i <= numberOfChunks; i++) {
                    MultimediaFile chunk = (MultimediaFile) in.readObject();
                    chunksReceived.add(chunk);
                }
                System.out.println("Received Stories from user:"+storyUser);
                System.out.println("Downloading stories to your device at "+ Paths.get(".").toAbsolutePath().normalize().toString() +"\\"+ user.getName() + "'s Folder");
                Map<String, List<MultimediaFile>> groupedFiles = chunksReceived.stream().collect(Collectors.groupingBy(MultimediaFile::getMessageId));
                for (String messageId : groupedFiles.keySet()) {
                    fileInChunks = groupedFiles.get(messageId);
                    Collections.sort(fileInChunks, Comparator.comparing(MultimediaFile::getChunkNumber));   //sort me vasi to chunk number
                    MultimediaFile composedFile = MultimediaFile.composeFile(fileInChunks);
                    composedFile.DownloadFile(Paths.get(".").toAbsolutePath().normalize().toString() +"\\"+ user.getName() + "'s Folder\\");
                    System.out.println("Downloaded story '" + composedFile.getMultimediaFileName() + "' by " + composedFile.getProfileName());
                }
                disconnect();

                break;
            case 9:
                //sub.interrupt();

                break;
            default:
                System.out.println("Please enter a number from the options available");
        }
        return;
        }
        catch (IOException | ClassNotFoundException Exception) {
            Exception.printStackTrace();
        }

    }
}






