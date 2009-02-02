piface <- function(dialog =  "main"){
  guiDialog <- switch(dialog,
      main = "rvl/piface/apps/PiPickerSA",
      anova = "rvl/piface/apps/AnovaPicker",
      regression = "rvl/piface/apps/LinRegGUI")
  pf <- .jnew(guiDialog)
  invisible(pf)
}
