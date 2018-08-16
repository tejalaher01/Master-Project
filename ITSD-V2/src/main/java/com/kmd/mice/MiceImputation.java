/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kmd.mice;

import com.mycompany.itsd.v1.GenerateMissingValues;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.apache.commons.io.FilenameUtils;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.repackaged.guava.base.Stopwatch;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Vector;


public class MiceImputation {
    String[] headers = {};
    String headerType = "";
    String filePath = "";
    int num_of_chains = 0, max_itr = 0;
    
    String os_name = "", dir_cot = "";
    List<String[]> csvMatrix = new ArrayList<String[]>();
    CSVWriter writer;
    CSVReader reader;
    Stopwatch timer;
    
    public MiceImputation(){
        
        parseDir();
    }
    
    public void impute(String filePath, String headerType, int num_of_chains, int max_itr){
        
        this.filePath = filePath;
        this.headerType = headerType;
        this.num_of_chains = num_of_chains;
        this.max_itr = max_itr;
        
        
        try{
            if(headerType.equals("Header")){
                reader = new CSVReader(new FileReader(filePath));
                
                headers = captureHeader(reader);
                
                
                reader = new CSVReader(new FileReader(filePath), CSVParser.DEFAULT_SEPARATOR,
                   CSVParser.DEFAULT_QUOTE_CHARACTER, 1);	
            }
            else{
                reader = new CSVReader(new FileReader(filePath));
            }	
            
        }
        catch(IOException e){
            
        }
        
        
        // create a script engine manager:
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        // create a Renjin engine:
        ScriptEngine engine = factory.getScriptEngine();
        
        try{
            engine.put("path", filePath);
            engine.put("headerType", headerType);
            engine.put("num_of_chains", num_of_chains);
            engine.put("max_itr", max_itr);
            
            engine.eval(new java.io.FileReader("Mice_Imputation.R"));
            
            System.out.println("Max iteration "+max_itr);
            System.out.println("Chains "+num_of_chains);
            
            Vector output = (Vector)engine.eval("miceImpute(path, headerType, num_of_chains, max_itr)");
            
            Matrix result = new Matrix(output);
            
            populateCsvMatrix(result);
            
            
        }
        
        catch(IOException e){
            System.out.println("IO Exception");
        }
        catch(ScriptException e){
            System.out.println("Script Exception");
        }
    }
    
    
    private void populateCsvMatrix(Matrix matrix){
        String[] columnVals = new String[matrix.getNumCols()];
        
        for(int i=0;i<matrix.getNumRows();i++){
            for(int j=0;j<matrix.getNumCols();j++){
                columnVals[j] = Double.toString(matrix.getElementAsDouble(i, j));
            }
            csvMatrix.add(columnVals);
            columnVals = new String[matrix.getNumCols()];
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
    
    
    public String saveFile(){
        
        File file = new File(this.filePath);
        String fileName = file.getName();
        fileName = FilenameUtils.removeExtension(fileName);
        String finalPath = "";
        try{
            if(getFileExtension(file).isEmpty()){
                writer = new CSVWriter(new FileWriter(file.getParent()+dir_cot+fileName+"_MICE_Impute"));
                finalPath = file.getParent()+dir_cot+fileName+"_MICE_Impute";
            }
            else{
                writer = new CSVWriter(new FileWriter(file.getParent()+dir_cot+fileName+"_MICE_Impute."+getFileExtension(file)));
                finalPath = file.getParent()+dir_cot+fileName+"_MICE_Impute."+getFileExtension(file);
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
