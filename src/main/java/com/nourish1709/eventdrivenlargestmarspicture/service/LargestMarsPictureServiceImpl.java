package com.nourish1709.eventdrivenlargestmarspicture.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourish1709.eventdrivenlargestmarspicture.controller.dto.FindPictureDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.tomcat.util.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.tomcat.util.codec.binary.StringUtils.newStringUtf8;

@Service
@RequiredArgsConstructor
public class LargestMarsPictureServiceImpl implements LargestMarsPictureService {

    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final List<Picture> pictures = new ArrayList<>();

    @Value("${queue.name}")
    private String queueName;

    @Value("${nasa.api.url}")
    private String nasaUrl;

    @Value("${nasa.api.key}")
    private String nasaApiKey;

    @SneakyThrows
    @Override
    public String postFindPictureEvent(FindPictureDto findPictureDto) {
        final Optional<Picture> pictureOptional = pictures.stream()
                .filter(picture -> picture.findPictureDto.equals(findPictureDto))
                .findAny();
        if (pictureOptional.isEmpty()) {
            final byte[] commandId = Base64.getEncoder()
                    .encode(objectMapper.writeValueAsBytes(findPictureDto));
            rabbitTemplate.send(queueName, new Message(commandId));
            return newStringUtf8(commandId);
        }
        return pictureOptional.get().commandId;
    }

    @Override
    public Optional<byte[]> getPicture(String commandId) {
        return pictures.stream()
                .filter(picture -> picture.commandId.equals(commandId))
                .map(Picture::body)
                .findAny();
    }

    @Override
    @SneakyThrows
    public void findLargestPicture(String commandId) {
        final FindPictureDto findPictureDto = objectMapper.readValue(
                Base64.getDecoder().decode(getBytesUtf8(commandId)), FindPictureDto.class);

        final URI nasaUri = UriComponentsBuilder.fromHttpUrl(nasaUrl)
                .queryParam("api_key", nasaApiKey)
                .queryParam("sol", findPictureDto.sol())
                .queryParamIfPresent("camera", Optional.ofNullable(findPictureDto.camera()))
                .build()
                .toUri();
        final JsonNode allPhotos = restTemplate.getForObject(nasaUri, JsonNode.class);
        final Picture largestPicture = requireNonNull(allPhotos).findValues("img_src").parallelStream()
                .max(Comparator.comparingLong(imgSrc ->
                        restTemplate.headForHeaders(imgSrc.asText())
                                .getContentLength()))
                .map(largestImg -> new Picture(commandId, findPictureDto,
                        restTemplate.getForObject(largestImg.asText(), byte[].class)))
                .orElseThrow(IllegalArgumentException::new);

        pictures.add(largestPicture);
    }

    private record Picture(String commandId, FindPictureDto findPictureDto, byte[] body) {

    }

}
