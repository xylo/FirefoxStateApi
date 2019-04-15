package de.endrullis.firefoxstateapi

import java.io.File
import java.nio.file.Files

import de.endrullis.firefoxstateapi.FirefoxState._

import scala.io.Source
import scala.util.matching.Regex

/**
 * An API to access the state of the Firefox web browser.
 *
 * @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
 */
class FirefoxStateApi {

	private val ProfileNamePattern: Regex = "Path=([^.]*)[.]default".r

	private val homeDir: String = System.getProperty("user.home")
	private val profilesFile    = new File(s"$homeDir/.mozilla/firefox/profiles.ini")

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
	def recoveryLz4File = new File(s"$homeDir/.mozilla/firefox/$profile.default/sessionstore-backups/recovery.jsonlz4")
	def recoveryFile = new File(s"$homeDir/.mozilla/firefox/$profile.default/sessionstore-backups/recovery.js")

	/** The cached state of the recovery.js file. */
	var state: FirefoxState = _

	/** Updates the cached state of the recovery.js and returns it. */
	def updateState(): FirefoxState = {
		if (recoveryLz4File.exists()) {
			import net.jpountz.lz4.LZ4Factory
			val factory = LZ4Factory.fastestInstance

			val compressedBytes = Files.readAllBytes(recoveryLz4File.toPath)
			val decompressedBytes = new Array[Byte](10000000)

			val decompressor = factory.safeDecompressor
			val decompressedLength = decompressor.decompress(compressedBytes, 8+4, compressedBytes.length-8-4, decompressedBytes, 0)

			val content = new String(decompressedBytes, 0, decompressedLength, "UTF-8")

			state = FirefoxState.parse(content)
		} else {
			state = FirefoxState.parse(recoveryFile)
		}
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
