package bugdemo;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This appears to be a bug in EclipseLink MOXy whereby if an XmlAnyElement has no namespace then a duplicate
 * default namepace tag is defined when emitting to an XMLStreamWriter
 */
public class Testcase
{
	private static final String INNER_DOC = "<x xmlns=\"\"><foo></foo></x>";

	@Test //expected=org.xml.sax.SAXParseException.class
	public void demoBug() throws Exception
	{
		MyXmlAnyElementType el = new MyXmlAnyElementType();
		el.someXmlBlock = parse(INNER_DOC).getDocumentElement();

		StringWriter sw = new StringWriter();
		final Marshaller marshaller = getMarshaller(MyXmlAnyElementType.class);
		marshaller.marshal(el, XMLOutputFactory.newInstance().createXMLStreamWriter(sw));

		System.out.println("Should be invalid XML: " + sw);

		// N.B. This blows up if invalid XML was output
		parse(sw.toString());
	}


	@Test
	public void testWorkaround() throws Exception
	{
		MyXmlAnyElementType el = new MyXmlAnyElementType();
		el.someXmlBlock = parse(INNER_DOC).getDocumentElement();

		StringWriter sw = new StringWriter();
		final Marshaller marshaller = getMarshaller(MyXmlAnyElementType.class);
		marshaller.marshal(el, new DuplicateNSFilteringXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(sw)));

		// N.B. This blows up if invalid XML was output
		parse(sw.toString());
	}


	@Test
	public void demoBugOnlyImpactsDefaultNamespace() throws Exception
	{
		// Test being in a non-default namespace
		MyXmlAnyElementType el = new MyXmlAnyElementType();
		el.someXmlBlock = parse("<x xmlns=\"urn:foo\" />").getDocumentElement();

		StringWriter sw = new StringWriter();
		final Marshaller marshaller = getMarshaller(MyXmlAnyElementType.class);
		marshaller.marshal(el, XMLOutputFactory.newInstance().createXMLStreamWriter(sw));

		// N.B. This blows up if invalid XML was output
		parse(sw.toString());
	}


	@Test
	public void demoBugOnlyImpactsDefaultNamespaceIfDeclared() throws Exception
	{
		// Test implicitly being in the default namespace
		MyXmlAnyElementType el = new MyXmlAnyElementType();
		el.someXmlBlock = parse("<x />").getDocumentElement();

		StringWriter sw = new StringWriter();
		final Marshaller marshaller = getMarshaller(MyXmlAnyElementType.class);

		marshaller.marshal(el, XMLOutputFactory.newInstance().createXMLStreamWriter(sw));

		// N.B. This blows up if invalid XML was output
		parse(sw.toString());
	}


	private Document parse(final String xml) throws ParserConfigurationException, IOException, SAXException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);
		factory.setExpandEntityReferences(false);
		factory.setXIncludeAware(false);
		return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
	}


	private Marshaller getMarshaller(Class<?> clazz) throws JAXBException
	{
		final javax.xml.bind.JAXBContext ctx = JAXBContext.newInstance(clazz);

		return ctx.createMarshaller();
	}
}
