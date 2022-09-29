package com.example.ourmessenger;
//package ourMessenger;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

import static java.lang.Thread.sleep;


public class Broker{

    static String port0 = "5000";
    static String port1 = "5001";
    static String port2 = "5002";

    static String ip0 = "192.168.1.7";
    static String ip1 = "192.168.1.7";
    static String ip2 = "192.168.1.7";

    private static ArrayList<Pair<String,String>> hardcodedBrokerInfo=new ArrayList<Pair<String,String>>();


    public static HashSet<String> registeredUsers=new HashSet<String>();

    public static ArrayList<String> conversationnames = new ArrayList<String>(){{add("katanemimena");add("vaseis");add("Asfaleia");add("Pithanotites");
        add("memes");add("erasmus");add("JAVA");}}; //onomata sunomiliwn
    public static HashMap<String,ArrayList<String>> participantsperconversation = new HashMap<String, ArrayList<String>>(); //eggegrammenoi se kathe sunomilia

    public static HashMap<String,ArrayList<MultimediaFile>> histories = new HashMap<String,ArrayList<MultimediaFile>>();  //to istoriko twn mhmumatwn gia kathes synomilia

    public static HashMap<String,ArrayList<String>> consumerNewMessages = new HashMap<String,ArrayList<String>>();


    public static void initHistories(){
        for(String conv:conversationnames){
            histories.put(conv,new ArrayList<MultimediaFile>());
        }
    }

    public static synchronized void sortHistory(String topic){
        ArrayList<MultimediaFile> history= histories.get(topic);
        Collections.sort(history, Comparator.comparing(MultimediaFile::getDateUploaded));
    }

    public synchronized static Map<String,ArrayList<String>> getParticipantsPerConversation(){
        return participantsperconversation;
    }

    public static int hashIPplusPort(String ip, String port) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        byte[] messageDigest1 = md.digest(ip.getBytes());

        byte[] messageDigest2= md.digest(port.getBytes());

        BigInteger bigInt1 = new BigInteger(1,messageDigest1);

        BigInteger bigInt2 = new BigInteger(1,messageDigest2);

        BigInteger temp = bigInt1.add(bigInt2);

        BigInteger threehundred = new BigInteger("300");

        int responsible_broker = temp.mod(threehundred).intValue();

        //metatrepw ton 3 se BigInt gia na kanw to modulo
        return temp.mod(threehundred).intValue();
        //to modulo pou ginetai epistrefei BigInteger, ara kanw .intValue() gia na to kanw Integer.
    }//hasharei to ip+port tou kathe broker. kaleitai treis fores



    static ArrayList<String> maxbrokerconvs = new ArrayList<String>();
    static ArrayList<String> middlebrokerconvs = new ArrayList<String>();
    static ArrayList<String> minbrokerconvs = new ArrayList<String>();

    static HashMap<String,ArrayList<Pair<MultimediaFile,Integer>>> hashMapWithStories = new HashMap<String,ArrayList<Pair<MultimediaFile,Integer>>>();
    //to hashmap me ta stories
    //sto prwto orisma exei to onoma autou pou anevazei
    //to deutero orisma exei ena arraylist apo pairs. sthn prwth thesh pairnei multimediafile, sth deuterh integer to time to live



    //h hashSynomilia epistrefei enan integer. o integer prokuptei apo to hashing ths sunomilias kai einai arithmos apo to 0 ews to 2
    //an vgalei 0, tote th sunomilia thn analamvanei o min broker, dhladh o broker tou opoiou to hashing(ip+port) einai to mikrotero
    //an vgalei 1, tote o middle broker
    //an vgalei 2, tote o maximum broker
    public static int hashSynomilia(String synom){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] messageDigest = md.digest(synom.getBytes());
        BigInteger bigInt = new BigInteger(1,messageDigest);
        BigInteger three = new BigInteger("3");
        int responsible_broker = bigInt.mod(three).intValue();
        return responsible_broker;
    }

    //vriskw ton armodio broker gia sugkekrimeno topic. epistrefw arraylist
    //sth thesh 0 tha einai h ip tou swstou, sth thesh 1 to port tou swstou
    public static ArrayList<String> findResponsibleBroker(String synom){
        ArrayList<String> temp = new ArrayList<String>();
        int num = hashSynomilia(synom);
        if(num==0){
            temp.add(minip);
            temp.add(minport);
        }else if(num==2){
            temp.add(maxip);
            temp.add(maxport);
        }else{
            temp.add(middleip);
            temp.add(middleport);
        }
        return temp;
    }//sth thesh 0 h ip, sth thesh 1 to port


    static String maxx="broker2";
    static String minn="broker2";
    static String maxip = ip2;
    static String minip = ip2;
    static String maxport = port2;
    static String minport = port2;

    static String middleip;
    static String middleport;

    static boolean amMax, amMin, amMiddle = false;


    //h middleOfThree vriskei ton middle metaksu triwn arithmwn
    public static int middleOfThree(int a, int b, int c){
        if ((a < b && b < c) || (c < b && b < a))
            return b;
        else if ((b < a && a < c) || (c < a && a < b))
            return a;
        else
            return c;
    }


    public static HashMap<String, ArrayList<String>> fillConversations(){
        for(String synom : conversationnames){
            participantsperconversation.put(synom,new ArrayList<String>());
        }

        return participantsperconversation;
    }

    //h subscribeAt kanei tis eggrafes atomwn stis synomilies
    public synchronized static boolean subscribeTo(ProfileName profileName, String synom){
        if (!participantsperconversation.get(synom).contains(profileName.getName())) {
            participantsperconversation.get(synom).add(profileName.getName());
            return true;
        }
        return false;

    } //ara gia thn eggrafh tha prepei na stelnetai ProfileName kai to onoma ths sunomilias


    public synchronized static boolean unsubscribeFrom(ProfileName profileName, String synom){
        if(participantsperconversation.get(synom).contains(profileName.getName())){
            participantsperconversation.get(synom).remove(profileName.getName());
            return true;
        }else{
            System.out.println("User "+profileName.getName()+" is not yet subscribed at "+synom);
            return false;
        }
    }//apeggrafh kapoiou apo kapoia synomilia

    public static HashMap<String,ArrayList<String>> responsibleBrokers;
    //to hashmap pou tha parei o user
    // tha exei prwto to onoma ths sunomilias kai deutero thn ip k to port tou broker
    //me to onoma tou pinaka .get(0) pairnoume thn ip, kai me to .get(1) to port
    public static HashMap<String,ArrayList<String>> fillTheHashMap(){
        responsibleBrokers = new HashMap<String, ArrayList<String>>();
        for (String s: conversationnames) {
            ArrayList<String> temp = new ArrayList<String>();
            temp = findResponsibleBroker(s);
            //System.out.println("For conversation "+s+", responsible broker has IP: "+temp.get(0)+" with port: "+temp.get(1));
            responsibleBrokers.put(s,temp);
        }
        //System.out.println(responsibleBrokers);
        return responsibleBrokers;
    }

    //h responsibilitiesToPrint tupwnei gia poies sunomilies einai upeuthinos kathe broker
    public static void responsibilitiesToPrint(String ip, String port){
        ArrayList<String> temp = new ArrayList<String>();
        if(amMax){
            System.out.println("I have the maximum hashing, so I am max broker.");
            System.out.print("My IP and port numbers are: "+ip+" "+port+" and I am responsible for: ");
            for(String s : maxbrokerconvs){
                System.out.print(" "+s);
            }
            System.out.print("\nMiddle broker with IP and port numbers: "+middleip+" "+middleport+"  is responsible for: ");
            for(String s : conversationnames){
                if(hashSynomilia(s)==1){
                    System.out.print(" "+s);
                }
            }
            System.out.print("\nMin broker with IP and port numbers: "+minip+" "+minport+" is responsible for: ");
            for(String s : conversationnames){
                if(hashSynomilia(s)==0){
                    System.out.print(" "+s);
                }
            }

        }else if(amMiddle){
            System.out.println("I have the median hashing, so I am middle broker.");
            System.out.print("My IP and port numbers are: "+ip+" "+port+" and I am responsible for: ");
            for(String s : middlebrokerconvs){
                System.out.print(" "+s);
            }
            System.out.print("\nMax broker with IP and port numbers: "+maxip+" "+maxport+" is responsible for: ");
            for(String s : conversationnames){
                if(hashSynomilia(s)==2){
                    System.out.print(" "+s);
                }
            }
            System.out.print("\nMin broker with IP and port numbers: "+minip+" "+minport+" is responsible for: ");
            for(String s : conversationnames){
                if(hashSynomilia(s)==0){
                    System.out.print(" "+s);
                }
            }
        }else{
            System.out.println("I have the minimum hashing, so I am min broker.");
            System.out.print("My IP and port numbers are: "+ip+" "+port+" and I am responsible for: ");
            for(String s : minbrokerconvs){
                System.out.print(" "+s);
            }
            System.out.print("\nMax broker with IP and port numbers: "+maxip+" "+maxport+" is responsible for: ");
            for(String s : conversationnames){
                if(hashSynomilia(s)==2){
                    System.out.print(" "+s);
                }
            }
            System.out.print("\nMiddle broker with IP and port numbers: "+middleip+" "+middleport+" is responsible for: ");
            for(String s : conversationnames){
                if(hashSynomilia(s)==1){
                    System.out.print(" "+s);
                }
            }
        }

    }

    static int broker0hashing;
    static int broker1hashing;
    static int broker2hashing;


    public static void findOrder(){
        int max = broker2hashing;

        if (broker0hashing > max || broker1hashing > max) {
            if (broker0hashing > broker1hashing) {
                max = broker0hashing;
                maxx = "broker0";
                maxip = ip0;
                maxport = port0;
            } else {
                max = broker1hashing;
                maxx = "broker1";
                maxip = ip1;
                maxport = port1;
            }
        }

        int min = broker2hashing;

        if (broker0hashing < min || broker1hashing < min) {
            if (broker0hashing < broker1hashing) {
                min = broker0hashing;
                minn = "broker0";
                minip = ip0;
                minport = port0;
            } else {
                min = broker1hashing;
                minn = "broker1";
                minip = ip1;
                minport = port1;
            }
        }

        int middle = middleOfThree(broker0hashing,broker1hashing,broker2hashing);

        if(middle == broker0hashing){
            middleip = ip0;
            middleport = port0;
        }else if(middle == broker1hashing){
            middleip = ip1;
            middleport = port1;
        }else{
            middleip = ip2;
            middleport = port2;
        }


    }//h findOrder vriskei th diataksh twn triwn brokers, mikroteros mesaios megaluteros

    public static void printWhoAmI(String ip, String port){
        if (ip.equals(maxip) && port.equals(maxport)){
            System.out.println("My IP+PORT have the biggest hashing, so I am responsible for 201-300 keys, or topics that have 2 as hashing result.");
            amMax = true;
        }else if(ip.equals(minip) && port.equals(minport)){
            System.out.println("My IP+PORT have the smallest hashing, so I am responsible for 0-100 keys, or topics that have 0 as hashing result.");
            amMin = true;
        }else{
            System.out.println("My IP+PORT have the median hashing, so I am responsible for 101-200 keys, or topics that have 1 as hashing result.");
            amMiddle = true;
        }
    }

    //ektupwnei gia kathe sunomilia to hashing ths
    public static void fillOnlyMyListAndHashTopics(){
        for (String conv : conversationnames) {
            int hashing = hashSynomilia(conv);
            System.out.println("conversation's name "+conv+", hashes in: "+hashing);
            if (hashing == 2 && amMax){
                maxbrokerconvs.add(conv);
            }
            if (hashing == 1 && amMiddle){
                middlebrokerconvs.add(conv);
            }
            if (hashing == 0 && amMin){
                minbrokerconvs.add(conv);
            }
        }
    }

    //autokaloumenh sunarthsh, se kathe guro meiwnei kata 1 to timeToLive tou kathe story
    public static void updateHashMapWithStories() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!hashMapWithStories.isEmpty()) {
                    for (Map.Entry<String, ArrayList<Pair<MultimediaFile,Integer>>> entry : hashMapWithStories.entrySet()) {
                        String key = entry.getKey();
                        ArrayList<Pair<MultimediaFile,Integer>> value = entry.getValue();
                        ListIterator<Pair<MultimediaFile,Integer>> it = hashMapWithStories.get(key).listIterator();
                        if(!value.isEmpty()) {
                            while (it.hasNext()) {
                                Pair pair = it.next();
                                //System.out.println("key : " + key + " value : " + pair.getElement0() + " " + pair.getElement1());
                                int old = (int) pair.getElement1();
                                int neww = old - 1;
                                MultimediaFile loc = (MultimediaFile) pair.getElement0();
                                int index = hashMapWithStories.get(key).indexOf(pair);
                                if (pair.getElement1().equals(0)) {
                                    System.out.println("expired story!");
                                    System.out.println("just removed oldest story from "+key+"'s stories. ");
                                    it.remove();
                                } else {
                                    if(pair.getElement1().equals(60)) {
                                        System.out.println(key+"'s '"+((MultimediaFile) pair.getElement0()).getMultimediaFileName()+"' story expires in another 60 seconds");
                                    }
                                    hashMapWithStories.get(key).set(index, new Pair<MultimediaFile, Integer>(loc, neww));
                                }
                            }
                        }
                    }

                }
            }
        }, 0, 1000);


    }

    public static void addNewNameOnStories(String name){
        hashMapWithStories.put(name,new ArrayList<Pair<MultimediaFile,Integer>>());
    }

    public synchronized static boolean uploadStory(String name,MultimediaFile story){
        hashMapWithStories.get(name).add(new Pair<MultimediaFile,Integer>(story,120));
        return true;
    }

    public static ArrayList<String> whoIsCurrentlyAvailable() {
        ArrayList<String> temp = new ArrayList<String>();
        for (Map.Entry<String, ArrayList<Pair<MultimediaFile, Integer>>> entry : hashMapWithStories.entrySet()) {
            String key = entry.getKey();
            ListIterator<Pair<MultimediaFile, Integer>> it = hashMapWithStories.get(key).listIterator();
            if (it.hasNext()) {
                temp.add(key);
            }
        }
        return temp;
    }


    public synchronized static ArrayList<MultimediaFile> watchStory(String name){
        ArrayList<MultimediaFile> userStories=new ArrayList<MultimediaFile>();
        System.out.println(hashMapWithStories.get(name).size());
        for(int j = 0; j<hashMapWithStories.get(name).size();j++){
            userStories.add(hashMapWithStories.get(name).get(j).getElement0());
        }
        return userStories;


    }






    public static void main(String[] args) throws NoSuchAlgorithmException{
        hardcodedBrokerInfo.add(new Pair<String,String> (ip0,port0)); hardcodedBrokerInfo.add(new Pair<String,String> (ip1,port1)); hardcodedBrokerInfo.add(new Pair<String,String> (ip2,port2));

        updateHashMapWithStories();
        fillConversations();

        Scanner sc= new Scanner(System.in);
        System.out.print("Enter your Ip:");
        String ip=sc.next();

        System.out.print("Enter your Port:");
        String port=sc.next();


        broker0hashing = hashIPplusPort("localhost",port0);
        System.out.println("Hash(IP+Port) of broker0 results in: "+broker0hashing);

        broker1hashing = hashIPplusPort("localhost",port1);
        System.out.println("Hash(IP+Port) of broker1 results in: "+broker1hashing);

        broker2hashing = hashIPplusPort("localhost", port2);
        System.out.println("Hash(IP+Port) of broker2 results in: "+broker2hashing);

        findOrder();

        System.out.println("What keys am I responsible for, according to my IP+PORT? [calculating..]");

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        printWhoAmI(ip,port);
        System.out.println("------------------------------------------------------------");

        System.out.println("What's the hashing of each conversation? [calculating..]");

        try {
            sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        fillOnlyMyListAndHashTopics();



        fillTheHashMap();
        System.out.println("------------------------------------------------------------");


        System.out.println("Infromation about other brokers and their responsibilities? [calculating..]");
        try {
            sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        responsibilitiesToPrint(ip,port);
        System.out.print("\n");


        initHistories();
        System.out.println("------------------------------------------------------------");
        System.out.println("Opening server and waiting for connections.. [pending]");

        new Server().openServer(Integer.parseInt(port));

    }//end main



} //end CLASS

class Pair<K, V> {

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

}

class Server {

    /* Define the socket that receives requests */
    ServerSocket providerSocket;

    /* Define the socket that is used to handle the connection */
    Socket connection = null;


    void openServer(int port) {
        try {

            /* Create Server Socket */
            providerSocket = new ServerSocket(port, 10);
            //synchronized (providerSocket)

            while (true) {
                /* Accept the connection */
                connection = providerSocket.accept();

                /* Handle the request */
                Thread t = new ActionsForClients(connection);
                t.start();

            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}

