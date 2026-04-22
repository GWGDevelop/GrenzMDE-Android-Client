package dk.grenzhandel.mde2026

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActMessage : MDEActivity() {

	companion object {
		private const val TITLE = "extra.title"
		private const val BUTTON = "extra.Button"
		private const	val MESSAGE = "extra.Message"
		private const	val HTML = "extra.AsHTML"

		fun Show(aContext: Context, Message: String, Title: String="Hinweis",
														AsHTML: Boolean=false, ButtonText: String="OK")
		{
			val itMessage = Intent(aContext, ActMessage::class.java).apply {
				putExtra(ActMessage.TITLE, Title)
				putExtra(ActMessage.MESSAGE, Message)
				putExtra(ActMessage.BUTTON, ButtonText)
				putExtra(ActMessage.HTML, AsHTML)
			}
			aContext.startActivity(itMessage)
		}
	}
	lateinit var BtnOK: Button
	lateinit var BtnCloseMessage: ImageButton
	lateinit var LbTitle: TextView
	lateinit var LbMessage: TextView
	lateinit var TxConnection: TextView
	var ShowAsHTML: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_message)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		LbTitle = findViewById< TextView>(R.id.LbTitle)
		LbMessage = findViewById< TextView>(R.id.TxMessage)
		TxConnection = findViewById< TextView>(R.id.TxConnection)
		BtnOK = findViewById<Button>(R.id.BtnOK)
		BtnCloseMessage = findViewById<ImageButton>(R.id.BtnCloseMessage)
		BtnOK.setOnClickListener(this::OnClickClose)
		BtnCloseMessage.setOnClickListener(this::OnClickClose)

		TxConnection.text = ConnectionInfo()
		LbTitle.text = intent.getStringExtra(TITLE) ?: "Hinweis"
		BtnOK.text =  intent.getStringExtra(BUTTON) ?: "OK"
		ShowAsHTML = intent.getBooleanExtra(HTML, false) ?: false
		val MsgTxt = intent.getStringExtra(MESSAGE) ?: "Es wurde kein Hinweistext übergeben."
		if (ShowAsHTML)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
				LbMessage.text = Html.fromHtml(MsgTxt, Html.FROM_HTML_MODE_LEGACY)
			else {
				@Suppress("DEPRECATION")
				LbMessage.text = Html.fromHtml(MsgTxt)
			}
		}
		else
			LbMessage.text = MsgTxt
	}

	private fun OnClickClose(aView: View)
	{
		finish()
	}


}