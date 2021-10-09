package bugdemo;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bugdemo.MyXmlAnyElementType")
public class MyXmlAnyElementType
{
	@XmlAnyElement(lax = false)
	public Element someXmlBlock;
}
