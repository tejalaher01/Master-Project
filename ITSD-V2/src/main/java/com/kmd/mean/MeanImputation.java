/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kmd.mean;


import com.mycompany.itsd.v1.GenerateMissingValues;
import com.mycompany.itsd.v1.ITSD;
import com.opencsv.CSVParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import org.apache.commons.io.FilenameUtils;

public class MeanImputation {
    String os_name = "", dir_cot = "";
    CSVReader reader ;
    List<String[]> csvMatrix = new ArrayList<String[]>();
    ArrayList<Double> avail_values = new ArrayList<Double>();
    StringBuilder csvRow = new StringBuilder();
    int indicator = 0;
    String filePath = "", headerType = "";
    CSVWriter writer;
    String[] headers = {};
    
    
    public void initialize(String filePath, String headerType){
        
        parseDir();
        
        

        int noOfRows = 0;
        int noOfColumns =0;
        int countMissingValues = 0;
        
        this.filePath = filePath;
        this.headerType = headerType;
        
        
        try {
            if(headerType.equals("Header")){
                reader = new CSVReader(new FileReader(filePath));
                
                headers = captureHeader(reader);
                
                
                reader = new CSVReader(new FileReader(filePath), CSVParser.DEFAULT_SEPARATOR,
                   CSVParser.DEFAULT_QUOTE_CHARACTER, 1);	
            }
            else{
                reader = new CSVReader(new FileReader(filePath));
            }

            //Read all rows at once
            csvMatrix = reader.readAll();

           //Number of rows
            noOfRows = csvMatrix.size(); 


            //Number of columns
            noOfColumns = csvMatrix.get(0).length;


            meanImputation(noOfRows,noOfColumns);


           //}
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
                System.out.println("index is out of bounds");

        }

    }
    
    private void parseDir(){
        if(System.getProperty("os.name").startsWith("Linux") || 
                System.getProperty("os.name").startsWith("Mac OS X")){
            dir_cot = "/";
        }
        else if(System.getProperty("os.name").startsWith("Windows")){
            dir_cot = "\\";
        }
        else{
            dir_cot = "/";
        }
    }

    public void meanImputation(int n_rows,int n_cols){
        for(int j=0;j<(n_cols);j++){
            avail_values.clear();
            System.out.println();
            //First loop to collect all available values from a particular attribute
            for(int i=0;i<n_rows;i++){
                
                if(csvMatrix.get(i)[j].equals("")){
                    //do nothing
                }
                else{
                   avail_values.add(Double.parseDouble(csvMatrix.get(i)[j]));
                }
            }
            // Second loop to fill up all the missing values in an attribute
            for(int i=0;i<n_rows;i++){
                if(csvMatrix.get(i)[j].equals("")){ 
                    String result = String.valueOf(calculateMean(avail_values));
                    
                    csvMatrix.get(i)[j] = result;
                    avail_values.add(Double.parseDouble(result));
                    
                    
                    
                }
                else{
                    //do nothing
                }
            }
            
            
            
            
            
        }
        
        
        System.out.println(csvMatrix.get(0).length);
        //saveFile();
        
        

    }
    
    private double calculateMean(ArrayList<Double> vals){
        
        double sum=0.0;
        double avg_res = 0.0;
        double size = vals.size();
        
        for(int k=0;k<vals.size();k++){
            sum = sum + vals.get(k);
        }
        
        avg_res = sum/size;
        System.out.println(sum);
        return avg_res;
    }
    
    
    
    
    
    public String saveFile(){
        
        File file = new File(this.filePath);
        String fileName = file.getName();
        fileName = FilenameUtils.removeExtension(fileName);
        String finalPath = "";
        try{
            if(getFileExtension(file).isEmpty()){
                writer = new CSVWriter(new FileWriter(file.getParent()+dir_cot+fileName+"_Mean_Impute"));
                finalPath = file.getParent()+dir_cot+fileName+"_Mean_Impute";
            }
            else{
                writer = new CSVWriter(new FileWriter(file.getParent()+dir_cot+fileName+"_Mean_Impute."+getFileExtension(file)));
                finalPath = file.getParent()+dir_cot+fileName+"_Mean_Impute."+getFileExtension(file);
            }
            
            if(headerType.equals("Header")){
                writer.writeNext(headers);
            }
            
            writer.writeAll(csvMatrix);
            writer.close();   
        }
        catch(IOException e){
            
        }
        
        return finalPath;
    }
    
    
    
    
    private static String getFileExtension(File file) {
        
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    
    public String[] captureHeader(CSVReader reader){
        String[] header = {};
        try {
            // if the first line is the header
             header = reader.readNext();
            //iterate through the list
        } catch (IOException ex) {
            Logger.getLogger(GenerateMissingValues.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return header;
    }
    
    

   
	
}
