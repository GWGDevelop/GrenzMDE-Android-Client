package dk.grenzhandel.mde2026

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class MDEActivity: AppCompatActivity()
{
	protected val App: MDEApplication get() = application as MDEApplication
	lateinit protected var Config: MDEConfigData
	lateinit protected var Const: MDEConst


	//--------------------------------------------------------------------------------------------
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		Config = App.Config
		Const =  App.Const
	}//fun onCreate

	fun ConnectionInfo(): String
	{
		if (CommandReader.User > "")
			return CommandReader.User + "@" + CommandReader.Company + "." + CommandReader.Server
		else if (CommandReader.Company > "")
			return CommandReader.Company + "." + CommandReader.Server
		else
			return CommandReader.Company + "." + CommandReader.Server
	}

}//abstract class MDEActivity