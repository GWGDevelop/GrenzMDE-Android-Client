package dk.grenzhandel.mde2026

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

data class ScanResult(
	val Barcode: String,
	val Type: String
)

object ScannerManager: DefaultLifecycleObserver
{
	private var receiver: BroadcastReceiver? = null
	private var listener: ((ScanResult) -> Unit)? = null
	private lateinit var Config: MDEConfigData

	//--------------------------------------------------------------------------------------------
	fun initialize(aContext: Context, aLifecycleOwner: LifecycleOwner, onScan: (ScanResult) -> Unit)
	{
		Config = (aContext.applicationContext as MDEApplication).Config
		listener = onScan
		aLifecycleOwner.lifecycle.addObserver(this)
		register(aContext)
	}//fun initialize

	//--------------------------------------------------------------------------------------------
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	private fun register(aContext: Context)
	{
		if (receiver != null) return

		receiver = object : BroadcastReceiver() {
			override fun onReceive(aContext: Context, BrdCstIntent: Intent)
			{
				if (BrdCstIntent.action != Config.scnIntentAction)
					return
				if (!BrdCstIntent.getBooleanExtra(Config.scnEventBCOK, false))
					return

				val BCData = BrdCstIntent.getByteArrayExtra(Config.scnEventBCContent)
				val BCLength = BrdCstIntent.getIntExtra(Config.scnEventBCLength, 0)
				var BCType = BrdCstIntent.getStringExtra(Config.scnEventBCType)?:""

				if (BCData != null) {
					var Barcode = String(BCData, 0, BCLength, Charsets.UTF_8)

					if (!Config.scnAllowedTypes.contains(BCType))
					{
						if (Config.scnDebugBarcodeTypes)
							Toast.makeText(aContext, "Barcode-Typ nicht zugelassen: ${Barcode}, Typ: ${BCType}", Toast.LENGTH_SHORT).show()
						return
					}
					if ((Config.scnPrefix > "") &&
						(Barcode.substring(0, Config.scnPrefix.length) == Config.scnPrefix))
					{
						Barcode = Barcode.substring(Config.scnPrefix.length)
					}
					if ((Config.scnSuffix > "") &&
						(Barcode.substring(Barcode.length - Config.scnSuffix.length) == Config.scnSuffix))
					{
						Barcode = Barcode.substring(0, Barcode.length - Config.scnSuffix.length)
					}
					listener?.invoke(ScanResult(Barcode, BCType))
				}
			}
		}
		aContext.registerReceiver(receiver, IntentFilter(Config.scnIntentAction))
	}//fun register

	//--------------------------------------------------------------------------------------------
	override fun onPause(owner: LifecycleOwner)
	{
		receiver?.let {
			(owner as Context).unregisterReceiver(it)
		}
		receiver = null
	}//fun onPause
}//object ScannerManager
