package dk.grenzhandel.mde2026

import android.app.Application

class MDEApplication: Application()
{
	lateinit var Config: MDEConfigData
	lateinit var Const: MDEConst

	//--------------------------------------------------------------------------------------------
	override fun onCreate()
	{
		super.onCreate()
		Config = MDEConfigData.GetConfig(this)
		Const = MDEConst()
	}//fun onCreate
}//class MDEApplication
