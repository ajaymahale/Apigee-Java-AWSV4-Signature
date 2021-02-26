// AWSV4SignatureTest.java
//
// Test code for the AWS V4 signature  callout for Apigee. Uses TestNG.
// For full details see the Readme accompanying this source file.
//
// Copyright (c) 2016 Apigee Corp, 2017-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// @author: Dino Chiesa

package com.google.apigee.callouts;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import mockit.Mock;
import mockit.MockUp;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AWSV4SignatureTest {
  private static final String testDataDir = "src/test/resources";
  private static final boolean verbose = false;

  static class Config {
    public static final String service = "service";
    public static final String region = "us-east-1";
    public static final String accessKeyId = "AKIDEXAMPLE";
    public static final String secretAccessKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
  }

  MessageContext msgCtxt;
  Message message;
  ExecutionContext exeCtxt;

  @BeforeMethod()
  public void testSetup1() {

    msgCtxt =
        new MockUp<MessageContext>() {
          private Map<String, Object> variables;

          public void $init() {
            getVariables();
          }

          private Map<String, Object> getVariables() {
            if (variables == null) {
              variables = new HashMap<String, Object>();
            }
            return variables;
          }

          @Mock()
          public Object getVariable(final String name) {
            return getVariables().get(name);
          }

          @Mock()
          public boolean setVariable(final String name, final Object value) {
            if (verbose)
              System.out.printf(
                  "setVariable(%s) <= %s\n", name, (value != null) ? value : "(null)");
            getVariables().put(name, value);
            return true;
          }

          @Mock()
          public boolean removeVariable(final String name) {
            if (verbose) System.out.printf("removeVariable(%s)\n", name);
            if (getVariables().containsKey(name)) {
              variables.remove(name);
            }
            return true;
          }
        }.getMockInstance();

    exeCtxt = new MockUp<ExecutionContext>() {}.getMockInstance();

    message =
        new MockUp<Message>() {
          private Map<String, Object> variables;
          private Map<String, Object> headers;
          private Map<String, Object> qparams;
          private String content;

          public void $init() {
            getVariables();
          }

          private Map<String, Object> getVariables() {
            if (variables == null) {
              variables = new HashMap<String, Object>();
            }
            return variables;
          }

          private Map<String, Object> getHeaders() {
            if (headers == null) {
              headers = new HashMap<String, Object>();
            }
            return headers;
          }

          private Map<String, Object> getQparams() {
            if (qparams == null) {
              qparams = new HashMap<String, Object>();
            }
            return qparams;
          }

          @Mock()
          public String getContent() {
            return this.content;
          }

          @Mock()
          public void setContent(String content) {
            this.content = content;
          }

          @Mock()
          public Object getVariable(final String name) {
            return getVariables().get(name);
          }

          @Mock()
          public boolean setVariable(final String name, final Object value) {
            getVariables().put(name, value);
            return true;
          }

          @Mock()
          public boolean removeVariable(final String name) {
            if (getVariables().containsKey(name)) {
              variables.remove(name);
            }
            return true;
          }

          @Mock()
          public String getHeader(final String name) {
            String lowerName = name.toLowerCase();
            if (getHeaders().containsKey(lowerName)) {
              return ((List<String>) (getHeaders().get(lowerName))).get(0);
            }
            return null;
          }

          @Mock()
          public List<String> getHeaders(final String name) {
            String lowerName = name.toLowerCase();
            if (getHeaders().containsKey(lowerName)) {
              return (List<String>) getHeaders().get(lowerName);
            }
            return null;
          }

          @Mock()
          public boolean setHeader(final String name, final Object value) {
            String lowerName = name.toLowerCase();
            if (verbose) {
              System.out.printf(
                  "setHeader(%s) <= %s\n", lowerName, (value != null) ? value : "(null)");
            }
            if (getHeaders().containsKey(lowerName)) {
              if (!lowerName.equals("host")) {
                List<String> values = (List<String>) getHeaders().get(lowerName);
                values.add(value.toString());
              }
            }
            else {
              List<String> values = new ArrayList<String>();
              values.add(value.toString());
              getHeaders().put(lowerName, values);
            }
            return true;
          }

          @Mock()
          public Set<String> getHeaderNames() {
            return getHeaders().entrySet().stream()
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
          }

          @Mock()
          public Set<String> getQueryParamNames() {
            return getQparams().entrySet().stream()
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
          }

          @Mock()
          public String getQueryParam(final String name) {
            if (getQparams().containsKey(name)) {
              return ((List<String>) (getQparams().get(name))).get(0);
            }
            return null;
          }

          @Mock()
          public boolean setQueryParam(final String name, final Object value) {
            if (verbose) {
              System.out.printf(
                  "setQueryParam(%s) <= %s\n", name, (value != null) ? value : "(null)");
            }
            if (getQparams().containsKey(name)) {
              List<String> values = (List<String>) getQparams().get(name);
              values.add(value.toString());
            }
            else {
              List<String> values = new ArrayList<String>();
              values.add(value.toString());
              getQparams().put(name, values);
            }
            return true;
          }

          @Mock()
          public List<String> getQueryParams(final String name) {
            if (getQparams().containsKey(name)) {
              return (List<String>) getQparams().get(name);
            }
            return null;
          }

        }.getMockInstance();

    System.out.printf("=============================================\n");
  }

  private void reportThings(Map<String, String> props) {
    String test = props.get("testname");
    System.out.println("test  : " + test);
    String cipher = msgCtxt.getVariable("crypto_cipher");
    System.out.println("cipher: " + cipher);
    String action = msgCtxt.getVariable("crypto_action");
    System.out.println("action: " + action);
    String output = msgCtxt.getVariable("crypto_output");
    System.out.println("output: " + output);
    String keyHex = msgCtxt.getVariable("crypto_key_b16");
    System.out.println("key   : " + keyHex);
    String ivHex = msgCtxt.getVariable("crypto_iv_b16");
    System.out.println("iv    : " + ivHex);
    String aadHex = msgCtxt.getVariable("crypto_aad_b16");
    System.out.println("aad   : " + aadHex);
    String saltHex = msgCtxt.getVariable("crypto_salt_b16");
    System.out.println("salt  : " + saltHex);
    // Assert.assertNotNull(ivHex);
    // Assert.assertNotNull(output);
  }

  @DataProvider(name = "batch1")
  public static Object[][] getDataForBatch1() throws IOException, IllegalStateException {

    // @DataProvider requires the output to be a Object[][]. The inner
    // Object[] is the set of params that get passed to the test method.
    // So, if you want to pass just one param to the constructor, then
    // each inner Object[] must have length 1.

    // Path currentRelativePath = Paths.get("");
    // String s = currentRelativePath.toAbsolutePath().toString();
    // System.out.println("Current relative path is: " + s);

    // read in all the subdirectories in the test-data directory

    File dataDir = new File(testDataDir);
    if (!dataDir.exists()) {
      throw new IllegalStateException("no test data directory.");
    }

    File[] dirs = dataDir.listFiles(File::isDirectory);
    if (dirs.length == 0) {
      throw new IllegalStateException("no tests found.");
    }
    Arrays.sort(dirs);
    Function<File, Object[]> toTestCase =
        (dir) -> {
          try {
            String name = dir.getName();
            return new Object[] {new TestCase(name, Paths.get(testDataDir, name))};
          } catch (java.lang.Exception exc1) {
            exc1.printStackTrace();
            throw new RuntimeException("uncaught exception", exc1);
          }
        };

    return Arrays.stream(dirs).map(toTestCase).toArray(Object[][]::new);
  }

  @Test
  public void testDataProviders() throws IOException {
    Assert.assertTrue(getDataForBatch1().length > 0);
  }

  @Test(dataProvider = "batch1")
  public void tests(TestCase tc) throws Exception {
    System.out.printf("%s\n", tc.getTestName());

    msgCtxt.setVariable("source", message);
    tc.parseInput(message);
    Properties props = new Properties();
    // props.setProperty("debug", "true");
    props.setProperty("debug", "true");
    props.setProperty("sign-content-sha256", "false");
    props.setProperty("source", "source");
    props.setProperty("key", Config.accessKeyId);
    props.setProperty("secret", Config.secretAccessKey);
    props.setProperty("region", Config.region);
    props.setProperty("service", Config.service);
    props.setProperty("endpoint", "https://" + message.getHeader("host"));

    AWSV4Signature callout = new AWSV4Signature(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    ExecutionResult expectedResult = ExecutionResult.SUCCESS;

    // check result and output
    Assert.assertEquals(actualResult, expectedResult, tc.getTestName() + " result not as expected");
    Assert.assertNull(msgCtxt.getVariable("awsv4sig_error"), tc.getTestName());
    Assert.assertEquals(
        msgCtxt.getVariable("awsv4sig_creq"), tc.canonicalRequest(), tc.getTestName());
    Assert.assertEquals(msgCtxt.getVariable("awsv4sig_sts"), tc.stringToSign(), tc.getTestName());
    Assert.assertEquals(message.getHeader("authorization"), tc.authorization(), tc.getTestName());
  }
}
