package dk.grenzhandel.mde2026

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicBoolean

object MDETcpClient {
 	private var Server: String = ""
 	private var Port: Int = 0
	private var TCPConnection: Socket? = null
	private var Sender: BufferedWriter? = null
	private var Receiver: BufferedReader? = null
	private var CmdTerminator: String = "||>>§§<<||"

	private val connected = AtomicBoolean(false)

	suspend fun connect(aHost: String, aPort: Int): Boolean =
		withContext(Dispatchers.IO)
		{
			try
			{
				if (connected.get())
					return@withContext true
				Server = aHost
				Port = aPort
				TCPConnection = Socket(aHost, aPort).apply { soTimeout = 2000 }
				Sender = BufferedWriter(OutputStreamWriter(TCPConnection!!.getOutputStream()))
				Receiver = BufferedReader(InputStreamReader(TCPConnection!!.getInputStream()))
				connected.set(TCPConnection!!.isConnected)
				Log.i("MDETcpClient", "Verbunden mit $Server:$Port")
				connected.get()	//Connect.Result
			}
			catch (e: Exception)
			{
				Log.e("MDETcpClient", "Verbindungsaufbau zu $Server fehlgeschlagen", e)
				false //Connect.Result
			}
		}

	suspend fun sendCommand(Command: String): String? =
		withContext(Dispatchers.IO)
		{
			if (!connected.get())
				return@withContext null
			try {
				Sender?.apply {
				  write(encode(Command))
					flush()
				}

				var line: String = ""
				do
				{
					line = line + Receiver?.readLine()
				} while (!line.contains(CmdTerminator))

				return@withContext line?.let { decode(it) }
			}
			catch (e: Exception) {
				Log.e("MDETcpClient", "Fehler beim Senden an $Server", e)
				null
			}
		}

	suspend fun disconnect() =
		withContext(Dispatchers.IO)
		{
			try
			{
				connected.set(false)
				Receiver?.close()
				Sender?.close()
				TCPConnection?.close()
			}
			catch (e: Exception)
			{
				Log.e("MDETcpClient", "Fehler beim Trennen der Verbindung zu $Server", e)
			}
		}

	fun connectAsync(aHost: String, aPort: Int,
										ConnectResult: (Boolean, String) -> Unit,
										LaunchScope: CoroutineScope = GlobalScope
										)
	{
		LaunchScope.launch(Dispatchers.IO) {
			val connected = connect(aHost, aPort)
			ConnectResult(connected, aHost)
		}
	}

	fun sendAsync(json: String, onResult: (String) -> Unit,
								LaunchScope: CoroutineScope = GlobalScope,
								aContext: Context?
	 							)
	{
		LaunchScope.launch(Dispatchers.IO) {
			val reply = sendCommand(json)
			if (reply.isNullOrBlank())
			{
				if (aContext != null)
					Toast.makeText(aContext, "Befehl nicht gesendet: keine Sevrer-Verbindung geöffnet.", 5).show()
			}
			else
	 			onResult(reply)
		}
	}


//Zur Zeit kein Encoding - gib zurück, was du kriegst.
 	private fun encode(s: String): String = s
/*
	{
		val SafeChars = """abcdefghijklmonpqrstuvwxyz 0123456789"{}ABCDEFGHIJKLMNOPQRSTUVWXYZ|()[\]<>?@!#$%&'*+,-./:;^_`~"""
		var Result = ""
		s.forEach { ch ->
			if (SafeChars.contains(ch))
				Result = Result + ch
			else
				Result = Result + "=" + ch.or
		 }

		s + CmdTerminator
	}
*/
	private fun decode(s: String): String = s
}
