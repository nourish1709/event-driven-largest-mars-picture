package com.nourish1709.eventdrivenlargestmarspicture.controller;

import com.nourish1709.eventdrivenlargestmarspicture.controller.dto.FindPictureDto;
import com.nourish1709.eventdrivenlargestmarspicture.service.LargestMarsPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/mars/pictures/largest")
@RequiredArgsConstructor
public class LargestMarsPictureController {

    private final LargestMarsPictureService largestMarsPictureService;

    @PostMapping
    public ResponseEntity<Void> findLargestPictureLocation(@RequestBody FindPictureDto findPictureDto) {
        var pictureId = largestMarsPictureService.postFindPictureEvent(findPictureDto);

        var pictureUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/mars/pictures/largest/" + pictureId)
                .build().toUri();

        return ResponseEntity.ok()
                .location(pictureUri)
                .build();
    }

    @GetMapping(value = "/{commandId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getLargestPicture(@PathVariable String commandId) {
        var picture = largestMarsPictureService.getPicture(commandId);

        if (picture.isEmpty()) {
            return ResponseEntity.ok("No pictures found".getBytes(StandardCharsets.UTF_8));
        }
        return ResponseEntity.ok(picture.get());
    }
}
