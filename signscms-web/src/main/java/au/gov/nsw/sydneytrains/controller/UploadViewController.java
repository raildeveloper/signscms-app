package au.gov.nsw.sydneytrains.controller;

import au.gov.nsw.sydneytrains.dao.CnfDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfData;
import au.gov.nsw.sydneytrains.xml.CnfXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;

@Controller
public class UploadViewController {
    private static final Logger logger = LoggerFactory
            .getLogger(UploadViewController.class);

    @RequestMapping(value = "/uploadView", method = RequestMethod.POST)
    public @ResponseBody
    ModelAndView uploadFileHandler(HttpServletRequest request,
                                   @RequestParam("devicePicker") String device, @RequestParam("viewPicker") String view,
                                   @RequestParam("file") MultipartFile file) {


        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String fileN = file.getOriginalFilename();

                String fileName2 = request.getSession().getServletContext().getRealPath("/");
                File imgDir = new File(fileName2 + File.separator + "resources" + File.separator + "images");


                File serverFile = new File(imgDir.getAbsolutePath()
                        + File.separator + fileN);
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();


                File xMLFile = new File(fileName2 + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "SCMS_Config.xml");

                // Update XML file
                if (updateXML(device, view, fileN, serverFile.getAbsolutePath(), xMLFile)) {
                    if (loadXmlConfigFile()) {
                        System.out.println("Init view success");
                    } else {
                        System.out.println("Init view failure");
                    }


                } else {
                    logger.info("Update XML Failure");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("deviceList");
    }

    private boolean updateXML(String device, String view, String fileName, String filePath, File xMLFilePath) {
        boolean result = false;
        logger.info("Update XML Failure");
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xMLFilePath);

            File nImagePath = new File(File.separator + "resources" + File.separator + "images" + File.separator + fileName);
            doc.getDocumentElement().normalize();

            NodeList listOfViews = doc.getElementsByTagName("VIEW");

            int totalViews = listOfViews.getLength();

            for (int s = 0; s < listOfViews.getLength(); s++) {

                Node node = listOfViews.item(s);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String id = node.getAttributes().getNamedItem("id").getNodeValue();
                    if (id.equals(view)) {

                        Element element = (Element) node;
                        NodeList listOfImages = ((Element) node).getElementsByTagName("IMAGE");
                        Element iElement = (Element) listOfImages.item(0);
                        String[] images = new String[listOfImages.getLength()];
                        for (int i = 0; i < listOfImages.getLength(); i++) {
                            Node iNode = listOfImages.item(i);
                            images[i] = iNode.getAttributes().getNamedItem("name").getNodeValue();

                            iNode.getAttributes().getNamedItem("name").setNodeValue(nImagePath.getAbsolutePath());

                            // Wrtite to XML File
                            // write the content into xml file
                            TransformerFactory transformerFactory = TransformerFactory.newInstance();
                            Transformer transformer = transformerFactory.newTransformer();
                            DOMSource source = new DOMSource(doc);
                            StreamResult newXML = new StreamResult(xMLFilePath);
                            transformer.transform(source, newXML);
                            result = true;
                            break;
                        }
                    }
                }


            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    /*
     * Read the configuration data from the XML configuration file.
     */
    private boolean loadXmlConfigFile() {

        // Load the configuration file into a CnfData instance
        CnfXml cnfXml = new CnfXml();
        if (!cnfXml.loadFile()) {
            return false;
        }

        CnfData newData = cnfXml.getCnfData();

        newData.setLinks(CnfData.getInstance().getAllLinks());
        newData.validateAndFix();

        // Store the loaded data into the H2Database
        CnfDao cnfDao = new CnfDao();
        cnfDao.setDbConnection(H2DatabaseAccess.getDbConnection());

        try {
            cnfDao.setAllCnfDevices(newData.getAllDevices());
            cnfDao.setAllCnfViews(newData.getAllViews());
            cnfDao.setAllCnfViewLinks(newData.getAllViews());
            cnfDao.setAllCnfSections(newData.getAllSections());
            cnfDao.setAllCnfLinks(newData.getAllLinks());
        } catch (SQLException e) {
            System.out.println("ERROR, failed to write XML Config file data to H2 database");
            return false;
        }

        // Update the singleton CnfData
        CnfData.getInstance().setDevices(newData.getAllDevices());
        CnfData.getInstance().setViews(newData.getAllViews());
        CnfData.getInstance().setSections(newData.getAllSections());
        CnfData.getInstance().setLinks(newData.getAllLinks());

        return true;
    }

}
