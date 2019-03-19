package au.gov.nsw.sydneytrains.service;

import au.gov.nsw.sydneytrains.model.CnfView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by administrator on 17/4/17.
 */
@Service
public class CnfViewService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CnfViewService.class);

    public CnfView getDefaultView() {
        LOGGER.debug("Return Default View");
        final String[] images = new String[4];
        images[0] = "images/airport.jpg";
        images[1] = "images/airport.jpg";
        images[2] = "images/airport.jpg";
        images[3] = "images/airport.jpg";

        CnfView cnfView = new CnfView("1", "TownHall", "Platform 5/6", 960, 240,"1", images);
        return cnfView;
    }
}
