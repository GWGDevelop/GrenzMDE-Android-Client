package dk.grenzhandel.mde2026

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActLogin : AppCompatActivity()
{
	lateinit var LbConnection: TextView
	lateinit var EdUser: EditText
	lateinit var EdPwd:EditText
	lateinit var BtnLogin: Button
	lateinit var Config: MDEConfigData

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_login)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LyoMain)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		LbConnection = findViewById<TextView>(R.id.LbServerConnection)
		EdUser = findViewById<EditText>(R.id.EdUserName)
		EdPwd = findViewById<EditText>(R.id.EdPwd)
		BtnLogin = findViewById<Button>(R.id.BtnLogin)
		BtnLogin.setOnClickListener(this::OnClickBtnLogin)
		Config = (application as MDEApplication).MDEConfig
		LbConnection.text = Config.NAVSelectedCompany + "@" + Config.srvrAddress
		ScannerManager.initialize(this, this, ::OnScanner)
	}

	private fun OnClickBtnLogin(aView: View)
	{
		Toast.makeText(this, "Moin User ${EdUser.text} !", Toast.LENGTH_SHORT).show()
//		val intServerConnect = Intent(this, ServerConnect::class.java)
//		startActivity(intServerConnect)
	}

	//--------------------------------------------------------------------------------
	private fun OnScanner(Barcode: ScanResult)
	{
		if (Barcode.Type == "CODE 128")
			Toast.makeText(this, "Moin User ${Barcode.Barcode} !", Toast.LENGTH_SHORT).show()
		else
			Toast.makeText(this, "Scan: ${Barcode.Barcode} vom Typ ${Barcode.Type}", Toast.LENGTH_SHORT).show()

		EdUser.setText(Barcode.Barcode)
	}

}