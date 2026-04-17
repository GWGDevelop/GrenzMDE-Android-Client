package dk.grenzhandel.mde2026

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import kotlin.system.exitProcess

data class MDEConfigData(
	var appConfigDir: String,
	val appConfigFile: String,
	var appConfigFileExists: Boolean,
	var MDEStartupTitle: String,
	var srvrAddress: String,
	var srvrPort: Int,
	var TCPTerminator: String,
	var TCPTimeout: Int,
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

	companion object
	{
		fun DefaultConfig (aContext: Context): MDEConfigData
		{
			val json = aContext.assets.open("MDEConfigDefaults.json").bufferedReader().use{it.readText()}
			try
			{
 				var Result = Gson().fromJson(json, MDEConfigData::class.java)
				Result.appConfigDir = Environment.DIRECTORY_DOCUMENTS
				return Result
			}
			catch (e: Error)
			{
				Toast.makeText(aContext, "Fehler beim Laden der Standardeinstellungen! Beende Programm", Toast.LENGTH_LONG).show()
				exitProcess(0)
			}
		} //fun DefaultConfig

		fun GetConfig(aContext: Context): MDEConfigData
		{
			var Result = DefaultConfig(aContext)
/*
			try {
				val StorageDir = Environment.getExternalStoragePublicDirectory(Result.appConfigDir)
				val ConfigFile = File(StorageDir, Result.appConfigFile)
				if (!ConfigFile.exists())
				{
					Result.appConfigFileExists = false
					SaveConfig(Result, aContext)
				}
				else	{
					Result = Gson().fromJson(ConfigFile.readText(), MDEConfigData::class.java)
					Result.appConfigFileExists = true
				}
			}
			catch (e: Error) {
				Toast.makeText(aContext, "Fehler beim Laden der Konfigurationsdatei!", Toast.LENGTH_LONG).show()
			}
 */
			return Result
		} //fun GetConfig

		fun SaveConfig(Config: MDEConfigData, aContext: Context)
		{
			try {
				val StorageDir = Environment.getExternalStoragePublicDirectory(Config.appConfigDir)
				if (!StorageDir.exists())
					StorageDir.mkdirs()
				val ConfigFile = File(StorageDir, Config.appConfigFile)
				val GSON = GsonBuilder().setPrettyPrinting().create()
				val JSON = GSON.toJson(Config)
				Config.appConfigFileExists = true
				ConfigFile.writeText(JSON)
				Toast.makeText(aContext, "Konfiguration gespeichert in ${ConfigFile.absolutePath}", Toast.LENGTH_LONG).show()
			}
			catch (e: Exception) {
				Config.appConfigFileExists = false
				Toast.makeText(aContext, "Fehler beim Speichern der Konfiguration: ${e.toString()}", Toast.LENGTH_LONG).show()
				Log.e("MDEConfig", "Fehler beim Speichern der Datei!", e)
			}
		} //fun SaveConfig
	} //companion Object
}
