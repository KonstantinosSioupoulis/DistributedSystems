package com.example.ourmessenger;
import java.net.*;
import java.io.*;

public class Consumer extends Thread{
    public ProfileName user;
    //ObjectOutputStream out = null;
    //ObjectInputStream in = null;
    //Socket requestSocket = null;

    public void run() {
        StayNotified notifiedBroker0=new StayNotified(user,"localhost",5000);
        StayNotified notifiedBroker1=new StayNotified(user,"localhost",5001);
        StayNotified notifiedBroker2=new StayNotified(user,"localhost",5002);
        notifiedBroker0.start();
        notifiedBroker1.start();
        notifiedBroker2.start();
    }

    public Consumer(ProfileName user){
        this.user=user;
    }


    class StayNotified extends Thread{
        private ProfileName user;
        private String Ip;
        private int port;
        public StayNotified(ProfileName user,String Ip,int port){
            this.user=user;
            this.Ip=Ip;
            this.port=port;
        }

        public void run(){
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            Socket requestSocket = null;
            try {
                 requestSocket=new Socket(Ip,port);
                 out=new ObjectOutputStream(requestSocket.getOutputStream());
                 in =new ObjectInputStream(requestSocket.getInputStream());
                out.writeObject("sub");
                //out.flush();
                out.writeObject(user);
                //out.flush();

                //perimene na mathaineis se poia topic erxontai kainourgia minimata
                while(true){
                    String topicWithNewMessage=(String)in.readObject();
                    synchronized (Client.topicsWithNewMessages){
                        Client.topicsWithNewMessages.add(topicWithNewMessage);
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
