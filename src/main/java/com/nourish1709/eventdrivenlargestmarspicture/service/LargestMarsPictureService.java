package com.nourish1709.eventdrivenlargestmarspicture.service;

import com.nourish1709.eventdrivenlargestmarspicture.controller.dto.FindPictureDto;

import java.util.Optional;

public interface LargestMarsPictureService {

    String postFindPictureEvent(FindPictureDto findPictureDto);

    Optional<byte[]> getPicture(String commandId);

    void findLargestPicture(String commandId);
}
