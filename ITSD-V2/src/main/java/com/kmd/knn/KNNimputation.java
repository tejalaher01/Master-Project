package com.kmd.knn;
import com.mycompany.itsd.v1.DTW;
import com.mycompany.itsd.v1.GenerateMissingValues;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang3.ArrayUtils;


public class KNNimputation {
    String os_name = "", dir_cot = "";
    CSVReader reader ;
    CSVWriter writer;
    String filePath = "";
    List<String[]> csvMatrix = new ArrayList<String[]>();
    
    String[] headers = {};
    String headerType = "";
    
    StringBuilder csvRow = new StringBuilder();
    int indicator = 0;
    
    int n_rows = 0,n_cols = 0;
    
    ArrayList<Integer> missing_row_indexes = new ArrayList<Integer>();
    ArrayList<Integer> missing_col_indexes = new ArrayList<Integer>();
    
    int k_neighbors = 0;
    
    ArrayList<Double> warp_distances = new ArrayList<Double>();
    ArrayList<Integer> instances = new ArrayList<Integer>();
    ArrayList<Integer> neighbor_instances = new ArrayList<Integer>();
    
    Double[] arr_instance;
    Double[] arr_instance_missing;

    public void initialize(String filePath, String headerType,int k_neighbors){
        parseDir();
        try{
            this.filePath = filePath;
            this.headerType = headerType;
            this.k_neighbors = k_neighbors;
            
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
            
            n_rows = csvMatrix.size();
            n_cols = csvMatrix.get(0).length;
            
            knnImputation();
        }
        catch(IOException e){
            
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
    
    public void knnImputation(){
        //first find out the instances with missing values
        for(int j=0;j<(n_cols);j++){
            for(int i=0;i<n_rows;i++){
                if(csvMatrix.get(i)[j].equals("")){
                    missing_row_indexes.add(i);
                    missing_col_indexes.add(j);
                }
                else{
                    
                }
                
            }
        }
        for(int k=0;k<missing_row_indexes.size();k++){
            arr_instance_missing = prepareInstance(missing_row_indexes.get(k));
            
            
            for(int i=0;i<n_rows;i++){
                if(missing_row_indexes.get(k) == i){
                    //The DTW distance with itself will always be 0
                }
                else{
                    
                    if(csvMatrix.get(i)[missing_col_indexes.get(k)].equals("")){
                        //No instance should have missing value in the same position as considered instance
                    }
                    else{
                        arr_instance = prepareInstance(i);
                        warp_distances.add(fastDtwCalc(arr_instance, arr_instance_missing));
                        instances.add(i);
                    }
                    
                }
                
            }
            printDistances();
            neighbor_instances = decideNeighbors(warp_distances,k_neighbors, instances);
            
            meanOfNeighbors(warp_distances, k_neighbors, neighbor_instances, 
                    missing_row_indexes.get(k),
                    missing_col_indexes.get(k));
            
            
            warp_distances.clear();
            neighbor_instances.clear();
            instances.clear();
            
            
        }
        
        saveFile();
        
    }
    
    private void printDistances(){
        for(int i=0;i<warp_distances.size();i++){
            System.out.print(warp_distances.get(i)+" ");
        }
        System.out.println("");
    }
    
    public Double[] prepareInstance(int instance_index){
        List<Double> instance = new ArrayList<Double>();
        List<Double> instance_missing = new ArrayList<Double>();
        
        for(int j=1;j<(n_cols);j++){
            if(!csvMatrix.get(instance_index)[j].equals("")){
                instance.add(Double.parseDouble(csvMatrix.get(instance_index)[j]));
            }
            
        }
        
        Double[] local_arr_instance = instance.toArray(new Double[instance.size()]);
        
        return local_arr_instance;
    }
    
    public ArrayList<Integer> decideNeighbors(ArrayList<Double> w_dist, 
        int k_val, ArrayList<Integer> n_instances){ //k_val is the user parameter 
        
        ArrayList<Integer> ref_n_instances = new ArrayList<Integer>();
        ArrayList<Double> ref_w_dist = new ArrayList<>(w_dist);
        
        Collections.sort(ref_w_dist);
        
        for(int i=0;i<k_val;i++){
            ref_n_instances.add(n_instances.get(w_dist.indexOf(ref_w_dist.get(i))));
            
            System.out.print(ref_n_instances.get(i)+" ");
        }
        System.out.println();
        
        return ref_n_instances;
    }
    
    
    private void invDistanceWeight(ArrayList<Double> w_dist, int k_val,
            ArrayList<Integer> ref_n_instances,int row_index,
            int col_index){
        
        double total_distance = 0.0, distance = 0.0, total_value = 0.0, imputed_result = 0.0;
        ArrayList<Double> ref_w_dist = new ArrayList<>(w_dist);
        Collections.sort(ref_w_dist);
        
        for(int i=0;i<k_val;i++){
            distance = distance + ref_w_dist.get(i);
            
        }
        //System.out.println("Distance is "+total_distance);
        
        for(int i=0;i<ref_w_dist.size();i++){
            total_distance = total_distance + ref_w_dist.get(i);
            
        }
        
        for(int i=0;i<k_val;i++){
            total_value = total_value +
                    Double.parseDouble(csvMatrix.get(ref_n_instances.get(i))[col_index]);
        }
        //System.out.println("Value is "+total_value);
        
        imputed_result = (total_value/total_distance)/(1/distance);
        //System.out.println("Imputed value is"+imputed_result+"\n");
        
        //Imputation
        csvMatrix.get(row_index)[col_index] = Double.toString(imputed_result);
    }
    
    private void meanOfNeighbors(ArrayList<Double> w_dist, int k_val,
            ArrayList<Integer> ref_n_instances,int row_index,
            int col_index){
        
        double imputed_mean_result = 0.0;
        
        for(int i=0;i<k_val;i++){
            imputed_mean_result = imputed_mean_result +
                    Double.parseDouble(csvMatrix.get(ref_n_instances.get(i))[col_index]);
        }
        
        imputed_mean_result = imputed_mean_result/k_val;
        
        //Imputation
        csvMatrix.get(row_index)[col_index] = Double.toString(imputed_mean_result);
        
    }

    
    public double fastDtwCalc(Double[] ins, Double[] ins_miss){
        
        
        double[] arr_ins = ArrayUtils.toPrimitive(ins);
        double[] arr_ins_miss = ArrayUtils.toPrimitive(ins_miss);
        
        /*Instance instance1 = new DenseInstance(arr_ins);
        Instance instance2 = new DenseInstance(arr_ins_miss);
        
        TimeSeries t1 = new TimeSeries(instance1);
        TimeSeries t2 = new TimeSeries(instance2);
        
        FastDTW d = new FastDTW();*/
        
        DTW dtw = new DTW(arr_ins, arr_ins_miss);
        
        return dtw.getDistance();
        
        
        
        
    }
    
    
    
    public String saveFile(){
        
        File file = new File(this.filePath);
        String fileName = file.getName();
        fileName = FilenameUtils.removeExtension(fileName);
        String finalPath = "";
        try{
            if(getFileExtension(file).isEmpty()){
                writer = new CSVWriter(new FileWriter(file.getParent()+dir_cot+fileName+"_KNN_Impute"));
                finalPath = file.getParent()+dir_cot+fileName+"_KNN_Impute";
            }
            else{
                writer = new CSVWriter(new FileWriter(file.getParent()+dir_cot+fileName+"_KNN_Impute."+getFileExtension(file)));
                finalPath = file.getParent()+dir_cot+fileName+"_KNN_Impute."+getFileExtension(file);
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
