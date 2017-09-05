package com.vortoaws.lambda.vorto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.eclipse.vorto.service.mapping.DataInput;
import org.eclipse.vorto.service.mapping.DataMapperBuilder;
import org.eclipse.vorto.service.mapping.IDataMapper;
import org.eclipse.vorto.service.mapping.IMappingSpecification;
import org.eclipse.vorto.service.mapping.ditto.DittoMapper;
import org.eclipse.vorto.service.mapping.ditto.DittoOutput;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class LambdaFunctionHandler implements RequestStreamHandler {

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

		// Convert the inputStream to String		
		String input = IOUtils.toString(inputStream);

		context.getLogger().log("Input: " + input);

		// Mapping code starts here
		DataMapperBuilder builder = IDataMapper.newBuilder();
		// add Loader to load Information Model and AWS IoT Button mappings from
		// the Vorto Repository
		builder.withSpecification(IMappingSpecification.newBuilder().modelId("devices.aws.button.AWSIoTButton:1.0.0")
				.key("awsiotbutton").build());

		DittoMapper mapper = builder.buildDittoMapper();

		DittoOutput mappedDittoOutput = mapper.map(DataInput.newInstance().fromJson(input));

		// Serialize mapped Eclipse Ditto format to JSON
		context.getLogger().log(mappedDittoOutput.toJson());

		outputStream.write(mappedDittoOutput.toJson().getBytes());

	}

}
