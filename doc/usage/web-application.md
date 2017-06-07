# Web Application

A basic example of a web application is available in the GitHub repository, under jodconverter-sample-webapp module.

The most important concept is that you should start a single OfficeManager instance when your webapp starts (using a servlet listener, Spring context configuration, or equivalent facility provided by your framework of choice), stop it when your webapp stops, and share it across all requests. The OfficeManager will take care of multi-threading.