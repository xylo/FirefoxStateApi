package de.endrullis.firefoxstateapi

import java.io.{File, StringReader}
import java.util
import javax.xml.bind.annotation._
import javax.xml.bind.annotation.adapters.{XmlAdapter, XmlJavaTypeAdapter}

import scala.annotation.meta.field
import scala.collection.JavaConverters._
import scala.io.Source

import FirefoxState._

/**
	* Utilities for reading the Firefox state from the recovery.js file.
	*
	* @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
	*/
object FirefoxState {

	type xmlElement = XmlElement@field
	type xmlElements = XmlElements@field
	type xmlTypeAdapter = XmlJavaTypeAdapter@field

	def parse(file: File): FirefoxState = {
		val jsonString = Source.fromFile(file, "UTF-8").getLines().mkString("\n")
		parse(jsonString)
	}

	def parse(json: String): FirefoxState = {
		import com.sun.jersey.api.json._

		val config = JSONConfiguration.natural.rootUnwrapping(true).build()
		val context = new JSONJAXBContext(config, classOf[FirefoxState])

		context.createJSONUnmarshaller().unmarshalFromJSON(new StringReader(json), classOf[FirefoxState])
	}

	class TabListAdapter extends ListAdapter[Tab]

	class ListAdapter[A] extends XmlAdapter[util.List[A], List[A]] {
		def marshal(v: List[A]): util.ArrayList[A] = new util.ArrayList(v.asJava)
		def unmarshal(v: util.List[A]): List[A] = v.asScala.toList
	}

	class StringOptionAdapter extends OptionAdapter[String](null, "")

	class OptionAdapter[A](nones: A*) extends XmlAdapter[A, Option[A]] {
		def marshal(v: Option[A]): A = v.getOrElse(nones(0))
		def unmarshal(v: A): Option[A] = if (nones contains v) None else Some(v)
	}

	/**
		* Firefox window.
		*
		* @param busy
		* @param screenX       window x position on the screen
		* @param screenY       window y position on the screen
		* @param height        window height
		* @param width         window width
		* @param selectedTabNo number of the selected tab
		* @param sizeMode      window size mode (e.g. maximized)
		* @param _tabs         all tabs of this window
		*/
	@XmlAccessorType(XmlAccessType.FIELD)
	case class Window(busy: Boolean,
	                  screenX: Int,
	                  screenY: Int,
	                  height: Int,
	                  width: Int,
	                  @xmlElement(name = "selected")
	                  selectedTabNo: Int,
	                  @xmlElement(name = "sizemode")
	                  sizeMode: String,
	                  //@xmlTypeAdapter(value = classOf[TabListAdapter])
	                  @xmlElements(value = Array(new xmlElement(name = "tabs", `type` = classOf[Tab])))
	                  _tabs: util.List[Tab]
	                 ) {
		private def this() = this(false, -1, -1, -1, -1, -1, null, null)

		@XmlTransient
		lazy val tabs: List[Tab] = _tabs.asScala.toList

		@XmlTransient
		lazy val selectedTab: Tab = tabs.maxBy(_.lastAccessed)
	}

	/**
		* Firefox tab.
		*
		* @param selectedEntryNo number of the selected tab entry
		* @param lastAccessed    time of the last access
		* @param hidden
		* @param image           icon image of this tab
		* @param _entries        all entries of this tab
		*/
	@XmlAccessorType(XmlAccessType.FIELD)
	case class Tab(@xmlElement(name = "index")
	               selectedEntryNo: Int,
	               lastAccessed: Long,
	               hidden: Boolean,
	               image: String,
	               @xmlElements(value = Array(new xmlElement(name = "entries", `type` = classOf[Entry])))
	               _entries: util.List[Entry]) {
		private def this() = this(-1, -1, false, null, null)

		/** All entries of this tab. */
		@XmlTransient
		lazy val entries: List[Entry] = _entries.asScala.toList

		/** The selected entry. */
		@XmlTransient
		lazy val selectedEntry: Entry = _entries.get(selectedEntryNo - 1)
	}

	/**
		* Firefox tab entry.
		*
		* @param ID       entry ID
		* @param charset  character set
		* @param title    title
		* @param url      URL
		* @param referrer referrer
		*/
	@XmlAccessorType(XmlAccessType.FIELD)
	case class Entry(ID: Long,
	                 charset: String,
	                 title: String,
	                 url: String,
	                 referrer: String) {
		private def this() = this(-1, null, null, null, null)
	}

	/**
		* Firefox cookie.
		*
		* @param expiry expiration date
		* @param host   host
		* @param name
		* @param path   cookie path
		* @param value  cookie value
		* @param httponly
		*/
	@XmlAccessorType(XmlAccessType.FIELD)
	case class Cookie(expiry: Long, host: String, name: String, path: String, value: String, httponly: Boolean) {
		private def this() = this(-1, null, null, null, null, false)
	}

	/**
		* Firefox session.
		*
		* @param lastUpdate    time of the last update
		* @param recentCrashes number of recent crashes
		* @param startTime     start time of the session
		*/
	@XmlAccessorType(XmlAccessType.FIELD)
	case class Session(lastUpdate: Long, recentCrashes: Int, startTime: Long) {
		private def this() = this(-1, -1, -1)
	}

}

/**
	* Firefox state read from the recovery.js file.
	*
	* @param version
	* @param selectedWindowNo selected window number
	* @param _windows         all Firefox windows
	* @param _cookies         all Firefox cookies
	* @param session          the current Firefox session
	* @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
	*/
@XmlRootElement(name = "recoveryJs")
@XmlAccessorType(XmlAccessType.FIELD)
case class FirefoxState(@xmlElements(value = Array(new xmlElement(`type` = classOf[String])))
                        version: util.List[String],
                        @xmlElement(name = "selectedWindow", required = true)
                        selectedWindowNo: Int,
                        //@xmlTypeAdapter(classOf[ListAdapter[Window]])
                        @xmlElements(value = Array(new xmlElement(name = "windows", `type` = classOf[Window])))
                        _windows: util.List[Window],
                        @xmlElements(value = Array(new xmlElement(name = "cookies", `type` = classOf[Cookie])))
                        _cookies: util.List[Cookie],
                        session: Session) {
	private def this() = this(null, -1, null, null, null)

	/** All Firefox windows. */
	@XmlTransient
	lazy val windows: List[Window] = _windows.asScala.toList

	/** The selected Firefox window. */
	@XmlTransient
	lazy val selectedWindow: Window = _windows.get(selectedWindowNo - 1)

	/** All cookies. */
	@XmlTransient
	lazy val cookies: List[Cookie] = _cookies.asScala.toList
}
