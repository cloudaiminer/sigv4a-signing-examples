package com.sigv4aSigning;

import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.net.*;
import java.util.List;
import java.util.Map;

public class SigV4Sign {

    private final AwsCredentials awsCredentials;
    public static final Integer PORT = 443;
    public static final String PROTOCOL_HTTPS = "https";

    // Create SigV4Sign using default credentials (e.g., instance profile, environment variables, etc.)
    public static SigV4Sign create() {
        return new SigV4Sign(DefaultCredentialsProvider.create().resolveCredentials());
    }

    // Create SigV4Sign with specified AWS credentials
    public static SigV4Sign create(AwsCredentials awsCredentials) {
        return new SigV4Sign(awsCredentials);
    }

    private SigV4Sign(AwsCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
    }

    // Method to sign request and return headers
    public Map<String, List<String>> getHeadersBasic(String serviceName,
                                                     Region region,
                                                     SdkHttpMethod method,
                                                     URI url) {

        SdkHttpFullRequest request = SdkHttpFullRequest.builder()
                .method(method)
                .encodedPath(url.getPath())
                .port(PORT)
                .protocol(PROTOCOL_HTTPS)
                .host(url.getHost())
                .build();

        // Create signer for SigV4
        Aws4Signer signer = Aws4Signer.create();

        // Sign the request
        Aws4SignerParams params = Aws4SignerParams.builder()
                .awsCredentials(awsCredentials)
                .signingName(serviceName)
                .signingRegion(region)
                .build();

        SdkHttpFullRequest signedRequest = signer.sign(request, params);

        // Return the signed headers
        return signedRequest.headers();
    }

    // Overloaded method for signing an already constructed request
    public Map<String, List<String>> getHeaders(SdkHttpFullRequest request,
                                                Region region,
                                                String serviceName) {

        Aws4Signer signer = Aws4Signer.create();

        Aws4SignerParams params = Aws4SignerParams.builder()
                .awsCredentials(awsCredentials)
                .signingName(serviceName)
                .signingRegion(region)
                .build();

        SdkHttpFullRequest signedRequest = signer.sign(request, params);

        return signedRequest.headers();
    }

    public AwsCredentials getAwsCredentials() {
        return awsCredentials;
    }
}

