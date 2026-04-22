package dk.grenzhandel.mde2026

import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.Executors

object MDETcpClient
{
	private var ServerAddress: String = ""
	private var Port: Int = 9999
	private var Timeout: Int = 10000
	private var Terminator: String = "\n"
	private var IsConnected: Boolean = false


	private var socket: Socket? = null
	private var reader: InputStreamReader? = null
	private var writer: OutputStreamWriter? = null
	private val uiHandler = Handler(Looper.getMainLooper())
	private val executor = Executors.newSingleThreadExecutor()
	private var waitDialog: ProgressDialog? = null

	private fun showWait(ctx: Context, text: String) {
		uiHandler.post {
			waitDialog = ProgressDialog(ctx).apply {
				setMessage(text)
				setCancelable(false)
				show()
			}
		}
	}

	//--------------------------------------------------------------------------------------------
	private fun hideWait()
	{
		uiHandler.post { waitDialog?.dismiss() }
	}//fun hideWait

	//--------------------------------------------------------------------------------------------
	fun Connect(ctx: Context, aHost: String, aPort: Int,
								aTerminator: String, aTimeout: Int,  OnConnected: (Boolean) -> Unit
								)
	{
		executor.execute {
			if (IsConnected)
			{
				uiHandler.post { OnConnected(true) }
			}
			else
				try {
					showWait(ctx, "Verbinde mit Server...")
					ServerAddress = aHost
					Port = aPort
					Timeout = aTimeout
					Terminator = aTerminator
					socket = Socket()
					socket!!.connect(InetSocketAddress(ServerAddress, Port), Timeout)
					socket!!.soTimeout = Timeout
					reader = InputStreamReader(socket!!.getInputStream(), Charsets.UTF_8)
					writer = OutputStreamWriter(socket!!.getOutputStream(), Charsets.UTF_8)
					hideWait()
					IsConnected = true
					uiHandler.post { OnConnected(true) }
				}
				catch (ex: Exception)
				{
					hideWait()
					Disconnect()
					uiHandler.post { OnConnected(false) }
				}
		}
	}//fun Connect

	//--------------------------------------------------------------------------------------------
	fun SendCommand(ctx: Context, JSONCommand: String, WaitMessage: String, ReplyCallback: (String) -> Unit)
	{
		showWait(ctx, WaitMessage)

		executor.execute {
			try {
				Log.d("TCP", "Sende an Server: ${JSONCommand} mit Terminator ${Terminator}")
				// --- Senden inkl. Terminator ---
				writer?.apply {
					write(JSONCommand)
					write(Terminator)
					flush()
				}

				// --- Antwort lesen bis Terminator ---
				val reply = readUntilTerminator()

				hideWait()
				uiHandler.post { ReplyCallback(reply) }

			} catch (ex: SocketTimeoutException) {
				hideWait()
				uiHandler.post {
					ReplyCallback("""{"error":"Keine Antwort vom Server - Zeitüberschreitung"}""")
				}
			} catch (ex: Exception) {
				hideWait()
				uiHandler.post {
					ReplyCallback("""{"error":"Communication error"}""")
				}
			}
		}
	}//fun SendCommand

	// ------------------------------------------------------------
	private fun readUntilTerminator(): String
	{
		val buffer = StringBuilder()
		val termLen = Terminator.length
		val charBuffer = CharArray(1)

		while (true) {
			val read = reader?.read(charBuffer) ?: -1
			if (read < 0) throw IOException("Server closed connection")

			buffer.append(charBuffer[0])

			if (buffer.length >= termLen &&
				buffer.substring(buffer.length - termLen) == Terminator
			) {
				return buffer.substring(0, buffer.length - termLen)
			}
		}
	}//fun readUntilTerminator

	//--------------------------------------------------------------------------------------------
	fun Disconnect()
	{
		try {
			IsConnected = false
			reader?.close()
			writer?.close()
			socket?.close()
		}
		catch (_: Exception) {}
		reader = null
		writer = null
		socket = null
	}//fun Disconnect

}//object MDETcpClient