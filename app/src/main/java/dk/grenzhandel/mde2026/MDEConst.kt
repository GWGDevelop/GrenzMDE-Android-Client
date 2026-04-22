package dk.grenzhandel.mde2026

data class MDEConst(
	val commandStart: String = "hello",
	val commandLogin: String = "login",
	val commandMainMenu: String = "buttonlist",
	val commandDBGrid: String = "grid",
	val commandMessage: String = "message",
	val commandPopup: String = "popup",
	val repeatToCloseTimeout: Long = 2000
)
