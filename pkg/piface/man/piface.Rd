\name{piface}
\alias{piface}
\title{R wrapper for piface}
\description{
  R wrapper that allows to launch piface dialogs from the
  R command line
}
\usage{
piface(dialog = "main")
}
\arguments{
  \item{dialog}{Name of the dialog you would like to launch. Currently
    supported are \code{"main"} (for the welcome screen of piface), 
    \code{"anova"} (for Anova designs) and \code{"regression"} for 
    regression designs}
}
% TODO: improve description of return value
\value{
  Currently a string representation of the GUI dialog
  is returned invisibly
}
\references{Lenth, R. V. (2001). Some Practical Guidelines for Effective 
  Sample Size Determination, The American Statistician, 55, 187-193.
  
  \url{http://www.stat.uiowa.edu/~rlenth/Power/}
}
\author{Russell V. Lenth (entire piface application), Tobias Verbeke (wrapper)}
\examples{
  \dontrun{
  piface("anova")
  }
\keyword{manip}
