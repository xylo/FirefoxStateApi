package de.endrullis.firefoxstateapi

/**
	* Demo app for the FirefoxApi.
	*
	* @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
	*/
object FirefoxApiDemoApp extends App {

	val firefox = new FirefoxStateApi

	// print all profiles
	println("profiles: " + firefox.profiles)

	// print selected profile
	println("selected profile: " + firefox.profile)

	// update state
	firefox.updateState()

	// print session info
	println("session: " + firefox.session)  // shortcut for firefox.state.session

	// print all cookies
	println("cookies: " + firefox.cookies)  // shortcut for firefox.state.cookies

	// print all windows
	println("windows: " + firefox.windows)  // shortcut for firefox.state.windows

	// print selected window
	println("selected window: " + firefox.selectedWindow)  // shortcut for firefox.state.selectedWindow

	// print selected tab
	println("selected tab: " + firefox.selectedTab)  // shortcut for firefox.state.selectedWindow.selectedTab

	// print selected tab entry
	println("selected tab entry: " + firefox.selectedTabEntry)  // shortcut for firefox.state.selectedWindow.selectedTab.selectedEntry

	// print title and url of the selected tab entry
	println("title of selected tab entry: " + firefox.selectedTabEntry.title)
	println("URL of selected tab entry: " + firefox.selectedTabEntry.url)

}
