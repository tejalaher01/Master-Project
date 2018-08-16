library("tools")



h <- function(X, theta){
  return(t(theta) %*% X)
}

# function where I calculate the cost with current values
cost <- function(X, y, theta){
  result <- sum((X %*% theta - y)^2 ) / (2*length(y))
  
  return(result)
}

gradient <- function(X, y, theta){
  m <- nrow(X)
  sum <- rep(0, ncol(X))
  
  for (i in 1 : m) {
    sum <- sum + (h(X[i,], theta) - y[i]) * X[i,]
  }
  return(sum)
}


# The main algorithm 
gradientDescent <- function(X, y, maxit){
  alpha <- 0.001
  m <- nrow(X)
  theta <- rep(0, ncol(X))
  
  cost_history <- rep(0,maxit)
  
  for (i in 1 : maxit) {
    theta <- theta - alpha*(1/m)*gradient(X, y, theta)
    
    cost_history[i] <- cost(X, y, theta)
  }
  
  
  
  return(theta)
}




miceImpute <- function(path, headerType,num_of_chains, maxIt){
  if(headerType == "Header"){
    matrix <- read.csv(path, header = TRUE)
  }
  else{
    matrix <- read.csv(path, header = FALSE)
  }
  
  counter = 1
  #matrix <- as.matrix(matrix)
  
  print(matrix)
  
  #matrix <- t(matrix) #Transposing the matrix
  
  row_ind <- c()
  col_ind <- c()
  missing_vars <- list()
  index = 1
  
  for(i in 1:nrow(matrix)){
    for(j in 1:ncol(matrix)){
      if(is.na(matrix[i,j])){
        row_ind[[index]] <- i
        col_ind[[index]] <- j
        
        missing_vars[[index]] <- j
        index <- index + 1
        
      }
    }
  }
  missing_vars <- unique(unlist(missing_vars, use.names = FALSE))
  
  for(i in missing_vars){
    p <- mean(matrix[,i], na.rm = TRUE)
    
    for(m_index in 1:length(row_ind)){
      if(col_ind[m_index] == i){
        matrix[row_ind[m_index],col_ind[m_index]] <- p
      }
    }
    
  }
  
  print(matrix)
  
  
  for(i in 1:num_of_chains){
    for(j in missing_vars){
      test_indexes <- c(which(col_ind == j)) 
      test_instances <- row_ind[test_indexes] #Store the index of instances which had missing values
      #print(test_instances)
      
      theta <- gradientDescent(as.matrix(matrix[-test_instances,-j]), matrix[-test_instances,j],maxIt) #i variable will be predictor in a particular iteration
      
      for(instance in test_instances){
        vec_instance <- as.vector(t(matrix[instance,-j]))
        
        impute_result <- vec_instance %*% theta
        
        matrix[instance, j] <- impute_result
      }
      print(length(missing_vars))
      print(counter)
      counter = counter + 1
    }
    
    print("Iteration complete")
    
  }
  
  
  
  #parseFile(matrix, path)
  print(matrix)
  matrix <- as.matrix(matrix)
  
  return(matrix)
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
  
  
  final_Dir = paste(dir, new_FileName, sep="\\")
  
  print(final_Dir)
  
  write.table(matrix, file = final_Dir, row.names=FALSE, col.names = FALSE, sep=",")
}






