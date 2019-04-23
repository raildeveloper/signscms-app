package au.gov.nsw.sydneytrains.xml;

import au.gov.nsw.sydneytrains.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class description:
 *
 * CnfXml provides access to the XML configuration file.
 * It provides a read function to get the configuration data into a CnfData object.
 */
public class CnfXml {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String FILENAME = "SCMS_Config.xml";
	private final ClassLoader classLoader = getClass().getClassLoader();
	final File cnfFile = new File(classLoader.getResource(FILENAME).getFile());

	private CnfData cnfData;

	public CnfXml() {
		// Empty
	}

	public CnfData getCnfData() {
		return cnfData;
	}

	/*
	 * Load the contents of the file.
	 * Return false if there is an issue with the file.
	 */
	public boolean loadFile() {

		boolean result = false;

		System.out.println("Checking configuration file: "+ cnfFile.getAbsolutePath());

		if (!cnfFile.exists()) {
			System.out.println("ERROR: config file doesn't exist");
		} else if (!cnfFile.isFile()) {
			System.out.println("ERROR: config file is not a file");
		} else {
			System.out.println("SUCCESS: config file exists");

			// Force read from file. Need to clean the currently read in data first
			cnfData = new CnfData();

			result = readFile();
		}
		return result;
	}

	/*
	 * Read from the file. If anything fails, false is returned.
	 */
	private boolean readFile() {
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(cnfFile);

			// normalize text representation
			doc.getDocumentElement().normalize();
			System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());
			doc.getDocumentElement().getAttributeNode("version");

			NodeList rootNode = doc.getElementsByTagName("SCMS_DATA");
			if (rootNode.getLength() < 1) {
				System.out.println("ERROR in config data file: there is no SCMS_DATA element");
				return false;
			}
			String version = rootNode.item(0).getAttributes().getNamedItem("version").getNodeValue();
			final String dataVersion = "0.1";
			if (!version.equals(dataVersion)) {
				System.out.println("ERROR in config data file: required version is " + dataVersion);
				System.out.println("Version " + version + " is not supported.");
				return false;
			}

			NodeList listOfDevices = doc.getElementsByTagName("DEVICE");
			int totalDevices = listOfDevices.getLength();
			System.out.println("Total no of devices : " + totalDevices);

			HashMap<String, CnfDevice> devices = new HashMap<>();
			for (int s = 0; s < listOfDevices.getLength(); s++) {

				Node node = listOfDevices.item(s);
				if (node.getNodeType() == Node.ELEMENT_NODE) {

					String id = node.getAttributes().getNamedItem("id").getNodeValue();
					String name = node.getAttributes().getNamedItem("name").getNodeValue();
					String description = node.getAttributes().getNamedItem("description").getNodeValue();
					String pi_name = node.getAttributes().getNamedItem("PI_Name").getNodeValue();
					String pixHor = node.getAttributes().getNamedItem("pixHor").getNodeValue();
					String pixVer = node.getAttributes().getNamedItem("pixVer").getNodeValue();

					devices.put(id, new CnfDevice(id, name, description, pi_name,
							Integer.valueOf(pixHor), Integer.valueOf(pixVer)));
				}
			}
			cnfData.setDevices(devices);

			NodeList listOfViews = doc.getElementsByTagName("VIEW");
			int totalViews = listOfViews.getLength();
			System.out.println("Total no of views : " + totalViews);

			HashMap<String, CnfView> views = new HashMap<>();
			for (int s = 0; s < listOfViews.getLength(); s++) {

				Node node = listOfViews.item(s);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String id = node.getAttributes().getNamedItem("id").getNodeValue();
					String name = node.getAttributes().getNamedItem("name").getNodeValue();
					String description = node.getAttributes().getNamedItem("description").getNodeValue();
					String pixHor = node.getAttributes().getNamedItem("pixHor").getNodeValue();
					String pixVer = node.getAttributes().getNamedItem("pixVer").getNodeValue();
					String asso_device = node.getAttributes().getNamedItem("associatedDevice").getNodeValue();

					Element element = (Element) node;
					NodeList listOfImages = ((Element) node).getElementsByTagName("IMAGE");
					Element iElement = (Element) listOfImages.item(0);
					String[] images = new String[listOfImages.getLength()];
					for (int i = 0; i < listOfImages.getLength(); i++) {

						Node iNode = listOfImages.item(i);
						images[i] = iNode.getAttributes().getNamedItem("name").getNodeValue();
					}

					views.put(id, new CnfView(id, name, description,
							Integer.valueOf(pixHor), Integer.valueOf(pixVer), asso_device, images));
				}
			}
			cnfData.setViews(views);

			NodeList listOfSections = doc.getElementsByTagName("SECTION");
			int totalSections = listOfSections.getLength();
			System.out.println("Total no of sections : " + totalSections);

			HashMap<String, CnfSection> sections = new HashMap<>();
			for (int s = 0; s < totalSections; s++) {
				Node node = listOfSections.item(s);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String sectionId = node.getAttributes().getNamedItem("sectionId").getNodeValue();
					String deviceId = node.getAttributes().getNamedItem("deviceId").getNodeValue();
					String positionId = node.getAttributes().getNamedItem("positionId").getNodeValue();
					String vOffset = node.getAttributes().getNamedItem("vOffset").getNodeValue();

					// A section is invalid if the deviceId doesn't exist
					if (null == cnfData.getDeviceById(deviceId)) {
						System.out.println("ERROR in SECTION config data: deviceId " + deviceId + " sectionId " + sectionId);
					} else {
						sections.put(deviceId, new CnfSection(sectionId, deviceId, positionId, vOffset));
					}
				}
			}
			cnfData.setSections(sections);
			return true;

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	/*
	 * NOT USED. Also has not been updated (yet).
	 * Write to the file. If anything fails, false is returned.
	 */
	private boolean writeToFile() {

		BufferedWriter bw = null;
		FileWriter fw = null;

		try {
			String content = getAsXmlString();

			fw = new FileWriter(FILENAME + "w");
			bw = new BufferedWriter(fw);
			bw.write(content);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

				return true;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	public String getAsXmlString() {

		String strXml = "<?xml version=\"1.0\"?>\n";
		strXml += ("<SCMS_DATA version=\"0.1\">\n");

		strXml += ("<DEVICES>\n");
		HashMap<String, CnfDevice> devices = cnfData.getAllDevices();
		for (Map.Entry<String, CnfDevice> entry : devices.entrySet()) {
			String strDevice;
			strDevice = "<DEVICE"
					+ " id=\"" + entry.getValue().getDeviceId() + "\""
					+ " name=\"" + entry.getValue().getDeviceName() + "\""
					+ " description=\"" + entry.getValue().getDescription() + "\""
					+ " pixHor=\"" + entry.getValue().getPixelsHorizontal() + "\""
					+ " pixVer=\"" + entry.getValue().getPixelsVertical() + "\""
					+ " />\n";

			strXml += strDevice;
		}
		strXml += ("</DEVICES>\n");

		strXml += ("<VIEWS>\n");
		HashMap<String, CnfView> views = cnfData.getAllViews();
		for (Map.Entry<String, CnfView> entry : views.entrySet()) {
			String strView;
			strView = "<VIEW"
					+ " id=\"" + entry.getValue().getViewId() + "\""
					+ " name=\"" + entry.getValue().getViewName() + "\""
					+ " description=\"" + entry.getValue().getDescription() + "\""
					+ " pixHor=\"" + entry.getValue().getPixelsHorizontal() + "\""
					+ " pixVer=\"" + entry.getValue().getPixelsVertical() + "\""
					+ " >\n";

			String[] images = entry.getValue().getImages();
			if (images.length > 0) {
				strView += "<IMAGES>\n";
				for (int i = 0; i < images.length; i++) {
					String strImage;
					strImage = "<IMAGE"
							+ " name=\"" + images[i] + "\""
							+ " />\n";
					strView += strImage;
				}
				strView += "</IMAGES>\n";
			}
			strXml += strView;
			strXml += "</VIEW>\n";
		}
		strXml += ("</VIEWS>\n");

		strXml += ("<SECTIONS>\n");
		HashMap<String, CnfSection> sections = cnfData.getAllSections();
		for (Map.Entry<String, CnfSection> entry : sections.entrySet()) {
			String strSection;
			strSection = "<SECTION"
					+ " sectionId=\"" + entry.getValue().getSectionId() + "\""
					+ " deviceId=\"" + entry.getValue().getDeviceId() + "\""
					+ " positionId=\"" + entry.getValue().getPositionId() + "\""
					+ " vOffset=\"" + entry.getValue().getV_offset() + "\""
					+ " />\n";
			strXml += strSection;
		}
		strXml += ("</SECTIONS>\n");

		strXml += ("</SCMS_DATA>\n");

		return strXml;
	}

}
