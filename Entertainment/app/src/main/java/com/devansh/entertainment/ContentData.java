package com.devansh.entertainment;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ContentData {
    private String name, image, link,date;
    private Context context=null;
    private int percent;
    private String tags="",dataBaseName;
    private String combo="";

    public int getProgressPercent()
    {
        return percent;
    }
    public ContentData(String name, String image, String link) {
        this.name=name;
        this.image=image;
        this.link=link;
        this.percent=getPercentage();
    }
    public ContentData(String name, String image, String link, String date) {
        this.name=name;
        this.image=image;
        this.link=link;
        this.date=date;
        this.percent=getPercentage();
    }
    public ContentData(Context context,String name, String image, String link, String date) {
        this.name=name;
        this.image=image;
        this.link=link;
        this.date=date;
        this.context=context;
        this.percent=getPercentage();
    }
    public ContentData(Context context,String name, String image, String link, String date, String tags) {
        this.name=name;
        this.image=image;
        this.link=link;
        this.date=date;
        this.context=context;
        this.percent=getPercentage();
        this.tags=tags;
    }
    public ContentData(Context context,String name, String image, String link, String date, String tags, String databaseName) {
        this.name=name;
        this.image=image;
        this.link=link;
        this.date=date;
        this.context=context;
        try {
            this.percent = getPercentage();
        } catch (Exception exception) {
            exception.printStackTrace();
            percent=0;
        }
        this.tags=tags;
        this.dataBaseName=databaseName;
    }
    public String getDataBaseName()
    {
        return dataBaseName;
    }
    public String getTags(){
        return tags;
    }
    public String getName()
    {
        return name;
    }
    public String getImage()
    {
        return image;
    }
    public String getLink()
    {
        return link;
    }
    public String getDate()
    {
        return date;
    }
    public int getLength()
    {
        if(context==null) return 0;
        int length=0;
        String string;
        File file = new File(context.getFilesDir(),getName().replace(' ','+')+".txt");
        if(getLink().equals("webseries")){
            try{
                FileInputStream fileInputStream = context.openFileInput(getName().replace(' ','+')+".txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
                string=br.readLine();
                string = string.substring(string.indexOf('\t')+1);
                string = string.substring(string.indexOf('\t')+1);
                string = string.substring(string.indexOf('\t')+1);
                length = Integer.parseInt(string);
                return length;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch(Exception ex){
                return 0;
            }
            return 0;
        }
        if(!file.exists()) return 0;
        try {
            FileInputStream fileInputStream = context.openFileInput(getName().replace(' ','+')+".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
            string=br.readLine();
            try{
                if(string.contains("\t")) string = string.substring(string.indexOf('\t')+1);
                else return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            if(string.contains("\t")) string = string.substring(0,string.indexOf('\t'));
            {
                length= Integer.parseInt(string);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }
    public int getPercentage()
    {
        int progressOfContent;
        try {
            if (getLength() > 0){
                progressOfContent = 100 * getProgress() / getLength();
                return progressOfContent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int getProgress()
    {
        if(context==null) return 0;
        int length=0;
        String string;
        File file = new File(context.getFilesDir(),getName().replace(' ','+')+".txt");
        if(getLink().equals("webseries")){
            try{
                FileInputStream fileInputStream = context.openFileInput(getName().replace(' ','+')+".txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
                string=br.readLine();
                string = string.substring(string.indexOf('\t')+1);
                string = string.substring(string.indexOf('\t')+1);
                if(string.contains("\t")) string = string.substring(0,string.indexOf('\t'));
                length = Integer.parseInt(string);
                return length;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch(Exception ex){
                return 0;
            }
            return 0;
        }
        if(!file.exists()) return 0;
        try {
            FileInputStream fileInputStream = context.openFileInput(getName().replace(' ','+')+".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
            string=br.readLine();
            try {
               if (string.contains("\t")) {
                   length = Integer.parseInt(string.substring(0, string.indexOf('\t')));
               }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }
}
