package dk.grenzhandel.mde2026

import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import org.json.JSONArray

object CommandReader {
	private var ActiveReply: String = ""
	private var ActiveJSON: JSONObject? = null
	var ActiveServer: String = ""
	var ActiveDatabase: String = ""
	var ActiveCompany: String = ""
	var ActiveUser: String = ""
	var NewPage: String = ""

	private fun IsValidJson(json: String): Boolean
	{
		try {
			JSONObject(json)
			return true
		}
		catch (e: Exception)
		{
			return false
		}
	}

	fun EvalReply(NewReply: String): Boolean
	{
		if (IsValidJson(NewReply))
		{
			ActiveReply = NewReply
			ActiveJSON = JSONObject(NewReply)
			ActiveServer = ActiveJSON!!.optString("server")
			ActiveDatabase = ActiveJSON!!.optString("database")
			ActiveCompany = ActiveJSON!!.optString("company")
			ActiveUser = ActiveJSON!!.optString("username")
			NewPage = ActiveJSON!!.optString("page")
			return true
		} //if IsValidJson
		else {
			Log.e("JSON-Command", "Antwort vom Server ist Ungültig: ${NewReply}")
			return false

		}
	}

}