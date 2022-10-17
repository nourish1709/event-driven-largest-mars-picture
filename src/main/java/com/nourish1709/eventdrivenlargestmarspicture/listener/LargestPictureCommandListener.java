package com.nourish1709.eventdrivenlargestmarspicture.listener;

import com.nourish1709.eventdrivenlargestmarspicture.service.LargestMarsPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LargestPictureCommandListener {

    private final LargestMarsPictureService largestMarsPictureService;

    @RabbitListener(queues = "${queue.name}")
    public void listen(String commandId) {
        largestMarsPictureService.findLargestPicture(commandId);
    }
}
