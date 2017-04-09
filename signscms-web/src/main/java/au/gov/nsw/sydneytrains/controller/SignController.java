// SydneyTrains 2017

package au.gov.nsw.sydneytrains.controller;

import au.gov.nsw.sydneytrains.response.JsonResponse;
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

    @RequestMapping(value = "/", method = RequestMethod.GET)

    public @ResponseBody
    JsonResponse index() {
        JsonResponse res = new JsonResponse();
        res.setStatus("200");
        res.setResult("Welcome to SydneyTrains Digial Signage CMS");
        return res;
    }


}
