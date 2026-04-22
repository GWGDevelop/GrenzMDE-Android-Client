package dk.grenzhandel.mde2026

import android.util.Log
import org.json.JSONObject
import org.json.JSONArray
import kotlin.collections.mutableListOf

data class CommandButton(
		var CommandID: String = "",
		var Caption: String = "",
		var Params: String = "" ,
		var Shortcut: String = ""
)

object CommandReader {
	private var Reply: String = ""
	private var JSON: JSONObject? = null
	var Server: String = ""
	var Database: String = ""
	var Company: String = ""
	var User: String = ""
	var NewPage: String = ""
	var Messsage: String = ""
	val Buttons = mutableListOf<CommandButton>()

	//--------------------------------------------------------------------------------------------
	private fun IsJSONObject(json: String): Boolean
	{
		try {
			JSONObject(json)
			return true
		}
		catch (e: Exception)
		{
			return false
		}
	}//fun IsJSONObject

	//--------------------------------------------------------------------------------------------
	private fun IsJsonArray(json: String): Boolean
	{
		try {
			JSONArray(json)
			return true
		}
		catch (e: Exception)
		{
			return false
		}
	}//fun IsJsonArray

	//--------------------------------------------------------------------------------------------
	fun EvalReply(NewReply: String): Boolean
	{
		Log.d("TCP", "Antwort vom Server: ${NewReply}")
		if (IsJSONObject(NewReply))
		{
			Reply = NewReply
			JSON = JSONObject(NewReply)
			Server = JSON!!.optString("server")
			Database = JSON!!.optString("database")
			Company = JSON!!.optString("company")
			User = JSON!!.optString("username")
			NewPage = JSON!!.optString("page").lowercase()
			Messsage = JSON!!.optString("message")
			Buttons.clear()
			val BtnList = JSON!!.optJSONArray("buttons")
			if (BtnList != null)
			{
				for (i in 0 until BtnList.length())
				{
					val BtnListItem = BtnList.optJSONObject(i)
					if (BtnListItem != null)
					{
						val Btn = CommandButton()
						Btn.CommandID = BtnListItem.optString("function")
						Btn.Caption = BtnListItem.optString("caption")
						Btn.Shortcut = BtnListItem.optString("shortcut")
						Btn.Params = BtnListItem.optString("parameters")
						Buttons.add(Btn)
					}
				}
			}
			return true
		} //if IsValidJson
		else {
			Log.e("JSON-Command", "Antwort vom Server ist Ungültig: ${NewReply}")
			return false
		}
	}//fun EvalReply

}