/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.Caideyipi;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OpcUaArgumentsChecker {

  private static final String SCRIPT_HINT =
      "java -jar iotdb_opc_server-0.0.1-jar-with-dependencies.jar";

  private static final int CODE_ERROR = 1;
  private static final int MAX_HELP_CONSOLE_WIDTH = 88;

  private static final String USERNAME_ARGS = "u";
  private static final String USER_KEY = "user";
  private static final String USERNAME_KEY = "username";
  private static final String USER_DEFAULT_VALUE = "root";

  private static final String PASSWORD_ARGS = "pw";
  private static final String PASSWORD_KEY = "password";
  private static final String PASSWORD_DEFAULT_VALUE = "root";

  private static final String TCP_BIND_PORT_KEY = "tcp_port";
  private static final int TCP_BIND_PORT_DEFAULT_VALUE = 12686;

  private static final String HTTPS_BIND_PORT_KEY = "https_port";
  private static final int HTTPS_BIND_PORT_DEFAULT_VALUE = 8443;

  private static final String SECURITY_DIR_KEY = "security_dir";
  private static final String SECURITY_DIR_DEFAULT_VALUE =
      System.getProperty("user.home") + File.separatorChar + "iotdb_opc_server_security";

  private static final String ENABLE_ANONYMOUS_ACCESS_KEY = "enable_anonymous_access";
  private static final boolean ENABLE_ANONYMOUS_ACCESS_DEFAULT_VALUE = true;

  private static final String SECURITY_POLICY_KEY = "security_policy";
  public static final String SECURITY_POLICY_NONE_VALUE = "NONE";
  public static final String SECURITY_POLICY_BASIC_128_RSA_15_VALUE = "BASIC128RSA15";
  public static final String SECURITY_POLICY_BASIC_256_VALUE = "BASIC256";
  public static final String SECURITY_POLICY_BASIC_256_SHA_256_VALUE = "BASIC256SHA256";
  public static final String SECURITY_POLICY_AES128_SHA256_RSAOAEP_VALUE = "AES128_SHA256_RSAOAEP";
  public static final String SECURITY_POLICY_AES256_SHA256_RSAPSS_VALUE = "AES256_SHA256_RSAPSS";
  private static final Set<SecurityPolicy> SECURITY_POLICY_DEFAULT_VALUE =
      new HashSet<>(
          Arrays.asList(
              SecurityPolicy.Basic256Sha256,
              SecurityPolicy.Aes128_Sha256_RsaOaep,
              SecurityPolicy.Aes256_Sha256_RsaPss));

  static final String HELP_ARGS = "help";

  private static CommandLine commandLine;

  static OpcUaServerBuilder parseArgs(final String[] args) {
    final Options options = createOptions();
    final HelpFormatter hf = new HelpFormatter();
    hf.setWidth(MAX_HELP_CONSOLE_WIDTH);
    commandLine = null;

    if (args == null || args.length == 0) {
      return new OpcUaServerBuilder()
          .setTcpBindPort(TCP_BIND_PORT_DEFAULT_VALUE)
          .setHttpsBindPort(HTTPS_BIND_PORT_DEFAULT_VALUE)
          .setUser(USER_DEFAULT_VALUE)
          .setPassword(PASSWORD_DEFAULT_VALUE)
          .setSecurityDir(SECURITY_DIR_DEFAULT_VALUE)
          .setEnableAnonymousAccess(ENABLE_ANONYMOUS_ACCESS_DEFAULT_VALUE)
          .setSecurityPolicies(SECURITY_POLICY_DEFAULT_VALUE);
    }
    final boolean continues = parseCommandLine(options, args, hf);
    if (!continues) {
      System.exit(CODE_ERROR);
    }

    return new OpcUaServerBuilder()
        .setUser(getStringOptionsOrDefault(USER_KEY, USER_DEFAULT_VALUE))
        .setPassword(getStringOptionsOrDefault(PASSWORD_KEY, PASSWORD_DEFAULT_VALUE))
        .setSecurityDir(getStringOptionsOrDefault(SECURITY_DIR_KEY, SECURITY_DIR_DEFAULT_VALUE))
        .setTcpBindPort(getIntOptionOrDefault(TCP_BIND_PORT_KEY, TCP_BIND_PORT_DEFAULT_VALUE))
        .setHttpsBindPort(getIntOptionOrDefault(HTTPS_BIND_PORT_KEY, HTTPS_BIND_PORT_DEFAULT_VALUE))
        .setEnableAnonymousAccess(getEnableAnonymousAccessOrDefault())
        .setSecurityPolicies(parseSecurityPolicies());
  }

  private static Options createOptions() {
    final Options options = new Options();
    final Option help = new Option(HELP_ARGS, false, "Display help information. (optional)");
    help.setRequired(false);
    options.addOption(help);

    return options
        .addOption(
            Option.builder(TCP_BIND_PORT_KEY)
                .argName(TCP_BIND_PORT_KEY)
                .hasArg()
                .optionalArg(true)
                .desc(
                    String.format(
                        "TCP Port. Default is %s. (optional)", TCP_BIND_PORT_DEFAULT_VALUE))
                .build())
        .addOption(
            Option.builder(HTTPS_BIND_PORT_KEY)
                .argName(HTTPS_BIND_PORT_KEY)
                .hasArg()
                .optionalArg(true)
                .desc(
                    String.format(
                        "Https Port. Default is %s. (optional)", HTTPS_BIND_PORT_DEFAULT_VALUE))
                .build())
        .addOption(
            Option.builder(USERNAME_ARGS)
                .longOpt(USER_KEY)
                .argName(USERNAME_KEY)
                .hasArg()
                .optionalArg(true)
                .desc(String.format("User name, default is %s. (optional)", USER_DEFAULT_VALUE))
                .required()
                .build())
        .addOption(
            Option.builder(PASSWORD_ARGS)
                .longOpt(PASSWORD_KEY)
                .argName(PASSWORD_KEY)
                .hasArg(true)
                .optionalArg(true)
                .desc(String.format("Password. Default is %s. (optional)", PASSWORD_DEFAULT_VALUE))
                .build())
        .addOption(
            Option.builder(SECURITY_DIR_KEY)
                .argName(SECURITY_DIR_KEY)
                .hasArg(true)
                .optionalArg(true)
                .desc(
                    String.format(
                        "Security directory of OPC Server. Default is %s. (optional)",
                        SECURITY_DIR_DEFAULT_VALUE))
                .build())
        .addOption(
            Option.builder(ENABLE_ANONYMOUS_ACCESS_KEY)
                .argName(ENABLE_ANONYMOUS_ACCESS_KEY)
                .hasArg(true)
                .optionalArg(true)
                .desc(
                    String.format(
                        "Whether to enable anonymous access of this server. Default is %s. (optional)",
                        ENABLE_ANONYMOUS_ACCESS_DEFAULT_VALUE))
                .build())
        .addOption(
            Option.builder(SECURITY_POLICY_KEY)
                .argName(SECURITY_POLICY_KEY)
                .hasArg(true)
                .optionalArg(true)
                .desc(
                    String.format(
                        "The supported security policies of the server for the clients to connect. Default are %s. (optional)",
                        SECURITY_POLICY_DEFAULT_VALUE))
                .build());
  }

  private static boolean parseCommandLine(
      final Options options, final String[] newArgs, final HelpFormatter hf) {
    try {
      final CommandLineParser parser = new DefaultParser();
      commandLine = parser.parse(options, newArgs);
      if (commandLine.hasOption(HELP_ARGS)) {
        hf.printHelp(SCRIPT_HINT, options, true);
        return false;
      }
    } catch (final ParseException e) {
      System.out.println("For more information, please check the following hint.");
      hf.printHelp(SCRIPT_HINT, options, true);
      return false;
    }
    return true;
  }

  private static String getStringOptionsOrDefault(final String arg, final String defaultValue) {
    final String str = commandLine.getOptionValue(arg);
    return Objects.nonNull(str) ? str : defaultValue;
  }

  private static int getIntOptionOrDefault(final String arg, final int defaultValue) {
    final String str = commandLine.getOptionValue(arg);
    return Objects.nonNull(str) ? Integer.parseInt(str) : defaultValue;
  }

  private static boolean getEnableAnonymousAccessOrDefault() {
    final String str =
        commandLine.getOptionValue(OpcUaArgumentsChecker.ENABLE_ANONYMOUS_ACCESS_KEY);
    return Objects.nonNull(str)
        ? Boolean.parseBoolean(str)
        : OpcUaArgumentsChecker.ENABLE_ANONYMOUS_ACCESS_DEFAULT_VALUE;
  }

  private static Set<SecurityPolicy> parseSecurityPolicies() {
    final String str = commandLine.getOptionValue(OpcUaArgumentsChecker.SECURITY_POLICY_KEY);
    return Objects.nonNull(str)
        ? Arrays.stream(OpcUaArgumentsChecker.SECURITY_POLICY_KEY.replace(" ", "").split(","))
            .map(OpcUaArgumentsChecker::getSecurityPolicy)
            .collect(Collectors.toSet())
        : SECURITY_POLICY_DEFAULT_VALUE;
  }

  private static SecurityPolicy getSecurityPolicy(final String securityPolicy) {
    switch (securityPolicy.toUpperCase()) {
      case SECURITY_POLICY_NONE_VALUE:
        return SecurityPolicy.None;
      case SECURITY_POLICY_BASIC_128_RSA_15_VALUE:
        return SecurityPolicy.Basic128Rsa15;
      case SECURITY_POLICY_BASIC_256_VALUE:
        return SecurityPolicy.Basic256;
      case SECURITY_POLICY_BASIC_256_SHA_256_VALUE:
        return SecurityPolicy.Basic256Sha256;
      case SECURITY_POLICY_AES128_SHA256_RSAOAEP_VALUE:
        return SecurityPolicy.Aes128_Sha256_RsaOaep;
      case SECURITY_POLICY_AES256_SHA256_RSAPSS_VALUE:
        return SecurityPolicy.Aes256_Sha256_RsaPss;
      default:
        throw new UnsupportedOperationException(
            "The security policy can only be 'None', 'Basic128Rsa15', 'Basic256', 'Basic256Sha256', 'Aes128_Sha256_RsaOaep' or 'Aes256_Sha256_RsaPss'.");
    }
  }
}
