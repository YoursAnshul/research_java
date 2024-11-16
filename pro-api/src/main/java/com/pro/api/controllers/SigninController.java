package com.pro.api.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro.api.models.business.Claim;
import com.pro.api.models.business.SessionUserEmail;
import com.pro.api.models.dataaccess.User;
import com.pro.api.models.dataaccess.repos.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@Scope("session")
@RequestMapping("/signin")
public class SigninController {

    private static final Logger logger = LoggerFactory.getLogger(SigninController.class);
    @Autowired
    private ObjectMapper mapper;
    //	private Session hibernateSession;
    @Autowired
    UserRepository usersRepo;
    @Autowired
    DropDownValueRepository dvRepo;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private LocalDateTime currentTimestamp = LocalDateTime.now();
    private Boolean ClaimsReceived = false;
    private String RedirectUrl = "/";

    @Value("${spring.profiles.active}")
    private String environment;

    @Autowired
    private SessionUserEmail UserEmail;

    @ModelAttribute("UserEmail")
    public SessionUserEmail getUserEmail() {
        return UserEmail;
    }

    public SigninController() {
//		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
//		hibernateSession = sessionFactory.openSession();
    }

    @PostMapping("")
    public void index(@RequestParam(name = "wresult") String wresult,
                      @RequestParam(name = "wctx") Optional<String> wctx, HttpSession httpSession, ModelMap model,
                      HttpServletResponse response) throws Exception {
//        GeneralResponse response = new GeneralResponse();

        String wctxString = "";
        StringBuilder wctxResult = new StringBuilder();
        wctx.ifPresent(value -> wctxResult.append(value.toString()));
        wctxString = wctxResult.toString();

        try {
            List<Claim> claims = new ArrayList<Claim>();
            User user = new User();

            if(!activeProfile.equals("local")) {
                Boolean validSamlResponse = validateSamlResponse(wresult);
                // get SAML result
                claims = GetClaimsFromSAML(wresult);
                System.out.println("claims : " + claims);
                if (claims.size() > 0)
                    ClaimsReceived = true;

                // set claims as user variables and add a log entry
                for (Claim claim : claims) {
                    if (claim.getType().equals("emailaddress")) {
                        user.setEmailaddr(claim.getValue());
                    }

                    if (claim.getType().equals("eduPersonPrincipalName")) {
//                        user.setEppn(claim.getValue());

                        if (claim.getValue().contains("@duke.edu")) {
//                            user.setDukeuser(true);
                            user.setDempoid(claim.getValue().replace("@duke.edu", ""));
                        }
                    }

                    if (claim.getType().equals("givenname")) {
                        user.setFname(claim.getValue());
                    }

                    if (claim.getType().equals("surname")) {
                        user.setLname(claim.getValue());
                    }
                }

                HashMap<String, String> wctxParams = GetWctxParams(wctxString);
                for (Map.Entry<String, String> wctxParam : wctxParams.entrySet()) {
                    if (wctxParam.getKey().toLowerCase().equals("debug") || activeProfile.startsWith("local")) {
                        RedirectUrl = "https://localhost:8080/";
                    }
                }
            }else{
                //populate default user ...
                System.out.println("setting up default user for local profile");
                getDefaultUser(user);
            }
            UserLogin(user, httpSession, model);

//            response.Status = "Success";
//            response.Message = "Successfully logged in";
//            response.Subject = user;

//			return "redirect:" + RedirectUrl;
            response.sendRedirect(RedirectUrl);
        } catch (Exception ex) {
//			Logger.LogException(logsRepo, "RDA_System", ex);
            throw ex;
//            response.Status = "Failure";
//            response.Message = ex.Message;
        }


//        return response;
    }

    private void getDefaultUser(User user) {
        user.setEmailaddr("jeremiah.reed@duke.edu");
        user.setDempoid("jmr110");
        user.setFname("duke");
        user.setLname("duke");
    }

    // Method to validate the SAML response
    private boolean validateSamlResponse(String samlResponse) {
        try {
            // Parse the SAML response XML
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(samlResponse)));

            // Get the Signature element
            NodeList signatureElements = document.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
            if (signatureElements.getLength() == 0) {
                // Signature element not found, SAML response is not valid
                return false;
            }
            Element signatureElement = (Element) signatureElements.item(0);

            // Get the X509Certificate element
            NodeList certificateElements = signatureElement.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "X509Certificate");
            if (certificateElements.getLength() == 0) {
                // X509Certificate element not found, SAML response is not valid
                return false;
            }
            Element certificateElement = (Element) certificateElements.item(0);

            // Get the certificate value
            String certificateValue = certificateElement.getTextContent();

            // Validate the signature using the certificate
            // TODO: Implement the signature validation logic using the certificate

            // Return true if the signature is valid, false otherwise
            return true;
        } catch (Exception ex) {
            // Error occurred during validation, SAML response is not valid
            return false;
        }
    }

    /// <summary>
    /// Logs in an existing user and creates/logs in a new user
    /// </summary>
    /// <param name="user"></param>
    public void UserLogin(User user, HttpSession httpSession, ModelMap model) throws IOException, InterruptedException {
        User existingUser = null;
        if (activeProfile.equals(("local"))) {
            UserEmail.setUserEmail(user.getEmailaddr().toLowerCase());
            return;
        }

        try {
            existingUser = usersRepo.findFirstByEmailaddrIgnoreCase(user.getEmailaddr());
        } catch (Exception ex) {
        }

        //create user
        if (existingUser == null) {
            user.setEntryBy("RDA_System");
            user.setEntryDt(currentTimestamp);
            user.setModBy("RDA_System");
            user.setModDt(currentTimestamp);

            usersRepo.save(user);
            logger.info("saving user {}",user);
        }

        // login user
        // String emailAddress = (httpSession.getAttribute("UserEmail") == null ? "" :
        // httpSession.getAttribute("UserEmail").toString());
//		String emailAddress = "";
//		if (UserEmail != null) {
//			emailAddress = UserEmail.getUserEmail();
//		}
//        String loggedInUserEmail = (httpSession.getAttribute("UserEmail") == null ? "" : httpSession.getAttribute("UserEmail").toString());
        if (user.getEmailaddr() != null) {
            if (!user.getEmailaddr().trim().isEmpty()) {
                // model.addAttribute("UserEmail", user.getEmailaddress().toLowerCase());
                UserEmail.setUserEmail(user.getEmailaddr().toLowerCase());
                // UserEmail = new SessionUserEmail(emailAddress.toLowerCase());
                // httpSession.setAttribute("UserEmail", user.getEmailaddress());
            }
        }



    }

    /// <summary>
    /// Parses state values returned in the wctx variable alongside the claims
    /// </summary>
    /// <param name="wctx">The wctx parameter value from the SAML response</param>
    /// <returns>Dictionary of keys/values from wctx parameter value</returns>
    public HashMap<String, String> GetWctxParams(String wctx) {
        HashMap<String, String> wctxParams = new HashMap<String, String>();

        // if we don't have both & and = characters, then we don't have anything to work
        // with
        if (wctx.contains("&") && wctx.contains("=")) {
            String[] wctxParts = wctx.split("&");

            for (String wctxPart : wctxParts) {
                if (wctxPart.contains("=")) {
                    String[] nameAndValue = wctxPart.split("=");
                    if (nameAndValue.length > 1)
                        wctxParams.put(nameAndValue[0], nameAndValue[1]);
                }
            }
        }

        return wctxParams;
    }

    /// <summary>
    /// Parses a SAML XML response
    /// </summary>
    /// <param name="samlResponse"></param>
    /// <returns></returns>
    public List<Claim> GetClaimsFromSAML(String samlResponse)
            throws SAXException, IOException, ParserConfigurationException {
        List<Claim> claims = new ArrayList<Claim>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(samlResponse)));
        Node authenticationStatement = document.getElementsByTagName("saml:AuthenticationStatement").item(0);
        if (authenticationStatement != null) {
            String authenticationMethod = authenticationStatement.getAttributes().getNamedItem("AuthenticationMethod")
                    .getTextContent().trim();
            String authenticationInstant = authenticationStatement.getAttributes().getNamedItem("AuthenticationInstant")
                    .getTextContent().trim();
            claims.add(new Claim("AuthenticationMethod", authenticationMethod));
            claims.add(new Claim("AuthenticationInstant", authenticationInstant));
        }
        Node subject = document.getElementsByTagName("saml:Subject").item(0);

        if (subject != null) {
            claims.add(new Claim("NameIdentifier", subject.getTextContent().trim()));
        }

        NodeList claimElements = document.getElementsByTagName("saml:Attribute");

        for (Integer i = 0; i < claimElements.getLength(); i++) {
            String type = claimElements.item(i).getAttributes().getNamedItem("AttributeName").getTextContent().trim();
            String value = claimElements.item(i).getTextContent().trim();
            claims.add(new Claim(type, value));
        }

        return claims;
    }

}
