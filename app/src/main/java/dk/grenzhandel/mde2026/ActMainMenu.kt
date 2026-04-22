package dk.grenzhandel.mde2026

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ActMainMenu : MDEActivity()
{
	lateinit var BtnLogoff: ImageButton
	lateinit var BoxButtons: LinearLayout
	lateinit var LbTitle: TextView
	lateinit var TxConnection: TextView
	private var BackPressed = false

	//--------------------------------------------------------------------------------------------
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_mdemain_menu)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		TxConnection = findViewById<TextView>(R.id.TxConnection)
		LbTitle = findViewById<TextView>(R.id.LbTitle)
		BoxButtons = findViewById<LinearLayout>(R.id.BxButtons)
		BtnLogoff = findViewById<ImageButton>(R.id.BtnLogoff)

		onBackPressedDispatcher.addCallback(this) { OnClickBack() }
		BtnLogoff.setOnClickListener(this::OnClickLogoff)
		TxConnection.text = ConnectionInfo()

		AddMenuButtons()
	}//fun onCreate

	fun AddMenuButtons()
	{
		BoxButtons.removeAllViews()
		if (CommandReader.Buttons.count() < 1)
		{
			AlertDialog.Builder(this)
				.setTitle("Keine Berechtigung")
				.setMessage("Für den Anwender sind keine zugelassenen Funktionen eingetragen.")
				.setPositiveButton("OK", null)
				.show()
		}
		else {
			CommandReader.Buttons.forEach { BtnDef ->
				val Btn = MaterialButton(ContextThemeWrapper(this, R.style.MainMenuButtonStyle), null,
					com.google.android.material.R.attr.materialButtonStyle)

				Btn.height = 150
				Btn.text = BtnDef.Caption
				Btn.tag = BtnDef.CommandID
				Btn.setOnClickListener(this::OnClickMenuButton)
				BoxButtons.addView(Btn)
			}
		}
	}

	fun OnClickLogoff(aView: View)
	{
		OnClickBack()
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private fun OnClickBack()
	{
		if (BackPressed)
		{
			finish()
		}
		else {
			BackPressed = true
			Toast.makeText(this@ActMainMenu, "Zum Abmelden nochmals drücken", Toast.LENGTH_LONG).show()
			Handler(Looper.getMainLooper()).postDelayed({BackPressed=false}, Const.repeatToCloseTimeout)
		}
	}//fun OnClickBack

	fun OnClickMenuButton(aView: View)
	{
		val Btn = aView as? Button ?: return
 		val CmdID = Btn.tag as? Int ?: 0

		Toast.makeText(this@ActMainMenu, "Button geklickt: ${Btn.text} (Funktion Nr.${CmdID})", Toast.LENGTH_SHORT).show()
		when (CmdID)
		{



		}


	}


}//class ActMainMenu