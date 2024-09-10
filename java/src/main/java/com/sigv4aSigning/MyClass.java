package com.sigv4aSigning;
import com.sigv4aSigning.SigV4Sign;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;


import software.amazon.awssdk.auth.signer.AwsSignerExecutionAttribute;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MyClass {
    public static void main(String[] args) {
        SigV4Sign sigV4ASign = SigV4Sign.create(DefaultCredentialsProvider.create().resolveCredentials());

        String url = "https://hdx9uf10u5.execute-api.eu-west-2.amazonaws.com/prod/message";
        URI uri = URI.create(url);
        String serviceName = "execute-api";
	Region region = Region.EU_WEST_2;
        //RegionScope globalRegion = RegionScope.create("eu-west-2");
        SdkHttpMethod method = SdkHttpMethod.GET;

        SdkHttpFullRequest request = SdkHttpFullRequest.builder()
                .method(method)
                .encodedPath(uri.toString())
                .port(SigV4Sign.PORT)
                .protocol(SigV4Sign.PROTOCOL_HTTPS)
                .host(uri.getHost())
                .build();

        ExecutionAttributes ea = new ExecutionAttributes();
        ea.putAttribute(AwsSignerExecutionAttribute.AWS_CREDENTIALS, sigV4ASign.getAwsCredentials());
        ea.putAttribute(AwsSignerExecutionAttribute.SERVICE_SIGNING_NAME, serviceName);

        //Map<String, List<String>> headers = sigV4ASign.getHeaders(request, ea, globalRegion);
        Map<String, List<String>> headers = sigV4ASign.getHeadersBasic(
                serviceName,
                region,
                method,
                uri
        );
        try {
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            headers.forEach((key, value) -> con.setRequestProperty(key, value.get(0)));
            int responseCode = con.getResponseCode();
            BufferedReader in;
	    if (responseCode >= 200 && responseCode < 300) {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String line;
            StringBuffer sb = new StringBuffer();

            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            in.close();
            con.disconnect();
            System.out.println(sb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
