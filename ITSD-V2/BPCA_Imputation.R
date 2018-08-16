
repmat = function(X,m,n){
  ##R equivalent of repmat (matlab)
  mx = dim(X)[1]
  nx = dim(X)[2]
  matrix(t(matrix(X,mx,nx*n)),mx*m,nx*n,byrow=T)
}


BPCA_initmodel <- function(y, components) {
  ## Initialization, write static parameters to the central
  M <- NULL 
  M$rows <- nrow(y)
  M$cols <- ncol(y) 
  M$comps <- components ## Column number
  M$yest <- y ## Original data, NAs are set to 0 later on
  
  ## Find rows with missing values, etc...
  M$nans <- is.na(y)
  temp <- apply(M$nans, 1, sum)
  M$row_nomiss <- which(temp == 0)
  M$row_miss <- which(temp != 0)
  M$yest[M$nans] <- 0
  M$scores <- NULL
  
  ## Get the SVD of the complete rows
  covy <- cov(M$yest)
  values <- svd(covy, components, components)
  U <- values[[2]]
  S <- diag( values[[1]][1:components], nrow = components, ncol = components)
  V <- values[[3]]
  
  ## M$mean: column wise mean of the original data
  M$mean <- matrix(0, 1, M$cols)
  for(j in 1:M$cols) {
    idx <- which(!is.na(y[,j]))
    M$mean[j] <- mean(y[idx,j])
  }
  
  M$PA <- U %*% sqrt(S)
  M$tau <- 1 / ( sum(diag(covy)) - sum(diag(S)) )
  
  ## Constants etc
  taumax <- 1e10
  taumin <- 1e-10
  M$tau <- max( min(M$tau, taumax), taumin )
  
  M$galpha0 <- 1e-10
  M$balpha0 <- 1
  M$alpha <- (2 * M$galpha0 + M$cols) / (M$tau * diag(t(M$PA) %*% M$PA) + 2 * M$galpha0 / M$balpha0)
  
  M$gmu0 <- 0.001
  
  M$btau0 <- 1
  M$gtau0 <- 1e-10
  M$SigW <- diag(components)
  return(M)
}
BPCA_dostep <- function(M,y) {

  ## Empty matrix in which the scores are copied
  M$scores <- matrix(NA, M$rows, M$comps)

  ## Expectation step for data without missing values
  Rx <- diag(M$comps) + M$tau * t(M$PA) %*% M$PA + M$SigW
  Rxinv <- solve(Rx)
  idx <- M$row_nomiss

  if (length(idx) == 0) {
    trS <- 0
    T <- 0
  } else {
    dy <- y[idx,, drop=FALSE] - repmat(M$mean, length(idx), 1)
    x <- M$tau * Rxinv %*% t(M$PA) %*% t(dy)
    T <- t(dy) %*% t(x)
    trS <- sum(sum(dy * dy))

    ## Assign the scores for complete rows
    xTranspose <- t(x)
    for (i in 1:length(idx)) {
      M$scores[idx[i],] <- xTranspose[i,]
    }
  }
  ## Expectation step for incomplete data
  if( length(M$row_miss) > 0) {
    for(n in 1:length(M$row_miss)) {
      i  <- M$row_miss[n]
      dyo <- y[ i, !M$nans[i,], drop=FALSE] - M$mean[ !M$nans[i,], drop=FALSE]
      Wm <- M$PA[ M$nans[i,],, drop=FALSE]
      Wo <- M$PA[ !M$nans[i,],, drop=FALSE]
      Rxinv <- solve( (Rx - M$tau * t(Wm) %*% Wm))
      ex  <- M$tau * t(Wo) %*% t(dyo)
      x <- Rxinv %*% ex
      dym <- Wm %*% x
      dy <- y[i,, drop=FALSE]
      dy[ !M$nans[i,] ] <- t(dyo)
      dy[ M$nans[i,] ] <- t(dym)
      M$yest[i,] <- dy + M$mean
      T <- T + t(dy) %*% t(x)
      T[ M$nans[i,], ] <- T[ M$nans[i,],, drop=FALSE] + Wm %*% Rxinv
      trS <- trS + dy %*% t(dy) + sum(M$nans[i,]) / M$tau + 
        sum( diag(Wm %*% Rxinv %*% t(Wm)) ) 
      trS <- trS[1,1]
      ## Assign the scores for rows containing missing values
      M$scores[M$row_miss[n],] <- t(x)
    }
  }
  T <- T / M$rows
  trS <- trS / M$rows

  ## Maximation step
  Rxinv <- solve(Rx)
  Dw <- Rxinv + M$tau * t(T) %*% M$PA %*% Rxinv + 
    diag(M$alpha, nrow = length(M$alpha)) / M$rows
  Dwinv <- solve(Dw)
  M$PA <- T %*% Dwinv ## The new estimate of the principal axes (loadings)
  M$tau <- (M$cols + 2 * M$gtau0 / M$rows) / (trS - sum(diag(t(T) %*% M$PA)) +
                                              (M$mean %*% t(M$mean) * M$gmu0 + 2 * M$gtau0 / M$btau0) / M$rows)
  M$tau <- M$tau[1,1] ## convert to scalar
  M$SigW <- Dwinv * (M$cols / M$rows)
  M$alpha <- (2 * M$galpha0 + M$cols) / (M$tau * diag(t(M$PA) %*% M$PA) + 
                                         diag(M$SigW) + 2 * M$galpha0 / M$balpha0)

  return(M)
}

bpca <- function(Matrix, nPcs, maxSteps, 
                 verbose=interactive(), threshold=1e-4, ... ) {
  
  
  M <- BPCA_initmodel(Matrix, nPcs)
  tauold <- 1000
  
  for( step in 1:maxSteps ) {
    M <- BPCA_dostep(M, Matrix)
    print(M)
    if( step %% 10 == 0 ) {
      tau <- M$tau
      dtau <- abs(log10(tau) - log10(tauold))
      if ( verbose ) {
        cat("Step Number           : ", step, '\n')
        cat("Increase in precision : ", dtau, '\n')
        cat("----------", '\n')
      }
      if (dtau < threshold) {
        break
      }
      tauold <- tau
    }
  }
  
  R2cum <- rep(NA, nPcs)
  TSS <- sum(Matrix^2, na.rm=TRUE)
  for (i in 1:nPcs) {
    difference <-
      Matrix - (M$scores[,1:i, drop=FALSE] %*% t(M$PA[,1:i, drop=FALSE]) )
    R2cum[i] <- 1 - (sum(difference^2, na.rm=TRUE) / TSS)
  }
  
  return (M$yest)
  
}

init <- function(path, headerType, maxSt){
	if(headerType == "Header"){
    matrix <- read.csv(path, header = TRUE)
  }
  else{
    matrix <- read.csv(path, header = FALSE)
  }

	
	n_attr = ncol(matrix)
	final_attr = ceiling(n_attr/4)
	matrix <- as.matrix(matrix)
	
	
	
	result <- bpca(Matrix = matrix, nPcs = 3, maxSteps = maxSt, verbose = interactive(), threshold = 1e-04)
	
	print(final_attr)
	return(result)

}



