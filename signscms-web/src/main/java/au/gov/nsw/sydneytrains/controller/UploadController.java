package au.gov.nsw.sydneytrains.controller;

import au.gov.nsw.sydneytrains.dao.ContactUsDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Controller
public class UploadController {

    private static final Logger logger = LoggerFactory
            .getLogger(UploadController.class);

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody
    ModelAndView uploadFileHandler(@RequestParam("name") String name, @RequestParam("email") String email,
                                   @RequestParam("mobile") String mobile,
                                   @RequestParam("subject") String subject,
                                   @RequestParam("message") String message,
                                   @RequestParam("file") MultipartFile file) {

        boolean result = false;
        if (name != null) {
            try {
                ContactUsDao contactUsDao = H2DatabaseAccess.getContactUsDao();
                if (!file.isEmpty()) {
                    byte[] bytes = file.getBytes();
                    String fileN = file.getOriginalFilename();

                    //System.out.println(fileN + ":--> " + file.getContentType());
                    // Creating the directory to store file
                    String rootPath = System.getProperty("catalina.home");
                    File dir = new File(rootPath + File.separator + "tmpFiles");
                    //System.out.println("File path" + dir.getAbsolutePath());
                    if (!dir.exists())
                        dir.mkdirs();

                    // Create the file on server
                    File serverFile = new File(dir.getAbsolutePath()
                            + File.separator + fileN);
                    BufferedOutputStream stream = new BufferedOutputStream(
                            new FileOutputStream(serverFile));
                    stream.write(bytes);
                    stream.close();

                    logger.info("Server File Location="
                            + serverFile.getAbsolutePath());
                    result = contactUsDao.insertContactUsForm(name, email, message, subject, message, "OPEN", fileN, serverFile);
                } else {
                    result = contactUsDao.insertContactUsForm(name, email, message, subject, message, "OPEN");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        // Send email and change status




        return new ModelAndView("deviceList");





   /* public ModelAndView uploadFile(@RequestParam("file") MultipartFile multipartFile){
        long fileSize = multipartFile.getSize();
        String fileName = multipartFile.getOriginalFilename();
        ModelAndView modelAndView = new ModelAndView("upload-success");
        if(true){
            Map<String, Object> modelMap = new HashMap<>();
            modelMap.put("fileName", fileName);
            modelMap.put("fileSize", fileSize);
            modelAndView.addAllObjects(modelMap);
            return modelAndView;
        }
        return new ModelAndView("upload-failed");
    }*/
    }
}
