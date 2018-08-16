/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.itsd.v1;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComputeRMSE {
    CSVReader original_reader ;
    List<String[]> original_csvMatrix = new ArrayList<String[]>();
    
    CSVReader imputed_reader ;
    List<String[]> imputed_csvMatrix = new ArrayList<String[]>();
    
    CSVReader missing_reader ;
    List<String[]> missing_csvMatrix = new ArrayList<String[]>();
    
    StringBuilder csvRow = new StringBuilder();
    int indicator = 0;
    String original_filepath = "",imputed_filepath = "", missing_filepath;
    int original_cols = 0, original_rows = 0, imputed_cols = 0, imputed_rows = 0, missing_rows = 0, missing_cols = 0;
    String headerType = "";
    
    double rmse=0.0;
    int n = 0;
    int mismatch = 0;
    ArrayList<Integer> missing_row_index = new ArrayList<>();
    ArrayList<Integer> missing_col_index = new ArrayList<>();
    
    public ComputeRMSE(String original_filepath, String imputed_filepath, String missing_filepath, String headerType) {
        
        this.original_filepath = original_filepath;
        this.imputed_filepath = imputed_filepath;
        this.missing_filepath = missing_filepath;
        
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
                
                missing_reader = new CSVReader(new FileReader(missing_filepath), CSVParser.DEFAULT_SEPARATOR,
                   CSVParser.DEFAULT_QUOTE_CHARACTER, 1);	
                //Read all rows at once
                missing_csvMatrix = missing_reader.readAll();
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
                
                
                missing_reader = new CSVReader(new FileReader(missing_filepath));	
                //Read all rows at once
                missing_csvMatrix = missing_reader.readAll();
            }
            catch(IOException e){

            }
        }
        
    }
    
    public String calculateRMSE(){
        double sum = 0.0;
        int n=0;
        original_rows = original_csvMatrix.size(); 
       
        original_cols = original_csvMatrix.get(0).length;
        
        imputed_rows = imputed_csvMatrix.size(); 
       
        imputed_cols = imputed_csvMatrix.get(0).length;
        
        missing_rows = missing_csvMatrix.size(); 
       
        missing_cols = missing_csvMatrix.get(0).length; 
        
        System.out.println(missing_filepath);
        
        if(imputed_rows == original_rows && imputed_cols == original_cols && missing_rows == original_rows && missing_cols == original_cols){
            n = 0;
            for(int i=0;i<missing_rows;i++){
                    for(int j=0;j<missing_cols;j++){
                        if(missing_csvMatrix.get(i)[j].equals("")){
                            missing_row_index.add(i);
                            missing_col_index.add(j);
                        }
                    }
            }
            
            for(int i=0;i<missing_row_index.size();i++){
                sum = Double.parseDouble(original_csvMatrix.get(missing_row_index.get(i))[missing_col_index.get(i)]) - 
                                    Double.parseDouble(imputed_csvMatrix.get(missing_row_index.get(i))[missing_col_index.get(i)]);

                rmse = rmse + (Math.pow(sum, 2));
                n++;
                mismatch++;
            }
            
            
            
            if(mismatch != 0){
                rmse = (rmse/n);
                rmse = Math.sqrt(rmse);
                
                System.out.println(original_filepath);
                System.out.println(imputed_filepath);
                System.out.println("Missing values "+n);
                System.out.println("RMSE "+rmse);
            }
            else{
                rmse = 0.0;
            }
            
            return "RMSE Score: "+ Double.toString(rmse);
            
        }
        
        
        
        else{
            
            return "Datasets are not from same domain";
        
        }
        
    }
    
    
    
}

