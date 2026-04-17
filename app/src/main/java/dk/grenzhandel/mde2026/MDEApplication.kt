package dk.grenzhandel.mde2026

import android.app.Application

class MDEApplication: Application()
{
	lateinit var MDEConfig: MDEConfigData

	override fun onCreate()
	{
		super.onCreate()
		MDEConfig = MDEConfigData.GetConfig(this)
	}//fun onCreate


}//class MDEApplication
