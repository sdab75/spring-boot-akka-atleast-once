package com.ms.abc.rest;


import com.ms.event.AssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/abc")
public class AbcController {

    private static final Logger LOG = LoggerFactory.getLogger(AbcController.class);

    @Autowired
    private EventDispatcher eventDispatcher;

    @RequestMapping(value = "/event/to/def", method = RequestMethod.POST)
    @ResponseBody
    public  void postEventToDef(@RequestBody AssignmentEvent assignmentEvent,HttpServletRequest request){
        eventDispatcher.dispatchToDef(assignmentEvent);
    }
}
