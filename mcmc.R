if (!require("mi")){
  install.packages("mi")  
}
if(!require(("tools"))){
  install.packages("tools")
}
suppressMessages(library(mi))
library(tools)
getMethod("fit_model", signature(y = "continuous", data = "missing_data.frame"))
setMethod("fit_model", signature(y = "continuous", data = "missing_data.frame"), def =
            function(y, data, ...) {
              to_drop <- data@index[[y@variable_name]]
              X <- data@X[, -to_drop]
              start <- NULL
              # using glm.fit() instead of bayesglm.fit()
              out <- glm.fit(X, y@data, weights = data@weights[[y@variable_name]], start = start,
                             family = y@family, Warning = FALSE, ...)
              out$x <- X
              class(out) <- c("glm", "lm") # not "bayesglm" class anymore
              return(out)
            })


mcmcImpute <- function(filepath){
    # STEP 0: Get data
    matrix <- read.csv(filepath, header = FALSE)
    
    #matrix = matrix[col_index, ]
    
    col_num <- ncol(matrix)
    
    #data(nlsyV, package = "mi")
    mdf <- missing_data.frame(matrix)
    
    
    
    
    
    # STEP 4: impute
    ## Not run:
    
    
    if(!exists("imputations", env = .GlobalEnv)){
      imputations <- mi:::imputations
    }
  
    imputations <- mi(mdf, n.iter = 30, n.chains = 4)
  
   
  
    
    
    dfs <- complete(imputations, m = 1)
    
    final_matrix <- as.matrix(dfs)
    
    print(final_matrix[,1:col_num])
    
    return(final_matrix[,1:col_num])
      
    
}

parseFile <- function(matrix, path){
  os_name = Sys.info()['sysname']  
  
  file = basename(path)
  fileName = file_path_sans_ext(file)
  dir = dirname(path)
  ext = file_ext(file)
  
  if(ext == ''){
    
    new_FileName = paste(fileName, "_MCMC_Impute", sep="")
  }
  else{
    new_FileName = paste(fileName, "_MCMC_Impute.", ext, sep="")
  }
  
  if(os_name == 'Darwin') {
    final_Dir = paste(dir, new_FileName, sep="/")
  }
  else if(os_name == 'windows'){
    final_Dir = paste(dir, new_FileName, sep="\\")
  }
  else{
    final_Dir = paste(dir, new_FileName, sep="/")
  }
  
  print(final_Dir)
  
  write.table(matrix, file = final_Dir, row.names=FALSE, col.names = FALSE, sep=",")
}


file_path <- 'G:\\Ovgu\\Semester_5\\TeamProject\\Small_Dataset\\MiddlePhalanxOutlineAgeGroup\\MiddlePhalanxOutlineAgeGroup_TRAIN_Missing'

matrix <- mcmcImpute(file_path)
parseFile(matrix, file_path)




