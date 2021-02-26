# AWS V4 Signature callout

This directory contains the Java source code for a Java callout for Apigee
that constructs an AWS V4 Signature. It has no dependencies on the AWS SDK; instead it
follows the described signature process in the AWS Documentation.

## License

This code is Copyright (c) 2020-2021 Google LLC, and is released under the
Apache Source License v2.0. For information see the [LICENSE](LICENSE) file.

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## Using the Custom Policy

You do not need to build the Jar in order to use the custom policy.

When you use the policy to sign a request, AWS will accept the authenticated request.

The policy sets the appropriate headers in the AWS request:
- Authorization (with the appropriate signature)
- x-amz-date
- (optionally) x-amz-content-sha256


## Policy Configuration

You must configure it with your AWS Key and Secret, as well as the region, the service name, and the endpoint.
Example:

```
<JavaCallout name="JC-AWSSignV4">
    <Properties>
        <Property name="service">s3</Property>
        <Property name="endpoint">https://my-bucket-name.s3.amazonaws.com</Property>
        <Property name="region">us-west-1</Property>
        <Property name="key">{private.aws-key}</Property>
        <Property name="secret">{private.aws-secret-key}</Property>
        <Property name="source">outgoingAwsMessage</Property>
        <Property name="sign-content-sha256">true</Property> <!-- optional -->
    </Properties>
    <ClassName>com.google.apigee.callouts.AWSV4Signature</ClassName>
    <ResourceURL>java://apigee-callout-awsv4sig-20210225.jar</ResourceURL>
</JavaCallout>
```

The properties should be self-explanatory.
The optional property:

- sign-content-sha256.  When true, the policy adds a header `x-amz-content-sha256` which holds the SHA256 of the content (payload) for the message. It also includes that header in the signed headers.

For example, for a request like: `POST https://example.amazonaws.com/?Param1=value1`,

...assuming the date is 20150830T123600Z, the resulting Authorization header will be:

```
AWS4-HMAC-SHA256
Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request,
SignedHeaders=host;x-amz-date,
Signature=28038455d6de14eafc1f9222cf5aa6f1a96197d7deb8263271d420d138af7f11
```

(All on one line)

This is from a test case provided by Amazon.


## No Dependencies

There are no dependencies, other than the Java runtime.

## Building the Jar

You do not need to build the Jar in order to use the custom policy. The custom policy is
ready to use, with policy configuration. You need to re-build the jar only if you want
to modify the behavior of the custom policy. Before you do that, be sure you understand
all the configuration options - the policy may be usable for you without modification.

If you do wish to build the jar, you can use
[maven](https://maven.apache.org/download.cgi) to do so. The build requires
JDK8. Before you run the build the first time, you need to download the Apigee
Edge dependencies into your local maven repo.

Preparation, first time only: `./buildsetup.sh`

To build: `mvn clean package`

The Jar source code includes tests.

If you edit policies offline, copy [the jar file for the custom
policy](callout/target/apigee-callout-awsv4sig-20210225.jar) to your
apiproxy/resources/java directory.  If you don't edit proxy bundles offline,
upload that jar file into the API Proxy via the Apigee API Proxy Editor .


## Author

Dino Chiesa
godino@google.com