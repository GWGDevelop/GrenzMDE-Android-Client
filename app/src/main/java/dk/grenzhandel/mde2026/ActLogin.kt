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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import androidx.activity.addCallback


class ActLogin : AppCompatActivity()
{
	lateinit var LbConnection: TextView
	lateinit var EdUser: EditText
	lateinit var EdPwd:EditText
	lateinit var BtnLogin: Button
	lateinit var BtnCloseConnection: ImageButton
	lateinit var Config: MDEConfigData

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
		onBackPressedDispatcher.addCallback(this) { OnClickBack() }

		LbConnection = findViewById<TextView>(R.id.LbServerConnection)
		EdUser = findViewById<EditText>(R.id.EdUserName)
		EdPwd = findViewById<EditText>(R.id.EdPwd)
		BtnLogin = findViewById<Button>(R.id.BtnLogin)
		BtnLogin.setOnClickListener(this::OnClickBtnLogin)
		BtnCloseConnection = findViewById<ImageButton>(R.id.BtnCloseConnection)
		BtnCloseConnection.setOnClickListener(this::OnClickDisconnect)

		Config = (application as MDEApplication).MDEConfig
		LbConnection.text = Config.NAVSelectedCompany + "@" + Config.srvrAddress
		ScannerManager.initialize(this, this, ::OnScanner)
	}//fun onCreate

	private fun OnClickBack()
	{
		val Cmd = """{"action":"goodbye"}"""
		MDETcpClient.SendCommand(this, Cmd, "", {})
		MDETcpClient.Disconnect()
		finish()
	}

	private fun OnClickBtnLogin(aView: View)
	{
		Toast.makeText(this, "Moin User ${EdUser.text} !", Toast.LENGTH_SHORT).show()
	}//fun OnClickBtnLogin

	private fun OnClickDisconnect(aView: View)
	{
		OnClickBack()
	}

	//--------------------------------------------------------------------------------
	private fun OnScanner(Barcode: ScanResult)
	{
		val Cmd = """{"action":"login","barcode":"${Barcode}"}"""
		MDETcpClient.SendCommand(this, Cmd, "Anmeldung mit Barcode", ::EvalLoginReply)
	}//fun OnScanner

	fun EvalLoginReply(Reply: String)
	{
		try {
			Log.d("TCP", "Antwort vom Server: ${Reply}")
			val RespJSON = JSONObject(Reply)
			val page = RespJSON.optString("page").lowercase()
			if (page == "login")
			{
				val itLogin = Intent(this@ActLogin, ActLogin::class.java)
				startActivity(itLogin)
			}
			else
			{
				Toast.makeText(this@ActLogin, "unbekannter Seitenaufruf: ${page}", Toast.LENGTH_LONG).show()
				Log.e("TCP", "Unerwartete Antwort: ${RespJSON}")
			}
		}
		catch (e: Error)
		{
			Toast.makeText(this@ActLogin, "Fehler in der Server-Antwort: ${Reply}", Toast.LENGTH_LONG).show()
			Log.e("TCP", "Unerwartete Antwort: ${Reply}")
		}
	}

}//class ActLogin