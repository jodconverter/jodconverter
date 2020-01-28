package org.jodconverter;

public class Plugins {
  // Latest version -> https://sourceforge.net/projects/checkstyle/files/checkstyle
  public static final String checkstyleVersion = "8.29";
  // Latest version -> https://plugins.gradle.org/plugin/com.github.kt3k.coveralls
  public static final String coverallsVersion = "2.9.0";
  // Latest version -> https://plugins.gradle.org/plugin/nebula.integtest
  public static final String nebulaVersion = "7.0.5";
  // Latest version -> https://plugins.gradle.org/plugin/io.codearte.nexus-staging
  public static final String nexusStagingVersion = "0.21.2";
  // Latest version -> https://pmd.github.io
  public static final String pmdVersion = "6.21.0";
  // Latest version -> https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless
  public static final String spotlessVersion = "3.27.0";

  public static final String coveralls =
      "gradle.plugin.org.kt3k.gradle.plugin:coveralls-gradle-plugin:" + coverallsVersion;
  public static final String nebulaProject =
      "com.netflix.nebula:nebula-project-plugin:" + nebulaVersion;
  public static final String nexusStaging =
      "io.codearte.gradle.nexus:gradle-nexus-staging-plugin:" + nexusStagingVersion;
  public static final String spotless =
      "com.diffplug.spotless:spotless-plugin-gradle:" + spotlessVersion;
}
