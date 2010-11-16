package org.plugtree.server.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

public class SoapClient {

    private URL wsdlURL;
    private String namesspaceURI;
    private Map<String, Dispatch<SOAPMessage>> dispatchs = new HashMap<String, Dispatch<SOAPMessage>>();

    public SoapClient(String wsdl, String namesspaceURI) throws MalformedURLException {
        this.wsdlURL = new URL(wsdl);
        this.namesspaceURI = namesspaceURI;
    }

    public SOAPMessage invoke(String serviceName, String portName, String commandName, String prefix, String message) throws SOAPException {
        Dispatch<SOAPMessage> dispatch;
        if (!dispatchs.containsKey(serviceName)) {
            Service service = Service.create(wsdlURL, new QName(namesspaceURI, serviceName));
            dispatch = service.createDispatch(new QName(namesspaceURI, portName), SOAPMessage.class, Service.Mode.MESSAGE);
            dispatchs.put(serviceName, dispatch);
        }
        else {
            dispatch = dispatchs.get(serviceName);
        }

        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        QName payloadName = new QName(namesspaceURI, commandName, prefix);

        SOAPBodyElement executeElement = body.addBodyElement(payloadName);
        executeElement.addTextNode(message);
        
//        payloadName.ad
//        body.addTextNode(message);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            soapMessage.writeTo(baos);
            System.out.println(new String(baos.toByteArray()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dispatch.invoke(soapMessage);
    }

    public SOAPMessage invoke(String serviceName, String portName, String commandName, String message) throws SOAPException {
        return invoke(serviceName, portName, commandName, null, message);
    }

    public void setWsdlURL(URL wsdlURL) {
        this.wsdlURL = wsdlURL;
    }

    public URL getWsdlURL() {
        return wsdlURL;
    }

    public void setNamesspaceURI(String namesspaceURI) {
        this.namesspaceURI = namesspaceURI;
    }

    public String getNamesspaceURI() {
        return namesspaceURI;
    }

    public static void main(String[] args) throws MalformedURLException, SOAPException {
        String wsdl = "http://localhost:8383/drools-server/kservice/soap?wsdl";
        String namesspaceURI = "http://soap.jax.drools.org/";
        SoapClient client = new SoapClient(wsdl, namesspaceURI);

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"message\">\n";
        cmd += "      <org.test.Message>\n";
        cmd += "         <text>Helllo World</text>\n";
        cmd += "      </org.test.Message>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        SOAPMessage response = client.invoke("CommandExecutor", "CommandExecutorPort", "execute", "ns1", cmd);

        System.out.println(response.getSOAPBody().getValue());
    }

}
