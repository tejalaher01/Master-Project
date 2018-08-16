/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.itsd.v1;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class PlotGeneration{

    CSVReader original_reader ;
    List<String[]> original_csvMatrix = new ArrayList<String[]>();
    
    CSVReader imputed_reader ;
    List<String[]> imputed_csvMatrix = new ArrayList<String[]>();
    
    String original_filepath = "", imputed_filepath = "";
    
    int original_cols = 0, original_rows = 0, imputed_cols = 0, imputed_rows = 0;
    
    ArrayList<String> originalValues = new ArrayList<String>();
    
    ArrayList<String> imputedValues = new ArrayList<String>();
    
    public void initialize(String original_filepath, String imputed_filepath){
        
        this.original_filepath = original_filepath;
        this.imputed_filepath = imputed_filepath;
        
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
        
        //Data collection
        
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

                            originalValues.add(original_csvMatrix.get(i)[j]);
                            
                            imputedValues.add(imputed_csvMatrix.get(i)[j]);
                        }

                        else if(original_csvMatrix.get(i)[j].equals("")||
                                original_csvMatrix.get(i)[j].equals("")){
                            
                        }
                    }
                }
            }
            catch(NullPointerException e){
                
            }
            
            
            
            
        }
    }
    
    public ArrayList<String> getOriginalData(){
        return originalValues;
    }
    
    public ArrayList<String> getImputedData(){
        return imputedValues;
    }
    
}
