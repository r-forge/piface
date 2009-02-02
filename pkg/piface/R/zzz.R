.onLoad <- function(libname, pkgname){
  .jpackage(pkgname)
}
.onAttach <- function(libname, pkgname){
  message(paste("\npiface version ", packageDescription("piface")$Version, 
          "\n", sep = ""))
  message("\nUse 'piface()' to launch the main menu")
}
