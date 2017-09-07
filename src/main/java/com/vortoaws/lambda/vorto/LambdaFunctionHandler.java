package com.vortoaws.lambda.vorto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.vorto.service.mapping.DataInput;
import org.eclipse.vorto.service.mapping.DataMapperBuilder;
import org.eclipse.vorto.service.mapping.IDataMapper;
import org.eclipse.vorto.service.mapping.IMappingSpecification;
import org.eclipse.vorto.service.mapping.ditto.DittoMapper;
import org.eclipse.vorto.service.mapping.ditto.DittoOutput;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaFunctionHandler implements RequestStreamHandler {
	
	private static String baseUrl;
	private static String username;
	private static String password;
	private static String apiToken;

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

		// Initialize env variables
		intializeEnvVariables();
		
		// Convert the inputStream to String		
		String input = IOUtils.toString(inputStream);

		context.getLogger().log("Input: " + input);
		
		// Retrieve the serialNumber from json
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = objectMapper.readValue(input, new TypeReference<HashMap<String,Object>>(){});
		String thingId = (String) map.get("serialNumber");
		
		context.getLogger().log("\nthingId: " + thingId);

		// Mapping code starts here
		DataMapperBuilder builder = IDataMapper.newBuilder();
		// add Loader to load Information Model and AWS IoT Button mappings from
		// the Vorto Repository
		builder.withSpecification(IMappingSpecification.newBuilder().modelId("devices.aws.button.AWSIoTButton:1.0.0")
				.key("awsiotbutton").build());

		DittoMapper mapper = builder.buildDittoMapper();

		DittoOutput mappedDittoOutput = mapper.map(DataInput.newInstance().fromJson(input));
		
		// Serialize mapped Eclipse Ditto format to JSON
		context.getLogger().log("\nmappedDittoOutput: " + mappedDittoOutput.toJson());
		
		// Send dittoOutput to things
		ThingsProxy thingsProxy = ThingsProxy.create(baseUrl, username, password, apiToken);
		try {
			thingsProxy.updateFeatures(thingId, mappedDittoOutput.toJson(), context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		

		outputStream.write(mappedDittoOutput.toJson().getBytes());

	}
	
	public static void intializeEnvVariables() {
		baseUrl = System.getenv("baseUrl");
		username = System.getenv("username");
		password = System.getenv("password");
		apiToken = System.getenv("apiToken");
	}

}
