package com.example.ourmessenger;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class ProfileName implements Serializable {
     private static final long serialversionUID = -1L;
     private String profileName;
     private HashMap<String,ArrayList<MultimediaFile>> userVideoFilesMap =new HashMap<String, ArrayList<MultimediaFile>>();
     public HashMap<String,Integer> subscribedConversations =new HashMap<String, Integer>();
     public ProfileName(String profileName){
          this.profileName = profileName;
     }


     public ProfileName(){}

     public void updateUserVideoFilesMap(String topic,MultimediaFile file){
          if(userVideoFilesMap.containsKey(topic)){
               userVideoFilesMap.get(topic).add(file);
          }
          else{
               ArrayList<MultimediaFile> files= new ArrayList<MultimediaFile>();
               files.add(file);
               userVideoFilesMap.put(topic,files);
          }
     }

     public void updateSubscribedConversations(String topic,Integer offset){
          subscribedConversations.put(topic,offset);
     }

     public void unsubscribeFromConversation(String topic){
          subscribedConversations.remove(topic);
     }

     public boolean isSubscribedToConversation(String topic){
          if(subscribedConversations!=null && subscribedConversations.containsKey(topic)){
               return true;
          }
          return false;
     }

     public String getName(){
          return profileName;
     }
     public HashMap<String,Integer> getSubscribedConversations(){
          return subscribedConversations;
     }


}

