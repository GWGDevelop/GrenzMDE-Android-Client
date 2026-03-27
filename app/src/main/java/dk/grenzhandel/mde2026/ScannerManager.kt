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
	private lateinit var MDEConfig: MDEConfigData

	fun initialize(aContext: Context, aLifecycleOwner: LifecycleOwner, onScan: (ScanResult) -> Unit)
	{
		MDEConfig = (aContext.applicationContext as MDEApplication).MDEConfig
		listener = onScan
		aLifecycleOwner.lifecycle.addObserver(this)
		register(aContext)
	}

	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	private fun register(aContext: Context) {
		if (receiver != null) return

		receiver = object : BroadcastReceiver() {
			override fun onReceive(aContext: Context, BrdCstIntent: Intent) {
				if (BrdCstIntent.action != MDEConfig.scnIntentAction)
					return
				if (!BrdCstIntent.getBooleanExtra(MDEConfig.scnEventBCOK, false))
					return

				val BCData = BrdCstIntent.getByteArrayExtra(MDEConfig.scnEventBCContent)
				val BCLength = BrdCstIntent.getIntExtra(MDEConfig.scnEventBCLength, 0)
				var BCType = BrdCstIntent.getStringExtra(MDEConfig.scnEventBCType)?:""

				if (BCData != null) {
					var Barcode = String(BCData, 0, BCLength, Charsets.UTF_8)

					if (!MDEConfig.scnAllowedTypes.contains(BCType))
					{
						if (MDEConfig.scnDebugBarcodeTypes)
							Toast.makeText(aContext, "Barcode-Typ nicht zugelassen: ${Barcode}, Typ: ${BCType}", Toast.LENGTH_SHORT).show()
						return
					}
					if ((MDEConfig.scnPrefix > "") &&
						(Barcode.substring(0, MDEConfig.scnPrefix.length) == MDEConfig.scnPrefix))
					{
						Barcode = Barcode.substring(MDEConfig.scnPrefix.length)
					}
					if ((MDEConfig.scnSuffix > "") &&
						(Barcode.substring(Barcode.length - MDEConfig.scnSuffix.length) == MDEConfig.scnSuffix))
					{
						Barcode = Barcode.substring(0, Barcode.length - MDEConfig.scnSuffix.length)
					}
					listener?.invoke(ScanResult(Barcode, BCType))
				}
			}
		}
		aContext.registerReceiver(receiver, IntentFilter(MDEConfig.scnIntentAction))
	}

	override fun onPause(owner: LifecycleOwner) {
		receiver?.let {
			(owner as Context).unregisterReceiver(it)
		}
		receiver = null
	}
}
