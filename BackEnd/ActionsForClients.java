package com.example.ourmessenger;

import java.io.*;
import java.net.*;
import java.util.*;

public class ActionsForClients extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    List<MultimediaFile> chunks;
    String topic;
    ProfileName profileName;
    ArrayList<Thread> threads= new ArrayList<Thread>();
    ArrayList<Thread> storyThreads= new ArrayList<Thread>();
    MultimediaFile chunk;
    ArrayList<MultimediaFile> chunksReceived;
    int numberOfChunks;
    MultimediaFile composedFile;

    public ActionsForClients(Socket connection) {
        try {
            System.out.println("Got a connection...");
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String typeOfUser=(String) in.readObject();
            if (typeOfUser.equals("pub")) {
                boolean answer;
                int choice = 0;
                while (choice != 7) {
                    choice = in.readInt();
                    switch (choice) {
                        case 0:
                            out.writeObject(Broker.responsibleBrokers);
                            out.flush();
                            break;
                        case 2:
                            System.out.println("User wants to subscribe to a topic");
                            topic = (String) in.readObject();
                            profileName = (ProfileName) in.readObject();
                            synchronized (Broker.participantsperconversation) {
                                answer=Broker.subscribeTo(profileName,topic);
                            }
                            if (answer) {
                                System.out.println("Subscribed " + profileName.getName() + " to " + topic);
                                out.writeBoolean(true);
                                out.flush();
                                synchronized (Broker.consumerNewMessages.get(profileName.getName())) {
                                    synchronized (Broker.histories) {
                                        if (!Broker.histories.get(topic).isEmpty()) {
                                            Broker.consumerNewMessages.get(profileName.getName()).add(topic);
                                        }
                                    }
                                    Broker.consumerNewMessages.get(profileName.getName()).notifyAll();
                                }

                            } else {
                                System.out.println("User was already subscribed to the topic");
                                out.writeBoolean(false);
                                out.flush();
                            }


                            break;
                        case 3:                                  //thelei na diavasei ta teleutaia minimata
                            System.out.println("User wants to get conversation data of a Topic");
                            topic = (String) in.readObject();
                            int offset = in.readInt();

                            ArrayList<MultimediaFile> files = new ArrayList<MultimediaFile>();
                            synchronized (Broker.histories) {
                                files = Broker.histories.get(topic);
                            }
                            if (offset == files.size()) {
                                out.writeBoolean(false);
                                out.flush();
                                System.out.println("Conversation has nothing new yet");
                                break;
                            }
                            out.writeBoolean(true);
                            out.flush();
                            int numberOfChunksThatWillBeSent = 0;
                            for (int i = offset; i < files.size(); i++) {
                                MultimediaFile message = files.get(i);
                                numberOfChunksThatWillBeSent += message.getLengthOfFileChunks(512 * 1024);  //add the number of chunks needed for all files
                            }
                            out.writeInt(numberOfChunksThatWillBeSent);  //inform the user about how many chunks will be needed for all files
                            out.flush();
                            System.out.println("Sending latest messages to user");
                            for (int i = offset; i < files.size(); i++) {
                                MultimediaFile message = new MultimediaFile(files.get(i));
                                message.setMessageId(Integer.toString(i - offset));
                                chunks = message.makeFileIntoChunks();

                                Thread sendMessage = new SendMessage(message, i - offset + 1);
                                threads.add(sendMessage);

                                sendMessage.start();
                            }
                            for (Thread t : threads) {
                                t.join();
                            }

                            out.writeInt(files.size());
                            out.flush();
                            break;

                        //

                        case 4:                                 //thelei na kanei publish
                            System.out.println("User wants to publish a message");
                            topic = (String) in.readObject();    //pare se poio topic thelei na kanei publish
                            chunksReceived = new ArrayList<>();
                            numberOfChunks = in.readInt();
                            for (int i = 1; i <= numberOfChunks; i++) {
                                chunk = (MultimediaFile) in.readObject();
                                chunksReceived.add(chunk);
                                //System.out.println("Got chunk number:" + chunk.getChunkNumber());
                            }
                            System.out.println("Received all chunks");
                            Collections.sort(chunksReceived, Comparator.comparing(MultimediaFile::getChunkNumber));
                            composedFile = MultimediaFile.composeFile(chunksReceived);
                            //apothikeuse to sto istorikoog
                            synchronized (Broker.histories) {
                                Broker.histories.get(topic).add(composedFile);
                                Broker.sortHistory(topic);
                            }

                            synchronized (Broker.participantsperconversation){
                                for(String subscriber : Broker.participantsperconversation.get(topic)){
                                    synchronized (Broker.consumerNewMessages.get(subscriber)) {
                                        Broker.consumerNewMessages.get(subscriber).add(topic);
                                        Broker.consumerNewMessages.get(subscriber).notifyAll();
                                    }
                                }
                            }


                            out.writeBoolean(true);
                            out.flush();
                            break;

                        case 5:
                            System.out.println("User wants to get subscribed users of a topic");
                            topic = (String) in.readObject();
                            profileName = (ProfileName) in.readObject();
                            System.out.println("Sending subscribed users of topic " + topic);
                            synchronized (Broker.participantsperconversation) {
                                Map<String, ArrayList<String>> myParticipants = Broker.getParticipantsPerConversation();
                                out.writeObject(myParticipants.get(topic));
                                out.flush();
                            }
                            break;
                        case 6:
                            System.out.println("User wants to unsubscribe from a topic");
                            topic = (String) in.readObject();
                            profileName = (ProfileName) in.readObject();
                            synchronized (Broker.participantsperconversation) {
                                answer = Broker.unsubscribeFrom(profileName, topic);
                            }
                            out.writeBoolean(answer);
                            out.flush();

                            break;
                        case 7:
                            profileName=(ProfileName) in.readObject();
                            chunksReceived = new ArrayList<>();
                            numberOfChunks = in.readInt();
                            for (int i = 1; i <= numberOfChunks; i++) {
                                chunk = (MultimediaFile) in.readObject();
                                chunksReceived.add(chunk);
                            }
                            System.out.println("Received all chunks");
                            Collections.sort(chunksReceived, Comparator.comparing(MultimediaFile::getChunkNumber));  //kane sort me vasi ton arithmo tou chunk gia na kaneis recompose to arxeio
                            composedFile = MultimediaFile.composeFile(chunksReceived);

                            synchronized (Broker.hashMapWithStories){
                                answer=Broker.uploadStory(profileName.getName(),composedFile);
                            }
                            out.writeBoolean(answer);
                            out.flush();

                            break;

                        case 8:
                            synchronized (Broker.hashMapWithStories) {
                                ArrayList<String> activeUsers = Broker.whoIsCurrentlyAvailable();
                                out.writeObject(activeUsers);
                                out.flush();
                            }

                            String profile=(String) in.readObject();

                            synchronized (Broker.hashMapWithStories) {
                                ArrayList<MultimediaFile> stories = Broker.watchStory(profile);

                                if (stories.isEmpty()) {
                                    out.writeBoolean(false);
                                    out.flush();
                                    break;
                                }

                                out.writeBoolean(true);
                                out.flush();

                                numberOfChunksThatWillBeSent = 0;
                                for (int i = 0; i < stories.size(); i++) {
                                    MultimediaFile message = stories.get(i);
                                    numberOfChunksThatWillBeSent += message.getLengthOfFileChunks(512 * 1024);  //add the number of chunks needed for all files
                                }
                                out.writeInt(numberOfChunksThatWillBeSent);  //inform the user about how many chunks will be needed for all files
                                out.flush();
                                System.out.println("Sending latest messages to user");
                                for (int i = 0; i < stories.size(); i++) {
                                    MultimediaFile message = new MultimediaFile(stories.get(i));
                                    message.setMessageId(Integer.toString(i));                         //kane me vasi auto kalitera group ta filess ston client
                                    chunks = message.makeFileIntoChunks();

                                    System.out.println("Creating thread...");
                                    Thread sendMessage = new SendMessage(message, i + 1);
                                    storyThreads.add(sendMessage);

                                    sendMessage.start();     //steile to minima mesa se thread!!
                                }
                            }
                            for (Thread t : storyThreads) {
                                t.join();
                            }

                            break;

                    }
                    synchronized (out) {
                        out.reset();
                    }
                }
            }else if(typeOfUser.equals("sub")){

                ProfileName user=(ProfileName) in.readObject();
                Broker.registeredUsers.add(user.getName());
                Broker.addNewNameOnStories(user.getName());
                synchronized (Broker.consumerNewMessages) {
                    Broker.consumerNewMessages.put(user.getName(), new ArrayList<String>());
                }
                while (true){
                    //Enimerwse ton user opote exei nea minimata
                    if(Broker.consumerNewMessages.containsKey(user.getName())) {
                        synchronized (Broker.consumerNewMessages.get(user.getName())){
                            while (Broker.consumerNewMessages.get(user.getName()).isEmpty()) {
                                Broker.consumerNewMessages.get(user.getName()).wait();
                            }
                            int i=0;
                            while(i<Broker.consumerNewMessages.get(user.getName()).size()) {
                                String topic =Broker.consumerNewMessages.get(user.getName()).get(i);
                                System.out.println("Notified " + user.getName() + " that "+topic + " has new messages");
                                out.writeObject(topic);
                                out.flush();
                                Broker.consumerNewMessages.get(user.getName()).remove(topic);
                            }
                        }

                    }
                }
            }




        } catch (IOException e) {
            //e.printStackTrace();
        }catch (ClassNotFoundException e){
            //e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                // ioException.printStackTrace();
            }
        }
    }

    class SendMessage extends Thread{
        private MultimediaFile message;
        private int threadNumber;
        public SendMessage(MultimediaFile message,int number){
            this.message=message;
            this.threadNumber=number;
        }
        public void run(){
            try {
                List<MultimediaFile> chunks=message.makeFileIntoChunks();
                int i=0;
                System.out.println("Thread:"+threadNumber+" sending "+ chunks.size()+" chunks");
                for(MultimediaFile chunk:chunks) {
                    synchronized (out) {
                        out.writeObject(chunk);
                        out.flush();
                    }
                    //System.out.println("Thread:"+threadNumber +"sent chunk "+i);
                    i++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}


