package com.example.ourmessenger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Date;
import java.util.function.Supplier;



public class MultimediaFile implements Serializable {
    private static final long serialversionUID = -1L;
    private String multimediaFileName;
    private String profileName;
    private String dateCreated;
    private String length;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private byte[] multimediaFileChunk;          //ta bytes tou sigkekrimenou chunk
    private String path;
    private int ChunkNumber;                    //o arithmos tou chunk
    private String fileType;                    //o typos tou arxeiou px mov,mp4,png
    private int lengthInByteChunks;             //se posa chunk xwrizetai to arxeioo
    private int lengthOfWholeFileInNumberOfBytes;           //posa bytes exei olo to arxeio
    static int chunkSize = 512*1024;                           //apo posa bytes tha apoteleitai to kathe chunk
    private Date dateUploaded;                              //pote anevike to arxeio
    private String messageId;
    private Boolean hasExtra=false;
    private int extra;

    public String getProfileName() {
        return profileName;
    }

    public void setMultimediaFileName(String multimediaFileName) {
        this.multimediaFileName = multimediaFileName;
    }
    public String getMultimediaFileName(){
        return this.multimediaFileName;
    }

    public void setMultimediaFileChunk(byte[] chunk){
        this.multimediaFileChunk=chunk;
    }

    public byte[] getMultimediaFileChunk() {
        return multimediaFileChunk;
    }

    public int getLengthInByteChunks(){
        return lengthInByteChunks;
    }

    public void setLengthInByteChunks(int chunks){
        this.lengthInByteChunks=chunks;
    }

    public void setChunkNumber(int chunkNumber) {
        ChunkNumber = chunkNumber;
    }
    public int getChunkNumber(){
        return this.ChunkNumber;
    }

    public int getLengthOfFileChunks(int sizeOfChunk){
        return (int) Math.ceil((double)this.lengthOfWholeFileInNumberOfBytes / sizeOfChunk);
    }

    public void setMessageId(String id){
        this.messageId=id;
    }

    public String getMessageId() {
        return messageId;
    }

    public Date getDateUploaded() {
        return dateUploaded;
    }

    public String getPath(){
        return path;
    }

    public Boolean getHasExtra(){
        return hasExtra;
    }

    public int getExtra() {
        return extra;
    }

    public String getFileType() {
        return fileType;
    }

    public MultimediaFile(String multimediaFileName, String profileName, String dateCreated, String length, String framerate, String frameWidth, String frameHeight, String path, Date upload) {

        this.multimediaFileName = multimediaFileName;
        this.profileName = profileName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.framerate = framerate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.path=path;
        this.dateUploaded=upload;
        String extension = "";
        String temp=new File(path).getName();
        int i = temp.lastIndexOf('.');
        if (i >= 0) { extension = temp.substring(i+1); }
        this.fileType=extension;

        try {
            File file=new File(path);
            byte fileData[] = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(fileData);
            //byte[] fileData = Files.readAllBytes(path.toPath());
            this.lengthOfWholeFileInNumberOfBytes=fileData.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.FileToByte();
    }


    //copy constructor
    public MultimediaFile(MultimediaFile mFile){
        this.multimediaFileName = mFile.multimediaFileName;
        this.profileName = mFile.profileName;
        this.dateCreated = mFile.dateCreated;
        this.length = mFile.length;
        this.framerate = mFile.framerate;
        this.frameWidth = mFile.frameWidth;
        this.frameHeight = mFile.frameHeight;
        this.path=mFile.path;
        this.multimediaFileChunk=mFile.multimediaFileChunk;
        this.lengthInByteChunks=mFile.lengthInByteChunks;
        this.lengthOfWholeFileInNumberOfBytes=mFile.lengthOfWholeFileInNumberOfBytes;
        this.fileType=mFile.fileType;
        this.dateUploaded=mFile.dateUploaded;
        this.messageId=mFile.messageId;
    }

    public void FileToByte() {          //diavasma tou arxeio kai metatropi se bytes
        try {
            File file=new File(path);
            byte fileData[] = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(fileData);
            this.lengthOfWholeFileInNumberOfBytes=fileData.length;
            this.multimediaFileChunk=fileData;

        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        } catch (IOException e) {
            System.err.println("IOException");
        }
    }

    public List<byte[]> chunkifyFile() {
        byte[] data=this.multimediaFileChunk;
        List<byte[]> result = new ArrayList<>();
        int chunks = data.length / chunkSize;

        for (int i = 0; i < chunks; i++) {
            byte[] current = new byte[chunkSize];
            for (int j = 0; j < chunkSize; j++) {
                current[j] = data[j + i * chunkSize];
            }
            result.add(current);
        }
        //Add last chunk
        boolean isRemaining = data.length % chunkSize != 0;
        if (isRemaining){
            int remaining = data.length % chunkSize;
            int offset = chunks * chunkSize;

            byte[] current = new byte[remaining];
            for(int i = 0; i < remaining; ++i){
                current[i] = data[offset + i];
            }

            result.add(current);
        }

        return result;

    }
    public List<MultimediaFile> makeFileIntoChunks(){       //xwrise to minima se polla multimediaFile chunks
        List<MultimediaFile> chunks= new ArrayList<MultimediaFile>();
        //this.FileToByte();
        List<byte[]> chunkedBytes=this.chunkifyFile();
        MultimediaFile chunkFile;
        int numberOfChunk=1;
        for (byte[] chunk:chunkedBytes){
            chunkFile=new MultimediaFile(this);
            chunkFile.setChunkNumber(numberOfChunk);
            chunkFile.setMultimediaFileChunk(chunk);
            chunks.add(chunkFile);
            numberOfChunk++;

        }
        return chunks;
    }

    public static MultimediaFile composeFile(List<MultimediaFile> chunks) {         //re-compose ta chunks se ena multimedia file
        MultimediaFile composedFile= new MultimediaFile(chunks.get(0));
        byte[] composed = new byte[composedFile.lengthOfWholeFileInNumberOfBytes];
        int i=0;
        for(MultimediaFile chunk:chunks) {
            for (byte b : chunk.multimediaFileChunk) {
                composed[i] = b;
                i++;
            }
        }
        composedFile.setMultimediaFileChunk(composed);
        return composedFile;
    }

    public void DownloadFile(String filepath){
        this.hasExtra=false;
        filepath+=this.multimediaFileName;
        boolean fileExists=false;
        extra=1;
        File file;
        try {
            if(new File(filepath+"."+this.fileType).isFile()){ //se periptosi pou iparxei idi arxeio me to idio onoma
                fileExists=true;
                while(new File(filepath+" ("+extra+")"+"."+this.fileType).isFile()){
                    extra++;
                }

            }
            Path path;
            if(!fileExists) {
                file= new File(filepath+"."+this.fileType);
                //path = Paths.get(filepath+"."+this.fileType);
            }
            else{
                file=new File(filepath+" ("+extra+")"+"."+this.fileType);
                //path =Paths.get(filepath+" ("+extra+")"+"."+this.fileType);
                hasExtra=true;
            }
            //Files.write(path, this.multimediaFileChunk);
            FileOutputStream stream=new FileOutputStream(file.getPath());
            stream.write(this.multimediaFileChunk);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }

    }



}
