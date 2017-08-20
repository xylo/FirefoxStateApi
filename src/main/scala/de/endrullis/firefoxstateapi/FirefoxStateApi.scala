package de.endrullis.firefoxstateapi

import java.io.File

import scala.io.Source
import scala.util.matching.Regex

import FirefoxState._

/**
	* An API to access the state of the Firefox web browser.
	*
	* @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
	*/
class FirefoxStateApi {

	private val ProfileNamePattern: Regex = "Path=([^.]*)[.]default".r

	private val homeDir: String = System.getProperty("user.home")
	private val profilesFile = new File(s"$homeDir/.mozilla/firefox/profiles.ini")

	/** All profiles. */
	val profiles: List[String] = {
		if (profilesFile.exists()) {
			Source.fromFile(profilesFile, "UTF-8").getLines().collect {
				case ProfileNamePattern(name) => name
			}.toList
		} else {
			Nil
		}
	}

	/** Selected profile. */
	var profile: String = profiles.head

	/** The recovery.js file. */
	def recoveryFile = new File(s"$homeDir/.mozilla/firefox/$profile.default/sessionstore-backups/recovery.js")

	/** The cached state of the recovery.js file. */
	var state: FirefoxState = _

	/** Updates the cached state of the recovery.js and returns it. */
	def updateState(): FirefoxState = {
		state = FirefoxState.parse(recoveryFile)
		state
	}

	/** Firefox session. */
	def session: Session = state.session

	/** All Firefox cookies. */
	def cookies: List[Cookie] = state.cookies

	/** All Firefox windows. */
	def windows: List[Window] = state.windows

	/** The selected Firefox window. */
	def selectedWindow: Window = state.selectedWindow

	/** The selected Firefox tab. */
	def selectedTab: Tab = selectedWindow.selectedTab

	/** The selected Firefox tab entry. */
	def selectedTabEntry: Entry = selectedTab.selectedEntry

}
