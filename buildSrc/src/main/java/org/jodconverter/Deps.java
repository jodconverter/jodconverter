package org.jodconverter;

public class Deps {

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  // VERSIONS
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~

  // Compile
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  // Latest version -> https://mvnrepository.com/artifact/org.checkerframework/checker-qual
  public static final String checkerVersion = "3.5.0";

  // Main
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  // Latest version -> https://mvnrepository.com/artifact/commons-cli/commons-cli
  public static final String commonsCliVersion = "1.4";
  // Latest version -> https://mvnrepository.com/artifact/commons-io/commons-io
  public static final String commonsIoVersion = "2.7";
  // Latest version -> https://mvnrepository.com/artifact/org.libreoffice
  public static final String loVersion = "6.4.3";
  // Latest version -> https://mvnrepository.com/artifact/org.openoffice
  public static final String ooVersion = "4.1.2";
  // Latest version -> https://mvnrepository.com/artifact/org.springframework.boot/spring-boot
  public static final String springBootVersion = "2.3.3.RELEASE";

  // Test
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  // Latest version -> https://mvnrepository.com/artifact/org.powermock/powermock-api-mockito2
  public static final String powermockVersion = "2.0.7";
  // Latest version -> https://mvnrepository.com/artifact/com.github.tomakehurst/wiremock
  public static final String wiremockVersion = "2.27.1";

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  // DEPENDENCIES
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~

  // Compile
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~
  public static final String checkerQual = "org.checkerframework:checker-qual:" + checkerVersion;

  // Main
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~

  // Apache Commons Libraries
  public static final String commonsCli = "commons-cli:commons-cli:" + commonsCliVersion;
  public static final String commonsIo = "commons-io:commons-io:" + commonsIoVersion;

  // Spring Boot libraries
  public static final String springBootDependencies =
      "org.springframework.boot:spring-boot-dependencies:" + springBootVersion;
  public static final String springBootStarter = "org.springframework.boot:spring-boot-starter";
  public static final String springBootConfigurationProcessor =
      "org.springframework.boot:spring-boot-configuration-processor";
  public static final String springBootStarterTest =
      "org.springframework.boot:spring-boot-starter-test";

  public static final String loJuh = "org.libreoffice:juh:" + loVersion;
  public static final String loJurt = "org.libreoffice:jurt:" + loVersion;
  public static final String loRidl = "org.libreoffice:ridl:" + loVersion;
  public static final String loUnoil = "org.libreoffice:unoil:" + loVersion;

  public static final String ooJuh = "org.openoffice:juh:" + ooVersion;
  public static final String ooJurt = "org.openoffice:jurt:" + ooVersion;
  public static final String ooRidl = "org.openoffice:ridl:" + ooVersion;
  public static final String ooUnoil = "org.openoffice:unoil:" + ooVersion;

  // Spring Libraries
  public static final String springCore = "org.springframework:spring-core";
  public static final String springContext = "org.springframework:spring-context";
  public static final String springTest = "org.springframework:spring-test";

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
  public static final String junitVintageEngine = "org.junit.vintage:junit-vintage-engine";
  public static final String mockito = "org.mockito:mockito-core";
  public static final String powermockJunit =
      "org.powermock:powermock-module-junit4:" + powermockVersion;
  public static final String powermockMockito =
      "org.powermock:powermock-api-mockito2:" + powermockVersion;
  // We now have to use the wiremock standalone version since
  // jetty dependency is provided by spring boot
  public static final String wiremock =
      "com.github.tomakehurst:wiremock-standalone:" + wiremockVersion;
}
