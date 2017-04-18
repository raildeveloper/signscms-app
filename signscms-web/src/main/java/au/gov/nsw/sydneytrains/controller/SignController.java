// SydneyTrains 2017

package au.gov.nsw.sydneytrains.controller;

import au.gov.nsw.sydneytrains.model.CnfView;
import au.gov.nsw.sydneytrains.service.CnfViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;


/**
 * Created by administrator on 9/4/17.
 */

@Controller
public class SignController {

    private final Logger logger = LoggerFactory.getLogger(SignController.class);
    private final CnfViewService cnfViewService;

    @Autowired
    public SignController(CnfViewService cnfViewService) {
        this.cnfViewService = cnfViewService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Map<String, Object> model) {

        logger.info("index() is executed!");
        System.out.println("index executed");
        return "sign";
    }


    @RequestMapping(value = "/device/sign", method = RequestMethod.GET)

    public @ResponseBody
    CnfView index() {
        System.out.println("HELLO WORLD");
        final String[] images = new String[4];
        images[0] = "images/airport.jpg";
        images[1] = "images/airport.jpg";
        images[2] = "images/airport.jpg";
        images[3] = "images/airport.jpg";

        CnfView cnfView = new CnfView(1, "TownHall", "Platform 5/6", 960, 240, images);

        return cnfView;
    }


}
