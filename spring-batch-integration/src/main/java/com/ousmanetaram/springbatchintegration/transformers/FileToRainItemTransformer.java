package com.ousmanetaram.springbatchintegration.transformers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ousmanetaram.springbatchintegration.model.Location;
import com.ousmanetaram.springbatchintegration.model.PinPoint;
import com.ousmanetaram.springbatchintegration.model.RainItem;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

public class FileToRainItemTransformer
		implements GenericTransformer<Message<File>, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(
			FileToRainItemTransformer.class);

	private static final String HEADER_FILE_NAME = "file_name";


	@Override
	public String transform(Message<File> message) {
		String fileName = (String) message	.getHeaders()
										.get(HEADER_FILE_NAME);
		try {
			NetcdfFile netCdfFile = NetcdfFile.open(message	.getPayload()
															.getPath());

			Optional<String> timeStep = findAttributeByName(netCdfFile, "TimeStep");
			Optional<String> title = findAttributeByName(netCdfFile, "title");

			RainItem rainItem = new RainItem();
			title	.map(this::findRainfallType)
					.ifPresent(rainItem::setRainType);
			timeStep.map(this::convertToLocalDateTime)
					.ifPresent(rainItem::setEventDate);
			rainItem.setName(fileName)
					.setPinpoints(retrievePinpoints(netCdfFile, rainItem.getRainType(),
							rainItem.getEventDate()));
			Gson gson = new GsonBuilder()	.setPrettyPrinting()
											.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
											.create();

			return gson.toJson(rainItem);
		} catch (IOException e) {
			LOGGER.error(" Error while tranforming file {} ", fileName, e);
		}
		return null;
	}


	private Set<PinPoint> retrievePinpoints(NetcdfFile netCdfFile, String rainfallType,
			LocalDateTime eventDate) throws IOException {
		Set<PinPoint> pinpoints = new HashSet<>();
		Array longitudeArray = netCdfFile	.findVariable("Longitude")
											.read();
		Array latitudeArray = netCdfFile.findVariable("Latitude")
										.read();
		Array genericTargetArray = netCdfFile	.findVariable(rainfallType)
												.read();
		Index index = genericTargetArray.getIndex();
		for (int i = 0; i < latitudeArray.getSize(); i++) {
			for (int j = 0; j < longitudeArray.getSize(); j++) {
				PinPoint pinPoint = new PinPoint();
				Location location = new Location(
						String.valueOf(longitudeArray.getFloat(j)),
						String.valueOf(latitudeArray.getFloat(i)));
				pinPoint.setLocation(location);
				pinPoint.setRainValue(genericTargetArray.getDouble(index.set(i, j)));
				pinPoint.setEventDate(eventDate);
				pinPoint.setRainEvent(rainfallType);
				pinpoints.add(pinPoint);
			}
		}
		return pinpoints;
	}

	private String findRainfallType(String titre) {
		if (titre	.toLowerCase()
					.contains("inondation"))
			return "LameRuiss";
		if (titre	.toLowerCase()
					.contains("pluie"))
			return "Rainfall";
		if (titre	.toLowerCase()
					.contains("alerte"))
			return "Alerte";
		return "";
	}

	private Optional<String> findAttributeByName(NetcdfFile netCdfFile,
			String attributeName) {
		return netCdfFile	.getGlobalAttributes()
							.stream()
							.filter(attribute -> StringUtils.equals(attribute.getName(),
									attributeName))
							.map(Attribute::getStringValue)
							.findFirst();
	}

	private LocalDateTime convertToLocalDateTime(String strDatetime) {
		String strCompleDateTime = strDatetime.replaceAll("_", " ");
		return LocalDateTime.parse(strCompleDateTime,
				DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
	}


	class LocalDateAdapter implements JsonSerializer<LocalDateTime> {

		@Override
		public JsonElement serialize(LocalDateTime dateTime, Type arg1,
				JsonSerializationContext arg2) {
			return new JsonPrimitive(
					dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		}

	}
}
