package org.jodconverter;

public class Deps {

  // Compile
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  // Latest version -> https://mvnrepository.com/artifact/org.checkerframework/checker-qual
  public static final String checkerQual = "org.checkerframework:checker-qual:3.29.0";

  // Main
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~

  // Apache Commons Libraries
  // Latest version -> https://mvnrepository.com/artifact/commons-cli/commons-cli
  public static final String commonsCli = "commons-cli:commons-cli:1.5.0";
  // Latest version -> https://mvnrepository.com/artifact/commons-io/commons-io
  public static final String commonsIo = "commons-io:commons-io:2.11.0";

  // Spring Boot libraries
  // Latest version -> https://mvnrepository.com/artifact/org.springframework.boot/spring-boot
  public static final String springBootVersion = "2.7.8";
  public static final String springBootDependencies =
      "org.springframework.boot:spring-boot-dependencies:" + springBootVersion;
  public static final String springBootStarter = "org.springframework.boot:spring-boot-starter";
  public static final String springBootConfigurationProcessor =
      "org.springframework.boot:spring-boot-configuration-processor:" + springBootVersion;
  public static final String springBootStarterTest =
      "org.springframework.boot:spring-boot-starter-test";

  // Latest version -> https://mvnrepository.com/artifact/org.libreoffice
  public static final String loVersion = "7.4.1";
  public static final String libreoffice = "org.libreoffice:libreoffice:" + loVersion;

  // Latest version -> https://mvnrepository.com/artifact/org.openoffice
  public static final String ooVersion = "4.1.2";
  public static final String ooJuh = "org.openoffice:juh:" + ooVersion;
  public static final String ooJurt = "org.openoffice:jurt:" + ooVersion;
  public static final String ooRidl = "org.openoffice:ridl:" + ooVersion;
  public static final String ooUnoil = "org.openoffice:unoil:" + ooVersion;

  // Spring Libraries
  public static final String springCore = "org.springframework:spring-core";
  public static final String springContext = "org.springframework:spring-context";
  public static final String springTest = "org.springframework:spring-test";

  // Latest version -> https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api
  public static final String javaxAnnotations = "javax.annotation:javax.annotation-api:1.3.2";

  // HTTP Components libraries
  public static final String httpcomponentsHttpcore = "org.apache.httpcomponents:httpcore";
  public static final String httpcomponentsHttpclient = "org.apache.httpcomponents:httpclient";
  public static final String httpcomponentsHttpmime = "org.apache.httpcomponents:httpmime";
  public static final String httpcomponentsFluenthc = "org.apache.httpcomponents:fluent-hc";

  // Logging Libraries
  public static final String slf4jApi = "org.slf4j:slf4j-api";
  public static final String slf4jLog4j = "org.slf4j:slf4j-log4j12";

  // Other Libraries
  public static final String gson = "com.google.code.gson:gson";

  // Test
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  public static final String assertj = "org.assertj:assertj-core";
  public static final String junitJupiterApi = "org.junit.jupiter:junit-jupiter-api";
  public static final String junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine";
  public static final String junitJupiterParams = "org.junit.jupiter:junit-jupiter-params";
  public static final String mockito = "org.mockito:mockito-core";
  public static final String mockitoInline = "org.mockito:mockito-inline";
  // We now have to use the wiremock standalone version since
  // jetty dependency is provided by spring boot
  // Latest version -> https://mvnrepository.com/artifact/com.github.tomakehurst/wiremock
  public static final String wiremock = "com.github.tomakehurst:wiremock-standalone:2.27.2";
}
