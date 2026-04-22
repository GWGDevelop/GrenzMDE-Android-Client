package dk.grenzhandel.mde2026

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import kotlin.system.exitProcess

class ActMDEStart : MDEActivity() {
	private val STORAGE_PERMISSION_REQUEST = 1234
	lateinit var BtnConnect: Button
	lateinit var BtnCloseApp: ImageButton
	lateinit var BtnInfo: ImageButton
	lateinit var BoxButtons: LinearLayout
	lateinit var TxTitle: TextView
	lateinit var TxServer: TextView
	lateinit var TxPort: TextView
	var SelectedCompany: String = ""
	private val CompanyButtons = mutableListOf<MaterialButton>()
	private var BackPressed = false

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_mdestart)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		BtnConnect = findViewById<Button>(R.id.BtnConnect)
		BtnCloseApp = findViewById<ImageButton>(R.id.BtnCloseApp)
		BtnInfo =	findViewById<ImageButton>(R.id.BtnInfo)
		TxTitle = findViewById<TextView>(R.id.LbStartupTitle)
		TxServer = findViewById<TextView>(R.id.TxServer)
		TxPort = findViewById<TextView>(R.id.TxPort)
		BoxButtons = findViewById<LinearLayout>(R.id.BxCompanyButtons)

		onBackPressedDispatcher.addCallback(this) { OnClickBack() }
		BtnConnect.setOnClickListener(this::OnClickBtnConnect)
		BtnCloseApp.setOnClickListener(this::OnClickClose)
		BtnInfo.setOnClickListener(this::OnClickShowHelp)

		if (!Config.appConfigFileExists) {
			if (ensureStoragePermission())
				MDEConfigData.SaveConfig(Config, this)
			else
				Toast.makeText(this, "Konfiguration NICHT gespeichert - Berechtigung wurde verweigert.", Toast.LENGTH_LONG).show()
		}
		TxTitle.text = Config.MDEStartupTitle
		TxServer.text = Config.srvrAddress
		TxPort.text = Config.srvrPort.toString()
		AddCompanyButtons(Config.NAVAvailableCompanies, Config.NAVDefaultCompany)
	} //fun onCreate

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	override fun dispatchKeyEvent(event: KeyEvent): Boolean
	{
		if (event.action == KeyEvent.ACTION_DOWN) {
			when (event.keyCode) {
				in KeyEvent.KEYCODE_0 .. KeyEvent.KEYCODE_9 ->
				{
					val Idx = event.keyCode - KeyEvent.KEYCODE_0
					if (Idx in CompanyButtons.indices)
						CompanyButtons[Idx].performClick()
				}

				KeyEvent.KEYCODE_ESCAPE  ->
					BtnCloseApp.performClick()
				else ->
					return super.dispatchKeyEvent(event)
			}
			return true // Event verbraucht
		}
		return super.dispatchKeyEvent(event)
	}//fun dispatchKeyEvent

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun OnClickBack()
	{
		if (BackPressed)
		{
			finishAffinity()
			exitProcess(0)
		}
		else {
			BackPressed = true
			Toast.makeText(this@ActMDEStart, "Zum Beenden nochmals drücken", Toast.LENGTH_LONG).show()
			Handler(Looper.getMainLooper()).postDelayed({BackPressed=false}, Const.repeatToCloseTimeout)
		}
	}//fun OnClickBack

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun OnClickClose(aView: View)
	{
		OnClickBack()
	}//fun OnClickClose

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun	OnClickShowHelp(aView: View)
	{
		ActMessage.Show(this, getString(R.string.scannerHelp), "Scanner-Einstellungen", true, "Verstanden")
	}//fun	OnClickShowHelp

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun OnClickBtnConnect(aView: View)
	{
		if ( SelectedCompany == "")
			Toast.makeText(this@ActMDEStart, "Kein Mandant ausgewählt. Kann nicht verbinden.", Toast.LENGTH_LONG).show()
		else
		{
			Config.NAVSelectedCompany = SelectedCompany
			MDETcpClient.Connect(this, Config.srvrAddress, Config.srvrPort,
												Config.TCPTerminator, Config.TCPTimeout, ::ProcessConnectResult)
		}
	}//fun OnClickBtnConnect

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun AddCompanyButtons(Companies: List<String>, Default: String)
	{
		Companies.forEach { Company ->
			val Btn = MaterialButton(ContextThemeWrapper(this, R.style.CompanyButtonStyle), null,
				com.google.android.material.R.attr.materialButtonStyle)
			Btn.text = Company
			Btn.tag = Company
			if (Company == Default)
				SelectedCompany = Company	//So wird "SelectedCompany" nur dann als Standard gesetzt, wenn Default in der Liste enthalten ist.

			Btn.setOnClickListener {
				SelectedCompany = Company
				UpdateButtons()
			}
			CompanyButtons.add(Btn)
			BoxButtons.addView(Btn)
		}
		UpdateButtons()
	}//fun AddCompanyButtons

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun UpdateButtons()
	{
		CompanyButtons.forEach { Btn ->
			val colorRes =
				if (Btn.tag == SelectedCompany)
					R.color.companyBtn_selected
				else
					R.color.companyBtn

			Btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(Btn.context, colorRes))
		}
	} //fun UpdateButtons

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	fun ProcessConnectResult(IsConnected: Boolean)
	{
		if (IsConnected)
		{
			val Cmd = """{"action":"${Const.commandStart}","device":"${Config.MDEDeviceName}","company":"${Config.NAVSelectedCompany}"}"""
			MDETcpClient.SendCommand(this, Cmd, "Starte Anmeldung", ::EvalHelloReply)
		}
		else
		{
			AlertDialog.Builder(this)
				.setTitle("Fehler")
				.setMessage("Server antwortet nicht.")
				.setPositiveButton("OK", null)
				.show()
		}
	}//fun ProcessConnectResult

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	fun EvalHelloReply(Reply: String)
	{
		if (CommandReader.EvalReply(Reply))
		{
			when (CommandReader.NewPage)
			{
				Const.commandLogin -> startActivity(Intent(this, ActLogin::class.java))
				Const.commandMainMenu -> startActivity(Intent(this, ActMainMenu::class.java))
				Const.commandMessage -> ActMessage.Show(this, CommandReader.Messsage)
				else -> ActMessage.Show(this, "Der Server hat einen unzulässigen Seitenaufruf gesendet: ${CommandReader.NewPage}", "Fehler")
			}
		}
		else //CommandReader meldet Antwort als ungültig
			ActMessage.Show(this, "Ungültige Antwort vom Server. Support kontaktieren", "Server-Fehler")
	}//fun EvalHelloReply

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	fun EvalLoginReply(Reply: String)
	{
		CommandReader.EvalReply(Reply)
	}//fun EvalLoginReply

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun ensureStoragePermission(): Boolean {
		val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
		if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
			return true
		}
		// Benutzer um Erlaubnis fragen
		requestPermissions(arrayOf(permission), STORAGE_PERMISSION_REQUEST)
		return false
	} // fun ensureStoragePermission

}