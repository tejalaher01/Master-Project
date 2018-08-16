/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.itsd.v1;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;


public class GenerateMissingValues {
    String os_name = "", dir_cot = "";
    String filePath = "";
    int n_delete_data = 0; 
    double NumberOfDeletion = 0;
    CSVReader reader;
    String[] row;
    int countRow = 0, countColumn = 0, totalValues = 0;
    List<String[]> csvMatrix;
    String headerStatus = "";
    StringBuilder csvRow = new StringBuilder();
    Random rand;
    CSVWriter writer;
    String headers[] = {};
    
    public double getFilePathRatio(String filePath, int n_delete_data, String headerStatus){
        parseDir();
        
        this.filePath = filePath;
        this.n_delete_data = n_delete_data;
        this.headerStatus = headerStatus;
        rand = new Random();
        
        return processFile();
    }
    
    private double processFile(){
        try {
            
            
            if(headerStatus.equals("Header")){
                reader = new CSVReader(new FileReader(filePath));
                
                headers = captureHeader(reader);
                
                
                reader = new CSVReader(new FileReader(filePath), CSVParser.DEFAULT_SEPARATOR,
                   CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
            }
            else{
                reader = new CSVReader(new FileReader(filePath));
            }
            
            
            csvMatrix = reader.readAll();
            
            countRow = csvMatrix.size(); //Number of rows
            countColumn = csvMatrix.get(0).length; //Number of columns
            
            totalValues = (countRow * countColumn); //Total values
            
            System.out.println("Total Values "+totalValues);
            
            NumberOfDeletion = Math.floor(totalValues*((double)n_delete_data/100)); //Values to be deleted
            
            System.out.println("Total Deletion "+NumberOfDeletion);
            
            
            
            //Random index values are being replaced by empty string
            for(int i=0;i<NumberOfDeletion;i++){ 
                int rowRand = rand.nextInt(countRow);
                int columnRand = 1 + rand.nextInt(countColumn-1); //Starting from 1 as Column index 0 is target label
                
                
                csvMatrix.get(rowRand)[columnRand] = "";
                
                
            }
            
            
            
            
            
            
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
        return NumberOfDeletion;
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
    
    public String saveFile(){
        
        File file = new File(this.filePath);
        String fileName = file.getName();
        fileName = FilenameUtils.removeExtension(fileName);
        String finalPath = "";
        try{
            if(getFileExtension(file).isEmpty()){
                writer = new CSVWriter(new FileWriter(file.getParent()+"/"+fileName+"_Missing"));
                finalPath = file.getParent()+"/"+fileName+"_Missing";
            }
            else{
                writer = new CSVWriter(new FileWriter(file.getParent()+"/"+fileName+"_Missing."+getFileExtension(file)));
                finalPath = file.getParent()+"/"+fileName+"_Missing."+getFileExtension(file);
            }
            
            if(headerStatus.equals("Header")){
                writer.writeNext(headers);
            }
            
            writer.writeAll(csvMatrix);
            writer.close();

            
            reader = new CSVReader(new FileReader(finalPath));
            csvMatrix = reader.readAll();

            countRow = csvMatrix.size(); //Number of rows
            countColumn = csvMatrix.get(0).length; //Number of columns

            
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
