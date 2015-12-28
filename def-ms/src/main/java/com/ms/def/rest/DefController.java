package com.ms.def.rest;

import com.ms.event.AssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/def")
public class DefController {

    private static final Logger LOG = LoggerFactory.getLogger(DefController.class);

    @Autowired
    private EventDispatcher eventDispatcher;

    @RequestMapping(value = "/event/to/abc", method = RequestMethod.POST)
    @ResponseBody
    public  void postEventToDef(@RequestBody AssignmentEvent assignmentEvent,HttpServletRequest request){
        assignmentEvent.setModuleId(UUID.randomUUID().toString());
        System.out.println("Controller : Dispatching to Abc Actor===>"+assignmentEvent.getModuleId());
        eventDispatcher.dispatchToAbc(assignmentEvent);
    }
}
