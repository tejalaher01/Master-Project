/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.itsd.v1;

import com.kmd.bpca.BPCAimputation;
import com.kmd.knn.KNNimputation;
import com.kmd.mean.MeanImputation;
import com.kmd.mice.MiceImputation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.PopupBuilder;
import javafx.stage.Stage;
import javafx.util.Pair;


public class ITSD extends Application {
    
    String os_name = "", dir_cot = "";
    MeanImputation mean;
    GenerateMissingValues generate;
    RMSECalculation rmse;
    
    TextField filePath, ThreshCombo,Imputefilepath, e_missing_filepath, e_impute_filepath, eval_missing_filepath;
    
    Button Browsebtn,Processbtn,ImputeBrowsebtn,Imputebtn, e_ImputeBrowsebtn, 
            e_MissingBrowsebtn, Evaluatebtn, CancelImputebtn, Plotbtn, evalMissingbtn;
    
    Label fileLabel, percentageLabel, headerLabel,approachFilelbl,projectNameLbl,approachlbl,
            evaluationLabel, eMissinglbl, eImputelbl,eEvaluationlbl,evalMissinglbl, progresslbl;
    
    ProgressBar pb = new ProgressBar(0.0);
    ProgressIndicator pi = new ProgressIndicator(0.0);
    
    ComboBox ApproachCombo;
    
    TextArea rmse_notification;
    
    Alert alert = new Alert(AlertType.INFORMATION);
    
    final double MAX_FONT_SIZE = 20.0; //define max font size you need
    
    String approach_name = "", approach_filepath="";
    
    String filepath_impute = "";
    
    String headerType = ""; /*Very important for dataset property*/
    
    long startTime = 0;
    long stopTime = 0;
    
    
    void initialization(){
        parseDir(); //Parse directory according to OS 
        
        generate = new GenerateMissingValues();
        
        alert.setTitle("ITSD Notification");
      
        filePath = new TextField();
        Imputefilepath = new TextField();
        
        e_missing_filepath = new TextField();
        e_impute_filepath = new TextField();
        eval_missing_filepath = new TextField();
        
        projectNameLbl = new Label("Framework for Imputing Missing Time Series Data");
        fileLabel = new Label("File Location");
        percentageLabel = new Label("Threshold %");
        
        approachlbl = new Label("Approach(s)");
        approachFilelbl = new Label("File Location");
        
        headerLabel = new Label("Header");
        
        evaluationLabel = new Label("Evaluation of different imputation approaches");
        eMissinglbl = new Label("File path(Original data)");
        eImputelbl = new Label("File path(Imputed data)");
        evalMissinglbl = new Label("File path(Missing data)");
        eEvaluationlbl = new Label("Evaluation Report");
        progresslbl = new Label("Progress");
        
        ThreshCombo = new TextField();
        ApproachCombo = new ComboBox();
        
        
        Browsebtn = new Button("Browse");
        Processbtn = new Button("Process");
        
        ImputeBrowsebtn = new Button("Browse");
        Imputebtn = new Button("Impute");
        
        e_ImputeBrowsebtn = new Button("Browse");
        e_MissingBrowsebtn = new Button("Browse");
        evalMissingbtn = new Button("Browse");
        Evaluatebtn = new Button("Evaluate approach");
        
        CancelImputebtn = new Button("Cancel");
        
        Plotbtn = new Button("Generate Plot");
        
        rmse_notification = new TextArea();
        
        
    }
    
    private void parseDir(){
        if(System.getProperty("os.name").startsWith("Linux") || 
                System.getProperty("os.name").equals("Mac OS X")){
            dir_cot = "/";
        }
        else if(System.getProperty("os.name").startsWith("Windows")){
            dir_cot = "\\";
            System.out.println("Accessed");
        }
        else{
            dir_cot = "/";
        }
    }
    
    public void resizingAndAlignmentMissingDataPanel(){
        //Label for filepath
        fileLabel.setTranslateX(10);
        fileLabel.setTranslateY(40);
        
        //Label for missing data number
        percentageLabel.setTranslateX(10);
        
        //Project label
        projectNameLbl.setStyle("-fx-font: 16 arial;");
        projectNameLbl.setTranslateX(350);
        
        //Filepath textview size and alignment
        filePath.setTranslateX(100);
        filePath.setTranslateY(40);
        filePath.setPrefWidth(300);
        filePath.setDisable(true);
        
        //Browse Button
        Browsebtn.setTranslateX(410);
        Browsebtn.setTranslateY(40);
        
        //PercentageLabel
        percentageLabel.setTranslateY(80);
        
        //A method for setting up Combo of Threshold
        setUpThresholdCombo();
        
        
        //Generate Button
        Processbtn.setTranslateX(410);
        Processbtn.setTranslateY(80);
        
    }
    
     
    
    private void setUpThresholdCombo(){
        
        ThreshCombo.setPrefWidth(300);
        ThreshCombo.setTranslateX(100);
        ThreshCombo.setTranslateY(80);
    }
    
    private void resizeAndAlignApproachPanel(){
        
        //Label for filepath
        approachFilelbl.setTranslateX(10);
        approachFilelbl.setTranslateY(150);
        //Filepath textview size and alignment
        Imputefilepath.setTranslateX(100);
        Imputefilepath.setTranslateY(150);
        Imputefilepath.setPrefWidth(300);
        Imputefilepath.setDisable(true);
        
        //Browse Button
        ImputeBrowsebtn.setTranslateX(410);
        ImputeBrowsebtn.setTranslateY(150);
        
        approachlbl.setTranslateX(10);
        approachlbl.setTranslateY(200);
        
        Imputebtn.setTranslateX(410);
        Imputebtn.setTranslateY(200);
        
        progresslbl.setTranslateX(600);
        progresslbl.setTranslateY(100);
        
        pb.setTranslateX(600);
        pb.setTranslateY(120);
        pb.setPrefWidth(280);
        
        pi.setTranslateX(750);
        pi.setTranslateY(130);
        
        CancelImputebtn.setTranslateX(920);
        CancelImputebtn.setTranslateY(120);
        
        
        
        setUpApproachCombo();
    }
    
    private void setUpApproachCombo(){
        ApproachCombo.getItems().addAll(
            "Mean","KNN","MCMC","MICE","BPCA"  
        );
        
        ApproachCombo.setPromptText("Select the Approach(s) for Imputation");
        ApproachCombo.setPrefWidth(300);
        ApproachCombo.setTranslateX(100);
        ApproachCombo.setTranslateY(200);
    }
    
    
    
    private void resizeAndAlignEvaluatePanel(){
        //Evaluation label
        evaluationLabel.setStyle("-fx-font: 16 arial;");
        evaluationLabel.setTranslateX(320);
        evaluationLabel.setTranslateY(280);
        
        //Label for filepath
        eMissinglbl.setTranslateX(120);
        eMissinglbl.setTranslateY(320);
        //Filepath textview size and alignment
        e_missing_filepath.setTranslateX(270);
        e_missing_filepath.setTranslateY(320);
        e_missing_filepath.setPrefWidth(300);
        e_missing_filepath.setDisable(true);
        ;
        
        //Browse Button
        e_MissingBrowsebtn.setTranslateX(650);
        e_MissingBrowsebtn.setTranslateY(320);
        
        //Label for filepath
        eImputelbl.setTranslateX(115);
        eImputelbl.setTranslateY(380);
        //Filepath textview size and alignment
        e_impute_filepath.setTranslateX(270);
        e_impute_filepath.setTranslateY(380);
        e_impute_filepath.setPrefWidth(300);
        e_impute_filepath.setDisable(true);
        
        //Browse Button
        e_ImputeBrowsebtn.setTranslateX(650);
        e_ImputeBrowsebtn.setTranslateY(380);
        
        
        
         
        
        evalMissinglbl.setTranslateX(115);
        evalMissinglbl.setTranslateY(440);
        
        eval_missing_filepath.setTranslateX(270);
        eval_missing_filepath.setTranslateY(440);
        eval_missing_filepath.setPrefWidth(300);
        eval_missing_filepath.setDisable(true);
        
        //Browse Button
        evalMissingbtn.setTranslateX(650);
        evalMissingbtn.setTranslateY(440);
        
        //Label for Evaluation screen
        eEvaluationlbl.setTranslateX(750);
        eEvaluationlbl.setTranslateY(290);
        
        
        rmse_notification.setTranslateX(750);
        rmse_notification.setTranslateY(380);
        rmse_notification.setPrefSize(20, 150);
        rmse_notification.setEditable(false);
        
        //Evaluate Button
        Evaluatebtn.setTranslateX(400);
        Evaluatebtn.setTranslateY(480);
        
        //Plot graph button 
        Plotbtn.setPrefWidth(115);
        Plotbtn.setTranslateX(400);
        Plotbtn.setTranslateY(520);
        
    }
    public void allEvents(){
       //Browse button event
       Browsebtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(null);
                
                filePath.setText(selectedFile.getAbsolutePath().toString());  
                
                if(filePath.getText().toString().isEmpty()){
                    headerType = "";
                }
                else{
                    headerType = headerPopup();
                }
            }
        }); 
       
       //Process button event
       Processbtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               if(filePath.getText().trim().isEmpty()){
                   alert.setContentText("You need to upload a file first");
                   alert.setHeaderText(null);
                   alert.showAndWait();
               }
               else{
                    try{
                       
                       
                       double n_deleted = 
                       generate.getFilePathRatio(filePath.getText().toString(), 
                           Integer.parseInt(ThreshCombo.getText().toString()),
                       headerType);
                        
                        alert.setContentText("File with missing values generated "
                                + "successfully\n Number of values deleted: "
                        +Double.toString(n_deleted));
                        alert.setHeaderText(null);
                        alert.showAndWait();
                        
                        //Fill up the next text field
                        String filePath_missing = generate.saveFile();
                        Imputefilepath.setText(filePath_missing);
                        /* End */
                   }
                   catch(NullPointerException e){
                        alert.setContentText("Select Threshold Ratio");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                   }
               }
                
                
            }
        });
       
       
       
       //Browse button event
       ImputeBrowsebtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(null);
                
                Imputefilepath.setText(selectedFile.getAbsolutePath().toString());
                
                if(Imputefilepath.getText().toString().isEmpty()){
                    headerType = "";
                }
                else{
                    headerType = headerPopup();
                }
            }
        }); 
       
        Imputebtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                if(!headerType.equals("")){
                    try{
                        approach_name = ApproachCombo.getSelectionModel().getSelectedItem().toString();
                        approach_filepath = Imputefilepath.getText().toString();
                        
                        if(approach_name.equals("Mean")){
                            startTime = System.nanoTime(); //Instantly start counting time
                            
                            mean = new MeanImputation();

                            Task<Void> task = new Task<Void>() {

                                @Override
                                protected Void call() throws Exception {

                                    for (int i = 0; i <= 50; i++) {
                                        updateProgress(i, 100);
                                        Thread.sleep(50);

                                        if(i == 50){
                                            mean.initialize(approach_filepath, headerType);
                                        }
                                    }

                                    for (int j = 50; j <= 100; j++) {
                                        updateProgress(j, 100);
                                        Thread.sleep(50);

                                        if(j == 50){

                                            filepath_impute = mean.saveFile(); //Get the imputed filepath

                                        }
                                    }


                                    return null;
                                }


                            };

                            //When the task is complete
                            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent event) {
                                    stopTime = System.nanoTime();
                                    String timeMessage = sendTimeFig();
                                    if(!filePath.getText().trim().isEmpty()){    
                                        //Fill up the next text field
                                        showNotification("Imputation using Mean approach completed\n\n"+timeMessage);
                                        e_impute_filepath.setText(filepath_impute);
                                        e_missing_filepath.setText(filePath.getText().toString());
                                        eval_missing_filepath.setText(Imputefilepath.getText().toString());
                                    }
                                    else{
                                        showNotification("Imputation using Mean approach completed, Since we couldn't"
                                                + " find original dataset, evaluation is not possible\n\n"+timeMessage);
                                    }
                                }
                            });

                            CancelImputebtn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                                public void handle(ActionEvent event) {

                                    task.cancel();

                                }
                            });

                            pb.progressProperty().bind(task.progressProperty());
                            pi.progressProperty().bind(task.progressProperty());



                            //start the thread
                            new Thread(task).start();


                        }

                        else if(approach_name.equals("KNN")){
                            
                            startTime = System.nanoTime(); //Instantly start counting time
                            
                            int k = userOneInputPopup("K Nearest Neighbors", "Input k-value(Integer):");
                            
                            if(k != 0){
                                KNNimputation knn = new KNNimputation();

                                Task<Void> task = new Task<Void>() {

                                    @Override
                                    protected Void call() throws Exception {

                                        for (int i = 0; i <= 50; i++) {
                                            updateProgress(i, 100);
                                            Thread.sleep(50);

                                            if(i == 50){
                                                knn.initialize(approach_filepath,headerType, k);
                                            }
                                        }

                                        for (int j = 50; j <= 100; j++) {
                                            updateProgress(j, 100);
                                            Thread.sleep(50);

                                            if(j == 50){

                                                filepath_impute = knn.saveFile(); //Get the imputed filepath

                                            }
                                        }


                                        return null;
                                    }


                                };

                                //When the task is complete
                                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                    @Override
                                    public void handle(WorkerStateEvent event) {
                                        stopTime = System.nanoTime();
                                        String timeMessage = sendTimeFig();
                                        
                                        if(!filePath.getText().trim().isEmpty()){

                                            //Fill up the next text field
                                            showNotification("Imputation using KNN approach completed\n\n"+timeMessage);
                                            e_impute_filepath.setText(filepath_impute);
                                            e_missing_filepath.setText(filePath.getText().toString());
                                            eval_missing_filepath.setText(Imputefilepath.getText().toString());
                                        }
                                        else{
                                            showNotification("Imputation using KNN approach completed, Since we couldn't"
                                                    + " find original dataset, evaluation is not possible\n\n"+timeMessage);
                                        }
                                    }
                                });

                                CancelImputebtn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                    public void handle(ActionEvent event) {

                                        task.cancel();

                                    }
                                });

                                pb.progressProperty().bind(task.progressProperty());
                                pi.progressProperty().bind(task.progressProperty());



                                //start the thread
                                new Thread(task).start();
                            }
                            //Zero Value found
                            else{
                                alert.setContentText("Parameter k as zero or null is not allowed");
                                alert.setHeaderText("ITSD Notification");
                                alert.showAndWait();
                            }

                        }

                        else if(approach_name.equals("BPCA")){
                            startTime = System.nanoTime(); //Instantly start counting time
                            
                            int maxSt = userOneInputPopup("Bayesian Principle Component Analysis", "Input Max Steps");

                            if(maxSt != 0){
                                BPCAimputation bpca = new BPCAimputation();

                                Task<Void> task = new Task<Void>() {

                                    @Override
                                    protected Void call() throws Exception {

                                        for (int i = 0; i <= 30; i++) {
                                            updateProgress(i, 100);
                                            Thread.sleep(50);

                                            if(i == 30){
                                                bpca.impute(approach_filepath,headerType, maxSt);
                                            }
                                        }

                                        for (int j = 30; j <= 100; j++) {
                                            updateProgress(j, 100);
                                            Thread.sleep(50);

                                            if(j == 50){

                                                filepath_impute = bpca.saveFile(); //Get the imputed filepath

                                            }
                                        }


                                        return null;
                                    }


                                };

                                //When the task is complete
                                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                    @Override
                                    public void handle(WorkerStateEvent event) {
                                        
                                        stopTime = System.nanoTime();
                                        String timeMessage = sendTimeFig();

                                        if(!filePath.getText().trim().isEmpty()){
                                            showNotification("Imputation using BPCA approach completed\n\n"+timeMessage);
                                            //Fill up the next text field

                                            e_impute_filepath.setText(filepath_impute);
                                            e_missing_filepath.setText(filePath.getText().toString());
                                            eval_missing_filepath.setText(Imputefilepath.getText().toString());
                                        }
                                        else{
                                            showNotification("Imputation using BPCA approach completed, Since we couldn't"
                                                    + " find original dataset, evaluation is not possible\n\n"+timeMessage);
                                        }
                                    }
                                });

                                CancelImputebtn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                    public void handle(ActionEvent event) {

                                        task.cancel();

                                    }
                                });

                                pb.progressProperty().bind(task.progressProperty());
                                pi.progressProperty().bind(task.progressProperty());



                                //start the thread
                                new Thread(task).start();
                            }

                            else{
                                alert.setContentText("Max Steps as zero or null is not allowed");
                                alert.setHeaderText("ITSD Notification");
                                alert.showAndWait();
                            }
                        }

                        else if(approach_name.equals("MICE")){
                            startTime = System.nanoTime(); //Instantly start counting time
                            
                            ArrayList<Integer> inputs = userMultiInputPopup();

                            if(inputs.size() == 0 || inputs.size() != 1 || inputs.get(0) != 0 || inputs.get(1) != 0){
                                MiceImputation mice = new MiceImputation();

                                Task<Void> task = new Task<Void>() {

                                    @Override
                                    protected Void call() throws Exception {

                                        for (int i = 0; i <= 30; i++) {
                                            updateProgress(i, 100);
                                            Thread.sleep(50);

                                            if(i == 30){
                                                mice.impute(approach_filepath, headerType, inputs.get(0), inputs.get(1));
                                            }
                                        }

                                        for (int j = 30; j <= 100; j++) {
                                            updateProgress(j, 100);
                                            Thread.sleep(50);

                                            if(j == 50){

                                                filepath_impute = mice.saveFile(); //Get the imputed filepath

                                            }
                                        }


                                        return null;
                                    }


                                };

                                //When the task is complete
                                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                    @Override
                                    public void handle(WorkerStateEvent event) {
                                        
                                        stopTime = System.nanoTime();
                                        String timeMessage = sendTimeFig();
                                        
                                        if(!filePath.getText().trim().isEmpty()){

                                            //Fill up the next text field
                                            showNotification("Imputation using MICE approach completed\n\n"+timeMessage);
                                            e_impute_filepath.setText(filepath_impute);
                                            e_missing_filepath.setText(filePath.getText().toString());
                                            eval_missing_filepath.setText(Imputefilepath.getText().toString());
                                        }
                                        else{
                                            showNotification("Imputation using MICE approach completed, Since we couldn't"
                                                    + " find original dataset, evaluation is not possible\n\n"+timeMessage);
                                        }
                                    }
                                });

                                CancelImputebtn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                    public void handle(ActionEvent event) {
                                        
                                        task.cancel();

                                    }
                                });

                                pb.progressProperty().bind(task.progressProperty());
                                pi.progressProperty().bind(task.progressProperty());



                                //start the thread
                                new Thread(task).start();
                            }
                            else{

                            }
                        }
                    }
                    catch(NullPointerException e){
                        alert.setContentText("Please select an approach");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                    }
                }
                
                
                
            }
        });
       
       //Browse button event
       e_MissingBrowsebtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(null);
                
                e_missing_filepath.setText(selectedFile.getAbsolutePath().toString());
                
                if(e_missing_filepath.getText().toString().isEmpty()){
                    headerType = "";
                }
                else{
                    headerType = headerPopup();
                }
            }
        });
       
       //Browse button event
       e_ImputeBrowsebtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(null);
                
                e_impute_filepath.setText(selectedFile.getAbsolutePath().toString());
                
                if(e_impute_filepath.getText().toString().isEmpty()){
                    headerType = "";
                }
                else{
                    headerType = headerPopup();
                }
            }
        });
       
       //Browse button event
       evalMissingbtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(null);
                
                eval_missing_filepath.setText(selectedFile.getAbsolutePath().toString());
                
                if(eval_missing_filepath.getText().toString().isEmpty()){
                    headerType = "";
                }
                else{
                    headerType = headerPopup();
                }
            }
        });
       
       //Browse button event
        Evaluatebtn.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 /*rmse = new RMSECalculation(e_missing_filepath.getText().toString()
                         , e_impute_filepath.getText().toString(), headerType);

                 String msg = rmse.calculateRMSE();

                 rmse_notification.setText(msg);*/
                 
                ComputeRMSE rmse = new ComputeRMSE(e_missing_filepath.getText().toString()
                         , e_impute_filepath.getText().toString(),eval_missing_filepath.getText().toString(), headerType);
                 
                 String msg = rmse.calculateRMSE();

                 rmse_notification.setText(msg);
             }
         });
        
        //Browse button event
        Plotbtn.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                showGraph();
             }
         });
       
       
    }
    
    
    
    @Override
    public void start(Stage primaryStage) {
        
        initialization();
        resizingAndAlignmentMissingDataPanel();
        
        resizeAndAlignApproachPanel();
        resizeAndAlignEvaluatePanel();
        allEvents();
        
        
        
        GridPane root = new GridPane();
        
        GridPane missingValView = new GridPane();
        
        GridPane approachView = new GridPane();
        
        GridPane headerView = new GridPane();
        
        GridPane evaluateView = new GridPane();
        
        
        missingValView.getChildren().addAll(projectNameLbl,fileLabel,filePath,Browsebtn, percentageLabel,
                ThreshCombo,Processbtn);
        
        
        
        
        approachView.getChildren().addAll(approachFilelbl,Imputefilepath,ImputeBrowsebtn,
                approachlbl,ApproachCombo, Imputebtn, progresslbl,pb, pi, CancelImputebtn);
        
        
        
        evaluateView.getChildren().addAll(evaluationLabel,eMissinglbl,e_missing_filepath,
        e_MissingBrowsebtn,eImputelbl,e_impute_filepath,e_ImputeBrowsebtn, evalMissinglbl, eval_missing_filepath, evalMissingbtn, 
        eEvaluationlbl,rmse_notification, Evaluatebtn, Plotbtn);
        
        root.getChildren().addAll(evaluateView, approachView,  missingValView);
        
        Scene scene = new Scene(root, 1000, 700);
        
        
        
        
        primaryStage.setTitle("Framework for missing value imputation of Time series Data");
        primaryStage.setScene(scene);
        
        primaryStage.show();
        
        
    }
    
    private String headerPopup(){
        List<String> choices = new ArrayList<>();
        choices.add("Header");
        choices.add("No Header");
        

        ChoiceDialog<String> dialog = new ChoiceDialog<>("No Header", choices);
        dialog.setTitle("Choose Dataset Type");
        dialog.setHeaderText("Specify Dataset");
        dialog.setContentText("Choose header property:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            System.out.println("Your choice: " + result.get());
            
        }
        
        return result.get().toString();
    }
    
    private int userOneInputPopup(String HeaderText, String message){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("User parameter");
        dialog.setHeaderText(HeaderText);
        dialog.setContentText(message);

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            try{
                return Integer.parseInt(result.get().toString());
            }
            catch(Exception e){
                alert.setContentText("Only integer values are expected!!");
                alert.setHeaderText("ITSD Notification");
                alert.showAndWait();
            }
        }
        else{
            return 0;
        }
        return 0;
    }
    
    private ArrayList<Integer> userMultiInputPopup(){
        ArrayList<Integer> inputs = new ArrayList<Integer>();
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("User parameter");
        dialog.setHeaderText("Multiple imputations for chained equations");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Number of chains");
        TextField password = new TextField();
        password.setPromptText("Number of iterations(Linear Regression)");

        grid.add(new Label("Number of chains:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Number of iterations(Linear Regression):"), 0, 1);
        grid.add(password, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        
        

        result.ifPresent(usernamePassword -> {
            //System.out.println("Username=" + usernamePassword.getKey() + ", Password=" + usernamePassword.getValue());
            inputs.add(Integer.parseInt(usernamePassword.getKey().toString()));
            inputs.add(Integer.parseInt(usernamePassword.getValue().toString()));
            
        });
        return inputs;
    }
    
    
    
    private void showGraph(){
        PlotGeneration generate = new PlotGeneration();
        ArrayList<String> originalValues, imputedValues;
        //Passing original and imputed filepath
        generate.initialize(e_missing_filepath.getText().toString(), e_impute_filepath.getText().toString());
        
        //Get original and imputed data
        originalValues = generate.getOriginalData();
        imputedValues = generate.getImputedData();
        
        Stage stage = new Stage();
        stage.setTitle("Original Values vs Missing Values Graph");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        
        //creating the chart
        final LineChart<Number,Number> lineChart = 
                new LineChart<Number,Number>(xAxis,yAxis);
        
                
        lineChart.setTitle("Original Values vs Missing Values Graph");
        
        lineChart.setCreateSymbols(false);
        
        lineChart.getStyleClass().add("thick-chart");
        
        //defining a series
        XYChart.Series originalDataseries = new XYChart.Series();
        XYChart.Series ImputedDataseries = new XYChart.Series();
        
        originalDataseries.setName("Original Values");
        //populating the series with data
        for(int i=0;i<originalValues.size();i++){
            originalDataseries.getData().add(new XYChart.Data(i, Double.parseDouble(originalValues.get(i))));
        }
        
        
        ImputedDataseries.setName("Imputed Values");
        
        for(int i=0;i<imputedValues.size();i++){
            ImputedDataseries.getData().add(new XYChart.Data(i, Double.parseDouble(imputedValues.get(i))));
        }
        
        
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(originalDataseries);
        lineChart.getData().add(ImputedDataseries);
        
        
       
        stage.setScene(scene);
        stage.show();
    }
    
    private void showNotification(String message){
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    
    
    private String sendTimeFig(){
        long elapsedTime = stopTime - startTime;
        double totalSecs = (double)elapsedTime / 1000000000.0;
        
        long hours = (long)(totalSecs / 3600);
        long minutes = (long)(totalSecs % 3600) / 60;
        long seconds = (long)(totalSecs % 60);
        
        String timeMessage = "Execution Time: "+ Long.toString(hours)+"h "+Long.toString(minutes)+"m "
                +Long.toString(seconds)+"s";
        
        return timeMessage;
    }
    
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        launch(args);
       
    }
    
}
