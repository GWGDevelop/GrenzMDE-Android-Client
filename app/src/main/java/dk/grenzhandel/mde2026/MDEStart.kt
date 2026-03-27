package dk.grenzhandel.mde2026

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MDEStart : AppCompatActivity() {
	lateinit var Config: MDEConfigData
	lateinit var BtnConnect: Button
	lateinit var TxTitle: TextView
	lateinit var TxServer: TextView
	lateinit var TxPort: TextView
	var SelectedCompany: String = ""
	private val CompanyButtons = mutableListOf<Button>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_mdestart)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		BtnConnect = findViewById<Button>(R.id.btnConnect)
		BtnConnect.setOnClickListener(this::OnClickBtnConnect)
		TxTitle = findViewById<TextView>(R.id.txStartupTitle)
		TxServer = findViewById<TextView>(R.id.txServer)
		TxPort = findViewById<TextView>(R.id.txPort)
		val BoxButtons = findViewById<LinearLayout>(R.id.bxCompanyButtons)
		Config = (application as MDEApplication).MDEConfig

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
	}

	private fun UpdateButtons()
	{
		CompanyButtons.forEach { Btn ->
			val colorRes = if (Btn.text == SelectedCompany)
				R.color.companyBtn_selected
			else
				R.color.companyBtn

			Btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(Btn.context, colorRes))
		}
	}

	private fun OnClickBtnConnect(aView: View)
	{
		if ( SelectedCompany == "")
			Toast.makeText(this, "Kein Mandant ausgewählt. Kann nicht verbinden.", Toast.LENGTH_LONG).show()
		else {
			Toast.makeText(this, "Verbinde mit ${SelectedCompany}@${Config.srvrAddress}:${Config.srvrPort}", Toast.LENGTH_LONG).show()
			Config.NAVSelectedCompany = SelectedCompany

			val itLogin = Intent(this, ActLogin::class.java)
			startActivity(itLogin)
		}
	}
}