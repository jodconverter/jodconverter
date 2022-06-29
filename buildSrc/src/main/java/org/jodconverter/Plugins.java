package org.jodconverter;

public class Plugins {
  // Latest version -> https://github.com/checkstyle/checkstyle/releases/
  // 9.3 is the latest supporting java8, so stay with it until we set the minimal version to 11
  public static final String checkstyleVersion = "9.3";
  //public static final String checkstyleVersion = "10.3.1";
  // Latest version -> https://plugins.gradle.org/plugin/com.github.kt3k.coveralls
  public static final String coverallsVersion = "2.12.0";
  // Latest version -> https://plugins.gradle.org/plugin/nebula.integtest
  public static final String nebulaVersion = "9.6.3";
  // Latest version -> https://plugins.gradle.org/plugin/io.codearte.nexus-staging
  public static final String nexusStagingVersion = "0.30.0";
  // Latest version -> https://pmd.github.io
  public static final String pmdVersion = "6.47.0";
  // Latest version -> https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless
  public static final String spotlessVersion = "6.7.2";

  public static final String coveralls =
      "gradle.plugin.org.kt3k.gradle.plugin:coveralls-gradle-plugin:" + coverallsVersion;
  public static final String nebulaProject =
      "com.netflix.nebula:nebula-project-plugin:" + nebulaVersion;
  public static final String nexusStaging =
      "io.codearte.gradle.nexus:gradle-nexus-staging-plugin:" + nexusStagingVersion;
  public static final String spotless =
      "com.diffplug.spotless:spotless-plugin-gradle:" + spotlessVersion;
}
