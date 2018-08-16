library("mice")
library("tools")


miceImpute <- function(path, max_itr){
  matrix <- read.csv(path, header = FALSE)
  print("Welcome to MICE Imputation")
  tempData <- mice(matrix,m=5,maxit = max_itr,meth='pmm',seed=500)
  summary(tempData)
  completedData <- complete(tempData,1)
  
  completedData <- as.matrix(completedData)
  
  
  
  parseFile(completedData, path)
}

parseFile <- function(matrix, path){
  
  
  file = basename(path)
  fileName = file_path_sans_ext(file)
  dir = dirname(path)
  ext = file_ext(file)
  
  if(ext == ''){
    
    new_FileName = paste(fileName, "_MICE_Impute", sep="")
  }
  else{
    new_FileName = paste(fileName, "_MICE_Impute.", ext, sep="")
  }
  
  
  final_Dir = paste(dir, new_FileName, sep="/")
  
  print(final_Dir)
  
  write.table(matrix, file = final_Dir, row.names=FALSE, col.names = FALSE, sep=",")
}




