/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.itsd.v1;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RMSECalculation {
    CSVReader original_reader ;
    List<String[]> original_csvMatrix = new ArrayList<String[]>();
    
    CSVReader imputed_reader ;
    List<String[]> imputed_csvMatrix = new ArrayList<String[]>();
    
    
    StringBuilder csvRow = new StringBuilder();
    int indicator = 0;
    String original_filepath = "",imputed_filepath = "";
    int original_cols = 0, original_rows = 0, imputed_cols = 0, imputed_rows = 0;
    String headerType = "";
    
    double rmse=0.0;
    int n = 0;
    int mismatch = 0;
    
    
    public RMSECalculation(String original_filepath, String imputed_filepath, String headerType) {
        
        this.original_filepath = original_filepath;
        this.imputed_filepath = imputed_filepath;
        
        if(headerType.equals("Header")){
            try{
                original_reader = new CSVReader(new FileReader(original_filepath),CSVParser.DEFAULT_SEPARATOR,
                   CSVParser.DEFAULT_QUOTE_CHARACTER, 1);	
                //Read all rows at once
                original_csvMatrix = original_reader.readAll();

                imputed_reader = new CSVReader(new FileReader(imputed_filepath), CSVParser.DEFAULT_SEPARATOR,
                   CSVParser.DEFAULT_QUOTE_CHARACTER, 1);	
                //Read all rows at once
                imputed_csvMatrix = imputed_reader.readAll();
            }
            catch(IOException e){

            }
        }
        else{
            try{
                original_reader = new CSVReader(new FileReader(original_filepath));	
                //Read all rows at once
                original_csvMatrix = original_reader.readAll();

                imputed_reader = new CSVReader(new FileReader(imputed_filepath));	
                //Read all rows at once
                imputed_csvMatrix = imputed_reader.readAll();
            }
            catch(IOException e){

            }
        }
        
    }
    
    public String calculateRMSE(){
        double sum = 0.0;
        
        original_rows = original_csvMatrix.size(); 
       
        original_cols = original_csvMatrix.get(0).length;
        
        imputed_rows = imputed_csvMatrix.size(); 
       
        imputed_cols = imputed_csvMatrix.get(0).length;
        
        if(imputed_rows == original_rows && imputed_cols == original_cols){
            
            try{
                for(int j=0;j<original_cols;j++){
                    for(int i=0;i<original_rows;i++){
                        if(!(original_csvMatrix.get(i)[j].equals(imputed_csvMatrix.get(i)[j]))){

                            
                            sum = Double.parseDouble(original_csvMatrix.get(i)[j]) - 
                                    Double.parseDouble(imputed_csvMatrix.get(i)[j]);

                            rmse = rmse + (Math.pow(sum, 2));
                            n++;
                            mismatch++;
                        }

                        else if(original_csvMatrix.get(i)[j].equals("")||
                                original_csvMatrix.get(i)[j].equals("")){
                            return "Files with missing values are not allowed for "
                                    + "Evaluation";
                        }
                    }
                }
            }
            catch(NullPointerException e){
                return "Files with missing values are not allowed for "
                                    + "Evaluation";
            }
            
            
            rmse = (rmse/n);
            rmse = Math.sqrt(rmse);

            /*System.out.println(original_filepath);
            System.out.println(imputed_filepath);
            System.out.println("Missing values "+n);
            System.out.println("RMSE "+rmse);*/

            
            return "RMSE Score: "+ Double.toString(rmse);
            
        }
        
        
        
        else{
            
            return "Datasets are not from same domain";
        
        }
        
    }
    
    
    
}
