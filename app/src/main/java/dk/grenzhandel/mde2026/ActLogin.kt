package dk.grenzhandel.mde2026

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import androidx.activity.addCallback


class ActLogin : MDEActivity()
{
	lateinit var LbConnection: TextView
	lateinit var EdUser: EditText
	lateinit var EdPwd:EditText
	lateinit var BtnLogin: Button
	lateinit var BtnCloseConnection: ImageButton
	lateinit var BtnInfo: ImageButton

	//--------------------------------------------------------------------------------------------
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_login)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LyoMain)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		LbConnection = findViewById<TextView>(R.id.LbConnection)
		EdUser = findViewById<EditText>(R.id.EdUserName)
		EdPwd = findViewById<EditText>(R.id.EdPwd)
		BtnLogin = findViewById<Button>(R.id.BtnLogin)
		BtnCloseConnection = findViewById<ImageButton>(R.id.BtnCloseConnection)
		BtnInfo =	findViewById<ImageButton>(R.id.BtnInfo)

		LbConnection.text = ConnectionInfo()
		BtnCloseConnection.setOnClickListener(this::OnClickDisconnect)
		BtnLogin.setOnClickListener(this::OnClickBtnLogin)
		BtnInfo.setOnClickListener(this::OnClickShowHelp)
		onBackPressedDispatcher.addCallback(this) { OnClickBack() }

		ScannerManager.initialize(this, this, ::OnScanner)
	}//fun onCreate

	//--------------------------------------------------------------------------------------------
	private fun OnClickBack()
	{
		val Cmd = """{"action":"goodbye"}"""
		MDETcpClient.SendCommand(this, Cmd, "", {})
		MDETcpClient.Disconnect()
		finish()
	}//fun OnClickBack

	//--------------------------------------------------------------------------------
	private fun OnClickDisconnect(aView: View)
	{
		OnClickBack()
	}//fun OnClickDisconnect

	//--------------------------------------------------------------------------------------------
	private fun OnClickBtnLogin(aView: View)
	{
		val Cmd = """{"action":"login","username":"${EdUser.text}","password":"${EdPwd.text}"}"""
		MDETcpClient.SendCommand(this, Cmd, "Anmeldung mit Benutzername/ID", ::EvalLoginReply)
		EdPwd.setText("")
	}//fun OnClickBtnLogin

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun	OnClickShowHelp(aView: View)
	{
			ActMessage.Show(this, getString(R.string.scannerHelp), "Scanner-Einstellungen", true, "Verstanden")
	}//fun	OnClickShowHelp

	//--------------------------------------------------------------------------------
	private fun OnScanner(Barcode: ScanResult)
	{
		val Cmd = """{"action":"login","barcode":"${Barcode.Barcode}"}"""
		MDETcpClient.SendCommand(this, Cmd, "Anmeldung mit Barcode", ::EvalLoginReply)
		EdUser.setText("")
		EdPwd.setText("")
	}//fun OnScanner

	//--------------------------------------------------------------------------------
	fun EvalLoginReply(Reply: String)
	{
		if (CommandReader.EvalReply(Reply))
		{
			when (CommandReader.NewPage)
			{
				Const.commandMainMenu -> {
					EdUser.setText("")
					EdPwd.setText("")
					startActivity(Intent(this, ActMainMenu::class.java))
				}
				Const.commandMessage -> ActMessage.Show(this, CommandReader.Messsage)
				else -> ActMessage.Show(this, "Der Server hat einen unzulässigen Seitenaufruf gesendet: ${CommandReader.NewPage}", "Fehler")
			}
		}
		else //CommandReader meldet Antwort als ungültig
			ActMessage.Show(this, "Ungültige Antwort vom Server. Support kontaktieren", "Server-Fehler")
	}//fun EvalLoginReply

}//class ActLogin