package dk.grenzhandel.mde2026

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import kotlin.system.exitProcess

class MDEStart : AppCompatActivity() {
	private val STORAGE_PERMISSION_REQUEST = 1234
	lateinit var Config: MDEConfigData
	lateinit var BtnConnect: Button
	lateinit var BtnCloseApp: ImageButton
	lateinit var TxTitle: TextView
	lateinit var TxServer: TextView
	lateinit var TxPort: TextView
	var SelectedCompany: String = ""
	private val CompanyButtons = mutableListOf<Button>()

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
		onBackPressedDispatcher.addCallback(this) { OnClickBack() }

		BtnConnect = findViewById<Button>(R.id.btnConnect)
		BtnConnect.setOnClickListener(this::OnClickBtnConnect)
		BtnCloseApp = findViewById<ImageButton>(R.id.BtnCloseApp)
		BtnCloseApp.setOnClickListener(this::OnClickClose)

		TxTitle = findViewById<TextView>(R.id.txStartupTitle)
		TxServer = findViewById<TextView>(R.id.txServer)
		TxPort = findViewById<TextView>(R.id.txPort)
		val BoxButtons = findViewById<LinearLayout>(R.id.bxCompanyButtons)
		Config = (application as MDEApplication).MDEConfig
		if (!Config.appConfigFileExists) {
			if (ensureStoragePermission())
				MDEConfigData.SaveConfig(Config, this)
			else
				Toast.makeText(this, "Konfiguration NICHT gespeichert - Berechtigung wurde verweigert.", Toast.LENGTH_LONG).show()
		}

		TxTitle.text = Config.MDEStartupTitle
		TxServer.text = Config.srvrAddress
		TxPort.text = Config.srvrPort.toString()
		val Companies = Config.NAVAvailableCompanies
		val DefaultCompany = Config.NAVDefaultCompany

 		Companies.forEach { Company ->
			val Btn = MaterialButton(ContextThemeWrapper(this, R.style.CompanyButtonStyle), null,
																com.google.android.material.R.attr.materialButtonStyle)
			Btn.text = Company
			if (Company == DefaultCompany)
				SelectedCompany = Company	//So wird "SelectedCompany" nur dann auf Default gesetzt, wenn DefaultCompany in der Liste enthalten ist.

			Btn.setOnClickListener {
				SelectedCompany = Company
				UpdateButtons()
			}
			CompanyButtons.add(Btn)
			BoxButtons.addView(Btn)
		}
		UpdateButtons()
	} //fun onCreate

	private fun OnClickBack()
	{
		finishAffinity()
		exitProcess(0)
	}

	private fun OnClickClose(aView: View)
	{
		OnClickBack()
	}

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

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun UpdateButtons()
	{
		CompanyButtons.forEach { Btn ->
			val colorRes = if (Btn.text == SelectedCompany)
				R.color.companyBtn_selected
			else
				R.color.companyBtn

			Btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(Btn.context, colorRes))
		}
	} //fun UpdateButtons

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun OnClickBtnConnect(aView: View)
	{
		if ( SelectedCompany == "")
			Toast.makeText(this@MDEStart, "Kein Mandant ausgewählt. Kann nicht verbinden.", Toast.LENGTH_LONG).show()
		else
		{
			Config.NAVSelectedCompany = SelectedCompany
			MDETcpClient.Connect(this, Config.srvrAddress, Config.srvrPort,
												Config.TCPTerminator, Config.TCPTimeout, ::ProcessConnectResult)
		}
	}//fun OnClickBtnConnect

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	fun ProcessConnectResult(IsConnected: Boolean)
	{
		if (IsConnected)
		{
			val Cmd = """{"action":"hello","device":"${Config.MDEDeviceName}","company":"${Config.NAVSelectedCompany}"}"""
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
		CommandReader.EvalReply(Reply)
		try {
			Log.d("TCP", "Antwort vom Server: ${Reply}")
			val RespJSON = JSONObject(Reply)
			val page = RespJSON.optString("page").lowercase()
			if (page == "login")
			{
				val itLogin = Intent(this@MDEStart, ActLogin::class.java)
				startActivity(itLogin)
			}
			else
			{
				Toast.makeText(this@MDEStart, "unbekannter Seitenaufruf: ${page}", Toast.LENGTH_LONG).show()
				Log.e("TCP", "Unerwartete Antwort: ${RespJSON}")
			}
		}
		catch (e: Error)
		{
			Toast.makeText(this@MDEStart, "Fehler in der Server-Antwort: ${Reply}", Toast.LENGTH_LONG).show()
			Log.e("TCP", "Unerwartete Antwort: ${Reply}")
		}
	}//fun EvalHelloReply
}