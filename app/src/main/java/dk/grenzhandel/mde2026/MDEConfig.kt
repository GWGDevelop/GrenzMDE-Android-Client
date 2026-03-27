package dk.grenzhandel.mde2026

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.google.gson.Gson
import kotlin.system.exitProcess

data class MDEConfigData(
	val appConfigDir: String,
	val appConfigFile: String, //Dieser Eintrag kann nur beim Start aus den Defaults gesetzt werden. Deshalb ist er als "val" definiert, nicht "var".
	var MDEStartupTitle: String,
	var srvrAddress: String,
	var srvrPort: Int,
	var NAVDefaultCompany: String,
	var NAVAvailableCompanies: List<String>,
	var NAVSelectedCompany: String,

	var scnPrefix: String,
	var scnSuffix: String,
	var scnAllowedTypes: List<String>,
	var scnIntentAction: String,
	var scnEventBCOK: String,
	var scnEventBCContent: String,
	var scnEventBCLength: String,
	var scnEventBCType: String,
	var scnDebugBarcodeTypes: Boolean
) {

	companion object {
		fun GetConfig(aContext: Context): MDEConfigData {
			var Result = SetDefaultConfig(aContext)
			val StorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
/*
			val ConfigDatei = File(Result.appConfigFile)
			if (ConfigDatei.exists()) {
//				loadXmlOverrides(Result)
			}
			else {
				ConfigDatei.parentFile?.mkdirs()
//				saveXmlConfig(Result)
			}
 */
			return Result
		}

		fun SetDefaultConfig (aContext: Context): MDEConfigData {
			val json = aContext.assets.open("MDEConfigDefaults.json").bufferedReader().use{it.readText()}
			try
			{
 				val Result = Gson().fromJson(json, MDEConfigData::class.java)
				return Result
			}
			catch (e: Error)
			{
				Toast.makeText(aContext, "Fehler beim Laden der Standardeinstellungen! Beende Programm", Toast.LENGTH_LONG).show()
				exitProcess(0)
			}
		}
	}
}
